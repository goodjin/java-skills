# lettuce-core

## 核心类和接口

AskRedirectionEvent
AsyncExecutions<T>
AsyncNodeSelection<K,
BaseNodeSelectionAsyncCommands<K,
BaseNodeSelectionCommands<K,
Batcher
BitFieldArgs
BraveTracing
ClusterClientOptions
ClusterPartitionParser
ClusterPushHandler
ClusterReadOnlyCommands
ClusterSlotRange
ClusterSlotsParser
ClusterTopologyChangedEvent

## 主要设计模式

- 观察者模式: ReconnectEventListener
- 建造者模式: GeoRadiusStoreArgs,ZAddArgs,DefaultCommandLatencyCollectorOptions
- 工厂模式: DefaultCommandLatencyCollector,MicrometerTracing,NodeConnectionFactory
- 策略模式: ExecutableCommandLookupStrategy,RedisCodecResolver
- 装饰器模式: DefaultCommandLatencyCollector,ClusterCommand
- 单例模式: DefaultCommandLatencyCollector,Tracing,BraveTracing

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
