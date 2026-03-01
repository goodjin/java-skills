# jedis

## 核心类和接口

AccessControlLogEntry
AccessControlUser
Apply
BFInsertParams
BFReserveParams
BloomFilterCommands
BloomFilterPipelineCommands
CFInsertParams
CFReserveParams
ClusterCommandArguments
ClusterCommandObjects
ClusterConnectionProvider
ClusterPipeline
ClusterShardInfo
ClusterShardNodeInfo

## 主要设计模式

- 观察者模式: 未明显发现
- 建造者模式: TimeSeriesBuilderFactory,Builder,AbstractClientBuilder
- 工厂模式: PipeliningBase,MultiNodePipelineBase,LatencyHistoryInfo
- 策略模式: MultiDbConfig
- 装饰器模式: JedisByteMap,JedisByteHashMap
- 单例模式: 未明显发现

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
