# Redisson 分布式集合深入分析

## 概述

Redisson 提供了完整的分布式集合实现，包括 Map、List、Set、Queue 等。这些集合不仅支持基本的 CRUD 操作，还支持分布式特性如：本地缓存、写入后模式、键过期监听等。

---

## 核心类结构

### RMap 分布式映射

```
RedissonMap<K, V>
    │
    ├── RedissonExpirable (可过期)
    │       │
    │       └── RedissonObject (基础对象)
    │
    ├── RedissonMapCache<K, V> (带缓存的 Map)
    │       │
    │       └── RedissonLocalCachedMap<K, V> (本地缓存)
    │
    └── RedissonLiveObjectService (Live Objects)
```

### RList 分布式列表

```
RedissonList<V>
    │
    └── BaseRedissonList<V>
            │
            └── RedissonExpirable
```

---

## RMap 分布式映射

### 核心数据结构

RedissonMap 基于 Redis Hash 实现：

```
key: redisson_map:{mapName}
    │
    ├── field (序列化后的 key)  →  value (序列化后的 value)
    └── ...
```

### 核心实现分析

```java
// RedissonMap.java - 核心属性
public class RedissonMap<K, V> extends RedissonExpirable implements RMap<K, V> {
    
    final RedissonClient redisson;
    final MapOptions<K, V> options;
    final WriteBehindService writeBehindService;  // 异步写入服务
    final MapWriteBehindTask writeBehindTask;    // 写入任务
}
```

### 基本操作

```java
// 获取元素
public V get(Object key) {
    return get(getAsync(key));
}

public RFuture<V> getAsync(K key) {
    return commandExecutor.readAsync(getRawName(), codec, HGET, 
            getRawName(), encode(key));
}

// 存放元素
public V put(K key, V value) {
    return get(putAsync(key, value));
}

public RFuture<V> putAsync(K key, V value) {
    return commandExecutor.writeAsync(getRawName(), codec, HSET, 
            getRawName(), encode(key), encode(value));
}

// 删除元素
public V remove(Object key) {
    return get(removeAsync(key));
}

// 获取大小
public int size() {
    return get(sizeAsync());
}

public RFuture<Integer> sizeAsync() {
    return commandExecutor.readAsync(getRawName(), codec, HLEN, getRawName());
}
```

### MapOptions 配置选项

RedissonMap 支持丰富的配置选项：

```java
MapOptions<K, V> options = MapOptions.<K, V>defaults()
    .writer(new MapWriter<K, V>() {
        @Override
        public void write(Map<K, V> map) {
            // 异步写入逻辑
        }
    })
    .writeMode(WriteMode.WRITE_THROUGH)  // WRITE_THROUGH / WRITE_BEHIND
    .writeDelay(100)                      // 写入延迟 (毫秒)
    .writeBatchSize(100)                 // 批量大小
    .keySerializer(new SomeSerializer()) // 键序列化器
    .valueSerializer(new SomeSerializer()) // 值序列化器
    .ttl(30, TimeUnit.SECONDS)           // 默认 TTL
    .maxIdleTime(10, TimeUnit.MINUTES)   // 最大空闲时间
    .build();
```

### 写入模式

#### 1. WRITE_THROUGH (同步写入)

```java
// 写入时同步到数据源
options.writeMode(WriteMode.WRITE_THROUGH);
```

#### 2. WRITE_BEHIND (异步写入)

```java
// 写入后异步批量刷新到数据源
options.writeMode(WriteMode.WRITE_BEHIND)
       .writeDelay(500)  // 500ms 后批量写入
       .writeBatchSize(100);
```

### 本地缓存 (LocalCachedMap)

```java
// RedissonLocalCachedMap - 支持本地缓存的 Map
RLocalCachedMap<String, User> map = redisson.getLocalCachedMap(
    "userMap",
    LocalMapConfig.builder()
        .cacheSize(10000)                    // 缓存最大数量
        .maxIdle(TimeUnit.MINUTES.toMillis(10))  // 最大空闲时间
        .timeToLive(TimeUnit.MINUTES.toMillis(10)) // 存活时间
        .invalidationsQueueCapacity(100000)   // 失效队列容量
        .build()
);
```

**特点：**
- 客户端本地缓存，减少网络往返
- 支持缓存失效同步
- 通过 Redis PubSub 通知其他节点缓存失效

### 锁支持

RedissonMap 为每个 key 提供独立的锁：

```java
// 获取指定 key 的锁
RLock lock = map.getLock(key);
lock.lock();
try {
    // 原子操作
    V value = map.get(key);
    // ...
} finally {
    lock.unlock();
}

// 获取公平锁
RFairLock fairLock = map.getFairLock(key);

// 获取读写锁
RReadWriteLock rwLock = map.getReadWriteLock(key);
```

---

## RList 分布式列表

### 核心数据结构

RedissonList 基于 Redis List 实现：

```
key: redisson_list:{listName}
    │
    ├── index 0  →  value
    ├── index 1  →  value
    └── ...
```

### 核心实现

```java
// BaseRedissonList.java
public class BaseRedissonList<V> extends RedissonExpirable {
    
    public int size() {
        return get(sizeAsync());
    }
    
    public RFuture<Integer> sizeAsync() {
        return commandExecutor.readAsync(getRawName(), codec, LLEN_INT, getRawName());
    }
    
    public boolean add(V e) {
        return get(addAsync(e));
    }
    
    public RFuture<Boolean> addAsync(V e) {
        return commandExecutor.writeAsync(getRawName(), codec, RPUSH_BOOLEAN, 
                getRawName(), encode(e));
    }
    
    public V get(int index) {
        return get(getAsync(index));
    }
    
    public RFuture<V> getAsync(int index) {
        return commandExecutor.readAsync(getRawName(), codec, LINDEX, 
                getRawName(), index);
    }
    
    public V remove(int index) {
        return get(removeAsync(index));
    }
    
    public RFuture<V> removeAsync(int index) {
        return commandExecutor.writeAsync(getRawName(), codec, LREM, 
                getRawName(), 1, encode(get(index)));
    }
}
```

### 阻塞操作

```java
// 获取并移除队首元素（阻塞）
RBlockingQueue<String> queue = redisson.getBlockingQueue("queue");
String element = queue.poll(10, TimeUnit.SECONDS);

// 获取队尾元素
String last = queue.peekLast();

// 带超时的获取
String elem = queue.poll(10, TimeUnit.SECONDS);
```

---

## 与 Jedis/Lettuce 对比

### 功能对比

| 特性 | Redisson | Jedis | Lettuce |
|------|----------|-------|---------|
| **Map 实现** | RMap 完整实现 | Hash 操作封装 | Redis commands |
| **List 实现** | RList 完整实现 | List 操作封装 | Redis commands |
| **本地缓存** | ✅ LocalCachedMap | ❌ | ❌ |
| **Write-Behind** | ✅ 异步批量写入 | ❌ | ❌ |
| **键过期监听** | ✅ MapCache | ❌ | ❌ |
| **对象序列化** | 内置多种编解码器 | 手动处理 | 手动处理 |
| **完整 API** | ✅ 符合 Java 集合规范 | ⚠️ 基础操作 | ⚠️ 基础操作 |

### 代码复杂度对比

**Redisson:**
```java
RMap<String, User> map = redisson.getMap("users");
map.put("1", new User("John"));
User user = map.get("1");
map.getLock("1").lock();  // 分布式锁
```

**Jedis:**
```java
// 需要手动处理序列化
Map<String, String> hash = new HashMap<>();
hash.put("1", serialize(new User("John")));
jedis.hset("users", "1", serialize(user));
// 锁需要手动实现
String lock = jedis.set("lock:1", "1", "NX", "PX", 30000);
```

**Lettuce:**
```java
// 类似的复杂度，需要手动封装
Map<String, String> hash = new HashMap<>();
hash.put("1", serialize(user));
redisCommands.hset("users", hash);
// 分布式锁需要额外实现
```

---

## 流程图

### Map 写入流程 (Write-Behind 模式)

```
┌─────────────────────────────────────────────────────────────┐
│                      put(key, value)                        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  写入本地缓存 (本地操作)                      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│               写入 Redis (HSET)                             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
                   ┌───────────────┐
                   │  Write-Mode?  │
                   └───────────────┘
                     │            │
          WRITE_THROUGH      WRITE_BEHIND
                     │            │
                     ▼            ▼
           ┌─────────────────┐   ┌─────────────────┐
           │ 立即写入数据源  │   │ 加入写入队列   │
           └─────────────────┘   └─────────────────┘
                                         │
                                         ▼
                              ┌─────────────────────────┐
                              │  定时批量写入 (延迟后)   │
                              └─────────────────────────┘
                                         │
                                         ▼
                              ┌─────────────────────────┐
                              │      写入数据源          │
                              └─────────────────────────┘
```

### 本地缓存同步流程

```
Node A                                        Node B
   │                                             │
   │──── put("key", "value") ──────────────────>│
   │                                             │
   │       本地缓存                               │
   │  ┌─────────────────┐                        │
   │  │ "key" → "value" │                        │
   │  └─────────────────┘                        │
   │                                             │
   │──── publish("invalidate", "key") ──────────│
   │                                             │
   │                                    本地缓存 │
   │                                    ┌───────┤
   │                                    │失效key│
   │                                    └───────┘
```

---

## 总结

Redisson 分布式集合的核心特点：

1. **完整的集合 API**：完全符合 Java 集合框架规范
2. **多种数据结构**：Map、List、Set、Queue、SortedSet 等
3. **本地缓存支持**：LocalCachedMap 减少网络开销
4. **Write-Behind 模式**：异步批量写入，提高性能
5. **键级锁**：每个 key 支持独立的分布式锁
6. **丰富配置**：序列化器、过期时间、缓存策略等

相比 Jedis/Lettuce，Redisson 提供了更高层次的抽象，大大简化了分布式应用的开发。
