# mybatis-3

## 核心类和接口

AnnotationConstants
ArrayTypeHandler
BigDecimalTypeHandler
BigIntegerTypeHandler
BindingException
BlobByteObjectArrayTypeHandler
BlobTypeHandler
BlockingCache
BoundSql
ByteTypeHandler
Cache
CacheBuilder
CacheException
CacheKey
ChooseSqlNode

## 主要设计模式

- 观察者模式: 未明显发现
- 建造者模式: ResultMap,Environment,ResultMapping
- 工厂模式: ResultMap,Environment,CacheBuilder
- 策略模式: AbstractSQL
- 装饰器模式: CacheBuilder,PooledDataSource,UnpooledDataSource
- 单例模式: MappedStatement,DefaultParameterHandler,ExpressionEvaluator

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
