# Guava Cache 本地缓存源码分析

## 概述

Guava Cache 是 Google Guava 库提供的本地内存缓存实现，提供了高性能的键值对缓存功能。与 JDK 的 `ConcurrentHashMap` 相比，Guava Cache 提供了丰富的缓存策略，包括：
- 自动加载缓存值
- LRU（最近最少使用）淘汰策略
- 基于时间（访问时间/写入时间）的过期策略
- 弱引用/软引用支持
- 缓存统计
- 淘汰监听器

> ⚠️ **注意**：Guava 官方推荐使用 [Caffeine](https://github.com/ben-manes/caffeine/wiki) 作为替代方案，Caffeine 提供了更好的性能和更多的特性。

## 源码结构

```
com.google.common.cache/
├── Cache.java                    # 缓存接口
├── LoadingCache.java             # 带自动加载的缓存接口
├── CacheBuilder.java             # 缓存构建器（核心）
├── CacheLoader.java              # 缓存加载器
├── LocalCache.java                # 核心实现（~5000行）
├── CacheStats.java               # 缓存统计
├── RemovalListener.java          # 淘汰监听器
└── ...
```

## 实现原理

### 1. CacheBuilder - 建造者模式

`CacheBuilder` 使用建造者模式来配置缓存参数：

```java
LoadingCache<Key, Graph> graphs = CacheBuilder.newBuilder()
    .maximumSize(10000)                    // 最大容量
    .expireAfterWrite(Duration.ofMinutes(10))  // 写入后过期
    .removalListener(MY_LISTENER)           // 淘汰监听器
    .build(new CacheLoader<Key, Graph>() {
        public Graph load(Key key) throws AnyException {
            return createExpensiveGraph(key);
        }
    });
```

关键配置方法：

| 方法 | 说明 |
|------|------|
| `maximumSize(long size)` | 设置最大缓存条目数 |
| `maximumWeight(long weight)` | 设置最大权重 |
| `expireAfterWrite(Duration duration)` | 写入后过期 |
| `expireAfterAccess(Duration duration)` | 访问后过期 |
| `weakKeys()` | 弱引用键 |
| `weakValues()` | 弱引用值 |
| `softValues()` | 软引用值 |
| `removalListener()` | 淘汰监听器 |
| `recordStats()` | 开启统计 |

### 2. LocalCache - 核心实现

`LocalCache` 是缓存的核心实现，灵感来源于 JDK 的 `ConcurrentHashMap`：

```java
final class LocalCache<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V>
```

#### 2.1 分段锁设计

LocalCache 采用**分段锁（Segment）**设计：

```
┌─────────────────────────────────────────┐
│              LocalCache                │
├─────────┬─────────┬─────────┬───────────┤
│Segment 0│Segment 1│Segment 2│... Segment N│
├─────────┼─────────┼─────────┼───────────┤
│HashTable│HashTable│HashTable│  HashTable│
└─────────┴─────────┴─────────┴───────────┘
```

- 默认分段数：`4`（可通过 `concurrencyLevel` 配置）
- 每个 Segment 是一个独立的哈希表 + LRU 淘汰策略
- 不同 Segment 的操作互不阻塞，提高并发度

#### 2.2 LRU 淘汰策略

每个 Segment 维护自己的 LRU 队列：

```java
// Segment 中的关键字段
final Segment<K, V>[] segments;
final ReferenceQueue<K> keyReferenceQueue;    // 键引用队列
final ReferenceQueue<V> valueReferenceQueue;  // 值引用队列
final Queue<ReferenceEntry<K, V>> writeQueue;  // 写入顺序队列
final Queue<ReferenceEntry<K, V>> accessQueue; // 访问顺序队列
```

淘汰时机：
- 每次写操作时检查是否需要淘汰
- 每隔 DRAIN_THRESHOLD（63）次操作后批量淘汰
- 调用 `cleanUp()` 时主动淘汰

#### 2.3 缓存命中流程

```java
// LocalCache.get() 核心逻辑
V get(K key, CacheLoader<? super K, V> loader) {
    // 1. 计算哈希值
    int hash = hash(key);
    
    // 2. 定位到具体的 Segment
    Segment<K, V> segment = segments[hash & segmentMask];
    
    // 3. 在 Segment 的哈希表中查找
    ReferenceEntry<K, V> entry = segment.getEntry(hash, key);
    
    if (entry != null) {
        // 4. 检查是否过期
        long now = ticker.read();
        V value = entry.getValue();
        if (value != null && !isExpired(entry, now)) {
            // 5. 记录命中
            segment.recordHit(entry);
            return value;
        }
        // 6. 准备重新加载
        return loadValue(entry, key, hash, loader, now);
    }
    
    // 7. 缓存未命中，需要加载
    return loadOrCompute(key, loader);
}
```

#### 2.4 自动加载机制

```java
// LoadingCache.get(key) 实际调用
V get(K key, CacheLoader<? super K, V> loader) {
    // 如果缓存不存在，调用 loader.load(key) 加载
    // 支持 reload（重新加载）和 bulkLoad（批量加载）
}

// 批量加载
LoadingCache<K, V> cache = CacheBuilder.newBuilder()
    .build(CacheLoader.asyncReloading(loader, executor));
```

### 3. ReferenceEntry - 缓存条目

缓存中的每个条目都是 `ReferenceEntry`：

```java
interface ReferenceEntry<K, V> {
    V getValue();                      // 获取值
    void setValue(V value);            // 设置值
    long getWriteTime();               // 写入时间
    long getAccessTime();              // 最后访问时间
    ReferenceEntry<K, V> getNext();   // 哈希链 next
    // ...
}
```

支持多种引用类型：
- **StrongReference**：强引用（默认）
- **WeakReference**：弱引用，GC 时自动回收
- **SoftReference**：软引用，内存不足时回收
- **WeakValueReference**：弱引用值

## 代码示例

### 基础用法

```java
// 创建缓存
LoadingCache<String, User> userCache = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .build(new CacheLoader<String, User>() {
        @Override
        public User load(String userId) throws Exception {
            return userService.findById(userId);  // 模拟数据库查询
        }
    });

// 使用缓存
User user = userCache.get("user123");  // 首次调用会加载
User cached = userCache.get("user123"); // 直接返回缓存
```

### 统计功能

```java
LoadingCache<String, User> userCache = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .recordStats()  // 开启统计
    .build(new CacheLoader<String, User>() {
        @Override
        public User load(String key) { return loadFromDB(key); }
    });

// 获取统计信息
CacheStats stats = userCache.stats();
System.out.println("命中率: " + stats.hitRate());
System.out.println("加载时间: " + stats.averageLoadPenalty() + "ms");
System.out.println("驱逐次数: " + stats.evictionCount());
```

### 淘汰监听器

```java
RemovalListener<String, User> listener = notification -> {
    System.out.println("移除原因: " + notification.getCause());
    System.out.println("移除的键: " + notification.getKey());
    System.out.println("移除的值: " + notification.getValue());
};

LoadingCache<String, User> cache = CacheBuilder.newBuilder()
    .maximumSize(100)
    .removalListener(listener)
    .build(...);
```

### 手动失效

```java
// 单个失效
cache.invalidate("user123");

// 批量失效
cache.invalidateAll(Arrays.asList("user1", "user2", "user3"));

// 清空所有
cache.invalidateAll();
```

### 异步缓存

```java
// 使用 AsyncLoadingCache 进行异步加载
AsyncLoadingCache<String, User> asyncCache = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .buildAsync(CacheLoader.asyncReloading(loader, executor));

// 返回 ListenableFuture
ListenableFuture<User> future = asyncCache.get("user123");
future.addListener(() -> {
    User user = future.get();
    // 处理结果
}, executor);
```

## Guava Cache vs JDK

| 特性 | Guava Cache | JDK HashMap | JDK ConcurrentHashMap |
|------|-------------|-------------|----------------------|
| 自动加载 | ✅ | ❌ | ❌ |
| LRU 淘汰 | ✅ | ❌ | ❌ |
| 时间过期 | ✅ | ❌ | ❌ |
| 弱/软引用 | ✅ | ❌ | ❌ |
| 缓存统计 | ✅ | ❌ | ❌ |
| 淘汰监听 | ✅ | ❌ | ❌ |
| 线程安全 | ✅ | ❌ | ✅ |
| 批量操作 | ✅ | ✅ | ✅ |
| 阻塞加载 | ✅ | ❌ | ❌ |

## 性能特点

1. **高并发**：分段锁设计支持高并发读写
2. **低延迟**：使用 `Striped64`/原子操作减少锁竞争
3. **内存友好**：支持弱引用/软引用，避免内存泄漏
4. **预热能力**：支持统计和热点数据发现

## 最佳实践

1. **选择合适的容量**：避免过大导致 GC 压力
2. **设置合理的过期时间**：根据数据特性调整
3. **开启统计监控**：用于缓存调优
4. **处理异常**：避免缓存穿透导致雪崩
5. **考虑使用 Caffeine**：生产环境推荐

```java
// 推荐使用 Caffeine（更现代的选择）
Cache<String, User> cache = Caffeine.newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .refreshAfterWrite(1, TimeUnit.MINUTES)  // 异步刷新
    .recordStats()
    .build();
```
