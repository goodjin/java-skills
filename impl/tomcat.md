# Tomcat 项目分析

## 项目简介
Tomcat 是 Apache 开发的开源 Web 服务器和 Servlet 容器，实现了 Java EE 中的 Servlet 和 JSP 规范。

## 核心类结构

### 1. Server（服务器）
- **位置**: `org.apache.catalina.Server`
- **实现**: `StandardServer`
- **职责**: Tomcat 顶层容器，管理整个服务器生命周期

### 2. Service（服务）
- **位置**: `org.apache.catalina.Service`
- **实现**: `StandardService`
- **职责**: 包含 Engine 和多个 Connector

### 3. Engine（引擎）
- **位置**: `org.apache.catalina.Engine`
- **实现**: `StandardEngine`
- **职责**: 处理请求的顶层容器

### 4. Host（虚拟主机）
- **位置**: `org.apache.catalina.Host`
- **实现**: `StandardHost`
- **职责**: 虚拟主机，支持多域名

### 5. Context（应用上下文）
- **位置**: `org.apache.catalina.Context`
- **实现**: `StandardContext`
- **职责**: Web 应用容器，管理 Web 应用的 Servlet

### 6. Connector（连接器）
- **位置**: `org.apache.catalina.connector.Connector`
- **协议**: HTTP, HTTPS, AJP
- **职责**: 监听端口，接收请求

### 7. Servlet 容器
- **核心**: `Container` 接口
- **层级**: Engine → Host → Context → Wrapper
- **处理**: `Pipeline` 责任链

## 设计模式

### 1. 责任链模式
- `Pipeline` + `Valve` 处理请求
- 每个 Valve 处理特定功能

### 2. 组合模式
- 容器层级结构
- Container 接口统一管理

### 3. 观察者模式
- `Lifecycle` 事件机制
- `LifecycleListener` 监听生命周期

### 4. 策略模式
- 多种 Connector 实现
- 多种 ProtocolHandler

### 5. 工厂模式
- `InstanceManager` 管理 Servlet 实例
- `ObjectFactory` 创建对象

## 请求处理流程

```
Connector → ProtocolHandler → Adapter → Pipeline → Valve → Servlet
```

### 1. 接收请求
- `NioEndpoint` 监听端口
- `NioChannel` 处理 NIO

### 2. 解析协议
- `Http11Processor` 解析 HTTP
- 生成 `Request` / `Response`

### 3. 路由分发
- `Mapper` 映射 URL 到 Context
- `Wrapper` 找到 Servlet

### 4. 执行 Servlet
- `Servlet.service()` 方法
- 处理 GET/POST 等

### 5. 响应返回
- 写回 Channel
- 释放资源

## 代码技巧

### 1. 嵌入式 Tomcat
```java
Tomcat tomcat = new Tomcat();
tomcat.setPort(8080);
tomcat.addWebapp("/", "/path/to/webapp");
tomcat.start();
tomcat.getServer().await();
```

### 2. 自定义 Valve
```java
public class LogValve implements Valve {
    @Override
    public void invoke(Request request, Response response, Pipeline pipeline) {
        long start = System.currentTimeMillis();
        pipeline.invokeNext(request, response);
        System.out.println(request.getRequestURI() + " took " + (System.currentTimeMillis() - start) + "ms");
    }
}
```

### 3. 生命周期监听
```java
public class MyLifecycleListener implements LifecycleListener {
    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        if (Lifecycle.BEFORE_INIT_EVENT.equals(event.getType())) {
            System.out.println("Tomcat initializing");
        }
    }
}
```

### 4. 异步 Servlet
```java
@WebServlet(asyncSupported = true)
public class AsyncServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AsyncContext asyncContext = req.startAsync();
        asyncContext.start(() -> {
            // 异步处理
            asyncContext.complete();
        });
    }
}
```

### 5. 过滤器链
```java
@WebFilter("/*")
public class MyFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        chain.doFilter(request, response);
    }
}
```

## 代码规范

### 1. 部署结构
```
webapp/
  WEB-INF/
    web.xml
    classes/
    lib/
  index.jsp
```

### 2. web.xml 配置
- Servlet 映射
- 过滤器配置
- 监听器配置

### 3. 性能优化
- 线程池配置
- 连接数配置
- 压缩配置

## 值得学习的地方

1. **架构设计**: 容器层级结构
2. **责任链**: Pipeline + Valve
3. **NIO 模型**: 多路复用
4. **类加载**: 隔离机制
5. **生命周期**: 状态管理
6. **线程池**: 高并发处理
7. **内存管理**: 减少 GC
