# druid

## 核心类和接口

BalancedClickhouseDriver
BalancedClickhouseDriverNative
Base64
CKWallProvider
CalciteMySqlNodeVisitor
CalciteSqlBasicCall
CallableStatementProxy
CallableStatementProxyImpl
ClickhouseWallVisitor
ClobProxy
ClobProxyImpl
ConnectionProxy
ConnectionProxyImpl
Consumer<T>
DB2WallProvider

## 主要设计模式

- 观察者模式: MonitorClientContextListener
- 建造者模式: 未明显发现
- 工厂模式: WallProviderStatLoggerImpl,WallVisitorUtils,WallFilter
- 策略模式: 未明显发现
- 装饰器模式: WallFilter,StatementProxyImpl,WrapperProxy
- 单例模式: DruidDriver,ServletPathMatcher,JdbcTraceManager

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
