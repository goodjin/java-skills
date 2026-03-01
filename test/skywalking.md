# skywalking 测试分析

## 测试框架
主目录test/为e2e测试，非传统单元测试

## 测试文件数量
38 个(主要为e2e测试用例，非单元测试)

## 测试策略特点
- 主要为e2e(端到端)测试
- 包含java-test-service测试服务
- trace-mocker用于模拟链路追踪数据
- 包含e2e-service-consumer/producer示例服务
- 传统单元测试较少，聚焦集成测试
- 使用SkyWalking自身进行监控验证
