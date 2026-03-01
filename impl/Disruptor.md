# Disruptor 项目分析

## 项目简介
Disruptor 是 LMAX 开发的高性能并发队列，被认为是 Java 中最快的消息/事件传递组件。

## 核心类结构

### 1. RingBuffer（环形缓冲区）
- **位置**: `com.lmax.disruptor.RingBuffer`
- **职责**: 存储事件的核心数据结构
- **特点**: 预分配内存、无 GC、数组索引

### 2. Sequencer（序列器）
- **接口**: `com.lmax.disruptor.Sequencer`
- **实现**: `SingleProducerSequencer`, `MultiProducerSequencer`
- **职责**: 管理生产者和消费者的位置

### 3. Sequence（序列）
- **位置**: `com.lmax.disruptor.Sequence`
- **职责**: 跟踪消费者/生产者的当前位置

### 4. EventProcessor（事件处理器）
- **接口**: `com.lmax.disruptor.EventProcessor`
- **实现**: `BatchEventProcessor`

### 5. WaitStrategy（等待策略）
- **接口**: `com.lmax.disruptor.WaitStrategy`
- **实现**: 
  - `BlockingWaitStrategy`
  - `BusySpinWaitStrategy`
  - `YieldingWaitStrategy`
  - `PhasedBackoffWaitStrategy`

### 6. EventHandler（事件处理器）
- **接口**: `com.lmax.disruptor.EventHandler`
- **职责**: 处理事件

## 设计模式

### 1. 环形缓冲区模式
- 预分配内存
- 避免 GC
- 数组索引高效

### 2. 策略模式
- 多种 `WaitStrategy` 实现

### 3. 观察者模式
- `EventHandler` 监听事件

### 4. 批处理模式
- `BatchEventProcessor` 批量处理

## 代码技巧

### 1. CPU 缓存行对齐
```java
// 避免伪共享
abstract class RingBufferPad {
    protected byte p10, p11, p12... // padding
}
// 数组前后都有 padding
this.entries = (E[]) new Object[bufferSize + 2 * BUFFER_PAD];
```

### 2. 内存预分配
```java
// 事件对象预先创建
private void fill(final EventFactory<E> eventFactory) {
    for (int i = 0; i < bufferSize; i++) {
        entries[BUFFER_PAD + i] = eventFactory.newInstance();
    }
}
```

### 3. 数组索引掩码
```java
// 快速取模（2的幂次方）
this.indexMask = bufferSize - 1;
// 使用位运算代替取模
return entries[BUFFER_PAD + (int) (sequence & indexMask)];
```

### 4. 偏向锁优化
- SingleProducerSequencer 优化单生产者场景

### 5. 消费者协调
```java
// 依赖关系处理
SequenceBarrier barrier = ringBuffer.newBarrier(sequences);
```

## 代码规范

### 1. 清晰的类层次
- `RingBufferPad` -> `RingBufferFields` -> `RingBuffer`

### 2. 详细的文档
- 类和公共方法有 Javadoc
- 包含使用示例

### 3. 性能优先
- 代码简洁高效
- 减少对象创建

## 值得学习的地方

1. **高性能队列设计**: 理解环形缓冲区和 CAS
2. **CPU 缓存优化**: 缓存行对齐避免伪共享
3. **内存预分配**: 避免 GC 带来的延迟
4. **等待策略**: 根据场景选择合适的策略
5. **批处理**: 提高吞吐量
6. **无锁设计**: 使用 CAS 实现无锁并发
7. **数组索引**: 位运算优化
