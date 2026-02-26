# Dubbo 注册中心源码分析

## 概述

Dubbo 注册中心是服务发现的核心组件，负责管理 Provider 地址列表和消费者订阅关系。

## 核心类

| 类名 | 位置 | 职责 |
|------|------|------|
| Registry | dubbo-registry-api | 注册中心接口 |
| AbstractRegistry | dubbo-registry-api | 注册中心抽象基类 |
| FailbackRegistry | dubbo-registry-api | 失败重试抽象 |
| ZookeeperRegistry | dubbo-registry-zookeeper | ZK 实现 |
| NacosRegistry | dubbo-registry-nacos | Nacos 实现 |
| NotifyListener | dubbo-registry-api | 变更监听器 |

## 注册中心架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                        注册中心架构                                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌─────────┐                       ┌─────────┐                   │
│   │Provider │                       │Consumer │                   │
│   └────┬────┘                       └────┬────┘                   │
│        │ register()                      │ subscribe()            │
│        │                                 │                        │
│        ▼                                 ▼                        │
│   ┌─────────────────────────────────────────────┐                  │
│   │              Registry                       │                  │
│   │  ┌──────────┐  ┌──────────┐  ┌─────────┐  │                  │
│   │  │ register │  │subscribe │  │  lookup │  │                  │
│   │  └────┬─────┘  └────┬─────┘  └────┬────┘  │                  │
│   └───────┼──────────────┼─────────────┼───────┘                  │
│           │              │              │                          │
│           ▼              ▼              ▼                          │
│   ┌─────────────────────────────────────────────┐                  │
│   │        RegistryFactory (SPI)               │                  │
│   │  - ZookeeperRegistry                       │                  │
│   │  - NacosRegistry                           │                  │
│   │  - RedisRegistry                           │                  │
│   │  - MulticastRegistry                       │                  │
│   └────────────────────┬────────────────────────┘                  │
│                        │                                            │
│                        ▼                                            │
│   ┌─────────────────────────────────────────────┐                  │
│   │              ZK / Nacos / Redis            │                  │
│   └─────────────────────────────────────────────┘                  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## 注册中心接口

### 1. Registry 接口

```java
public interface Registry extends Node, RegistryService {
    // 继承 Node (isAvailable, destroy)
}

public interface RegistryService {
    /**
     * 注册服务
     */
    void register(URL url);
    
    /**
     * 取消注册
     */
    void unregister(URL url);
    
    /**
     * 订阅服务变更
     */
    void subscribe(URL url, NotifyListener listener);
    
    /**
     * 取消订阅
     */
    void unsubscribe(URL url, NotifyListener listener);
    
    /**
     * 查询服务
     */
    List<URL> lookup(URL url);
}
```

### 2. AbstractRegistry

```java
public abstract class AbstractRegistry implements Registry {
    
    // 本地缓存
    private final ConcurrentMap<URL, Set<URL>> registered = new ConcurrentHashMap<>();
    private final ConcurrentMap<URL, Map<URL, List<URL>>> subscribed = new ConcurrentHashMap<>();
    private final ConcurrentMap<URL, Set<NotifyListener>> listeners = new ConcurrentHashMap<>();
    
    // 内存缓存文件
    private Properties cache;
    
    @Override
    public void register(URL url) {
        // 1. 内存注册
        Set<URL> urls = registered.computeIfAbsent(url, k -> new ConcurrentHashSet<>());
        urls.add(url);
        
        // 2. 持久化到文件
        saveProperties(url);
        
        // 3. 子类实现远程注册
        doRegister(url);
    }
    
    @Override
    public void subscribe(URL url, NotifyListener listener) {
        // 1. 内存订阅
        Map<URL, List<URL>> urls = subscribed.computeIfAbsent(url, k -> new ConcurrentHashMap<>());
        urls.put(url, new ArrayList<>());
        
        // 2. 添加监听器
        Set<NotifyListener> listeners = this.listeners.computeIfAbsent(url, k -> new ConcurrentHashSet<>());
        listeners.add(listener);
        
        // 3. 子类实现远程订阅
        doSubscribe(url, listener);
    }
    
    @Override
    public void notify(URL url, NotifyListener listener, List<URL> urls) {
        // 更新内存缓存
        Map<URL, List<URL>> cacheMap = subscribed.get(url);
        if (cacheMap != null) {
            cacheMap.put(url, urls);
        }
        
        // 保存到文件
        saveProperties(url);
        
        // 通知监听器
        listener.notify(urls);
    }
}
```

### 3. FailbackRegistry

```java
public abstract class FailbackRegistry extends AbstractRegistry {
    
    // 失败重试定时任务
    private final ScheduledExecutorService retryExecutor = Executors.newScheduledThreadPool(1);
    
    // 失败的请求
    private final ConcurrentMap<URL, FailedRegistered> failedRegistered = new ConcurrentHashMap<>();
    private final ConcurrentMap<URL, FailedSubscribed> failedSubscribed = new ConcurrentHashMap<>();
    
    @Override
    public void register(URL url) {
        try {
            doRegister(url);
        } catch (Exception e) {
            // 记录失败，稍后重试
            failedRegistered.put(url, new FailedRegistered(url));
        }
    }
    
    // 定时重试
    private void addFailedSubscribed(URL url, NotifyListener listener) {
        retryExecutor.schedule(() -> {
            try {
                doSubscribe(url, listener);
                failedSubscribed.remove(url);
            } catch (Exception e) {
                // 继续重试
            }
        }, 5, TimeUnit.SECONDS);
    }
}
```

## Zookeeper 注册中心

### 1. ZookeeperRegistry

```java
public class ZookeeperRegistry extends FailbackRegistry {
    
    private static final String DEFAULT_ROOT = "dubbo";
    private final String root;
    private final ZookeeperClient zkClient;
    
    public ZookeeperRegistry(URL url, ZookeeperClientManager zookeeperClientManager) {
        this.root = url.getGroup(DEFAULT_ROOT);
        this.zkClient = zookeeperClientManager.connect(url);
        
        // 监听连接状态
        this.zkClient.addStateListener(state -> {
            if (state == StateListener.RECONNECTED) {
                fetchLatestAddresses();  // 重连后拉取最新地址
            } else if (state == StateListener.NEW_SESSION_CREATED) {
                recover();  // 重新注册和订阅
            }
        });
    }
    
    @Override
    public void doRegister(URL url) {
        // 创建持久节点
        zkClient.create(toUrlPath(url), url.getParameter(DYNAMIC_KEY, true), true);
    }
    
    @Override
    public void doSubscribe(final URL url, final NotifyListener listener) {
        if (ANY_VALUE.equals(url.getServiceInterface())) {
            // 订阅所有服务
            String root = toRootPath();
            ChildListener zkListener = (parentPath, children) -> {
                for (String child : children) {
                    if (!anyServices.contains(child)) {
                        anyServices.add(child);
                        // 递归订阅
                        subscribe(url.setPath(child).addParameters(...), listener);
                    }
                }
            };
            zkClient.addChildListener(root, zkListener);
        } else {
            // 订阅特定服务
            for (String path : toCategoriesPath(url)) {
                ChildListener zkListener = new RegistryChildListenerImpl(url, listener, latch);
                zkClient.create(path, false, true);
                List<String> children = zkClient.addChildListener(path, zkListener);
                // 立即通知
                notify(url, listener, toUrlsWithEmpty(url, path, children));
            }
        }
    }
    
    @Override
    public List<URL> lookup(URL url) {
        List<String> providers = new ArrayList<>();
        for (String path : toCategoriesPath(url)) {
            List<String> children = zkClient.getChildren(path);
            providers.addAll(children);
        }
        return toUrlsWithoutEmpty(url, providers);
    }
}
```

### 2. ZK 节点结构

```
/dubbo
├── com.example.HelloService
│   ├── providers
│   │   └── dubbo://192.168.1.1:20880/com.example.HelloService?version=1.0.0
│   ├── consumers
│   │   └── dubbo://192.168.1.2:54321/com.example.HelloService
│   ├── routers
│   │   └── ... (路由配置)
│   └── configurators
│       └── ... (动态配置)
```

## Nacos 注册中心

### 1. NacosRegistry

```java
public class NacosRegistry extends FailbackRegistry {
    
    private final NacosNamingServiceWrapper namingService;
    
    @Override
    public void doRegister(URL url) {
        Instance instance = createInstance(url);
        String serviceName = getServiceName(url, false);
        
        // 注册实例
        namingService.registerInstance(
            serviceName, 
            getUrl().getGroup(Constants.DEFAULT_GROUP), 
            instance
        );
    }
    
    @Override
    public void doSubscribe(final URL url, final NotifyListener listener) {
        Set<String> serviceNames = getServiceNames(url);
        
        for (String serviceName : serviceNames) {
            // 监听服务变更
            EventListener eventListener = new RegistryChildListenerImpl(serviceName, url, listener);
            namingService.subscribe(serviceName, group, eventListener);
            
            // 立即获取当前实例
            List<Instance> instances = namingService.getAllInstances(serviceName, group);
            notifySubscriber(url, serviceName, listener, instances);
        }
    }
    
    @Override
    public List<URL> lookup(URL url) {
        Set<String> serviceNames = getServiceNames(url);
        List<URL> urls = new ArrayList<>();
        
        for (String serviceName : serviceNames) {
            List<Instance> instances = namingService.getAllInstances(serviceName, group);
            urls.addAll(buildURLs(url, instances));
        }
        return urls;
    }
}
```

### 2. Nacos 服务名格式

```
# 默认格式: category:interface:version:group
providers:com.example.HelloService:1.0.0:dubbo

# 兼容格式: interface:version:group
com.example.HelloService:1.0.0:dubbo
```

## 服务发现流程

### 1. Provider 注册

```
Provider 启动
    │
    ▼
ZookeeperRegistry.doRegister()
    │
    ├── 创建节点: /dubbo/com.example.HelloService/providers/dubbo://...
    │
    └── 返回成功
```

### 2. Consumer 订阅

```
Consumer 启动
    │
    ▼
ZookeeperRegistry.doSubscribe()
    │
    ├── 监听路径: /dubbo/com.example.HelloService/providers
    │
    ├── 获取当前子节点列表
    │
    └── 回调 NotifyListener.notify()
         │
         ├── 更新 Directory 中的 Invoker 列表
         │
         ├── 触发 ClusterInvoker 重新选择
         │
         └── 通知业务层
```

### 3. 服务变更通知

```
Provider 下线
    │
    ▼
ZK 节点删除 / Nacos 实例删除
    │
    ├── 触发 ChildListener / EventListener
    │
    ├── Registry.notify()
    │
    ├── Directory.refreshInvoker()
    │
    ├── RouterChain.setInvokers()
    │
    └── ClusterInvoker 选择新节点
```

## 注册中心对比

| 特性 | Zookeeper | Nacos | Redis |
|------|-----------|-------|-------|
| 一致性 | CP | AP (可配置 CP) | AP |
| 可用性 | 低 (ZK 选举) | 高 | 高 |
| 性能 | 中 | 高 | 高 |
| 运维复杂度 | 高 | 低 | 中 |
| 推送模式 | 拉取 | 长连接 | 轮询/长连接 |
| 支持配置 | 否 | 是 | 是 |
| 支持元数据 | 否 | 是 | 是 |

## 与 SimpleRpcFramework 对比

| 特性 | SimpleRpcFramework | Dubbo 注册中心 |
|------|-------------------|---------------|
| 服务发现 | 手动指定 | 自动发现 |
| 地址管理 | 硬编码 | 注册中心管理 |
| 动态感知 | 无 | 变更通知 |
| 负载均衡 | 无 | 自动负载均衡 |
| 故障转移 | 无 | 自动切换 |
| 健康检查 | 无 | 心跳检测 |

## 代码示例

### 配置 Zookeeper 注册中心

```xml
<dubbo:registry 
    address="zookeeper://127.0.0.1:2181" 
    group="dubbo"
    timeout="5000" />
```

### 配置 Nacos 注册中心

```xml
<dubbo:registry 
    address="nacos://127.0.0.1:8848" 
    group="dubbo"
    namespace="public" />
```

### Java API 配置

```java
// 配置注册中心
RegistryConfig registry = new RegistryConfig();
registry.setAddress("zookeeper://127.0.0.1:2181");
registry.setGroup("dubbo");

// 配置应用
ApplicationConfig application = new ApplicationConfig("dubbo-provider");

// 配置服务
ServiceConfig<HelloService> service = new ServiceConfig<>();
service.setInterface(HelloService.class);
service.setRef(new HelloServiceImpl());
service.setRegistry(registry);
service.setApplication(application);

service.export();
```

### 自定义注册中心

```java
// 1. 实现 Registry 接口
public class MyRegistry extends FailbackRegistry {
    @Override
    protected void doRegister(URL url) {
        // 实现注册逻辑
    }
    
    @Override
    protected void doSubscribe(URL url, NotifyListener listener) {
        // 实现订阅逻辑
    }
}

// 2. 实现 RegistryFactory
public class MyRegistryFactory implements RegistryFactory {
    @Override
    public Registry getRegistry(URL url) {
        return new MyRegistry(url);
    }
}

// 3. SPI 注册
// META-INF/dubbo/org.apache.dubbo.registry.RegistryFactory
my=com.example.MyRegistryFactory
```

## 总结

Dubbo 注册中心的核心价值：

1. **服务注册与发现** - 自动化的服务地址管理
2. **变更通知机制** - 实时感知服务状态变化
3. **多注册中心支持** - ZK/Nacos/Redis 等多种实现
4. **本地缓存** - 提升性能和可用性
5. **失败重试** - 保证注册成功
6. **订阅关系管理** - 支持分组和版本

注册中心是 Dubbo 实现分布式服务治理的基础设施，是构建微服务架构的关键组件。
