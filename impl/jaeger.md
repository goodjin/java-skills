# Jaeger 项目分析

## 项目简介
Jaeger 是开源分布式追踪系统，由Uber开源，现为CNCF毕业项目。提供分布式追踪、事务监控、性能优化和根因分析能力。

## 核心类结构

### 1. JaegerTracer（追踪器）
- **位置**: `io.jaegertracing.internal.JaegerTracer`
- **职责**: 分布式追踪的核心实现
- **功能**: 创建Span、管理采样策略

### 2. Span（跨度）
- **位置**: `io.jaegertracing.internal.Span`
- **职责**: 代表一个工作单元
- **功能**: 记录操作名称、开始时间、标签、日志

### 3. Tracer.Builder（构建器）
- **职责**: 配置追踪器
- **功能**: 设置采样器、报告器、metrics

### 4. Reporter（报告器）
- **职责**: 发送追踪数据到后端
- **实现**: `RemoteReporter`, `InMemoryReporter`

### 5. Sampler（采样器）
- **职责**: 决定哪些请求被追踪
- **实现**: `ConstSampler`, `ProbabilisticSampler`, `RateLimitingSampler`

### 6. Metrics（指标）
- **职责**: 收集内部指标
- **实现**: 支持Prometheus、StatsD、InMemory

## 设计模式

### 1. 建造者模式（Builder）
- `JaegerTracer.Builder` 配置追踪器
- `Span.Builder` 构建Span

### 2. 装饰器模式（Decorator）
- `MetricsReporter` 包装Reporter添加指标收集

### 3. 策略模式（Strategy）
- 多种Sampler实现
- 多种Reporter实现

### 4. 工厂模式（Factory）
- `TracerFactory` 创建追踪器

## 代码技巧

### 1. 基础追踪
```java
// 创建追踪器
JaegerTracer tracer = new JaegerTracer.Builder("my-service")
    .withSampler(new ConstSampler(true))
    .withReporter(new InMemoryReporter())
    .build();

// 开始追踪
Span span = tracer.buildSpan("operation-name")
    .withTag("key", "value")
    .start();

// 执行操作
try {
    // 业务逻辑
} finally {
    span.finish();
}
```

### 2. 父子追踪
```java
Span parentSpan = tracer.buildSpan("parent").start();
try {
    Span childSpan = tracer.buildSpan("child")
        .asChildOf(parentSpan)
        .start();
    try {
        // 子操作
    } finally {
        childSpan.finish();
    }
} finally {
    parentSpan.finish();
}
```

### 3. 自定义Reporter
```java
public class CustomReporter implements Reporter {
    @Override
    public void report(Span span) {
        // 发送到自定义存储
        sendToBackend(span);
    }

    @Override
    public void close() {
        // 清理资源
    }
}
```

### 4. 采样策略
```java
// 限流采样：每秒允许N个追踪
Sampler rateLimitingSampler = new RateLimitingSampler(10);

// 概率采样：50%采样率
Sampler probabilisticSampler = new ProbabilisticSampler(0.5);
```

## 性能优化要点

1. **合理采样**: 高流量场景使用RateLimitingSampler
2. **异步报告**: 使用RemoteReporter批量发送
3. **标签优化**: 避免过多高基数字段
4. **采样决策**: 在入口处确定采样策略
5. **资源清理**: 确保Span正确finish避免内存泄漏
