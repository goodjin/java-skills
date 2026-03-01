# Undertow 项目分析

## 项目简介
Undertow 是 Red Hat 开发的轻量级、高性能 Web 服务器，是 JBoss AS/WildFly 的默认 Web 容器。

## 核心类结构

### 1. Undertow（服务器）
- **位置**: `io.undertow.Undertow`
- **核心**: `builder` 建造者模式
- **启动**: `start()` / `stop()`

### 2. XNIO Worker（Worker 线程）
- **位置**: `org.xnio.XnioWorker`
- **职责**: NIO 事件处理
- **核心**: 线程池管理

### 3. Listener（监听器）
- **位置**: `io.undertow.UndertowListener`
- **实现**: `HttpHandler`, `AjpListener`, `SpdyListener`
- **职责**: 协议解析

### 4. HttpHandler（HTTP 处理器）
- **位置**: `io.undertow.server.HttpHandler`
- **核心**: `handleRequest()` 方法
- **责任链**: `HttpHandler` 链

### 5. HttpServerExchange（交换）
- **位置**: `io.undertow.server.HttpServerExchange`
- **职责**: 请求/响应封装
- **特点**: 完全异步

### 6. RouteHandler（路由处理）
- **位置**: `io.undertow.server.handlers`
- **实现**: `PathHandler`, `RoutingHandler`
- **功能**: 路由匹配

### 7. WebSocket Handler
- **位置**: `io.undertow.websockets`
- **核心**: `WebSocketConnection`
- **支持**: 全双工通信

## 设计模式

### 1. 建造者模式
- `Undertow.builder()` 链式配置
- `ExchangeBuilder` 交换构建

### 2. 责任链模式
- `HttpHandler` 链式调用
- `PredicateHandler` 条件处理

### 3. 观察者模式
- `ServletContext` 事件通知
- `Session` 会话管理

### 4. 策略模式
- 多种 `HttpHandler` 实现
- 多种 `SessionManager`

### 5. 异步非阻塞
- `AsyncSSLStreamSourceChannel`
- `Future` / `Callback` 回调

## 请求处理流程

```
Worker (NIO) → Listener → HttpHandler Chain → Application
```

### 1. NIO 事件
- `XNIO` 处理 Selector
- `Channel` 读写事件

### 2. 协议解析
- `HttpOpenListener` 监听连接
- `HttpReadListener` 读取数据

### 3. 创建 Exchange
- `HttpServerExchange` 封装请求/响应
- 设置 `HttpHandler`

### 4. Handler 处理
- 责任链式调用
- 最终到达应用代码

### 5. 响应返回
- 异步写回
- 资源释放

## 代码技巧

### 1. 嵌入式 Undertow
```java
Undertow server = Undertow.builder()
    .addHttpListener(8080, "localhost")
    .setHandler(exchange -> {
        exchange.getResponseSender().send("Hello");
    })
    .build();
server.start();
```

### 2. 路由处理
```java
RoutingHandler handler = new RoutingHandler()
    .get("/api/users", exchange -> {
        exchange.getResponseSender().send("User list");
    })
    .get("/api/user/{id}", exchange -> {
        String id = exchange.getPathParameters().get("id").getFirst();
        exchange.getResponseSender().send("User: " + id);
    })
    .setFallbackHandler(exchange -> {
        exchange.setStatusCode(404);
        exchange.getResponseSender().send("Not found");
    });
```

### 3. WebSocket
```java
server.setHandler(new WebSocketProtocolHandler()
    .addWebSocketListener(new WebSocketListener() {
        @Override
        public void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
            channel.send(message.getData());
        }
    }));
```

### 4. 文件上传
```java
exchange.getRequestReceiver()
    .receiveFullBytes((exchange, data) -> {
        // 处理上传数据
    });
```

### 5. Session 管理
```java
Session session = exchange.getSession();
session.setAttribute("user", user);
Object user = session.getAttribute("user");
```

### 6. 静态资源
```java
server.setHandler(new ResourceHandler()
    .setResourceManager(new ClassPathResourceManager(App.class))
    .addWelcomeFiles("index.html"));
```

## 代码规范

### 1. Handler 编写
- 保持 Handler 轻量
- 避免阻塞操作
- 使用异步 IO

### 2. 错误处理
- `ExceptionHandler` 统一处理
- 设置适当的错误码

### 3. 资源释放
- 使用 `IoUtils.safeClose()`
- `CompletableFuture` 确保完成

## 值得学习的地方

1. **轻量级**: 核心库很小
2. **高性能**: 异步非阻塞
3. **嵌入式**: 易于集成
4. **Handler 链**: 灵活的扩展
5. **流处理**: 高效内存使用
6. **WebSocket**: 原生支持
7. **NIO 模型**: XNIO 封装
