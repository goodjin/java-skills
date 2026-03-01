# async-http-client

## 核心类和接口

AsyncCompletionHandlerBase
AsyncHandler<T>
AsyncHttpClient
AsyncHttpClientConfig
AsyncHttpClientState
Body
BodyChunkedInput
BodyDeferringAsyncHandler
BodyGenerator
BoundRequestBuilder
ByteArrayMultipartPart
ByteArrayPart
ChannelManager
ChannelPool
ChannelPoolPartitioning

## 主要设计模式

- 观察者模式: ResumableRandomAccessFileListener
- 建造者模式: RequestBuilderBase,ProxyServer,Response
- 工厂模式: PropertiesBasedResumableProcessor,ResumableAsyncHandler,TransferCompletionHandler
- 策略模式: HttpHandler,DefaultChannelPool,DefaultKeepAliveStrategy
- 装饰器模式: NettyResponse,ReleasePermitOnComplete
- 单例模式: RequestBuilderBase,AuthenticatorUtils,NtlmEngine

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
