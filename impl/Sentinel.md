# Sentinel 项目分析

## 项目简介
Sentinel 是阿里巴巴开源的流量控制、熔断降级组件，为微服务提供保障。

## 核心类结构

### 1. Sph（资源保护入口）
- **位置**: `com.alibaba.csp.sentinel.Sph`
- **职责**: 提供资源保护的入口方法
- **实现**: `CtSph`

### 2. ProcessorSlotChain（处理器链）
- **位置**: `com.alibaba.csp.sentinel.slotchain.ProcessorSlotChain`
- **职责**: 责任链处理各种规则

### 3. ResourceWrapper（资源包装）
- **接口**: `com.alibaba.csp.sentinel.slotchain.ResourceWrapper`
- **职责**: 封装资源名称和类型

### 4. Entry（条目）
- **位置**: `com.alibaba.csp.sentinel.Entry`
- **职责**: 记录资源调用信息

### 5. Rule（规则）
- **基类**: `com.alibaba.csp.sentinel.slots.block.Rule`
- **实现**: `FlowRule`, `DegradeRule`, `SystemRule`

### 6. MetricFetcher（指标采集）
- **位置**: `com.alibaba.csp.sentinel.metric.MetricFetcher`
- **职责**: 采集运行时指标

## 设计模式

### 1. 责任链模式
- `ProcessorSlotChain` 处理请求
- 限流、熔断、系统保护依次处理

### 2. 观察者模式
- `MetricFetcher` 指标采集

### 3. 策略模式
- 多种 `Rule` 实现
- 多种 `Slot` 实现

### 4. 门面模式
- `Sph` 提供统一入口

### 5. 单例模式
- 静态实例管理

## 代码技巧

### 1. 滑动窗口算法
```java
// 统计请求通过/拒绝数量
private final MetricBucket[] data;
// 窗口 rollover 处理
```

### 2. 令牌桶算法
```java
// 限流实现
double newestToken = maxToken - (curTime - lastFillTime) * rate;
```

### 3. 熔断器模式
```java
// 半开状态恢复
if (halfOpenRequest.tryPass()) {
    return true;
}
```

### 4. 缓存优化
```java
// ProcessorSlotChain 缓存
private static volatile Map<ResourceWrapper, ProcessorSlotChain> chainMap;
```

### 5. 上下文传递
```java
// Context 线程本地存储
Context context = ContextUtil.getContext();
```

## 代码规范

### 1. 包结构
- `sentinel-core` - 核心模块
- `sentinel-adapter` - 适配器
- `sentinel-cluster` - 集群模式

### 2. SPI 机制
- 扩展点定义在 `META-INF/services`

### 3. 清晰的接口设计
- `ProcessorSlot` 接口职责清晰

## 值得学习的地方

1. **流量控制**: 理解限流算法（令牌桶、滑动窗口）
2. **熔断降级**: 理解熔断器状态转换
3. **系统保护**: 基于 QPS/并发数/响应时间保护
4. **责任链**: 灵活的处理器链设计
5. **指标采集**: 滑动窗口统计
6. **注解支持**: `@SentinelResource` 使用
7. **规则配置**: 动态规则推送
