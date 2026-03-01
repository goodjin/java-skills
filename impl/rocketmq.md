# rocketmq

## 核心类和接口

AccessResource
Acl
AclAuthorizationHandler
AclConfig
AuthConfig
AuthMigrator
AuthenticationContextBuilder<AuthenticationContext>
AuthenticationEvaluator
AuthenticationException
AuthenticationFactory
AuthenticationMetadataManager
AuthenticationMetadataManagerImpl
AuthenticationMetadataProvider
AuthenticationProvider<AuthenticationContext>
AuthenticationStrategy

## 主要设计模式

- 观察者模式: 未明显发现
- 建造者模式: DefaultAuthorizationContextBuilder,DefaultAuthenticationContextBuilder
- 工厂模式: AuthorizationEvaluator,DefaultAuthorizationProvider,UserAuthorizationHandler
- 策略模式: AuthConfig,AuthorizationEvaluator,AuthorizationFactory
- 装饰器模式: 未明显发现
- 单例模式: AuthorizationFactory,AuthenticationFactory

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
