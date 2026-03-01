# dubbo-2

## 核心类和接口

AbsentConfigurator
AbsentConfiguratorFactory
AdaptiveLoadBalance
AddressListener
AddressMatch
AffinityProviderAppStateRouter<T>
AffinityProviderAppStateRouterFactory
AffinityRouterRule
AffinityRuleParser
AffinityServiceStateRouter<T>
AffinityServiceStateRouterFactory
AffinityStateRouter<T>
AffinityStateRouterFactory
AppScriptRouterFactory
AppScriptStateRouter<T>

## 主要设计模式

- 观察者模式: ScopeClusterInvoker
- 建造者模式: DefaultFilterChainBuilder
- 工厂模式: AdaptiveLoadBalance,ShortestResponseLoadBalance,MergeableClusterScopeModelInitializer
- 策略模式: 未明显发现
- 装饰器模式: MockClusterWrapper,ScopeClusterWrapper,ProtocolFilterWrapper
- 单例模式: ArrayMerger,MergerFactory,TailStateRouter

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
