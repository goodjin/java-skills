# lettuce-core 测试分析

## 测试框架
JUnit 5 (Jupiter) + AssertJ

## 测试文件数量
522 个测试文件

## 测试策略特点
- Redis客户端 lettuce 的全面测试
- 使用AssertJ进行流畅断言
- 包含集成测试和单元测试
- 覆盖连接、命令执行、集群、哨兵等特性
- 测试按功能模块组织(biz/paluch/redis)
- 包含可扩展性测试(MyExtendedRedisClientIntegrationTests)
