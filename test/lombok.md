# lombok 测试分析

## 测试框架
JUnit 4 (RunWith Suite)

## 测试文件数量
1472 个测试文件

## 测试策略特点
- 代码生成工具的全面测试
- 使用JUnit Suite组织大量测试
- 测试分为多个模块：configuration, eclipse, javac等
- 覆盖注解处理器、配置解析、代码生成等核心功能
- 包含编译器集成测试(eclipse, javac)
- 测试资源丰富，大量AST转换验证
