# rocketmq 测试分析

## 测试框架
JUnit 4

## 测试文件数量
146 个测试文件

## 测试策略特点
- 分布式消息队列测试
- 使用@Before/@After进行测试生命周期管理
- 包含smoke测试(NormalMessageSendAndRecvIT)
- 消息发送/接收集成测试
- 测试组织在test/src/test/java下
- 覆盖普通消息、顺序消息、事务消息等场景
