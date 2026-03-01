# transmittable-thread-local

## 核心类和接口

ClassInfo
PriorityBlockingQueueTtlTransformlet
TtlTransformer
TtlTransformlet
class

## 主要设计模式

- 观察者模式: 未明显发现
- 建造者模式: 未明显发现
- 工厂模式: ForkJoinTtlTransformlet,AbstractExecutorTtlTransformlet,TtlExtensionTransformletManager
- 策略模式: 未明显发现
- 装饰器模式: TtlTransformletHelper,AbstractExecutorTtlTransformlet
- 单例模式: 未明显发现

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
