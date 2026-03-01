# hystrix

## 核心类和接口

CachedValuesHistogram
CollapserTimer
CumulativeCollapserEventCounterStream
CumulativeCommandEventCounterStream
CumulativeThreadPoolEventCounterStream
ExceptionNotWrappedByHystrix
ExceptionThreadingUtility
Exceptions
ExecutionResult
HealthCountsStream
Hystrix
HystrixBadRequestException
HystrixCachedObservable<R>
HystrixCircuitBreaker
HystrixCollapserBridge<BatchReturnType,

## 主要设计模式

- 观察者模式: RequestCollapser
- 建造者模式: HystrixPropertiesChainedProperty
- 工厂模式: HystrixCommandGroupKey,HystrixCollapser,HystrixThreadPoolProperties
- 策略模式: HystrixCollapser,HystrixThreadPoolProperties,HystrixTimer
- 装饰器模式: HystrixCollapser,AbstractCommand,HystrixObservableCollapser
- 单例模式: HystrixThreadPoolProperties,HystrixTimer,PlatformSpecific

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
