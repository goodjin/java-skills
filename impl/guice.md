# guice

## 核心类和接口

AnnotatedBindingBuilder<T>
AnnotatedConstantBindingBuilder
AnnotatedElementBuilder
Annotations
Binder
BindingBuilder<T>
ConstantBindingBuilder
CreationException
Element
ElementVisitor<V>
ErrorsException
ExposureBuilder<T>
Injector
KotlinSupportInterface
LinkedBindingBuilder<T>

## 主要设计模式

- 观察者模式: InternalProviderInstanceBindingImpl
- 建造者模式: Modules,Binder,InjectorShell
- 工厂模式: BindingProcessor,LinkedBindingImpl,InternalFactory

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
