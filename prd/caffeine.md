# Caffeine 项目需求分析

## 项目定位（一句话）

高性能、近乎最优的本地缓存库。

## 核心功能列表

- 自动加载缓存条目（同步/异步）
- 基于容量回收
- 基于时间回收（访问后/写入后）
- 异步刷新
- 弱引用键
- 弱引用/软引用值
- 移除通知
- 外部资源写入
- 统计信息
- JSR-107 JCache支持
- Guava适配器

## 快速开始要点

```java
LoadingCache<Key, Graph> graphs = Caffeine.newBuilder()
    .maximumSize(10_000)
    .expireAfterWrite(Duration.ofMinutes(5))
    .refreshAfterWrite(Duration.ofMinutes(1))
    .build(key -> createExpensiveGraph(key));
```

- Spring Cache集成
- Play Framework、Micronaut、Quarkus等框架集成

## 文档结构特点

- 用户指南详细
- API文档完整
- 性能基准公开

## 资源链接

- 官网: https://github.com/ben-manes/caffeine/wiki
- GitHub: https://github.com/ben-manes/caffeine
