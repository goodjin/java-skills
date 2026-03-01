# RxJava 项目分析

## 项目简介
RxJava 是 Reactive Extensions 的 Java 实现，是一个用于构建异步和基于事件的响应式程序的库。

## 核心类结构

### 1. Observable（可观察对象）
- **位置**: `io.reactivex.rxjava3.core.Observable`
- **职责**: 响应式流的核心类，提供创建、转换和消费数据流的方法
- **设计**: 采用流式 API 设计，支持链式调用

### 2. Observer（观察者）
- **接口**: `io.reactivex.rxjava3.core.Observer`
- **方法**: `onSubscribe()`, `onNext()`, `onError()`, `onComplete()`

### 3. Disposable（可处置对象）
- **职责**: 用于取消订阅和释放资源
- **实现类**: `DisposableObserver`, `CompositeDisposable`

### 4. Scheduler（调度器）
- **位置**: `io.reactivex.rxjava3.schedulers.Scheduler`
- **实现**:
  - `Schedulers.io()` - I/O 调度器
  - `Schedulers.computation()` - 计算调度器
  - `Schedulers.newThread()` - 新线程调度器

## 设计模式

### 1. 观察者模式（Observer）
- Observable（被观察者）和 Observer（观察者）
- 支持 pull 和 push 两种模式

### 2. 装饰器模式（Decorator）
- 各种 Operator 包装原始 Observable
- 如 `map()`, `filter()`, `flatMap()` 等

### 3. 责任链模式（Chain of Responsibility）
- `ProcessorSlotChain` 处理链
- 每个 slot 负责特定功能

### 4. 迭代器模式（Iterator）
- `Iterator` 接口用于遍历数据流

### 5. 工厂模式（Factory）
- 各种 `create()`, `just()`, `fromIterable()` 工厂方法

## 代码技巧

### 1. 延迟执行
```java
// 订阅时才执行创建逻辑
public final Disposable subscribe(Observer<? super T> observer) {
    return subscribeActual(observer);
}
```

### 2. 背压处理
- 支持多种背压策略
- `Flowable` 支持背压，`Observable` 不支持

### 3. 线程切换
```java
.observeOn(Schedulers.io())
.subscribeOn(Schedulers.computation())
```

### 4. 错误处理
```java
.onErrorResumeNext(throwable -> {
    return Observable.empty();
})
```

### 5. 资源管理
```java
using(resourceFactory, resourceObservable, resourceDisposer)
```

## 代码规范

### 1. 命名规范
- 使用有意义的类名和方法名
- 操作符使用动词或动词短语

### 2. 注释规范
- 类和公共方法有 Javadoc
- 复杂的操作符有详细的文档说明

### 3. API 设计
- 流式 API 设计
- 方法链式调用
- 重载方法丰富

## 值得学习的地方

1. **响应式编程思想**: 理解响应式流的概念和优势
2. **丰富的操作符**: 200+ 操作符，掌握常用操作符的使用
3. **线程调度**: 灵活的线程管理
4. **背压处理**: 了解背压策略和适用场景
5. **错误恢复**: 健壮的错误处理机制
6. **内存管理**: 避免内存泄漏的实践（Disposable）
