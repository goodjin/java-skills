# Awaitility 项目需求分析

## 项目定位（一句话）

用于简化异步系统测试的DSL，允许以简洁易读的方式表达异步期望。

## 核心功能列表

- 异步操作等待
- 超时控制
- 条件轮询
- 支持JUnit/TestNG集成
- 支持Kotlin时间API
- 流畅的API

## 快速开始要点

```java
await().atMost(5, SECONDS).until(customerStatusIsUpdated());
```

- 支持until()、untilAsserted()等方法

## 文档结构特点

- GitHub Wiki文档
- 使用指南详细
- Javadoc完整

## 资源链接

- GitHub: https://github.com/awaitility/awaitility
- 文档: https://github.com/awaitility/awaitility/wiki/Getting_started
