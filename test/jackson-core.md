# jackson-core 测试分析

## 测试框架
JUnit 5 (Jupiter)

## 测试文件数量
257 个测试文件

## 测试策略特点
- 详细的JSON解析/序列化单元测试
- 包含性能测试(perf目录)
- 测试按模块组织在tools/jackson/core/unittest下
- 使用JUnit 5的动态测试特性
- 覆盖流式API、数据绑定、对象序列化等核心功能
