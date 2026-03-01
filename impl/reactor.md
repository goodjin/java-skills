# Reactor 项目分析

## 项目简介
Reactor 是 Spring 生态的响应式编程库，提供基于 Reactive Streams 标准的实现。是 Spring WebFlux 的基石，提供 Flux（0-N元素）和 Mono（0-1元素）两种响应式类型。

## 核心类结构

### 1. Flux（流）
- **位置**: `reactor.core.publisher.Flux`
- **职责**: 响应式流，表示0到N个元素的序列
- **特点**: 完整实现 Reactive Streams API

### 2. Mono（单）
- **位置**: `reactor.core.publisher.Mono`
- **职责**: 响应式单值，表示0或1个元素
- **特点**: 适合异步返回单个结果

### 3. Subscriber（订阅者）
- **位置**: `reactor.core.Subscriber`
- **职责**: 消费响应式流
- **方法**: `onSubscribe()`, `onNext()`, `onError()`, `onComplete()`

### 4. Subscription（订阅）
- **职责**: 控制数据流
- **方法**: `request()`, `cancel()`

### 5. Scheduler（调度器）
- **位置**: `reactor.core.scheduler.Schedulers`
- **实现**:
  - `Schedulers.boundedElastic()` - 阻塞任务（推荐用于IO）
  - `Schedulers.parallel()` - 并行计算
  - `Schedulers.single()` - 单线程
  - `Schedulers.immediate()` - 当前线程

### 6. Context（上下文）
- **位置**: `reactor.util.context.Context`
- **职责**: 跨操作符传递数据
- **类似**: ThreadLocal的响应式版本

## 设计模式

### 1. 观察者模式（Observer）
- Publisher（发布者）和 Subscriber（订阅者）
- 支持异步数据流

### 2. 装饰器模式（Decorator）
- 各种 `Operator` 包装原始 Flux/Mono
- 如 `map()`, `flatMap()`, `filter()` 等

### 3. 责任链模式（Chain of Responsibility）
- 操作符链式调用
- 每个操作符负责特定转换

### 4. 工厂模式（Factory）
- `Flux.just()`, `Flux.fromIterable()`, `Mono.fromCallable()` 等

### 5. 策略模式（Strategy）
- 多种 `Retry` 策略
- 多种背压策略

## 代码技巧

### 1. 创建响应式流
```java
// 从已知数据创建
Flux<String> flux = Flux.just("A", "B", "C");
Mono<String> mono = Mono.just("Hello");

// 从异步操作创建
Mono<User> userMono = Mono.fromCallable(() -> userService.findById(id));

// 从Publisher创建
Flux<Integer> flux = Flux.from(stream);
```

### 2. 转换操作
```java
flux.map(String::toUpperCase)
    .flatMap(this::processAsync)
    .filter(item -> item.isValid())
    .collectList()
    .subscribe();
```

### 3. 线程调度
```java
// 指定发布者执行线程
.subscribeOn(Schedulers.boundedElastic())

// 指定订阅者执行线程
.observeOn(Schedulers.parallel())

// 切换到阻塞操作
.transformDeferred(this::blockingOperation)
```

### 4. 背压处理
```java
// 消费者控制请求数量
flux.request(10);

// 背压策略
flux.onBackpressureBuffer()
    .onBackpressureDrop()
    .onBackpressureLatest()
```

### 5. 错误处理
```java
mono.doOnError(error -> log.error(error))
    .onErrorResume(e -> Mono.just(defaultValue))
    .onErrorReturn(fallbackValue)
    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
```

### 6. Context使用
```java
// 写入Context
mono.subscriberContext(Context.of("key", "value"))

// 读取Context
mono.transformDeferred Mono<String> transformDeferred(ctx -> 
    ctx.getOrDefault("key", "default"))
```

### 7. 冷热流
```java
// 冷流：每次订阅重新执行
Flux<Integer> cold = Flux.create(sink -> {
    // 每次订阅都执行
});

// 热流：共享数据
ConnectableFlux<Integer> hot = Flux.just(1, 2, 3).publish();
hot.connect();
```

## 性能优化要点

1. **使用boundedElastic**: 阻塞IO操作使用boundedElastic调度器
2. **避免阻塞**: 在响应式链中避免同步阻塞调用
3. **正确使用Mono/Flux**: 单值用Mono，多值用Flux
4. **背压处理**: 生产快于消费时使用背压策略
5. **Context传递**: 使用Context而非ThreadLocal
6. **资源清理**: 使用 `using()` 或 `doFinally()` 清理资源
