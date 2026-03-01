# elasticsearch-oss

## 核心类和接口

AggConstructionContentionBenchmark
AggregatorBenchmark
AllocationBenchmark
AvailableIndexFoldersBenchmark
Base64VectorBenchmark
BeatsMapperBenchmark
BlockBenchmark
BlockKeepMaskBenchmark
BlockReadBenchmark
BytesArrayIndexOfBenchmark
BytesArrayReadLongBenchmark
BytesArrayReadVLongBenchmark
BytesRefSwissHashBenchmark
CodePointCountBenchmark
CompressionMetrics

## 主要设计模式

- 观察者模式: 未明显发现
- 建造者模式: NearConstantWithOutliersSupplier,CounterWithResetsSupplier,GcdFriendlySupplier
- 工厂模式: TDigestBench,MemoryStatsBenchmark,OptimizedTextBenchmark
- 策略模式: TestShardRoutingRoleStrategies
- 装饰器模式: 未明显发现
- 单例模式: MappingParsingBenchmark,IndexNameExpressionResolverBenchmark,ScriptScoreBenchmark

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
