# Jaeger 项目需求分析

## 项目定位（一句话）

开源分布式追踪系统，由Uber开源，现为CNCF毕业项目。

## 核心功能列表

- 分布式追踪
- 分布式事务监控
- 性能优化
- 根因分析
- 服务依赖可视化
- 采样策略
- 后端存储支持
- v2版本已发布

## 快速开始要点

```bash
# Docker快速启动
docker run -d --name jaeger \
  -e COLLECTOR_OTLP_ENABLED=true \
  -p 6831:6831/udp \
  -p 16686:16686 \
  jaegertracing/all-in-one:latest
```

## 文档结构特点

- CNCF毕业项目
- 文档详细
- 社区活跃

## 资源链接

- 官网: https://www.jaegertracing.io
- GitHub: https://github.com/jaegertracing/jaeger
