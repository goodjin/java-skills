# mockito

## 核心类和接口

AbstractAwareMethod
AllInvocationsFinder
AndroidPlatform
AnnotationEngine
ArgumentCaptor<T>
ArgumentMatcher<T>
ArgumentMatcherAction
ArgumentMatcherStorage
ArgumentMatcherStorageImpl
BeanPropertySetter
CaptorAnnotationProcessor
ClassPathLoader
ConsoleMockitoLogger
ConstructorInjection
DefaultDoNotMockEnforcer

## 主要设计模式

- 观察者模式: DefaultMockitoSession
- 建造者模式: 未明显发现
- 工厂模式: Invocation,InvocationFactory,MockSettings
- 策略模式: InvocationMatcher,ArgumentMatcherAction,MatcherApplicationStrategy
- 装饰器模式: HashCodeAndEqualsMockWrapper,HashCodeAndEqualsSafeSet,Primitives
- 单例模式: MockType,RealMethod,SpyAnnotationEngine

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
