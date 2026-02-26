# Dubbo RPC 原理

> 基于源码分析 Apache Dubbo 3.x

## 核心概念

### 1. 核心接口

```java
// Protocol: 协议入口
@SPI("dubbo")
public interface Protocol {
    <T> Exporter<T> export(Invoker<T> invoker);   // 导出服务
    <T> Invoker<T> refer(Class<T> type, URL url);  // 引用服务
    void destroy();
}

// Invoker: 调用实体
public interface Invoker<T> extends Node {
    Class<T> getInterface();
    Result invoke(Invocation invocation) throws RpcException;
}

// Exporter: 服务导出器
public interface Exporter<T> {
    Invoker<T> getInvoker();
    void unexport();
}
```

### 2. 架构分层

```
┌─────────────────────────────────────────────────────┐
│                   Service Layer                       │
│  (Provider  ProviderConfig  ConsumerConfig)          │
├─────────────────────────────────────────────────────┤
│                   RPC Layer                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │ dubbo-rpc   │  │ dubbo-rpc  │  │ dubbo-rpc  │ │
│  │ -dubbo      │  │ -hessian   │  │ -injvm     │ │
│  └─────────────┘  └─────────────┘  └─────────────┘ │
├─────────────────────────────────────────────────────┤
│                 Remoting Layer                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │ dubbo-      │  │ dubbo-     │  │ dubbo-     │ │
│  │ remoting-netty│ │ remoting-  │  │ remoting-  │ │
│  │             │  │ zookeeper  │  │ redis      │ │
│  └─────────────┘  └─────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────┘
```

### 3. 调用流程

```
Consumer                           Provider
   │                                  │
   │───refer()───────────────────────▶│ 创建 Invoker
   │                                  │
   │───invoke(Invocation)─────────────▶│ 服务调用
   │                                  │
   │◀───Result────────────────────────│ 返回结果
```

## 源码分析

### 1. DubboProtocol

```java
public class DubboProtocol extends AbstractProtocol {
    public static final String NAME = "dubbo";
    public static final int DEFAULT_PORT = 20880;
    
    // 服务导出
    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) {
        // 1. 获取服务 key
        String key = serviceKey(invoker.getUrl());
        // 2. 创建 DubboExporter
        DubboExporter<T> exporter = new DubboExporter<>(invoker, key, exporterMap);
        // 3. 启动服务器
        openServer(url);
        return exporter;
    }
    
    // 服务引用
    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) {
        // 1. 创建 DubboInvoker
        DubboInvoker<T> invoker = new DubboInvoker<>(...);
        // 2. 返回代理
        return proxyFactory.getInvoker(invoker);
    }
}
```

### 2. 服务注册 (Registry)

```java
// dubbo-registry 模块
public interface RegistryService {
    void register(URL url);    // 注册服务
    void subscribe(URL url, NotifyListener listener);  // 订阅服务
    void unsubscribe(URL url, NotifyListener listener);
}

// ZookeeperRegistry
public class ZookeeperRegistry extends FailbackRegistry {
    // 基于 ZooKeeper 的临时顺序节点
    // /dubbo/{interface}/providers/{url}
    // /dubbo/{interface}/consumers/{url}
}
```

### 3. 负载均衡

```java
// RandomLoadBalance
public class RandomLoadBalance extends AbstractLoadBalance {
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        int length = invokers.size();
        boolean sameWeight = true;
        int[] weights = new int[length];
        int totalWeight = 0;
        for (int i = 0; i < length; i++) {
            int weight = getWeight(invokers.get(i), invocation);
            totalWeight += weight;
            weights[i] = totalWeight;
            if (sameWeight && i > 0 && weight != weights[i - 1]) {
                sameWeight = false;
            }
        }
        // 随机选择
        int offset = ThreadLocalRandom.current().nextInt(totalWeight);
        for (int i = 0; i < length; i++) {
            if (offset < weights[i]) {
                return invokers.get(i);
            }
        }
        return invokers.get(ThreadLocalRandom.current().nextInt(length));
    }
}
```

## 与我的 Demo 对比

| 方面 | 源码 | 我的实现 |
|------|------|----------|
| 协议 | Dubbo/Hessian/JSON | 简化反射 |
| 网络 | Netty | ServerSocket |
| 注册中心 | ZooKeeper/Nacos | 无 |
| 负载均衡 | Random/RoundRobin/LeastActive | 简单随机 |
| 集群容错 | Failover/Failfast/Failsafe | 无 |
| 序列化 | Hessian/Kryo/Protobuf | Java 序列化 |

## 最佳实践

### 1. 服务分组

```yaml
# provider
dubbo:
  service:
    com.example.UserService:
      group: v1
      version: 1.0.0

# consumer
dubbo:
  reference:
    com.example.UserService:
      group: v1
      version: 1.0.0
```

### 2. 集群容错

```java
@DubboCluster(failover = 3)  // 失败自动切换
public class UserServiceImpl implements UserService {}

@DubboCluster(failfast = true)  // 快速失败
public class OrderServiceImpl implements OrderService {}
```

### 3. 超时配置

```yaml
dubbo:
  consumer:
    timeout: 3000        # 3秒超时
    retries: 2           # 重试次数
```
