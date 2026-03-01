# powermock

## 核心类和接口

AbstractPowerMockTestListenerBase
AnnotationEnablerListener
ArrayMerger
ArrayMergerImpl
ArrayUtil
Asserts
ClassFinalModifierMockTransformer
ClassLocator
ClassMarker
ClassPathAdjuster
ClassReplicaCreator
ClassWrapper<T>
ClassWrapperFactory<T>
ClassloaderWrapper
ConcreteClassGenerator

## 主要设计模式

- 观察者模式: AbstractPowerMockTestListenerBase
- 建造者模式: ConfigurationBuilder,MockClassLoaderBuilder,TestClassAwareTransformer
- 工厂模式: ConfigurationFactory,GlobalConfiguration,ConfigurationBuilder
- 策略模式: ConstructorsMockTransformer,MethodSizeMockTransformer,ClassFinalModifierMockTransformer
- 装饰器模式: JavassistMockClassLoader,JavaAssistClassMarkerFactory,ClassMarker
- 单例模式: ClassReplicaCreator,MockRepository,PowerMockTestNotifierImpl

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
