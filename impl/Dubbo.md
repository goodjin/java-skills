# Dubbo 项目分析

## 项目简介
Apache Dubbo 是一个高性能、轻量级的开源 Java RPC 框架，用于构建微服务架构。

## 核心类结构

### 1. URL（统一资源定位符）
- **位置**: `org.apache.dubbo.common.URL`
- **职责**: 统一的配置和服务描述格式
- **设计**: 不可变对象，线程安全

### 2. Protocol（协议）
- **位置**: `org.apache.dubbo.rpc.Protocol`
- **职责**: 服务暴露和引用的核心接口

### 3. Invoker（调用者）
- **接口**: `org.apache.dubbo.rpc.Invoker`
- **职责**: 封装远程调用细节

### 4. Exporter / Invoker
- **职责**: 服务导出和服务调用

### 5. Cluster（集群）
- **职责**: 负载均衡和故障转移
- **实现**: `FailoverClusterInvoker`, `FailfastClusterInvoker` 等

### 6. Router（路由）
- **职责**: 条件路由、脚本路由

## 设计模式

### 1. 装饰器模式
- `ProtocolFilterWrapper` 添加过滤链

### 2. 责任链模式
- `Filter` 链
- `Invoker` 链

### 3. 策略模式
- 多种 `Cluster` 实现
- 多种 `LoadBalance` 实现

### 4. 工厂模式
- `ExtensionLoader` SPI 加载

### 5. 代理模式
- `JavassistProxyFactory` 创建代理

### 6. 观察者模式
- `Registry` 服务发现

## 代码技巧

### 1. SPI 机制
```java
// 加载扩展实现
ExtensionLoader.getExtensionLoader(Protocol.class).getExtension("dubbo");
```

### 2. URL 驱动设计
```java
// 所有配置通过 URL 传递
URL url = new URL("dubbo", "localhost", 20880, "/org.example.DemoService");
```

### 3. 服务导出
```java
// 服务暴露过程
Exporter<?> exporter = protocol.export(invoker);
```

### 4. 集群容错
```java
// 失败自动切换
Cluster cluster = Cluster.getCluster("failover");
Invoker<T> invoker = cluster.join(directory);
```

### 5. 异步编程
```java
// CompletableFuture 支持
CompletableFuture<String> future = invoker.invokeMethodAsync();
```

## 代码规范

### 1. 包结构
- `dubbo-common` - 公共模块
- `dubbo-rpc` - RPC 相关
- `dubbo-cluster` - 集群相关
- `dubbo-registry` - 注册中心

### 2. 命名规范
- 使用有意义的类名
- 常量命名统一

### 3. 配置驱动
- 大量使用配置类
- 支持 XML 和注解配置

## 值得学习的地方

1. **RPC 原理**: 理解远程调用的完整流程
2. **SPI 机制**: Dubbo 的扩展点加载机制
3. **微服务架构**: 服务治理、负载均衡、熔断等
4. **集群容错**: 多种容错策略
5. **服务发现**: 注册中心原理
6. **序列化**: 高效的序列化方式
7. **动态代理**: 代理模式的应用
