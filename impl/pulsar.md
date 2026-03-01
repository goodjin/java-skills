# pulsar

## 核心类和接口

AnnotationListener
ExtendedNettyLeakDetector<T>
FailFastNotifier
FastThreadLocalCleanupListener
HeapDumpUtil
HeapHistogramUtil
JacocoDumpListener
ManualTestUtil
MockitoCleanupListener
PulsarTestListener
RetryAnalyzer
SingletonCleanerListener
ThreadDumpUtil
ThreadLeakDetectorListener
TraceTestResourceCleanupListener

## 主要设计模式

- 观察者模式: TraceTestResourceCleanupListener
- 建造者模式: 未明显发现
- 工厂模式: MockitoThreadLocalStateCleaner,TestRetrySupport,ExtendedNettyLeakDetector
- 策略模式: ThreadLeakDetectorListener
- 装饰器模式: 未明显发现
- 单例模式: MockitoThreadLocalStateCleaner,FailFastNotifier,FastThreadLocalStateCleaner

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
