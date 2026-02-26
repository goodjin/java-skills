# Guava 并发工具源码分析

## 概述

Guava 提供了丰富的并发工具，主要包括：
- **ListenableFuture**：可监听 Future
- **RateLimiter**：令牌桶限流器

这些工具极大地简化了 Java 并发编程。

---

## 一、ListenableFuture 可监听 Future

### 1.1 概述

`ListenableFuture` 是对 JDK `Future` 的扩展，允许注册回调监听器，在 Future 完成时自动执行回调。

### 1.2 源码结构

```
com.google.common.util.concurrent/
├── ListenableFuture.java        # 接口定义
├── AbstractFuture.java          # 抽象实现
├── SettableFuture.java          # 可设置的 Future
├── Futures.java                 # 工具类（transform, catching 等）
├── ExecutionList.java           # 执行列表
├── MoreExecutors.java           # 执行器工具
└── ...
```

### 1.3 接口定义

```java
public interface ListenableFuture<V> extends Future<V> {
    /**
     * 注册监听器，在 Future 完成时执行
     */
    void addListener(Runnable listener, Executor executor);
}
```

相比 JDK `Future`：
- JDK 只能通过 `get()` 阻塞等待结果
- ListenableFuture 支持回调，避免阻塞

### 1.4 实现原理

#### 1.4.1 ExecutionList - 监听器管理

`ExecutionList` 管理所有注册的监听器：

```java
// 简化版实现
public class ExecutionList implements Runnable {
    private RunnableExecutorPair list;
    
    public void add(Runnable listener, Executor executor) {
        // 添加到链表头部
        list = new RunnableExecutorPair(listener, executor, list);
    }
    
    @Override
    public void run() {
        // 按顺序执行所有监听器
        RunnableExecutorPair current = list;
        while (current != null) {
            current.executor.execute(current.runnable);
            current = current.next;
        }
    }
    
    // 内部类
    private static class RunnableExecutorPair {
        final Runnable runnable;
        final Executor executor;
        RunnableExecutorPair next;
        // ...
    }
}
```

#### 1.4.2 AbstractFuture 实现

```java
public abstract class AbstractFuture<V> implements ListenableFuture<V> {
    // 使用 AtomicReference 存储结果或异常
    private final AtomicReference<Waiter> waiters = new AtomicReference<>();
    private final ExecutionList executionList = new ExecutionList();
    
    @Override
    public void addListener(Runnable listener, Executor executor) {
        // 如果已完成，立即执行
        if (isDone()) {
            executeListener(listener, executor);
            return;
        }
        // 否则添加到 ExecutionList
        executionList.add(listener, executor);
    }
    
    protected boolean set(@Nullable V value) {
        // 设置结果并触发所有监听器
        if (ATOMIC_HELPER.casValue(this, null, value)) {
            completionLatch.countDown();
            executionList.execute();
            return true;
        }
        return false;
    }
    
    protected boolean setException(Throwable throwable) {
        // 设置异常并触发所有监听器
        if (ATOMIC_HELPER.casThrowable(this, null, throwable)) {
            executionList.execute();
            return true;
        }
        return false;
    }
}
```

#### 1.4.3 Future 转换链

`Futures` 类提供了丰富的转换方法：

```java
// 同步转换
ListenableFuture<Result> future = Futures.transform(
    originalFuture,
    input -> transform(input),
    executor
);

// 异步转换
ListenableFuture<Result> future = Futures.transformAsync(
    originalFuture,
    input -> asyncTransform(input),  // 返回 ListenableFuture
    executor
);

// 错误处理
ListenableFuture<Result> future = Futures.catching(
    originalFuture,
    IOException.class,
    e -> handleError(e),
    executor
);
```

### 1.5 代码示例

#### 基本用法

```java
// 创建 ListenableFuture
ListeningExecutorService executor = MoreExecutors.listeningDecorator(
    Executors.newFixedThreadPool(10)
);

ListenableFuture<String> future = executor.submit(() -> {
    // 模拟耗时操作
    return fetchData();
});

// 方式1: 使用 addListener
future.addListener(() -> {
    try {
        String result = future.get();
        System.out.println("结果: " + result);
    } catch (Exception e) {
        e.printStackTrace();
    }
}, MoreExecutors.directExecutor());

// 方式2: 使用 Futures.addCallback（推荐）
Futures.addCallback(future, new FutureCallback<String>() {
    @Override
    public void onSuccess(@Nullable String result) {
        System.out.println("成功: " + result);
    }
    
    @Override
    public void onFailure(Throwable t) {
        System.out.println("失败: " + t.getMessage());
    }
}, executor);
```

#### 链式操作

```java
ListenableFuture<User> userFuture = executor.submit(() -> getUser(id));
ListenableFuture<Order> orderFuture = Futures.transform(
    userFuture,
    user -> getUserOrders(user.getUserId()),
    executor
);
ListenableFuture<Double> totalFuture = Futures.transform(
    orderFuture,
    orders -> orders.stream().mapToDouble(Order::getAmount).sum(),
    executor
);

// 组合多个 Future
ListenableFuture<List<String>> combinedFuture = Futures.allAsList(
    future1, future2, future3
);
```

#### SettableFuture

```java
// 用于手动控制 Future 的完成
SettableFuture<String> future = SettableFuture.create();

// 模拟异步操作
asyncOperation(new Callback() {
    @Override
    public void onSuccess(String result) {
        future.set(result);  // 设置成功结果
    }
    
    @Override
    public void onError(Exception e) {
        future.setException(e);  // 设置异常
    }
});

return future;
```

### 1.6 与 JDK 对比

```java
// JDK Future - 只能阻塞等待
Future<String> jdkFuture = executor.submit(() -> "result");
try {
    String result = jdkFuture.get();  // 阻塞！
} catch (ExecutionException e) {
    e.printStackTrace();
}

// Guava ListenableFuture - 支持回调
ListenableFuture<String> guavaFuture = executor.submit(() -> "result");
guavaFuture.addListener(() -> {
    try {
        String result = guavaFuture.get();  // 不阻塞！
        System.out.println(result);
    } catch (Exception e) {
        e.printStackTrace();
    }
}, executor);
```

| 特性 | ListenableFuture | JDK Future |
|------|-----------------|------------|
| 回调支持 | ✅ | ❌ |
| 链式转换 | ✅ | ❌ |
| 错误恢复 | ✅ | ❌ |
| 组合多个 | ✅ | ✅（有限） |

---

## 二、RateLimiter 令牌桶限流器

### 2.1 概述

`RateLimiter` 是基于令牌桶算法的限流器，特点是：
- **平滑限流**：不是一把一把地发放令牌，而是平滑地发放
- **突发处理**：允许一定程度的突发流量
- **预热支持**：支持预热阶段，限制冷启动时的流量

### 2.2 源码结构

```
com.google.common.util.concurrent/
├── RateLimiter.java             # 抽象基类
├── SmoothRateLimiter.java       # 平滑限流实现
│     ├── SmoothBursty          # 突发平滑（默认）
│     └── SmoothWarmingUp       # 预热平滑
└── SleepingStopwatch.java       # 计时器（用于测试）
```

### 2.3 算法原理

#### 2.3.1 令牌桶算法

```
         请求到达
             │
             ▼
       ┌──────────┐
       │ 有令牌？  │
       └────┬─────┘
          │ 是    │ 否
          ▼       ▼
      通过请求   等待/拒绝
```

#### 2.3.2 Guava 的平滑实现

Guava 不只是简单的令牌桶，还实现了**平滑突发**（SmoothBursty）：

```java
// RateLimiter 核心字段
abstract class RateLimiter {
    // 当前存储的令牌数
    double storedPermits;
    
    // 最大存储令牌数
    double maxStoredPermits;
    
    // 添加一个令牌所需的时间（微秒）
    double stableIntervalMicros;
    
    // 下次可获取令牌的时间
    long nextFreeTicketMicros;
}
```

#### 2.3.3 获取令牌流程

```java
public double acquire() {
    return acquire(1);
}

public double acquire(int permits) {
    // 1. 等待直到获取令牌
    long waitTime = reserve(permits);
    sleepMicros(waitTime);
    return convertMicrosToSeconds(waitTime);
}

final long reserve(int permits) {
    // 检查并更新令牌数
    return reserveEarliestAvailable(permits, nowMicros());
}

abstract long reserveEarliestAvailable(
    int permits, long nowMicros) {
    // 由子类实现
}
```

#### 2.3.4 SmoothBursty 实现

```java
// SmoothBursty - 默认实现，允许突发
class SmoothBursty extends SmoothRateLimiter {
    @Override
    long reserveEarliestAvailable(int permits, long nowMicros) {
        // 1. 重新计算存储的令牌数
        resync(nowMicros);
        
        // 2. 从存储令牌中获取
        double storedToUse = min(storedPermits, permits);
        long waitMicros = storedPermitsToWaitTime(
            storedToUse, 
            permits - storedToUse
        );
        
        // 3. 更新状态
        this.nextFreeTicketMicros = nowMicros + waitMicros;
        
        return waitMicros;
    }
    
    void resync(long nowMicros) {
        if (nowMicros > nextFreeTicketMicros) {
            // 计算新增的令牌数
            double newPermits = (nowMicros - nextFreeTicketMicros) 
                / stableIntervalMicros;
            storedPermits = min(maxStoredPermits, 
                storedPermits + newPermits);
            nextFreeTicketMicros = nowMicros;
        }
    }
}
```

#### 2.3.5 预热机制 - SmoothWarmingUp

```java
// SmoothWarmingUp - 带预热的限流
class SmoothWarmingUp extends SmoothRateLimiter {
    private final long warmupPeriodMicros;
    private final double coldFactor;
    
    @Override
    long reserveEarliestAvailable(int permits, long nowMicros) {
        resync(nowMicros);
        
        // 计算等待时间
        long waitMicros = storedPermitsToWaitTime(
            storedPermits, 
            permits
        );
        
        this.nextFreeTicketMicros = nowMicros + waitMicros;
        return waitMicros;
    }
    
    // 关键：预热期间的令牌获取时间更长
    long storedPermitsToWaitTime(double storedPermits, 
                                  double permitsToTake) {
        // 在阈值以下：稳定速率
        // 在阈值以上：逐渐增加延迟（预热中）
    }
}
```

### 2.4 代码示例

#### 基础用法

```java
// 创建限流器：每秒 2 个令牌
RateLimiter limiter = RateLimiter.create(2.0);

for (int i = 0; i < 10; i++) {
    double waitTime = limiter.acquire();
    System.out.printf("请求 %d: 等待 %.3f 秒%n", i, waitTime);
}

// 输出（平滑分配）：
// 请求 0: 等待 0.000 秒
// 请求 1: 等待 0.500 秒
// 请求 2: 等待 0.500 秒
// 请求 3: 等待 0.500 秒
// ...
```

#### 突发处理

```java
RateLimiter limiter = RateLimiter.create(2.0);

// 空闲一段时间后，突发获取
Thread.sleep(2000);  // 空闲 2 秒

limiter.acquire(5);   // 突发获取 5 个令牌 - 几乎不等待！
// 因为积累了存储令牌

limiter.acquire();    // 这个需要等待，因为刚才用完了
```

#### 预热限流

```java
// 每秒 1 个令牌，预热 3 秒
RateLimiter warmingUp = RateLimiter.create(
    1.0, 
    Duration.ofSeconds(3)
);

// 前几个请求会比较慢（预热中）
for (int i = 0; i < 5; i++) {
    double waitTime = warmingUp.acquire();
    System.out.printf("请求 %d: 等待 %.3f 秒%n", i, waitTime);
}

// 预热期间输出：
// 请求 0: 等待 0.000 秒
// 请求 1: 等待 1.000 秒  （慢）
// 请求 2: 等待 1.500 秒  （更慢）
// 请求 3: 等待 2.000 秒  （接近稳定）
// 请求 4: 等待 1.000 秒  （稳定）
```

#### 限制速率发送请求

```java
RateLimiter limiter = RateLimiter.create(100.0); // 100 QPS

void sendRequests(List<Request> requests) {
    for (Request request : requests) {
        limiter.acquire();  // 限速
        sendAsync(request); // 发送请求
    }
}
```

#### 尝试获取（带超时）

```java
RateLimiter limiter = RateLimiter.create(10.0);

if (limiter.tryAcquire()) {
    // 获取到令牌，立即处理
    process();
} else {
    // 无法获取，跳过或排队
    skip();
}

// 带超时
if (limiter.tryAcquire(100, TimeUnit.MILLISECONDS)) {
    process();
} else {
    // 超时
}
```

#### 按权重限流

```java
RateLimiter limiter = RateLimiter.create(1000.0); // 1000 字节/秒

void sendPacket(byte[] packet) {
    limiter.acquire(packet.length);  // 按字节数获取令牌
    network.send(packet);
}
```

### 2.5 与 JDK 对比

| 特性 | RateLimiter | Semaphore | DelayQueue |
|------|-------------|-----------|------------|
| 限流方式 | 按速率 | 按并发数 | 按延迟 |
| 突发处理 | ✅ 平滑突发 | ❌ | ❌ |
| 预热支持 | ✅ | ❌ | ❌ |
| 平滑限流 | ✅ | ❌ | ✅ |
| 实现算法 | 令牌桶 | 信号量 | 优先级队列 |

```java
// Semaphore 实现简单限流
Semaphore semaphore = new Semaphore(10);
semaphore.acquire();  // 获取一个许可
try {
    process();
} finally {
    semaphore.release();
}

// RateLimiter 实现平滑限流
RateLimiter limiter = RateLimiter.create(10);
limiter.acquire();
process();
```

### 2.6 核心对比表

| 指标 | ListenableFuture | RateLimiter |
|------|------------------|-------------|
| 作用 | 异步编程 | 流量控制 |
| 核心优势 | 回调+链式 | 平滑限流+突发 |
| 适用场景 | 并发任务编排 | API 限流、并发控制 |
| 线程安全 | ✅ | ✅ |

---

## 总结

Guava 并发工具提供了：

1. **ListenableFuture**：解决 JDK Future 只能阻塞的问题，支持回调、链式转换
2. **RateLimiter**：基于令牌桶的平滑限流器，支持突发和预热

这些工具是构建高性能 Java 应用的重要组成部分，建议在现代项目中使用。
