# Dubbo 集群负载均衡源码分析

## 概述

Dubbo 集群层负责将多个 Provider 组合成虚拟服务，提供负载均衡、集群容错、路由筛选等能力。

## 核心类

| 类名 | 位置 | 职责 |
|------|------|------|
| Cluster | dubbo-cluster | 集群接口 (SPI) |
| ClusterInvoker | dubbo-cluster | 集群调用器接口 |
| AbstractClusterInvoker | dubbo-cluster | 集群调用器抽象基类 |
| Directory | dubbo-cluster | 服务目录接口 |
| LoadBalance | dubbo-cluster | 负载均衡接口 |
| RouterChain | dubbo-cluster | 路由链 |

## 架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                          集群架构                                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌──────────────┐     ┌──────────────┐                           │
│   │   Consumer   │────▶│   Directory  │  服务目录 (从注册中心获取)  │
│   └──────────────┘     └───────┬──────┘                           │
│                                │ list()                            │
│                                ▼                                   │
│                        ┌───────────────┐                           │
│                        │   RouterChain │  路由链 (条件路由/标签)    │
│                        └───────┬───────┘                           │
│                                │ route()                           │
│                                ▼                                   │
│   ┌──────────────────────────────────────────────────────────────┐ │
│   │                    ClusterInvoker                            │ │
│   │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │ │
│   │  │  Failover  │  │  Failfast  │  │  Failsafe  │  ...    │ │
│   │  └─────────────┘  └─────────────┘  └─────────────┘         │ │
│   └───────────────────────────┬─────────────────────────────────┘ │
│                               │ doInvoke()                        │
│                               ▼                                   │
│                        ┌───────────────┐                           │
│                        │  LoadBalance  │  负载均衡选择             │
│                        └───────┬───────┘                           │
│                                │ select()                          │
│                                ▼                                   │
│                        ┌───────────────┐                           │
│                        │    Invoker    │  最终调用的 Provider       │
│                        └───────────────┘                           │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## Cluster 接口

### 1. Cluster 接口定义

```java
@SPI(Cluster.DEFAULT)
public interface Cluster {
    String DEFAULT = "failover";
    
    /**
     * 将目录中的 Invoker 合并为一个虚拟 Invoker
     */
    @Adaptive
    <T> Invoker<T> join(Directory<T> directory, boolean buildFilterChain) throws RpcException;
}
```

### 2. 常用 Cluster 实现

| 实现类 | 名称 | 描述 |
|--------|------|------|
| FailoverClusterInvoker | 失败自动切换 | 失败后重试其他节点 |
| FailfastClusterInvoker | 快速失败 | 失败立即报错 |
| FailsafeClusterInvoker | 失败安全 | 失败忽略，返回空 |
| FailbackClusterInvoker | 失败自动恢复 | 失败后定时重试 |
| ForkingClusterInvoker | 并行调用 | 并行调用多个，成功一个即可 |
| BroadcastClusterInvoker | 广播调用 | 广播调用所有节点 |
| AvailableClusterInvoker | 可用性调用 | 调用第一个可用节点 |
| MergeableClusterInvoker | 结果合并 | 合并多个结果 |

### 3. FailoverCluster 实现

```java
public class FailoverCluster implements Cluster {
    @Override
    public <T> Invoker<T> join(Directory<T> directory, boolean buildFilterChain) {
        // 创建 FailoverClusterInvoker
        return new FailoverClusterInvoker<>(directory);
    }
}
```

## Directory 接口

### 1. Directory 定义

```java
public interface Directory<T> extends Node {
    /**
     * 获取服务接口类型
     */
    Class<T> getInterface();
    
    /**
     * 列出所有可调用的 Invoker
     */
    List<Invoker<T>> list(Invocation invocation) throws RpcException;
    
    /**
     * 获取所有 Invoker
     */
    List<Invoker<T>> getAllInvokers();
    
    /**
     * 获取消费者 URL
     */
    URL getConsumerUrl();
}
```

### 2. AbstractDirectory

```java
public abstract class AbstractDirectory<T> implements Directory<T> {
    
    protected final URL url;
    protected final Directory<T> parent;
    protected volatile List<Invoker<T>> invokers;
    protected volatile List<Invoker<T>> cachedInvokers;
    
    @Override
    public List<Invoker<T>> list(Invocation invocation) throws RpcException {
        // 1. 从路由链获取
        List<Invoker<T>> invokers = doList(invocation);
        
        // 2. 路由过滤
        return routerChain.route(getUrl(), invokers, invocation);
    }
}
```

## LoadBalance 接口

### 1. LoadBalance 定义

```java
@SPI(RandomLoadBalance.NAME)
public interface LoadBalance {
    /**
     * 从列表中选择一个 Invoker
     */
    @Adaptive("loadbalance")
    <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException;
}
```

### 2. 负载均衡策略

| 实现类 | 名称 | 描述 |
|--------|------|------|
| RandomLoadBalance | 随机 | 基于权重的随机 |
| RoundRobinLoadBalance | 轮询 | 加权轮询 |
| LeastActiveLoadBalance | 最少活跃数 | 选择活跃数最少的 |
| ConsistentHashLoadBalance | 一致性哈希 | 相同参数到相同节点 |
| ShortestResponseLoadBalance | 最短响应时间 | 选择响应时间最短的 |
| AdaptiveLoadBalance | 自适应 | 基于实时负载动态选择 |

### 3. RandomLoadBalance 实现

```java
public class RandomLoadBalance extends AbstractLoadBalance {
    public static final String NAME = "random";
    
    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        int length = invokers.size();
        
        // 不需要权重
        if (!needWeightLoadBalance(invokers, invocation)) {
            return invokers.get(ThreadLocalRandom.current().nextInt(length));
        }
        
        // 权重计算
        boolean sameWeight = true;
        int[] weights = new int[length];
        int totalWeight = 0;
        
        for (int i = 0; i < length; i++) {
            int weight = getWeight(invokers.get(i), invocation);
            totalWeight += weight;
            weights[i] = totalWeight;
            if (sameWeight && totalWeight != weight * (i + 1)) {
                sameWeight = false;
            }
        }
        
        // 基于权重随机
        if (totalWeight > 0 && !sameWeight) {
            int offset = ThreadLocalRandom.current().nextInt(totalWeight);
            for (int i = 0; i < length; i++) {
                if (offset < weights[i]) {
                    return invokers.get(i);
                }
            }
        }
        
        // 权重相同，均匀分布
        return invokers.get(ThreadLocalRandom.current().nextInt(length));
    }
}
```

### 4. LeastActiveLoadBalance 实现

```java
public class LeastActiveLoadBalance extends AbstractLoadBalance {
    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        int length = invokers.size();
        int leastActive = -1;
        int leastCount = 0;
        int[] leastIndexes = new int[length];
        int totalWeight = 0;
        int firstWeight = true;
        
        for (int i = 0; i < length; i++) {
            Invoker<T> invoker = invokers.get(i);
            int active = RpcStatus.getStatus(invoker.getUrl(), invocation.getMethodName())
                    .getActive();
            int weight = getWeight(invoker, invocation);
            
            // 找最小活跃数
            if (leastActive == -1 || active < leastActive) {
                leastActive = active;
                leastCount = 1;
                leastIndexes[0] = i;
                totalWeight = weight;
                firstWeight = true;
            } else if (active == leastActive) {
                leastIndexes[leastCount++] = i;
                totalWeight += weight;
                if (firstWeight == false && weight != lastWeight) {
                    sameWeight = false;
                }
            }
            
            if (firstWeight) {
                firstWeight = false;
                lastWeight = weight;
            }
        }
        
        // 在最小活跃数的 Invoker 中加权随机
        if (leastCount == 1) {
            return invokers[leastIndexes[0]];
        }
        
        // ... 权重计算逻辑
    }
}
```

### 5. ConsistentHashLoadBalance 实现

```java
public class ConsistentHashLoadBalance extends AbstractLoadBalance {
    
    private final ConcurrentHashMap<String, ConsistentHashSelector<?>> selectors = new ConcurrentHashMap<>();
    
    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        String methodName = RpcUtils.getMethodName(invocation);
        int identityHashCode = System.identityHashCode(invokers);
        
        // 获取或创建 Selector
        ConsistentHashSelector<T> selector = selectors.get(methodName);
        if (selector == null || selector.identityHashCode != identityHashCode) {
            selectors.put(methodName, new ConsistentHashSelector<>(invokers, identityHashCode));
            selector = selectors.get(methodName);
        }
        
        // 选择 Invoker
        return selector.select(invocation);
    }
    
    // 一致性哈希环
    private static final class ConsistentHashSelector<T> {
        private final TreeMap<Long, Invoker<T>> virtualInvokers = new TreeMap<>();
        private final int replicaNumber;
        
        ConsistentHashSelector(List<Invoker<T>> invokers, int identityHashCode) {
            for (Invoker<T> invoker : invokers) {
                String address = invoker.getUrl().getAddress();
                // 每台服务器生成 160 个虚拟节点
                for (int i = 0; i < replicaNumber / 4; i++) {
                    byte[] digest = md5(address + i);
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }
        
        Invoker<T> select(Invocation invocation) {
            // 基于参数生成 hash
            byte[] digest = md5(invocation.getArguments());
            return selectForKey(hash(digest, 0));
        }
    }
}
```

## RouterChain 路由链

### 1. 路由类型

| 类型 | 实现类 | 描述 |
|------|--------|------|
| 条件路由 | ConditionRouter | 基于条件表达式的路由 |
| 脚本路由 | ScriptRouter | 基于 Java/Groovy 脚本的路由 |
| 标签路由 | TagRouter | 基于服务标签的路由 |
| 状态路由 | StateRouter | 基于实例状态的路由 |

### 2. 路由链工作流程

```java
public class RouterChain<T> {
    
    public List<Invoker<T>> route(URL url, List<Invoker<T>> invokers, Invocation invocation) {
        // 依次执行所有路由
        for (Router router : routers) {
            invokers = router.route(invokers, url, invocation);
        }
        return invokers;
    }
}
```

## ClusterInvoker 流程图

```
┌─────────────────────────────────────────────────────────────────────┐
│                    AbstractClusterInvoker.invoke()                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  1. checkWhetherDestroyed()                                         │
│     └── 检查是否已销毁                                              │
│                                                                     │
│  2. List<Invoker<T>> invokers = list(invocation)                  │
│     ├── Directory.list()                                           │
│     └── RouterChain.route()                                        │
│                                                                     │
│  3. checkInvokers(invokers, invocation)                           │
│     └── 检查是否有可用 Provider                                     │
│                                                                     │
│  4. LoadBalance loadbalance = initLoadBalance(invokers, ...)      │
│     └── 初始化负载均衡器                                            │
│                                                                     │
│  5. return doInvoke(invocation, invokers, loadbalance)             │
│     └── 子类实现集群容错逻辑                                        │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       doSelect 流程                                 │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  1. 检查 Sticky 调用                                                │
│     └── 优先使用上次的 Invoker                                     │
│                                                                     │
│  2. LoadBalance.select()                                           │
│     └── 基于负载均衡策略选择                                        │
│                                                                     │
│  3. 检查是否可选                                                   │
│     └── 如果不可用或已选过，进行 Reselect                          │
│                                                                     │
│  4. Reselect                                                       │
│     └── 尝试选择其他可用 Invoker                                   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## 与 SimpleRpcFramework 对比

| 特性 | SimpleRpcFramework | Dubbo 集群 |
|------|-------------------|-----------|
| 负载均衡 | 无 | 6+ 种策略 |
| 集群容错 | 无 | 8+ 种策略 |
| 服务发现 | 手动指定地址 | 注册中心自动发现 |
| 路由能力 | 无 | 条件路由/标签路由 |
| 动态扩缩容 | 不支持 | 支持 |
| 健康检查 | 无 | 可用性检查 |

## 代码示例

### 使用指定集群策略

```java
// XML 配置
<dubbo:reference cluster="failover" retries="2" />

// 注解配置
@DubboReference(cluster = "failover", retries = 2)
private HelloService helloService;

// API 配置
ReferenceConfig<HelloService> ref = new ReferenceConfig<>();
ref.setCluster("failover");
ref.setRetries(2);
```

### 使用指定负载均衡

```java
// 方法级别
<dubbo:reference loadbalance="leastactive" />

// 注解配置
@DubboReference(loadbalance = "leastactive")
private HelloService helloService;

// 特定方法
@DubboReference(methods = {@DubboMethod(loadbalance = "consistenthash")})
private HelloService helloService;
```

### 自定义 Cluster

```java
// 1. 实现 Cluster 接口
public class MyCluster implements Cluster {
    @Override
    public <T> Invoker<T> join(Directory<T> directory) {
        return new MyClusterInvoker<>(directory);
    }
}

// 2. 在 META-INF/dubbo/org.apache.dubbo.rpc.cluster.Cluster 中注册
my-cluster=com.example.MyCluster
```

## 总结

Dubbo 集群层提供了完整的分布式服务治理能力：

1. **Directory** - 统一的服务入口，屏蔽注册中心差异
2. **RouterChain** - 灵活的路由能力，支持多维度筛选
3. **ClusterInvoker** - 多种容错策略，保障服务可用性
4. **LoadBalance** - 多种负载均衡，优化资源利用

这些能力使得 Dubbo 能够构建高可用的分布式系统。
