# Dubbo RPC 源码分析

## 核心接口

### 1. Protocol (SPI 接口)
```java
@SPI("dubbo")
public interface Protocol {
    // 导出服务 (服务端)
    <T> Exporter<T> export(Invoker<T> invoker);
    
    // 引用服务 (客户端)
    <T> Invoker<T> refer(Class<T> type, URL url);
    
    void destroy();
}
```

### 2. Invoker (调用执行器)
```java
public interface Invoker<T> extends Node {
    Class<T> getInterface();
    Result invoke(Invocation invocation) throws RpcException;
}
```

### 3. Invocation (调用上下文)
```java
public interface Invocation extends Serializable {
    String getMethodName();
    Class<?>[] getParameterTypes();
    Object[] getArguments();
    Map<String, Object> getAttachments();
}
```

## 架构流程

```
┌─────────────────────────────────────────────────┐
│                   Consumer                       │
├─────────────────────────────────────────────────┤
│  Proxy (代理)                                    │
│      │                                           │
│  Cluster (集群)                                  │
│      ├── LoadBalance                            │
│      └── Directory                              │
│      │                                           │
│  Invoker (调用)                                  │
└──────│──────────────────────────────────────────┘
       │ RPC
┌──────▼──────────────────────────────────────────┐
│                    Provider                      │
├─────────────────────────────────────────────────┤
│  Exporter (导出)                                 │
│      │                                           │
│  Invoker (执行)                                  │
│      │                                           │
│  Filter (过滤链)                                │
│      │                                           │
│  Actual Service (实际服务)                        │
└─────────────────────────────────────────────────┘
```

## 核心模块

| 模块 | 作用 |
|------|------|
| dubbo-rpc-api | 核心接口定义 |
| dubbo-rpc-dubbo | Dubbo 协议实现 |
| dubbo-cluster | 集群、负载均衡 |
| dubbo-registry | 注册中心 |
| dubbo-remoting | 网络通信 (Netty) |

## 最佳实践

### 服务导出
```java
// 服务端
@DubboService
public class HelloServiceImpl implements HelloService {
    public String sayHello(String name) {
        return "Hello, " + name;
    }
}
```

### 服务引用
```java
// 客户端
@DubboReference
private HelloService helloService;
```

### 负载均衡策略
- RandomLoadBalance (随机)
- RoundRobinLoadBalance (轮询)
- LeastActiveLoadBalance (最少活跃)
- ConsistentHashLoadBalance (一致性哈希)
