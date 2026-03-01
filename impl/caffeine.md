# caffeine

## 核心类和接口

AsyncCache<K,
AsyncCacheLoader<K,
AsyncLoadingCache<K,
Cache<K,
CacheLoader<K,
Expiry<K,
Interner<E>
LoadingCache<K,
Policy<K,
RemovalListener<K,
Scheduler
StatsCounter
Ticker
Weigher<K,

## 主要设计模式

- 观察者模式: Async
- 建造者模式: LocalCacheFactory,NodeFactory
- 工厂模式: BoundedLocalCache,UnboundedLocalCache,Interner
- 策略模式: 未明显发现
- 装饰器模式: BoundedLocalCache
- 单例模式: Scheduler,Weigher,Buffer

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
