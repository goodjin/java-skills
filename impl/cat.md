# cat

## 核心类和接口

AlertConfigManager
AlertEntity
AlertManager
AlertPolicyManager
AlertService
ComponentsConfigurator
Contactor
ContactorManager
DXSpliter
DataCheckEntity
DataChecker
DecoratorManager
DefaultDataChecker
MailSender
MailSpliter

## 主要设计模式

- 观察者模式: 未明显发现
- 建造者模式: 未明显发现
- 工厂模式: RuleType
- 策略模式: 未明显发现
- 装饰器模式: ComponentsConfigurator,DecoratorManager,ServerRuleFTLDecorator
- 单例模式: 未明显发现

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
