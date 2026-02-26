# DubboProtocol 源码分析

## 概述

DubboProtocol 是 Dubbo 框架中负责 RPC 协议实现的核心类，它管理着服务的导出(export)和引用(refer)全过程。

## 核心类

| 类名 | 位置 | 职责 |
|------|------|------|
| DubboProtocol | dubbo-rpc-dubbo | Dubbo 协议实现 |
| DubboExporter | dubbo-rpc-dubbo | 服务导出器 |
| DubboInvoker | dubbo-rpc-dubbo | 服务调用器 |
| Exporter | dubbo-rpc-api | 服务导出接口 |
| Invoker | dubbo-rpc-api | 服务调用接口 |

## 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                         DubboProtocol                        │
├─────────────────────────────────────────────────────────────┤
│  export(Invoker)                                            │
│    ├── 创建 DubboExporter                                    │
│    ├── openServer() - 启动 Netty Server                     │
│    └── optimizeSerialization() - 优化序列化                  │
├─────────────────────────────────────────────────────────────┤
│  refer(Class, URL)                                          │
│    ├── protocolBindingRefer()                               │
│    ├── 创建 DubboInvoker                                    │
│    └── getClients() - 获取连接池                             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    ExchangeHandler                          │
│  (处理请求/响应)                                            │
│  - reply() 处理调用请求                                     │
│  - received() 接收消息                                      │
│  - connected() 连接建立                                     │
│  - disconnected() 连接断开                                  │
└─────────────────────────────────────────────────────────────┘
```

## Export 流程分析

### 1. 服务导出入口

```java
@Override
public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
    checkDestroyed();
    URL url = invoker.getUrl();

    // 1. 创建 Exporter
    String key = serviceKey(url);
    DubboExporter<T> exporter = new DubboExporter<>(invoker, key, exporterMap);

    // 2. 处理回调事件
    boolean isStubSupportEvent = url.getParameter(STUB_EVENT_KEY, DEFAULT_STUB_EVENT);
    boolean isCallbackService = url.getParameter(IS_CALLBACK_SERVICE, false);

    // 3. 打开服务器
    openServer(url);
    
    // 4. 优化序列化
    optimizeSerialization(url);

    return exporter;
}
```

### 2. 启动 Server

```java
private void openServer(URL url) {
    checkDestroyed();
    String key = url.getAddress();
    boolean isServer = url.getParameter(IS_SERVER_KEY, true);

    if (isServer) {
        ProtocolServer server = serverMap.get(key);
        if (server == null) {
            serverMap.put(key, createServer(url));
        } else {
            server.reset(url);
        }
    }
}

private ProtocolServer createServer(URL url) {
    // 设置默认参数
    url = URLBuilder.from(url)
            .addParameterIfAbsent(CHANNEL_READONLYEVENT_SENT_KEY, Boolean.TRUE.toString())
            .addParameterIfAbsent(HEARTBEAT_KEY, String.valueOf(DEFAULT_HEARTBEAT))
            .addParameter(CODEC_KEY, DubboCodec.NAME)
            .build();

    // 创建 ExchangeServer (基于 Netty)
    ExchangeServer server = Exchangers.bind(url, requestHandler);
    
    return new DefaultProtocolServer(server);
}
```

### 3. 请求处理 Handler

```java
requestHandler = new ExchangeHandlerAdapter(frameworkModel) {
    @Override
    public CompletableFuture<Object> reply(ExchangeChannel channel, Object message) throws RemotingException {
        if (!(message instanceof Invocation)) {
            throw new RemotingException(...);
        }
        
        Invocation inv = (Invocation) message;
        // 获取对应的 Invoker
        Invoker<?> invoker = getInvoker(channel, inv);
        
        // 设置上下文类加载器
        Thread.currentThread().setContextClassLoader(
                invoker.getUrl().getServiceModel().getClassLoader());
        
        // 执行业务调用
        RpcContext.getServiceContext().setRemoteAddress(channel.getRemoteAddress());
        Result result = invoker.invoke(inv);
        
        return result.thenApply(Function.identity());
    }
};
```

## Refer 流程分析

### 1. 服务引用入口

```java
@Override
public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
    checkDestroyed();
    return protocolBindingRefer(type, url);
}

@Override
public <T> Invoker<T> protocolBindingRefer(Class<T> serviceType, URL url) throws RpcException {
    checkDestroyed();
    optimizeSerialization(url);

    // 创建 DubboInvoker
    DubboInvoker<T> invoker = new DubboInvoker<>(serviceType, url, getClients(url), invokers);
    invokers.add(invoker);

    return invoker;
}
```

### 2. 获取客户端连接

```java
private ClientsProvider getClients(URL url) {
    int connections = url.getParameter(CONNECTIONS_KEY, 0);
    
    // 默认共享连接
    if (connections == 0) {
        String shareConnectionsStr = ...;
        connections = Integer.parseInt(shareConnectionsStr);
        return getSharedClient(url, connections);
    }
    
    // 独立连接
    List<ExchangeClient> clients = IntStream.range(0, connections)
            .mapToObj((i) -> initClient(url))
            .collect(Collectors.toList());
    return new ExclusiveClientsProvider(clients);
}
```

### 3. 创建客户端

```java
private ExchangeClient initClient(URL url) {
    String clientType = url.getParameter(CLIENT_KEY, DEFAULT_REMOTING_CLIENT);
    
    // 构建 URL
    url = new ServiceConfigURL(...);
    url = url.addParameter(CODEC_KEY, DubboCodec.NAME);
    
    // 懒连接 or 立即连接
    return url.getParameter(LAZY_CONNECT_KEY, false)
            ? new LazyConnectExchangeClient(url, requestHandler)
            : Exchangers.connect(url, requestHandler);
}
```

## 关键数据结构

### ExporterMap
```java
// 服务 key -> Exporter 映射
private final Map<String, Exporter<?>> exporterMap = new ConcurrentHashMap<>();

// 服务 key 格式: port/group/version/interface
private String serviceKey(URL url) {
    return serviceKey(url.getPort(), url.getPath(), url.getVersion(), url.getGroup());
}
```

### ServerMap
```java
// 地址 -> ProtocolServer 映射
private final Map<String, ProtocolServer> serverMap = new ConcurrentHashMap<>();
```

### ReferenceClientMap
```java
// 地址 -> 共享客户端列表
private final Map<String, SharedClientsProvider> referenceClientMap = new ConcurrentHashMap<>();
```

## 与 SimpleRpcFramework 对比

| 特性 | SimpleRpcFramework | DubboProtocol |
|------|------------------|---------------|
| 服务导出 | `new ServerSocket()` | `Exchangers.bind()` 基于 Netty |
| 请求处理 | 同步阻塞 | 异步非阻塞 |
| 连接管理 | 每次请求新建连接 | 连接池复用 |
| 序列化 | Java ObjectStream | Hessian/Kryo/ProtoBuf |
| 线程模型 | 单线程 | 多线程池 |
| 心跳检测 | 无 | 支持 |
| 回调支持 | 无 | 支持 |

## 代码示例

### 服务导出
```java
// Dubbo 方式
DubboProtocol protocol = new DubboProtocol(frameModel);
Invoker<HelloService> invoker = new DubboExporter<>(serviceImpl, key, exporterMap);
protocol.export(invoker);

// 对比 SimpleRpcFramework
SimpleRpcFramework.export(serviceImpl, 8080);
```

### 服务引用
```java
// Dubbo 方式
Invoker<HelloService> invoker = protocol.refer(HelloService.class, url);
HelloService proxy = proxyFactory.getProxy(invoker);

// 对比 SimpleRpcFramework
HelloService service = SimpleRpcFramework.refer(HelloService.class, "localhost", 8080);
```

## 总结

DubboProtocol 的核心价值:

1. **统一的协议抽象** - SPI 机制支持多种协议扩展
2. **高性能网络通信** - 基于 Netty 的异步非阻塞模型
3. **连接池管理** - 共享连接减少资源消耗
4. **完善的服务治理** - 优雅停机、心跳检测、回调支持
