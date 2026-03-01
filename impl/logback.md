# logback

## 核心类和接口

Abbreviator
AsyncAppender
BasicConfigurator
CallerData
CallerDataConverter
ClassNameOnlyAbbreviator
ClassOfCallerConverter
ClassPackagingData
ClassicConstants
ClassicEnvUtil
ClassicVersionUtil
ConfigurationAction
ConfigurationModel
ConfigurationModelHandler
ConfigurationModelHandlerFull

## 主要设计模式

- 观察者模式: ContextDetachingSCL
- 建造者模式: UrlCssBuilder,DefaultCssBuilder
- 工厂模式: LoggerContextFilter,ContextSelector,ContextJNDISelector
- 策略模式: 未明显发现
- 装饰器模式: 未明显发现
- 单例模式: 未明显发现

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
