# Redisson 分析

## 核心特性

### 分布式集合
```java
RMap<String, User> map = redisson.getMap("myMap");
RList<String> list = redisson.getList("myList");
RSet<String> set = redisson.getSet("mySet");
```

### 分布式锁
```java
RLock lock = redisson.getLock("myLock");
lock.lock();
try {
    // 业务逻辑
} finally {
    lock.unlock();
}

// 公平锁
RFairLock fairLock = redisson.getFairLock("myLock");
```

### 分布式对象
```java
RAtomicLong atomic = redisson.getAtomicLong("myAtomic");
RCountDownLatch latch = redisson.getCountDownLatch("myLatch");
RSemaphore semaphore = redisson.getSemaphore("mySemaphore");
```

### 延迟队列
```java
RDelayedQueue<String> delayedQueue = redisson.getDelayedQueue(queue);
delayedQueue.offer("msg", 10, TimeUnit.SECONDS);
```

## 对比 Jedis/Lettuce

| 特性 | Redisson | Jedis | Lettuce |
|------|----------|-------|---------|
| API | 面向对象 | 命令式 | 反应式 |
| 分布式数据结构 | 完整支持 | 无 | 无 |
| 分布式锁 | 原生支持 | 需自行实现 | 需自行实现 |
| 性能 | 中 | 高 | 高 |

## 最佳实践

```java
Config config = new Config();
config.useSingleServer()
    .setAddress("redis://127.0.0.1:6379")
    .setPassword("password");

RedissonClient redisson = Redisson.create(config);

// 分布式锁
RLock lock = redisson.getLock("order:123");
boolean acquired = lock.tryLock(10, 30, TimeUnit.SECONDS);
```
