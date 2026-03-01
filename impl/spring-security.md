# spring-security

## 核心类和接口

AccessDecisionManager
AccessDecisionVoter<S>
AclEntryAfterInvocationCollectionFilteringProvider
AclEntryAfterInvocationProvider
AclEntryVoter
AffirmativeBased
AfterInvocationManager
AfterInvocationProvider
AfterInvocationProviderManager
AnnotationMetadataExtractor<A
AspectJCallback
AuthenticatedVoter
AuthenticationCredentialsNotFoundEvent
AuthorizationFailureEvent
AuthorizedEvent

## 主要设计模式

- 观察者模式: LoggerListener
- 建造者模式: 未明显发现
- 工厂模式: AbstractAccessDecisionManager,PrePostInvocationAttributeFactory,PreInvocationAuthorizationAdviceVoter
- 策略模式: AbstractSecurityInterceptor,AnnotationMetadataExtractor,AbstractRetryEntryPoint
- 装饰器模式: 未明显发现
- 单例模式: 未明显发现

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
