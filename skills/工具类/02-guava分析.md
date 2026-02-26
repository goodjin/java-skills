# Guava 分析

## 核心模块

| 模块 | 功能 |
|------|------|
| cache | 本地缓存 |
| collect | 集合增强 |
| concurrent | 并发工具 |
| eventbus | 事件总线 |
| hash | 哈希工具 |
| base | 基础工具 |
| primitives | 原始类型 |

## 常用工具

### 缓存
```java
LoadingCache<Key, Graph> graphs = CacheBuilder.newBuilder()
    .maximumSize(10000)
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .build(key -> createGraph(key));
```

### 集合
```java
// 不可变集合
ImmutableList<String> list = ImmutableList.of("a", "b", "c");

// MultiMap (一键多值)
Multimap<String, String> multimap = ArrayListMultimap.create();
multimap.put("fruit", "apple");
multimap.put("fruit", "banana");

// Table (二维表)
Table<String, String, Integer> table = HashBasedTable.create();
table.put("row1", "col1", 1);
```

### 并发
```java
// ListenableFuture
ListenableFuture<Query> queryFuture = service.query(query);
Futures.addCallback(queryFuture, callback, executor);

// RateLimiter
RateLimiter limiter = RateLimiter.create(1000);
limiter.acquire();
```

### EventBus
```java
// 发布
eventBus.post(new OrderEvent(order));

// 订阅
@Subscribe
public void handleOrder(OrderEvent event) {
    // 处理
}
```

## 对比 JDK

| 特性 | Guava | JDK |
|------|-------|-----|
| 缓存 | Cache | 无 |
| 不可变集合 | Immutable* | Collections.unmodifiable* |
| MultiMap | 有 | 无 |
| EventBus | 有 | PropertyChangeListener |
