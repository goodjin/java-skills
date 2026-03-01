# jackson-core

## 核心类和接口

ArrayTreeNode
BufferRecycler
BufferRecyclers
ByteArrayFeeder
ByteBufferFeeder
ByteQuadsCanonicalizer
ContentReference
DataOutputAsStream
DefaultIndenter
DefaultPrettyPrinter
DupDetector
ErrorReportConfiguration
FilteringGeneratorDelegate
FilteringParserDelegate
FormatFeature

## 主要设计模式

- 观察者模式: 未明显发现
- 建造者模式: StreamWriteConstraints,ByteArrayBuilder,StreamReadConstraints
- 工厂模式: module-info,SimpleNameMatcher,ByteQuadsCanonicalizer
- 策略模式: TokenFilter
- 装饰器模式: JsonGeneratorDecorator,InputDecorator,OutputDecorator
- 单例模式: DefaultIndenter,DefaultPrettyPrinter

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
