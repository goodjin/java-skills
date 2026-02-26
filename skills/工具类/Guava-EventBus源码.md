# Guava EventBus 源码分析

## 概述

EventBus 是 Guava 提供的事件总线组件，实现了**发布-订阅**模式。它允许组件之间松耦合通信，发布者无需知道订阅者是谁。

> ⚠️ **注意**：Guava 官方已不推荐使用 EventBus，建议使用依赖注入框架 + RxJava/Coroutines 等现代方案。但作为学习经典事件总线实现，仍有参考价值。

---

## 源码结构

```
com.google.common.eventbus/
├── EventBus.java              # 同步事件总线
├── AsyncEventBus.java        # 异步事件总线
├── SubscriberRegistry.java   # 订阅者注册表
├── Dispatcher.java           # 事件分发器
├── Subscriber.java          # 订阅者封装
├── EventHandler.java        # 事件处理器
├── DeadEvent.java           # 死事件
├── Subscribe.java           # 订阅注解
├── AllowConcurrentEvents.java # 允许并发注解
└── SubscriberExceptionHandler.java # 异常处理器
```

---

## 实现原理

### 1. 核心组件

```
EventBus
    │
    ├── SubscriberRegistry    # 管理所有订阅者
    │     └── Map<Class<?>, Subscriber>
    │
    ├── Dispatcher           # 事件分发策略
    │     ├── perThreadDispatchQueue  # 每线程队列
    │     ├── legacyAsync          # 遗留异步
    │     └── immediate            # 立即分发
    │
    └── Executor             # 执行器（默认 directExecutor）
```

### 2. 订阅者注册

#### 2.1 @Subscribe 注解

订阅者使用 `@Subscribe` 注解标记方法：

```java
public class EventListener {
    @Subscribe
    public void handleStringEvent(String event) {
        System.out.println("收到事件: " + event);
    }
    
    @Subscribe
    public void handleIntegerEvent(Integer event) {
        System.out.println("收到数字: " + event);
    }
}
```

#### 2.2 订阅者注册流程

```java
// EventBus.register()
public void register(Object listener) {
    SubscriberRegistry registry = this.subscribers;
    registry.register(listener);
}

// SubscriberRegistry.register()
void register(Object listener) {
    // 1. 扫描所有 @Subscribe 方法
    Map<Class<?>, EventHandler> handlersByType = handlerMethods(listener);
    
    for (Map.Entry<Class<?>, EventHandler> entry : handlersByType.entrySet()) {
        Class<?> eventType = entry.getKey();
        EventHandler handler = entry.getValue();
        
        // 2. 按事件类型注册
        CopyOnWriteArraySet<EventHandler> handlers =
            subscribers.get(eventType);
        if (handlers == null) {
            handlers = new CopyOnWriteArraySet<>();
            subscribers.put(eventType, handlers);
        }
        handlers.add(handler);
    }
}
```

#### 2.3 反射获取方法

```java
// 使用反射扫描 @Subscribe 方法
private static Map<Class<?>, EventHandler> handlerMethods(Object listener) {
    Map<Class<?>, EventHandler> handlers = new HashMap<>();
    
    for (Method method : listener.getClass().getDeclaredMethods()) {
        // 检查 @Subscribe 注解
        Subscribe subscribe = method.getAnnotation(Subscribe.class);
        if (subscribe == null) continue;
        
        // 检查参数（必须只有一个参数）
        Class<?>[] params = method.getParameterTypes();
        if (params.length != 1) {
            throw new IllegalArgumentException(...);
        }
        
        // 创建 EventHandler
        EventHandler handler = new EventHandler(listener, method);
        handlers.put(params[0], handler);
    }
    return handlers;
}
```

### 3. 事件发布

#### 3.1 发布流程

```java
// EventBus.post()
public void post(Object event) {
    // 1. 找到所有订阅该事件类型的订阅者
    SubscriberRegistry subscribers = this.subscribers;
    Collection<EventHandler> handlers = subscribers.getHandlersForEvent(event.getClass());
    
    if (handlers != null && !handlers.isEmpty()) {
        // 2. 遍历执行
        for (EventHandler handler : handlers) {
            dispatcher.dispatch(event, handler);
        }
    } else {
        // 3. 没有订阅者，发布 DeadEvent
        post(new DeadEvent(this, event));
    }
}
```

#### 3.2 事件类型匹配

EventBus 使用**继承层次**进行匹配：

```
事件类型: String extends Object
订阅者:
    - 订阅 String → 收到 String 事件 ✅
    - 订阅 Object → 收到所有事件 ✅

发布事件时:
    post("hello")
    → 查找 String 类型的订阅者
    → 查找 Object 类型的订阅者
```

```java
// SubscriberRegistry.getHandlersForEvent()
Collection<EventHandler> getHandlersForEvent(Class<?> eventType) {
    // 查找精确匹配
    Collection<EventHandler> handlers = subscribers.get(eventType);
    
    if (handlers != null) return handlers;
    
    // 查找父类/接口的订阅者
    for (Class<?> cl = eventType; cl != null; cl = cl.getSuperclass()) {
        for (Class<?> iface : cl.getInterfaces()) {
            handlers = concatenate(handlers, subscribers.get(iface));
        }
    }
    
    return handlers;
}
```

### 4. 事件分发

#### 4.1 Dispatcher 分发策略

```java
// 三种分发策略
enum Dispatcher {
    // 每线程队列（默认）
    perThreadDispatchQueue() {
        @Override
        void dispatch(Object event, EventHandler handler) {
            queue.get().add(handler);
        }
    },
    
    // 遗留异步
    legacyAsync() {
        @Override
        void dispatch(Object event, EventHandler handler) {
            executor.execute(() -> handler.handleEvent(event));
        }
    },
    
    // 立即分发
    immediate() {
        @Override
        void dispatch(Object event, EventHandler handler) {
            handler.handleEvent(event);
        }
    }
}
```

#### 4.2 EventHandler 执行

```java
// EventHandler.handleEvent()
final void handleEvent(final Method method, final Object event) {
    // 使用反射调用订阅者方法
    try {
        method.invoke(target, event);
    } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
        // 抛出异常时，由 exceptionHandler 处理
        handlerException.handleException(e.getCause(), context);
    }
}
```

### 5. 异步 EventBus

```java
// AsyncEventBus 使用单独的线程池执行
public class AsyncEventBus extends EventBus {
    public AsyncEventBus(String identifier, Executor executor) {
        super(identifier, executor, 
              Dispatcher.legacyAsync(), 
              handler);
    }
}
```

使用示例：

```java
// 创建异步事件总线
AsyncEventBus asyncBus = new AsyncEventBus(
    Executors.newFixedThreadPool(4)
);

// 订阅者的方法会在线程池中执行
class AsyncListener {
    @Subscribe
    public void handle(String event) {
        // 在线程池线程中执行
    }
}
```

---

## 代码示例

### 1. 基础用法

#### 定义事件

```java
// 事件可以是任意对象
public class OrderEvent {
    private final String orderId;
    private final double amount;
    
    public OrderEvent(String orderId, double amount) {
        this.orderId = orderId;
        this.amount = amount;
    }
    
    // getters...
}
```

#### 定义订阅者

```java
public class OrderSubscriber {
    @Subscribe
    public void onOrderCreated(OrderEvent event) {
        System.out.println("订单创建: " + event.getOrderId());
    }
    
    @Subscribe
    public void onOrderPaid(OrderEvent event) {
        System.out.println("订单支付: " + event.getAmount());
    }
}
```

#### 发布和订阅

```java
// 创建事件总线
EventBus eventBus = new EventBus();

// 注册订阅者
eventBus.register(new OrderSubscriber());

// 发布事件
eventBus.post(new OrderEvent("ORDER-001", 99.9));
eventBus.post(new OrderEvent("ORDER-002", 149.9));

// 输出:
// 订单创建: ORDER-001
// 订单创建: ORDER-002
```

### 2. 多个订阅者

```java
// 多个订阅者可以订阅同一类型
class SubscriberA {
    @Subscribe public void handle(String msg) {
        System.out.println("A: " + msg);
    }
}

class SubscriberB {
    @Subscribe public void handle(String msg) {
        System.out.println("B: " + msg);
    }
}

eventBus.register(new SubscriberA());
eventBus.register(new SubscriberB());

eventBus.post("hello");

// 输出:
// A: hello
// B: hello
```

### 3. 事件继承

```java
// 事件继承层次
class Event {}
class MessageEvent extends Event {
    private final String message;
    public MessageEvent(String message) { this.message = message; }
}
class TextMessageEvent extends MessageEvent {
    public TextMessageEvent(String text) { super(text); }
}

// 订阅父类
class GeneralSubscriber {
    @Subscribe
    public void handleEvent(Event event) {
        System.out.println("收到事件: " + event.getClass().getSimpleName());
    }
}

eventBus.register(new GeneralSubscriber());

eventBus.post(new Event());           // ✅ 收到
eventBus.post(new MessageEvent(""));  // ✅ 收到
eventBus.post(new TextMessageEvent("")); // ✅ 收到
```

### 4. 死事件处理

```java
// 当发布没有订阅者关注的事件时，会触发 DeadEvent
class DeadEventSubscriber {
    @Subscribe
    public void handle(DeadEvent deadEvent) {
        System.out.println("死事件: " + deadEvent.getEvent());
    }
}

eventBus.register(new DeadEventSubscriber());
eventBus.post("无订阅者的事件");  // 触发 DeadEvent
```

### 5. 异步处理

```java
// 创建异步事件总线
AsyncEventBus asyncBus = new AsyncEventBus(
    Executors.newFixedThreadPool(4)
);

asyncBus.register(new AsyncListener());

// 发布事件立即返回，异步执行处理
asyncBus.post(new OrderEvent("ORDER-001", 99.9));
System.out.println("已发布");
```

### 6. 异常处理

```java
// 自定义异常处理器
class LoggingHandler implements SubscriberExceptionHandler {
    @Override
    public void handleException(Throwable exception, 
                               SubscriberExceptionContext context) {
        System.out.println("异常: " + exception.getMessage());
        System.out.println("订阅者: " + context.getSubscriberMethod());
        System.out.println("事件: " + context.getEvent());
    }
}

EventBus eventBus = new EventBus(new LoggingHandler());
```

### 7. 取消订阅

```java
OrderSubscriber subscriber = new OrderSubscriber();
eventBus.register(subscriber);

// 取消订阅
eventBus.unregister(subscriber);
```

---

## 与 JDK 对比

### 传统 Observer 模式

```java
// JDK Observer 模式
class OrderObservable extends Observable {
    void notifyObservers() {
        setChanged();
        super.notifyObservers();
    }
}

class OrderObserver implements Observer {
    @Override
    public void update(Observable o, Object arg) {
        // 处理事件
    }
}

// 使用
OrderObservable observable = new OrderObservable();
observable.addObserver(new OrderObserver());
observable.notifyObservers();
```

### Guava EventBus

| 特性 | EventBus | JDK Observer |
|------|----------|-------------|
| 注解驱动 | ✅ | ❌ |
| 类型安全 | ✅ | ❌（Object 类型） |
| 继承支持 | ✅ | ❌ |
| 异步支持 | ✅ | ❌ |
| 死事件 | ✅ | ❌ |
| 异常处理 | ✅ | ❌ |
| 依赖 | 需要 Guava | JDK 内置 |

---

## 最佳实践与注意事项

### 1. 为什么官方不推荐使用

官方指出的 EventBus 缺点：
- 订阅者和发布者耦合，难以追踪
- 使用反射，代码混淆后可能失效
- 不支持等待多个事件
- 不支持背压
- 不支持线程控制
- 不与 RxJava/Coroutines 互操作

### 2. 更好的替代方案

```java
// 使用 RxJava
class RxBus {
    private final Subject<Object, Object> subject = 
        PublishSubject.create();
    
    public void post(Object event) {
        subject.onNext(event);
    }
    
    public <T> Observable<T> observe(Class<T> eventType) {
        return subject.ofType(eventType);
    }
}

// 使用 Kotlin Coroutines Channel
class CoroutineBus {
    private val channel = Channel<Any>(Channel.BUFFERED)
    
    suspend fun post(event: Any) {
        channel.send(event)
    }
    
    fun <T> subscribe(): ReceiveChannel<T> {
        return channel.receiveAsFlow()
            .filterIsInstance<T>()
            .consumedAsFlow()
    }
}
```

### 3. 如果仍要使用

```java
// 1. 避免事件滥用
// 2. 使用明确的事件类型
// 3. 处理好异常
// 4. 考虑使用 @AllowConcurrentEvents 提升性能
class ConcurrentListener {
    @AllowConcurrentEvents
    @Subscribe
    public void handleConcurrently(String event) {
        // 可以并发执行
    }
}

// 5. 使用常量定义事件类型
public class Events {
    public static final Class<OrderCreatedEvent> ORDER_CREATED = 
        OrderCreatedEvent.class;
    public static final Class<OrderPaidEvent> ORDER_PAID = 
        OrderPaidEvent.class;
}
```

---

## 总结

EventBus 是一个经典的发布-订阅实现，核心原理：

1. **反射扫描** `@Subscribe` 方法
2. **按类型存储** 订阅者到 `Map<Class, Set<Subscriber>>`
3. **继承匹配** 找到所有合适的订阅者
4. **分发执行** 调用订阅者方法

虽然官方已不推荐，但理解其实现对学习事件驱动架构很有帮助。现代项目推荐使用 RxJava、Coroutines 或依赖注入框架。
