# Fastjson2 需求文档分析

## 项目概述
- **项目名称**: Fastjson2
- **类型**: JSON解析库
- **定位**: 高性能JSON库，下一个十年的优化目标

## 文档结构

### 章节标题
1. 徽章（CI、覆盖率、Maven Central、版本、许可证）
2. 一句话介绍
3. 相关文档链接
4. 准备（Prepare）
   - 下载
   - 其他模块
     - 兼容fastjson-v1
     - Kotlin集成
     - Spring框架集成
5. 使用（Usage）
   - 解析JSONObject
   - 解析JSONArray
6. 更多功能特性说明

### 文档格式
- **文件**: README.md
- **格式**: Markdown
- **语言**: 英文/中文双语

## 描述风格

### 语气
- 技术导向
- 代码驱动
- 强调性能

### 关键特性描述方式
- 代码示例为主
- Maven/Gradle依赖配置
- 版本号标注

### 视觉元素
- 徽章展示
- 代码块（Maven/Gradle/Java/Kotlin）

## 关键要素

### 功能列表
- JSON解析/序列化
- JSONB协议支持
- 全解析和部分解析
- Java服务器和Android客户端
- Kotlin支持
- Android 8+支持
- JSON Schema支持

### 模块
- fastjson2-core
- fastjson（兼容v1）
- fastjson2-kotlin
- fastjson2-extension（Spring支持）

### 性能
- 相比v1显著提升
- 官方有基准测试数据

## 写作模式总结

### 优点
1. **代码驱动**: 以代码示例为主
2. **模块清晰**: 依赖配置明确
3. **版本信息**: 明确版本号

### 可改进点
1. 缺少特性列表
2. 缺少使用场景说明
3. 缺少性能对比数据
4. 快速开始不够"快速"
