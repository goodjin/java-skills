# AssertJ 项目需求分析

## 项目定位（一句话）

提供丰富、强类型断言的测试库，用于单元测试。

## 核心功能列表

- 强类型断言（String、Iterable、Map、Path、File等）
- 流畅的API（assertThat(underTest).xxx()）
- 代码补全友好
- 多模块支持：
  - Core模块：JDK类型断言
  - Guava模块：Guava类型断言
  - Joda Time模块
  - Neo4J模块
  - DB模块
  - Swing模块（UI测试）

## 快速开始要点

```xml
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>最新版本</version>
</dependency>
```

## 文档结构特点

- 官方文档：https://assertj.github.io/doc/
- 代码补全驱动
- 社区活跃

## 资源链接

- 官网: https://assertj.github.io/
- GitHub: https://github.com/assertj/assertj
