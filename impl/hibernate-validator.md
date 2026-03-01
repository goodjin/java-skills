# hibernate-validator

## 核心类和接口

AbstractConstraintCheck
AbstractElementVisitor<T,
AnnotationApiHelper
AnnotationDefaultMessageCheck
AnnotationParametersDecimalMinMaxCheck
AnnotationParametersDigitsCheck
AnnotationParametersGroupsCheck
AnnotationParametersPatternCheck
AnnotationParametersScriptAssertCheck
AnnotationParametersSizeLengthCheck
AnnotationPayloadUnwrappingCheck
AnnotationTypeCheck
AnnotationTypeMemberCheck
AnnotationUserMessageCheck
ClassCheck

## 主要设计模式

- 观察者模式: 未明显发现
- 建造者模式: AbstractMethodOverrideCheck,MethodInheritanceTree
- 工厂模式: SingleValuedChecks,ConstraintCheckFactory,package-info
- 策略模式: 未明显发现
- 装饰器模式: MessagerAdapter
- 单例模式: 未明显发现

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
