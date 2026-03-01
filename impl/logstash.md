# logstash

## 核心类和接口

AddressState
AliasDocumentReplace
AliasPlugin
AliasRegistry
And
BaseSetting<T>
BasicEventFactory
BooleanGauge
BooleanSetting
BreadthFirst
BytesSetting
CATrustedFingerprintTrustStrategy
CheckedSupplier<T>
CloudSettingAuth
CloudSettingId

## 主要设计模式

- 观察者模式: CompiledPipeline
- 建造者模式: BaseSetting,DSL
- 工厂模式: FileLockFactory,Logstash,ObjectMappers
- 策略模式: CATrustedFingerprintTrustStrategy,OutputStrategyExt,OutputDelegatorExt
- 装饰器模式: Timestamp,RubyIntegration,ConfigurationImpl
- 单例模式: NullTimerMetric,TimerMetric,TimerMetricFactory

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
