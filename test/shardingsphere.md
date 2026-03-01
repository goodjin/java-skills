# shardingsphere 测试分析

## 测试框架
JUnit 5 (Jupiter)

## 测试文件数量
1591 个测试文件

## 测试策略特点
- 分片数据库中间件的大规模测试
- 使用自定义测试框架Extension(AutoMockExtension)
- 包含infra/framework测试基础设施
- 覆盖SQL解析、改写、路由、结果聚合等核心流程
- 测试按模块组织(infra, executor, planner等)
- 使用JUnit 5的Extension机制进行测试扩展
