# shenyu

## 核心类和接口

APDiscoveryProcessor
AiProxyApiKeyController
AiProxyApiKeyMapper
AiProxySelectorResolverInvalidator
AlertReceiverController
AlertReceiverMapper
AlertReportController
ApiController
ApiMapper
ApiRuleRelationMapper
AppAuthController
AppAuthMapper
ApplicationStartListener
AuthParamMapper
AuthPathMapper

## 主要设计模式

- 观察者模式: RecordLogDataChangedAdapterListener
- 建造者模式: 未明显发现
- 工厂模式: DataChangedEventDispatcher,AiProxySelectorResolverInvalidator,WebsocketCollector
- 策略模式: 未明显发现
- 装饰器模式: 未明显发现
- 单例模式: WebsocketCollector,HttpLongPollingDataChangedListener,ClusterSelectMasterServiceJdbcImpl

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
