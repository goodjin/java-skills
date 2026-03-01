# zuul

## 核心类和接口

AccessLogPublisher
BaseSslContextFactory
BasicFilterUsageNotifier
BasicRequestStat
ByteBufUtil
CategorizedThreadFactory
ChannelConfig
ChannelConfigKey<T>
ChannelConfigValue<T>
ClientChannelManager
ClientConnectionsShutdown
ClientRequestReceiver
ClientResponseWriter
CloseOnIdleStateHandler
CommonChannelConfigKeys

## 主要设计模式

- 观察者模式: ProxyEndpoint
- 建造者模式: HttpRequestBuilder
- 工厂模式: PatternListStringProperty,Http1ConnectionCloseHandler,HttpMetricsChannelHandler
- 策略模式: 未明显发现
- 装饰器模式: ZuulSessionContextDecorator,SessionContextDecorator,ClientRequestReceiver
- 单例模式: RejectionUtils,HttpRequestReadTimeoutEvent,HttpRequestReadTimeoutHandler

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
