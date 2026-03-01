# guava

## 核心类和接口

ClassToInstanceMap<B
ComputationException
HashBasedTable<R,
ImmutableRangeMap<K
ImmutableSetMultimap<K,
Interner<E>
LinkedListMultimap<K
ListMultimap<K
Multiset<E
PeekingIterator<E
RangeMap<K
RangeSet<C
RowSortedTable<
SetMultimap<K
SortedMapDifference<K

## 主要设计模式

- 观察者模式: 未明显发现
- 建造者模式: CharEscaperBuilder,Escapers,ImmutableSortedMap
- 工厂模式: HashBasedTable,Iterables,Iterators
- 策略模式: MapMakerInternalMap
- 装饰器模式: CharEscaperBuilder,ForwardingConcurrentMap,Iterators
- 单例模式: DiscreteDomain,Cut,EmptyImmutableSetMultimap

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
