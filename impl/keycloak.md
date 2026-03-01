# keycloak

## 核心类和接口

Base64
Base64Url
BouncyIntegration
CertificateUtils
CertificateUtilsProvider
ClientConnection
CollectionUtil
CommaSeparatedListProfileConfigResolver
ConcurrentMultivaluedHashMap<K,
CryptoConstants
CryptoIntegration
CryptoProvider
DelegatingSerializationFilter
DurationConverter
ECDSACryptoProvider

## 主要设计模式

- 观察者模式: 未明显发现
- 建造者模式: UserIdentityExtractorProvider,KeycloakUriBuilder,DelegatingSerializationFilter
- 工厂模式: CryptoIntegration,CryptoProvider,Reflections
- 策略模式: 未明显发现
- 装饰器模式: 未明显发现
- 单例模式: SecretGenerator

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
