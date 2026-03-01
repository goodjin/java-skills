# gRPC Java 项目分析

## 项目简介
gRPC 是 Google 开发的高性能、通用的 RPC 框架，基于 HTTP/2 和 Protocol Buffers。

## 核心类结构

### 1. ServiceStub（服务存根）
- **位置**: `io.grpc.stub.*`
- **实现**: `AbstractBlockingStub`, `AbstractFutureStub`, `AbstractAsyncStub`
- **职责**: 客户端调用代理

### 2. Server（服务端）
- **位置**: `io.grpc.Server`
- **实现**: `NettyServer`
- **启动**: `ServerBuilder.forPort(port).addService(service).build()`

### 3. Channel（通道）
- **位置**: `io.grpc.Channel`
- **实现**: `ManagedChannel`
- **职责**: 维护连接池、负载均衡

### 4. MethodDescriptor（方法描述）
- **位置**: `io.grpc.MethodDescriptor`
- **职责**: 定义 RPC 方法元信息
- **类型**: `UNARY`, `SERVER_STREAMING`, `CLIENT_STREAMING`, `BIDI_STREAMING`

### 5. ServerServiceDefinition
- **职责**: 定义服务及其方法
- **绑定**: `ServerInterceptors.chain()` 拦截器链

### 6. ClientCall（客户端调用）
- **位置**: `io.grpc.ClientCall`
- **实现**: `ClientCalls.*` 各种调用方式
- **核心**: `start()` 发起调用

### 7. ServerCall（服务端调用）
- **位置**: `io.grpc.ServerCall`
- **职责**: 处理请求、发送响应

## 设计模式

### 1. 建造者模式
- `ServerBuilder`, `ChannelBuilder`
- 链式配置

### 2. 拦截器模式
- `ClientInterceptor` 客户端拦截
- `ServerInterceptor` 服务端拦截

### 3. 工厂模式
- `MethodDescriptor` 创建方法描述
- `ServiceDescriptor` 创建服务描述

### 4. 代理模式
- 动态代理生成 Stub

### 5. 观察者模式
- `StreamObserver` 异步流处理

## 代码技巧

### 1. 定义 Protobuf
```protobuf
service UserService {
    rpc GetUser (UserRequest) returns (UserResponse);
    rpc GetUsers (UserRequest) returns (stream UserResponse);
}
```

### 2. 服务端实现
```java
public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {
    @Override
    public void getUser(UserRequest request, StreamObserver<UserResponse> responseObserver) {
        UserResponse response = UserResponse.newBuilder()
            .setId(request.getId())
            .setName("test")
            .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
```

### 3. 客户端调用
```java
ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
    .usePlaintext()
    .build();
UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(channel);
UserResponse response = stub.getUser(request);
```

### 4. 拦截器
```java
public class LogInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            next.newCall(method, callOptions)) {};
    }
}
```

### 5. 负载均衡
```java
NameResolverRegistry.getDefaultRegistry().register(new DnsNameResolverProvider());
// 或使用 grpclb
```

## 代码规范

### 1. Protobuf 命名
- 驼峰命名法
- 消息名清晰

### 2. 包结构
- 按业务模块划分
- 分离 Proto 定义和 Java 实现

### 3. 错误处理
- 使用 `Status` 和 `StatusRuntimeException`
- 自定义错误码

## 值得学习的地方

1. **Protocol Buffers**: 高效序列化
2. **HTTP/2**: 多路复用、Header 压缩
3. **流式处理**: 四种调用类型
4. **拦截器链**: 扩展点设计
5. **连接池**: 复用连接
6. **元编程**: 动态代理
7. **负载均衡**: 客户端侧负载均衡
