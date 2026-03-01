# xxl-job

## 核心类和接口

AdminBizImpl
Consts
CronScheduleType
EmailJobAlarm
ExecutorRouteBusyover
ExecutorRouteConsistentHash
ExecutorRouteFailover
ExecutorRouteFirst
ExecutorRouteLFU
ExecutorRouteLRU
ExecutorRouteLast
ExecutorRouteRandom
ExecutorRouteRound
FixRateScheduleType
I18nUtil

## 主要设计模式

- 观察者模式: 未明显发现
- 建造者模式: 未明显发现
- 工厂模式: I18nUtil,FtlUtil,XxlJobDynamicScheduler
- 策略模式: I18nUtil,XxlJobDynamicScheduler,MisfireStrategyEnum
- 装饰器模式: FtlUtil
- 单例模式: 未明显发现

## 代码技巧亮点

- 高性能设计
- 并发优化
- 内存优化
