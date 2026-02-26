# Redisson 公平锁 (FairLock) 深入分析

## 概述

RedissonFairLock 是 Redisson 提供的公平分布式锁实现，它保证了所有等待线程获取锁的 FIFO（先进先出）顺序，解决了非公平锁可能导致的"饥饿"问题。

---

## 核心类结构

```
RedissonFairLock
    │
    └── RedissonLock (非公平锁)
            │
            └── RedissonBaseLock
                    │
                    └── RedissonExpirable
```

### 核心属性

```java
public class RedissonFairLock extends RedissonLock {
    
    private final long threadWaitTime;       // 线程等待超时时间
    private final String threadsQueueName;   // 等待队列 Redis List 键名
    private final String timeoutSetName;     // 超时时间 Sorted Set 键名
}
```

---

## 公平锁原理

### 数据结构

公平锁使用三个 Redis Key 来维护等待队列：

```
1. 锁键: redisson_lock:{lockName}
   └── Hash: {clientId}:{threadId} → 重入计数

2. 等待队列: redisson_lock_queue:{lockName}
   └── List: [threadId1, threadId2, threadId3, ...]

3. 超时集合: redisson_lock_timeout:{lockName}
   └── Sorted Set: threadId → 超时时间戳
```

### 获取锁流程

```
┌─────────────────────────────────────────────────────────────┐
│                   tryLockInnerAsync()                        │
│                   (公平锁 Lua 脚本)                          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  1. 清理过期线程 (remove stale threads)                     │
│     - 遍历队列头部的超时线程                                  │
│     - 从 List 和 Sorted Set 中移除                           │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  2. 检查获取条件                                              │
│     if (锁不存在 AND (队列为空 OR 当前线程在队列头部)) then  │
│         获取锁                                                │
│     else if (当前线程已持有锁) then                          │
│         重入计数+1                                           │
│     else                                                    │
│         返回 TTL                                             │
└─────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┴───────────────┐
              │                               │
              ▼                               ▼
        ┌─────────────┐               ┌─────────────┐
        │  获取成功    │               │  获取失败    │
        │  ttl = null │               │  ttl > 0    │
        └─────────────┘               └─────────────┘
              │                               │
              ▼                               ▼
        ┌─────────────┐               ┌─────────────────────────┐
        │ 从队列移除   │               │ 加入等待队列尾部        │
        │ 更新超时集合 │               │ (如不在队列中)          │
        └─────────────┘               └─────────────────────────┘
```

---

## 核心 Lua 脚本分析

### 获取锁脚本 (EVAL_LONG)

```lua
-- RedissonFairLock.tryLockInnerAsync()

-- 1. 清理过期线程
while true do 
    local firstThreadId2 = redis.call('lindex', KEYS[2], 0);
    if firstThreadId2 == false then 
        break; 
    end;
    
    local timeout = redis.call('zscore', KEYS[3], firstThreadId2);
    if timeout ~= false and tonumber(timeout) <= tonumber(ARGV[4]) then 
        redis.call('zrem', KEYS[3], firstThreadId2);
        redis.call('lpop', KEYS[2]);
    else 
        break; 
    end;
end;

-- 2. 检查是否可以获取锁
if (redis.call('exists', KEYS[1]) == 0) 
    and ((redis.call('exists', KEYS[2]) == 0) 
        or (redis.call('lindex', KEYS[2], 0) == ARGV[2])) then 

    -- 获取锁
    redis.call('lpop', KEYS[2]);
    redis.call('zrem', KEYS[3], ARGV[2]);
    
    -- 更新后续线程的超时时间
    local keys = redis.call('zrange', KEYS[3], 0, -1);
    for i = 1, #keys, 1 do 
        redis.call('zincrby', KEYS[3], -tonumber(ARGV[3]), keys[i]);
    end;
    
    -- 设置锁
    redis.call('hset', KEYS[1], ARGV[2], 1);
    redis.call('pexpire', KEYS[1], ARGV[1]);
    return nil;
end;

-- 3. 检查重入
if redis.call('hexists', KEYS[1], ARGV[2]) == 1 then 
    redis.call('hincrby', KEYS[1], ARGV[2],1);
    redis.call('pexpire', KEYS[1], ARGV[1]);
    return nil;
end;

-- 4. 线程已在队列中
local timeout = redis.call('zscore', KEYS[3], ARGV[2]);
if timeout ~= false then 
    local ttl = redis.call('pttl', KEYS[1]);
    return math.max(0, ttl); 
end;

-- 5. 加入等待队列
local lastThreadId = redis.call('lindex', KEYS[2], -1);
local ttl;
if lastThreadId ~= false and lastThreadId ~= ARGV[2] 
    and redis.call('zscore', KEYS[3], lastThreadId) ~= false then 
    ttl = tonumber(redis.call('zscore', KEYS[3], lastThreadId)) - tonumber(ARGV[4]);
else 
    ttl = redis.call('pttl', KEYS[1]);
end;

local timeout = ttl + tonumber(ARGV[3]) + tonumber(ARGV[4]);
if redis.call('zadd', KEYS[3], timeout, ARGV[2]) == 1 then 
    redis.call('rpush', KEYS[2], ARGV[2]);
end;
return ttl;
```

### 参数说明

| 参数 | 说明 |
|------|------|
| KEYS[1] | 锁键名 |
| KEYS[2] | 等待队列 List |
| KEYS[3] | 超时集合 Sorted Set |
| ARGV[1] | 锁过期时间 (leaseTime) |
| ARGV[2] | 锁名称 (clientId:threadId) |
| ARGV[3] | threadWaitTime (等待超时) |
| ARGV[4] | 当前时间戳 |

### 释放锁脚本

```lua
-- RedissonFairLock.unlockInnerAsync()

-- 1. 清理过期线程
while true do 
    local firstThreadId2 = redis.call('lindex', KEYS[2], 0);
    if firstThreadId2 == false then 
        break; 
    end;
    
    local timeout = redis.call('zscore', KEYS[3], firstThreadId2);
    if timeout ~= false and tonumber(timeout) <= tonumber(ARGV[4]) then 
        redis.call('zrem', KEYS[3], firstThreadId2);
        redis.call('lpop', KEYS[2]);
    else 
        break; 
    end;
end;

-- 2. 锁已释放，唤醒下一个等待线程
if (redis.call('exists', KEYS[1]) == 0) then 
    local nextThreadId = redis.call('lindex', KEYS[2], 0); 
    if nextThreadId ~= false then 
        -- 发布解锁消息给下一个线程
        redis.call(ARGV[5], KEYS[4] .. ':' .. nextThreadId, ARGV[1]); 
    end; 
    return 1; 
end;

-- 3. 检查持有锁的线程
if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then 
    return nil;  -- 未持有锁
end; 

-- 4. 重入计数 -1
local counter = redis.call('hincrby', KEYS[1], ARGV[3], -1); 

if (counter > 0) then 
    -- 还有重入，续期
    redis.call('pexpire', KEYS[1], ARGV[2]); 
    return 0; 
end; 

-- 5. 完全释放，删除锁，唤醒下一个线程
redis.call('del', KEYS[1]); 

local nextThreadId = redis.call('lindex', KEYS[2], 0); 
if nextThreadId ~= false then 
    -- 通知下一个线程获取锁
    redis.call(ARGV[5], KEYS[4] .. ':' .. nextThreadId, ARGV[1]); 
end; 
return 1;
```

---

## 等待队列管理

### 订阅机制

公平锁使用线程级别的订阅，确保只唤醒等待队列头部的线程：

```java
// RedissonFairLock.java
@Override
protected CompletableFuture<RedissonLockEntry> subscribe(long threadId) {
    // 每个线程订阅独立的 channel
    return pubSub.subscribe(
        getEntryName() + ":" + threadId,  // 例如: lock:thread-123
        getChannelName() + ":" + getLockName(threadId)
    );
}
```

### 公平性保证

1. **队列顺序**：所有等待线程加入 Redis List 尾部
2. **头部获取**：只有队列头部的线程才能获取锁
3. **依次唤醒**：释放锁时只通知队列头部的下一个线程
4. **超时清理**：定期清理超时的等待线程

---

## 流程图

### 线程获取锁流程

```
线程A                              Redis                      线程B
 │                                   │                           │
 │──── tryLock() ──────────────────>│                           │
 │                                   │                           │
 │     (锁空闲，队列空)               │                           │
 │<─── return null (获取成功) ──────│                           │
 │                                   │                           │
 │          持有锁                   │                           │
 │                                   │──── tryLock() ──────────>│
 │                                   │                           │
 │     (检查队列头部)                │<─── ttl=10000 (需等待)  │
 │                                   │                           │
 │                                   │──── subscribe() ────────>│
 │                                   │     (channel:A)         │
 │                                   │                           │
 │                                   │     [等待信号]           │
 │                                   │                           │
 │──── unlock() ───────────────────>│                           │
 │                                   │                           │
 │     检查队列，唤醒下一个           │                           │
 │     (线程B 在队列头部)            │                           │
 │                                   │──── publish(channel:A) ─>│
 │                                   │                           │──── latch.release()
 │                                   │                           │──── tryLock()
 │                                   │<─── [重试获取] ──────────│
 │                                   │                           │
 │                                   │     (现在线程B在头部)    │
 │                                   │<─── return null ────────│
 │                                   │                           │
```

### 队列状态变化

```
初始状态:
  Queue: []        Lock: (空)       TimeoutSet: {}

线程A 获取锁:
  Queue: []        Lock: {A:1}      TimeoutSet: {}

线程B 请求锁:
  Queue: [B]       Lock: {A:1}      TimeoutSet: {B:10000}

线程C 请求锁:
  Queue: [B,C]     Lock: {A:1}      TimeoutSet: {B:10000, C:20000}

线程A 释放锁:
  Queue: [C]       Lock: {B:1}      TimeoutSet: {C:10000}
                         ↑唤醒B
```

---

## 与非公平锁对比

| 特性 | RedissonLock (非公平) | RedissonFairLock (公平) |
|------|---------------------|------------------------|
| **获取顺序** | 不保证 FIFO | 保证 FIFO |
| **实现复杂度** | 简单 | 复杂 |
| **性能** | 高 | 较低 |
| **饥饿问题** | 可能发生 | 不会发生 |
| **数据结构** | 单 Hash | Hash + List + Sorted Set |
| **网络开销** | 低 | 较高 |

### 性能差异原因

1. **额外数据结构**：公平锁需要维护等待队列
2. **Lua 脚本更长**：需要处理队列逻辑
3. **每次操作更重**：入队、出队、更新超时集合

---

## 使用示例

```java
// 获取公平锁
RLock fairLock = redisson.getFairLock("myFairLock");

// 阻塞获取
fairLock.lock();
try {
    // 业务逻辑
} finally {
    fairLock.unlock();
}

// 尝试获取，带等待时间
boolean acquired = fairLock.tryLock(10, 30, TimeUnit.SECONDS);

// 可重入
fairLock.lock();
try {
    fairLock.lock();  // 重入
    try {
        // 业务逻辑
    } finally {
        fairLock.unlock();
    }
} finally {
    fairLock.unlock();
}
```

---

## 总结

RedissonFairLock 的核心特点：

1. **FIFO 顺序**：严格按请求顺序获取锁
2. **三数据结构**：Hash(锁) + List(队列) + Sorted Set(超时)
3. **超时清理**：自动清理长时间未获取锁的线程
4. **线程级订阅**：每个等待线程订阅独立的 channel
5. **可重入**：支持同一线程多次获取锁

**适用场景**：
- 需要严格保证获取顺序的业务
- 防止低优先级线程被"饿死"
- 金融类、订单类对顺序敏感的系统

**注意事项**：
- 公平锁性能低于非公平锁
- 需要合理设置 `threadWaitTime` 超时时间
- 长时间运行需监控队列长度
