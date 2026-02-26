# Dubbo Invoker 调用链源码分析

## 概述

Invoker 是 Dubbo 核心调用抽象，代表一次远程调用。从消费者角度看，调用链涉及 Proxy -> Filter -> Cluster -> Router -> LoadBalance -> Invoker -> 网络传输。

## 核心类

| 类名 | 位置 | 职责 |
|------|------|------|
| Invoker | dubbo-rpc-api | 调用抽象接口 |
| AbstractInvoker | dubbo-rpc-api | Invoker 基础实现 |
| DubboInvoker | dubbo-rpc-dubbo | Dubbo 协议调用器 |
| InvokerInvocationHandler | dubbo-rpc-api | JDK 动态代理处理器 |
| ListenerInvokerWrapper | dubbo-rpc-api | 监听器包装器 |

## 调用链架构图

```
┌────────────────────────────────────────────────────────────────────┐
│                         消费者侧调用链                              │
├────────────────────────────────────────────────────────────────────┤
│                                                                    │
│  ┌─────────────┐                                                  │
│  │   Proxy     │  JDK/CGLIB 动态代理                              │
│  └──────┬──────┘                                                  │
│         │ invoke()                                                 │
│         ▼                                                          │
│  ┌─────────────┐                                                  │
│  │   Filter    │  Filter 链 (Consumer 侧)                        │
│  │   Chain     │  - TokenFilter                                   │
│  └──────┬──────┘  - ContextFilter                                 │
│         │ invoke()                                                 │
│         ▼                                                          │
│  ┌─────────────┐                                                  │
│  │  Cluster    │  集群容错 (Failover/Failfast 等)               │
│  │  Invoker    │                                                  │
│  └──────┬──────┘                                                  │
│         │ invoke()                                                 │
│         ▼                                                          │
│  ┌─────────────┐                                                  │
│  │   Router    │  路由过滤 (条件路由/标签路由)                    │
│  │   Chain     │                                                  │
│  └──────┬──────┘                                                  │
│         │ route()                                                  │
│         ▼                                                          │
│  ┌─────────────┐                                                  │
│  │ LoadBalance │  负载均衡 (Random/RoundRobin/LeastActive)       │
│  └──────┬──────┘                                                  │
│         │ select()                                                 │
│         ▼                                                          │
│  ┌─────────────┐                                                  │
│  │   Dubbo     │  发起网络请求                                    │
│  │   Invoker   │  - 序列化请求                                    │
│  └──────┬──────┘  - 异步/同步调用                                  │
│         │ invoke()                                                 │
│         ▼                                                          │
│  ┌─────────────┐                                                  │
│  │   Channel   │  Netty Channel                                  │
│  │   Pipeline  │                                                  │
│  └─────────────┘                                                  │
│                                                                    │
└────────────────────────────────────────────────────────────────────┘
```

## 详细流程分析

### 1. 入口：InvokerInvocationHandler

这是 JDK 动态代理的入口，将方法调用转换为 RPC 调用：

```java
public class InvokerInvocationHandler implements InvocationHandler {
    private final Invoker<?> invoker;
    private final ServiceModel serviceModel;
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 跳过 Object 方法
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(invoker, args);
        }
        
        // 构建 RpcInvocation
        RpcInvocation rpcInvocation = new RpcInvocation(
                serviceModel,
                method.getName(),
                invoker.getInterface().getName(),
                protocolServiceKey,
                method.getParameterTypes(),
                args);
        
        // 执行业务方法
        return InvocationUtil.invoke(invoker, rpcInvocation);
    }
}
```

### 2. AbstractInvoker.invoke()

AbstractInvoker 实现了 Invoker 接口，提供模板方法：

```java
@Override
public Result invoke(Invocation inv) throws RpcException {
    // 1. 检查是否已销毁
    if (isDestroyed()) {
        logger.warn("Invoker is destroyed...");
    }
    
    RpcInvocation invocation = (RpcInvocation) inv;
    
    // 2. 准备调用上下文
    prepareInvocation(invocation);
    
    // 3. 执行调用并返回异步结果
    AsyncRpcResult asyncResult = doInvokeAndReturn(invocation);
    
    // 4. 如果是同步调用，等待结果
    waitForResultIfSync(asyncResult, invocation);
    
    return asyncResult;
}

private void prepareInvocation(RpcInvocation inv) {
    inv.setInvoker(this);
    // 添加 attachment
    addInvocationAttachments(inv);
    // 设置调用模式
    inv.setInvokeMode(RpcUtils.getInvokeMode(url, inv));
    // 附加调用 ID
    RpcUtils.attachInvocationIdIfAsync(getUrl(), inv);
    // 序列化 ID
    attachInvocationSerializationId(inv);
}

private AsyncRpcResult doInvokeAndReturn(RpcInvocation invocation) {
    AsyncRpcResult asyncResult;
    try {
        // 模板方法，由子类实现
        asyncResult = (AsyncRpcResult) doInvoke(invocation);
    } catch (InvocationTargetException e) {
        // 业务异常处理
        asyncResult = AsyncRpcResult.newDefaultAsyncResult(null, e.getTargetException(), invocation);
    }
    
    // 设置 Future
    if (setFutureWhenSync || invocation.getInvokeMode() != InvokeMode.SYNC) {
        RpcContext.getServiceContext().setFuture(new FutureAdapter<>(asyncResult.getResponseFuture()));
    }
    
    return asyncResult;
}
```

### 3. DubboInvoker.doInvoke()

DubboInvoker 实现了具体的远程调用逻辑：

```java
@Override
protected Result doInvoke(final Invocation invocation) throws Throwable {
    RpcInvocation inv = (RpcInvocation) invocation;
    final String methodName = RpcUtils.getMethodName(invocation);
    
    inv.setAttachment(PATH_KEY, getUrl().getPath());
    inv.setAttachment(VERSION_KEY, version);

    // 选择一个客户端
    ExchangeClient currentClient;
    List<? extends ExchangeClient> exchangeClients = clientsProvider.getClients();
    if (exchangeClients.size() == 1) {
        currentClient = exchangeClients.get(0);
    } else {
        // 轮询选择
        currentClient = exchangeClients.get(index.getAndIncrement() % exchangeClients.size());
    }
    
    // 计算超时时间
    int timeout = RpcUtils.calculateTimeout(getUrl(), invocation, methodName, DEFAULT_TIMEOUT);
    
    // 构建请求
    Request request = new Request();
    request.setData(inv);
    request.setVersion(Version.getProtocolVersion());

    // 判断是否单向调用
    boolean isOneway = RpcUtils.isOneway(getUrl(), invocation);
    
    if (isOneway) {
        // 单向调用：只发送，不等待响应
        boolean isSent = getUrl().getMethodParameter(methodName, Constants.SENT_KEY, false);
        currentClient.send(request, isSent);
        return AsyncRpcResult.newDefaultAsyncResult(invocation);
    } else {
        // 同步/异步调用
        request.setTwoWay(true);
        ExecutorService executor = getCallbackExecutor(getUrl(), inv);
        
        // 发送请求，获取 Future
        CompletableFuture<AppResponse> appResponseFuture =
                currentClient.request(request, timeout, executor)
                        .thenApply(AppResponse.class::cast);
        
        // 构建异步结果
        AsyncRpcResult result = new AsyncRpcResult(appResponseFuture, inv);
        result.setExecutor(executor);
        return result;
    }
}
```

### 4. DubboInvoker.isAvailable()

检查 Invoker 是否可用：

```java
@Override
public boolean isAvailable() {
    if (!super.isAvailable()) {
        return false;
    }
    // 检查所有连接是否可用
    for (ExchangeClient client : clientsProvider.getClients()) {
        if (client.isConnected() && !client.hasAttribute(Constants.CHANNEL_ATTRIBUTE_READONLY_KEY)) {
            return true;
        }
    }
    return false;
}
```

## Cluster Invoker 调用链

### AbstractClusterInvoker

Cluster 层负责集群容错和负载均衡：

```java
public abstract class AbstractClusterInvoker<T> implements ClusterInvoker<T> {
    
    @Override
    public Result invoke(final Invocation invocation) throws RpcException {
        checkWhetherDestroyed();
        
        // 1. 获取可调用列表（经过路由筛选）
        List<Invoker<T>> invokers = list(invocation);
        
        // 2. 检查 invokers
        checkInvokers(invokers, invocation);
        
        // 3. 初始化负载均衡器
        LoadBalance loadbalance = initLoadBalance(invokers, invocation);
        
        // 4. 执行业务逻辑（子类实现）
        return doInvoke(invocation, invokers, loadbalance);
    }
    
    protected Invoker<T> select(
            LoadBalance loadbalance, Invocation invocation, 
            List<Invoker<T>> invokers, List<Invoker<T>> selected) throws RpcException {
        
        // 1. 检查是否 sticky 调用
        boolean sticky = invokers.get(0).getUrl()
                .getMethodParameter(methodName, CLUSTER_STICKY_KEY, DEFAULT_CLUSTER_STICKY);
        
        // 2. 使用负载均衡选择
        Invoker<T> invoker = doSelect(loadbalance, invocation, invokers, selected);
        
        // 3. 如果选择的不可用，进行重选
        if (isSelected || isUnavailable) {
            invoker = reselect(loadbalance, invocation, invokers, selected, availableCheck);
        }
        
        return invoker;
    }
}
```

### FailoverClusterInvoker (失败自动切换)

```java
public class FailoverClusterInvoker<T> extends AbstractClusterInvoker<T> {
    
    @Override
    protected Result doInvoke(Invocation invocation, List<Invoker<T>> invokers, 
                              LoadBalance loadbalance) throws RpcException {
        List<Invoker<T>> invokersCopy = new ArrayList<>(invokers);
        
        int len = getUrl().getMethodParameter(invocation.getMethodName(), 
                                               RETRIES_KEY, DEFAULT_RETRIES) + 1;
        
        RpcException exception = null;
        List<Invoker<T>> invoked ArrayList<>(invokersCopy = new.size());
        
        for (int i = 0; i < len; i++) {
            try {
                // 重试时重新获取列表
                if (i > 0) {
                    checkWhetherDestroyed();
                    invokersCopy = list(invocation);
                }
                
                // 选择 Invoker
                Invoker<T> invoker = select(loadbalance, invocation, invokersCopy, invoked);
                invoked.add(invoker);
                
                // 执行调用
                Result result = invokeWithContext(invoker, invocation);
                return result;
                
            } catch (RpcException e) {
                // 记录异常，继续重试
                exception = e;
            }
        }
        
        throw exception;
    }
}
```

## 调用模式

Dubbo 支持三种调用模式：

```java
// 1. 同步调用 (SYNC)
HelloService service = proxy.get();
String result = service.sayHello("World");  // 阻塞等待

// 2. 异步调用 (ASYNC) - 使用 CompletableFuture
CompletableFuture<String> future = RpcContext.getContext().getCompletableFuture();
future.thenAccept(r -> System.out.println(r));

// 3. 单向调用 (ONEWAY) - 不等待响应
service.sayHello("World");  // 立即返回
```

## 与 SimpleRpcFramework 对比

| 特性 | SimpleRpcFramework | Dubbo Invoker |
|------|-------------------|---------------|
| 调用方式 | 同步阻塞 | 同步/异步/单向 |
| 连接管理 | 每次新建 | 连接池复用 |
| 超时控制 | 无 | 完整支持 |
| 重试机制 | 无 | Failover 等多种 |
| 上下文信息 | 无 | RpcContext 传递 |
| Future 支持 | 无 | CompletableFuture |

## 代码示例

### 完整调用链

```java
// 1. 创建代理 (Proxy)
HelloService helloService = ExtensionLoader.getExtensionLoader(ProxyFactory.class)
    .getAdaptiveExtension().getProxy(invoker);

// 2. 调用 (自动经过 Filter -> Cluster -> Router -> LoadBalance -> Invoker)
String result = helloService.sayHello("World");

// 底层等价于:
RpcInvocation invocation = new RpcInvocation(...);
invocation.setMethodName("sayHello");
invocation.setArguments(new Object[]{"World"});

// 经过 ClusterInvoker
Invoker<HelloService> clusterInvoker = cluster.join(directory);
Result result = clusterInvoker.invoke(invocation);

// 经过 DubboInvoker
DubboInvoker<HelloService> dubboInvoker = (DubboInvoker<HelloService>) selectedInvoker;
Result result = dubboInvoker.invoke(invocation);
```

## 总结

Dubbo 的 Invoker 调用链是一个设计精良的分层架构：

1. **动态代理** - 透明化 RPC 调用
2. **Filter 链** - 关注点分离
3. **Cluster 层** - 集群容错
4. **Router 链** - 路由筛选
5. **LoadBalance** - 负载均衡
6. **Invoker** - 实际远程调用

这种设计使得各层职责清晰，便于扩展和定制。
