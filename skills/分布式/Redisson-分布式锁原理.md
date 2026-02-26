# Redisson 分布式锁原理深入分析

## 概述

Redisson 是一个基于 Redis 的分布式 Java 对象/服务框架，其分布式锁是其最核心的功能之一。Redisson 分布式锁基于 Redis 的原子操作和发布订阅机制实现，支持可重入、锁续期（Watchdog）、公平锁等特性。

---

## 核心类结构

```
RedissonLock (非公平锁)
    │
    ├── RedissonBaseLock (基础锁实现)
    │       │
    │       └── RedissonExpirable (可过期对象)
    │
    └── RedissonFairLock (公平锁，继承 RedissonLock)
```

### 核心类说明

| 类名 | 职责 |
|------|------|
| `RedissonLock` | 非公平分布式锁实现，实现 `RLock` 接口 |
| `RedissonBaseLock` | 锁的基础抽象类，提供通用锁逻辑 |
| `RedissonFairLock` | 公平锁实现，保证获取锁的 FIFO 顺序 |
| `RedissonLockEntry` | 锁订阅条目，包含信号量和监听器 |
| `LockPubSub` | 锁的发布订阅服务，处理解锁消息 |

---

## 分布式锁原理

### 1. 数据结构

Redisson 使用 Redis Hash 结构存储锁信息：

```
锁键名 (redisson_lock:{lockName})
    │
    ├── {clientId}:{threadId}  →  重入计数
    └── PX/EX                  →  过期时间
```

### 2. 获取锁流程 (lock)

```java
// RedissonLock.java - lock() 方法核心逻辑
private void lock(long leaseTime, TimeUnit unit, boolean interruptibly) {
    long threadId = Thread.currentThread().getId();
    
    // 1. 尝试获取锁
    Long ttl = tryAcquire(-1, leaseTime, unit, threadId);
    
    // 2. 获取成功，直接返回
    if (ttl == null) {
        return;
    }
    
    // 3. 获取失败，订阅锁释放消息
    CompletableFuture<RedissonLockEntry> future = subscribe(threadId);
    
    // 4. 循环尝试获取锁
    while (true) {
        ttl = tryAcquire(-1, leaseTime, unit, threadId);
        if (ttl == null) {
            break;
        }
        
        // 5. 等待锁释放通知
        if (ttl >= 0) {
            entry.getLatch().tryAcquire(ttl, TimeUnit.MILLISECONDS);
        } else {
            entry.getLatch().acquireUninterruptibly();
        }
    }
    
    // 6. 取消订阅
    unsubscribe(entry, threadId);
}
```

### 3. 获取锁 Lua 脚本

```lua
-- tryLockInnerAsync 使用的 EVAL 脚本
-- 逻辑：如果锁不存在或者当前线程已持有锁，则获取锁

if (redis.call('exists', KEYS[1]) == 0) 
    or (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then 
    -- 重入计数 +1
    redis.call('hincrby', KEYS[1], ARGV[2], 1); 
    -- 设置过期时间
    redis.call('pexpire', KEYS[1], ARGV[1]); 
    return nil; 
end; 
-- 锁被占用，返回剩余 TTL
return redis.call('pttl', KEYS[1]);
```

**参数说明：**
- `KEYS[1]`：锁键名
- `ARGV[1]`：过期时间（毫秒）
- `ARGV[2]`：锁名称 `{clientId}:{threadId}`

### 4. 释放锁流程 (unlock)

```java
// RedissonBaseLock.java - unlockAsync()
public RFuture<Void> unlockAsync(long threadId) {
    String requestId = getServiceManager().generateId();
    return getServiceManager().execute(() -> unlockAsync0(threadId, requestId));
}

private RFuture<Void> unlockAsync0(long threadId, String requestId) {
    // 调用 Lua 脚本释放锁
    CompletionStage<Boolean> future = unlockInnerAsync(threadId, requestId);
    
    // 取消锁续期
    return future.handle((res, e) -> {
        cancelExpirationRenewal(threadId, res);
        // ...
    });
}
```

### 5. 释放锁 Lua 脚本

```lua
-- unlockInnerAsync 使用的 EVAL 脚本
local val = redis.call('get', KEYS[3]); 
if val ~= false then 
    return tonumber(val);
end; 

-- 检查当前线程是否持有锁
if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then 
    return nil;  -- 未持有锁，抛出异常
end; 

-- 减少重入计数
local counter = redis.call('hincrby', KEYS[1], ARGV[3], -1); 

if (counter > 0) then 
    -- 还有重入，续期
    redis.call('pexpire', KEYS[1], ARGV[2]); 
    return 0; 
else 
    -- 锁完全释放，删除锁键
    redis.call('del', KEYS[1]); 
    -- 发布锁释放消息
    redis.call(ARGV[4], KEYS[2], ARGV[1]); 
    return 1; 
end;
```

### 6. 锁续期机制 (Watchdog)

```java
// RedissonBaseLock.java
protected void scheduleExpirationRenewal(long threadId) {
    renewalScheduler.renewLock(getRawName(), threadId, getLockName(threadId));
}
```

Redisson 使用后台线程定期续期锁的过期时间，默认续期间隔为 `lockWatchdogTimeout / 3`（约 10 秒）。

```java
// 默认配置
private long lockWatchdogTimeout = 30 * 1000;  // 30 秒
```

---

## 获取锁流程图

```
┌─────────────────────────────────────────────────────────────────┐
│                         lock()                                   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│           tryAcquire() - 尝试获取锁                              │
│     (执行 Lua 脚本: exists / hexists / hincrby / pexpire)       │
└─────────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┴───────────────┐
              │                               │
              ▼                               ▼
        ┌─────────┐                    ┌─────────────┐
        │ ttl=null│                    │ ttl > 0     │
        │ 获取成功 │                    │ 获取失败     │
        └─────────┘                    └─────────────┘
              │                               │
              ▼                               ▼
        ┌─────────┐                ┌─────────────────────────┐
        │  返回   │                │ subscribe() 订阅消息     │
        └─────────┘                └─────────────────────────┘
                                             │
                                             ▼
                              ┌────────────────────────────────┐
                              │    while (true) 循环           │
                              │    1. tryAcquire() 重试        │
                              │    2. latch.wait(ttl) 等待     │
                              │    3. 收到解锁消息唤醒         │
                              └────────────────────────────────┘
                                             │
                                             ▼
                              ┌────────────────────────────────┐
                              │    unsubscribe() 取消订阅    │
                              └────────────────────────────────┘
                                             │
                                             ▼
                              ┌────────────────────────────────┐
                              │         返回                   │
                              └────────────────────────────────┘
```

---

## 等待机制详解

### 发布订阅模式

```
线程A (持有锁)                    Redis                       线程B (等待锁)
     │                              │                              │
     │──── lock() ─────────────────>│                              │
     │                              │──── tryAcquire() ──────────>│
     │                              │<─── ttl=5000 ───────────────│
     │                              │                              │
     │──── subscribe() ───────────>│                              │
     │                              │<─── [等待信号] ──────────────│
     │                              │                              │
     │──── unlock() ──────────────>│                              │
     │                              │──── publish(UNLOCK) ──────>│
     │                              │                              │──── latch.release()
     │                              │                              │──── tryAcquire()
     │                              │<──── [重试获取] ────────────│
     │                              │                              │
```

### RedissonLockEntry 结构

```java
public class RedissonLockEntry {
    private final Semaphore latch;           // 信号量，控制线程等待
    private final CompletableFuture<...> promise;
    private final ConcurrentLinkedQueue<Runnable> listeners;
}
```

等待线程通过 `latch.acquire()` 阻塞，持有锁线程释放时通过 `latch.release()` 唤醒。

---

## 核心接口 RLock

```java
public interface RLock extends Lock, RExpirable {
    
    // 阻塞获取锁
    void lock();
    
    // 带过期时间的锁
    void lock(long leaseTime, TimeUnit unit);
    
    // 可中断获取
    void lockInterruptibly() throws InterruptedException;
    
    // 尝试获取
    boolean tryLock();
    
    // 尝试获取，带等待时间
    boolean tryLock(long waitTime, long leaseTime, TimeUnit unit);
    
    // 释放锁
    void unlock();
    
    // 强制释放
    boolean forceUnlock();
    
    // 检查是否被持有
    boolean isLocked();
    
    // 检查当前线程是否持有
    boolean isHeldByCurrentThread();
    
    // 获取重入次数
    int getHoldCount();
}
```

---

## 与 Jedis/Lettuce 对比

| 特性 | Redisson | Jedis | Lettuce |
|------|----------|-------|---------|
| **锁类型** | 完整分布式锁实现 | 基础 SETNX | 基础 SETNX |
| **可重入** | ✅ 支持 | ❌ 需手动实现 | ❌ 需手动实现 |
| **锁续期** | ✅ Watchdog | ❌ 需手动实现 | ❌ 需手动实现 |
| **公平锁** | ✅ RedissonFairLock | ❌ 需手动实现 | ❌ 需手动实现 |
| **使用复杂度** | 低 | 高 | 高 |
| **Lua 脚本** | 内置 | 需手动编写 | 需手动编写 |

---

## 总结

Redisson 分布式锁的核心原理：

1. **基于 Redis 原子操作**：使用 Lua 脚本保证获取/释放锁的原子性
2. **可重入**：通过 Hash 结构存储线程重入计数
3. **发布订阅机制**：等待线程通过订阅锁释放消息实现阻塞/唤醒
4. **Watchdog 续期**：后台线程自动延长锁的过期时间，防止业务未完成锁过期
5. **非公平锁**：不保证获取锁的顺序，可能产生"饥饿"问题

对于需要公平锁的场景，可以使用 `RedissonFairLock`，它通过 Redis List 维护等待队列，保证 FIFO 顺序。
