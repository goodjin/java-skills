# Guava vs JDK 对比总结

## 概述

本文档对 Guava 核心功能与 JDK 进行全面对比，帮助开发者选择合适的工具。

---

## 一、集合工具对比

### 1.1 不可变集合

| 特性 | Guava ImmutableList | JDK Collections.unmodifiableList | JDK List.of |
|------|---------------------|----------------------------------|-------------|
| 真正不可变 | ✅ | ✅ | ✅ |
| 内存优化 | ✅（内部类优化） | ❌ | ✅ |
| 丰富 API | ✅ | ❌ | ❌ |
| null 支持 | ❌ | ❌ | ❌ |
| JDK 版本 | 任意 | 任意 | Java 9+ |

**代码对比**：

```java
// Guava - 推荐
ImmutableList<String> list = ImmutableList.of("a", "b", "c");

// JDK 9+
List<String> jdkList = List.of("a", "b", "c");

// JDK Collections - 不推荐
List<String> unmodifiable = Collections.unmodifiableList(new ArrayList<>(list));
```

### 1.2 Multimaps

| 特性 | Guava Multimap | JDK Map<K, List<V>> |
|------|----------------|---------------------|
| API 简洁 | ✅ 高 | ❌ 需手动处理 |
| null 处理 | get() 返回空集合 | 需要判空 |
| 自动清理 | ✅ | ❌ 需手动 |
| 实现多样性 | ✅ 多种 | ❌ 需自行实现 |

**代码对比**：

```java
// Guava - 简洁
ListMultimap<String, Integer> multimap = ArrayListMultimap.create();
multimap.put("a", 1);
multimap.put("a", 2);
System.out.println(multimap.get("a"));  // [1, 2]

// JDK - 繁琐
Map<String, List<Integer>> map = new HashMap<>();
map.computeIfAbsent("a", k -> new ArrayList<>()).add(1);
map.computeIfAbsent("a", k -> new ArrayList<>()).add(2);
System.out.println(map.get("a"));  // [1, 2]
```

### 1.3 集合工具函数

| 功能 | Guava | JDK |
|------|-------|-----|
| 集合转换 | `Lists.transform()` | `Collection.stream().map()` |
| 集合分片 | `Lists.partition()` | `Stream.ofAll().sliding()` (Java 9) |
| 集合交集 | `Sets.intersection()` | `Collection.retainAll()` |
| 集合并集 | `Sets.union()` | 手动实现 |
| 集合差集 | `Sets.difference()` | 手动实现 |
| Maps.difference | `Maps.difference()` | 手动实现 |

---

## 二、缓存工具对比

| 特性 | Guava Cache | JDK ConcurrentHashMap | JDK Cache |
|------|-------------|------------------------|-----------|
| 自动加载 | ✅ | ❌ | ❌ |
| LRU 淘汰 | ✅ | ❌ | ❌ |
| 时间过期 | ✅ | ❌ | ❌ |
| 弱/软引用 | ✅ | ❌ | ❌ |
| 缓存统计 | ✅ | ❌ | ❌ |
| 淘汰监听 | ✅ | ❌ | ❌ |
| 刷新机制 | ✅ reload | ❌ | ❌ |

> **推荐**：生产环境使用 [Caffeine](https://github.com/ben-manes/caffeine)

```java
// Guava Cache
LoadingCache<String, User> cache = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .build(CacheLoader.from(id -> loadUser(id)));

// Caffeine - 更推荐
Cache<String, User> caffeine = Caffeine.newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .build(id -> loadUser(id));
```

---

## 三、并发工具对比

### 3.1 Future

| 特性 | Guava ListenableFuture | JDK CompletableFuture | JDK Future |
|------|----------------------|---------------------|------------|
| 回调支持 | ✅ | ✅ | ❌ |
| 链式转换 | ✅ | ✅ | ❌ |
| 组合多个 | ✅ allAsList | ✅ allOf | ❌ |
| 错误恢复 | ✅ catching | ✅ handle | ❌ |
| Java 版本 | Java 7+ | Java 8+ | Java 5+ |

**代码对比**：

```java
// JDK CompletableFuture - Java 8+
CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> "result")
    .thenApply(s -> s.toUpperCase())
    .exceptionally(e -> "error");

// Guava ListenableFuture - Java 7+
ListenableFuture<String> lf = Futures.transform(
    supplyAsync(() -> "result"),
    s -> s.toUpperCase(),
    executor
);
```

### 3.2 限流器

| 特性 | Guava RateLimiter | JDK Semaphore | JDK DelayQueue |
|------|------------------|---------------|----------------|
| 限流方式 | 按速率 | 按并发数 | 按延迟 |
| 突发处理 | ✅ 平滑突发 | ❌ | ❌ |
| 预热支持 | ✅ | ❌ | ❌ |
| 平滑限流 | ✅ | ❌ | ✅ |

```java
// Guava RateLimiter - 平滑限流
RateLimiter limiter = RateLimiter.create(100);
limiter.acquire();  // 平滑获取

// JDK Semaphore - 并发控制
Semaphore semaphore = new Semaphore(10);
semaphore.acquire();  // 获取许可
```

### 3.3 原子操作

| 特性 | Guava | JDK |
|------|-------|-----|
| AtomicLongMap | ✅ 丰富 | ❌ |
| AtomicDouble | ✅ | ❌ |
| LongAdder | ✅ | ✅ (Java 8) |
| Striped64 | ✅ | ✅ (Java 8) |

---

## 四、EventBus 对比

| 特性 | Guava EventBus | JDK Observer | RxJava | Spring Events |
|------|---------------|--------------|--------|---------------|
| 类型安全 | ✅ | ❌ | ✅ | ✅ |
| 继承支持 | ✅ | ❌ | ✅ | ✅ |
| 异步支持 | ✅ | ❌ | ✅ | ✅ |
| 死事件 | ✅ | ❌ | ❌ | ❌ |
| 背压 | ❌ | ❌ | ✅ | ❌ |
| 依赖 | Guava | JDK | RxJava | Spring |
| 推荐程度 | ❌ 不推荐 | ❌ | ✅ | ✅ |

> **结论**：EventBus 已被官方标记为不推荐，建议使用 Spring 事件或 RxJava

---

## 五、常用工具对比表

| 场景 | Guava | JDK | 推荐 |
|------|-------|-----|------|
| 空值检查 | `Objects.requireNonNull()` | ❌ | Guava |
| 字符串处理 | `Splitter`, `Joiner` | `String.split()` | Guava |
| 函数式编程 | `Function`, `Predicate` | `java.util.function` | JDK 8+ |
| 预检条件 | `Preconditions.checkArgument()` | `if + throw` | Guava |
| 延迟求值 | `Supplier<T>` | `() -> T` | JDK 8+ |
| 不可变Map | `ImmutableMap` | `Map.of()` (Java 9) | JDK 9+ |
| 不可变Set | `ImmutableSet` | `Set.of()` (Java 9) | JDK 9+ |
| Optional | `Optional` | `java.util.Optional` | JDK 8+ |
| Stream | `Iterables.toStream()` | `Collection.stream()` | JDK 8+ |

---

## 六、选择建议

### 6.1 Java 版本选择

```
Java 7: 必须使用 Guava 大部分功能
Java 8+: 优先使用 JDK 内置（Stream, Optional, CompletableFuture）
Java 9+: 优先使用 JDK 不可变集合
```

### 6.2 场景选择

| 场景 | 推荐 |
|------|------|
| 需要不可变集合 | JDK 9+ 用 `List.of()`，否则用 Guava |
| 需要本地缓存 | Caffeine |
| 需要异步编程 | JDK CompletableFuture |
| 需要限流 | Guava RateLimiter |
| 需要 Multimaps | Guava |
| 需要事件总线 | Spring Events 或 RxJava |
| 需要反射工具 | Guava `Reflection` |

### 6.3 Guava 仍不可替代的功能

- **Multimap**：JDK 无直接替代
- **Cache/Caffeine**：本地缓存首选
- **集合工具函数**：比 Stream 更直观
- **Preconditions**：代码更简洁
- **EventBus**（历史项目）：维护老代码

---

## 七、迁移指南

### 7.1 ImmutableList → JDK 9+

```java
// Before
ImmutableList<String> list = ImmutableList.of("a", "b", "c");

// After (Java 9+)
List<String> list = List.of("a", "b", "c");
```

### 7.2 ListenableFuture → CompletableFuture

```java
// Before
ListenableFuture<V> lf = executor.submit(() -> compute());
Futures.addCallback(lf, callback, executor);

// After
CompletableFuture<V> cf = CompletableFuture.supplyAsync(() -> compute());
cf.thenAccept(result -> { /* callback */ });
```

### 7.3 Cache → Caffeine

```java
// Before
LoadingCache<K, V> cache = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .build(loader);

// After
Cache<K, V> cache = Caffeine.newBuilder()
    .maximumSize(1000)
    .build(loader);
```

### 7.4 EventBus → Spring Events

```java
// Before
eventBus.post(new OrderEvent());

// After (Spring)
applicationEventPublisher.publishEvent(new OrderEvent());

// 订阅
@EventListener
public void handleOrderEvent(OrderEvent event) { }
```

---

## 八、总结

| 类别 | Guava | JDK | 建议 |
|------|-------|-----|------|
| 集合-不可变 | ✅ | ✅ (Java 9+) | Java 9+ 用 JDK |
| 集合-Multimap | ✅ | ❌ | 用 Guava |
| 集合-工具 | ✅ | ✅ | 喜欢哪个用哪个 |
| 缓存 | ✅ | ❌ | 用 Caffeine |
| 并发-异步 | ✅ | ✅ (Java 8+) | 用 CompletableFuture |
| 并发-限流 | ✅ | ❌ | 用 RateLimiter |
| 事件总线 | ❌ | ❌ | 用 Spring/RxJava |

**最终建议**：
1. Java 8+ 项目优先使用 JDK 内置功能
2. 需要 Caffeine/RateLimiter/Multimap 时保留 Guava
3. 新项目避免使用 EventBus
4. 保持最小依赖原则
