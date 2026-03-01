# HikariCP 项目分析

## 项目简介
HikariCP 是一个高性能的 JDBC 数据库连接池，由 Brett Wooldridge 开发，被认为是 Java 最快的连接池。

## 核心类结构

### 1. HikariPool（连接池核心）
- **位置**: `com.zaxxer.hikari.pool.HikariPool`
- **职责**: 管理整个连接池的生命周期
- **关键字段**:
  - `connectionBag` - 连接存储结构
  - `houseKeepingExecutorService` - 定时任务调度器
  - `addConnectionExecutor` / `closeConnectionExecutor` - 连接创建/关闭线程池

### 2. ConcurrentBag（并发连接袋）
- **位置**: `com.zaxxer.hikari.util.ConcurrentBag`
- **核心创新**: 专门为连接池优化的无锁数据结构

### 3. PoolEntry（连接条目）
- **职责**: 封装数据库连接及其元数据

### 4. ProxyFactory（代理工厂）
- **职责**: 创建 JDBC 连接代理

## 设计模式

### 1. 享元模式（Flyweight）
- `PoolEntry` 复用连接对象
- `RingBuffer` 中的事件对象复用

### 2. 策略模式（Strategy）
- 多种 `WaitStrategy` 实现（BlockingWaitStrategy, BusySpinWaitStrategy 等）
- 多种 `MetricsTrackerFactory` 实现

### 3. 工厂模式（Factory）
- `ProxyFactory` 创建连接代理
- `PoolEntryCreator` 创建连接

### 4. 单例模式
- `SuspendResumeLock.FAUX_LOCK` 静态实例

## 代码技巧

### 1. 无锁化设计
```java
// ConcurrentBag 使用 ThreadLocal 减少锁竞争
final var list = threadLocalList.get();
for (var i = list.size() - 1; i >= 0; i--) {
    final var entry = list.remove(i);
    if (bagEntry.compareAndSet(STATE_NOT_IN_USE, STATE_IN_USE)) {
        return bagEntry;
    }
}
```

### 2. CPU 缓存行对齐
```java
// RingBufferFields 使用 padding 避免伪共享
private static final int BUFFER_PAD = 32;
// 数组预分配：bufferSize + 2 * BUFFER_PAD
this.entries = (E[]) new Object[bufferSize + 2 * BUFFER_PAD];
```

### 3. 快速失败（Fail-Fast）
```java
// 启动时检查连接
checkFailFast();
```

### 4. 自定义拒绝策略
```java
// 当线程池满时自定义行为
new CustomDiscardPolicy()
```

## 代码规范

### 命名规范
- 类名清晰表达职责（如 `HikariPool`、`PoolEntryCreator`）
- 使用完整单词，避免缩写
- 常量使用全大写下划线格式

### 注释规范
- 每个类都有详细的 Javadoc 注释
- 关键算法有详细的说明
- 版权声明统一

### 代码格式
- 使用 4 空格缩进
- 大括号遵循 K&R 风格
- 行长度限制合理

## 值得学习的地方

1. **极致性能优化**: 通过无锁设计、CPU 缓存行对齐、对象复用等手段
2. **清晰的职责分离**: 各个类职责单一明确
3. **完善的监控支持**: 集成 Micrometer、Prometheus 等监控框架
4. **灵活的配置**: 支持多种配置方式和扩展点
5. **优秀的错误处理**: 详细的异常信息和完善的日志
