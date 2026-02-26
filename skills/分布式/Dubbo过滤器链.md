# Dubbo Filter 过滤器链源码分析

## 概述

Dubbo Filter 机制是框架的核心扩展点之一，用于在 RPC 调用过程中嵌入自定义逻辑，如日志记录、性能监控、限流、异常处理等。

## 核心类

| 类名 | 位置 | 职责 |
|------|------|------|
| Filter | dubbo-rpc-api | Filter 接口 |
| BaseFilter | dubbo-rpc-api | 基础抽象 |
| ListenableFilter | dubbo-rpc-api | 可监听 Filter |
| FilterChain | dubbo-rpc-api | Filter 链构建 |
| ClusterFilter | dubbo-cluster | 集群 Filter (3.0 新增) |

## 架构图

### Filter 链架构 (2.x)

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Provider 侧                                 │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐     │
│  │  Echo   │───▶│ Timeout  │───▶│  Token   │───▶│ Generic  │ ... │
│  │ Filter  │    │ Filter   │    │ Filter   │    │ Filter   │     │
│  └──────────┘    └──────────┘    └──────────┘    └──────────┘     │
│       │                                                    │       │
│       │                   Invoker.invoke()               │       │
│       ▼                                                    ▼       │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │                     Business Service                         │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### Filter 链架构 (3.x)

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Consumer 侧                                 │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌────────────────────────────────────────────────────────────────┐│
│  │                    ClusterFilter Chain                         ││
│  │   在 LoadBalance 之前执行，拦截整个集群调用                    ││
│  └────────────────────────────────────────────────────────────────┘│
│                                  │                                 │
│                                  ▼                                 │
│  ┌────────────────────────────────────────────────────────────────┐│
│  │                  ClusterInvoker -> Invoker                     ││
│  │                                                                 ││
│  │   ┌──────────┐    ┌──────────┐    ┌──────────┐               ││
│  │   │  Token   │───▶│  Context │───▶│ Consumer │ ...           ││
│  │   │ Filter   │    │ Filter   │    │  Filter  │               ││
│  │   └──────────┘    └──────────┘    └──────────┘               ││
│  │                                              │                 ││
│  │              Invoker.invoke()                 │                 ││
│  │                                              ▼                 ││
│  │   ┌───────────────────────────────────────────────────────┐  ││
│  │   │                    DubboInvoker                       │  ││
│  │   │         (序列化请求 -> 网络传输 -> 接收响应)           │  ││
│  │   └───────────────────────────────────────────────────────┘  ││
│  └────────────────────────────────────────────────────────────────┘│
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## Filter 接口

### 1. Filter 接口定义

```java
@SPI(scope = ExtensionScope.MODULE)
public interface Filter extends BaseFilter {
    
    /**
     * 执行过滤逻辑
     * 必须在实现中调用 invoker.invoke(invocation) 将请求传递给下一个节点
     */
    Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException;
    
    /**
     * 监听器接口
     */
    interface Listener {
        /**
         * 正常响应时回调
         */
        void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation);
        
        /**
         * 异常时回调
         */
        void onError(Throwable t, Invoker<?> invoker, Invocation invocation);
    }
}
```

### 2. BaseFilter

```java
public interface BaseFilter {
    
    /**
     * Filter 默认实现
     */
    Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException;
    
    /**
     * 监听器 - 可选择性实现
     */
    interface Listener {
        void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation);
        void onError(Throwable t, Invoker<?> invoker, Invocation invocation);
    }
}
```

## 内置 Filter

### 1. EchoFilter - 回声测试

```java
@Activate(group = CommonConstants.PROVIDER)
public class EchoFilter implements Filter, Filter.Listener {
    
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 检查是否是 $echo 方法
        if (invocation.getMethodName().equals("$echo")) {
            Object param = invocation.getArguments()[0];
            return AsyncRpcResult.newDefaultAsyncResult(param, invocation);
        }
        return invoker.invoke(invocation);
    }
}
```

### 2. TimeoutFilter - 超时控制

```java
@Activate(group = CommonConstants.PROVIDER)
public class TimeoutFilter implements Filter, Filter.Listener {
    
    private static final ConcurrentMap<String, Integer> TIMEOUT_TRACKERS = new ConcurrentHashMap<>();
    
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 设置调用开始时间
        invocation.put(TIME_TOTAL_START, System.currentTimeMillis());
        return invoker.invoke(invocation);
    }
    
    @Override
    public void onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        Long startTime = (Long) invocation.get(TIME_TOTAL_START);
        if (startTime != null) {
            long elapsed = System.currentTimeMillis() - startTime;
            int timeout = invocation.getAttachment(TIMEOUT_KEY) != null 
                    ? Integer.parseInt(invocation.getAttachment(TIMEOUT_KEY)) 
                    : 0;
            if (elapsed > timeout && timeout > 0) {
                // 记录超时
                LOGGER.warn(..., "Invoke timeout...");
            }
        }
    }
}
```

### 3. ExceptionFilter - 异常处理

```java
@Activate(group = CommonConstants.PROVIDER)
public class ExceptionFilter implements Filter, Filter.Listener {
    
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        return invoker.invoke(invocation);
    }
    
    @Override
    public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
        if (appResponse.hasException()) {
            Throwable exception = appResponse.getException();
            
            // 检查是否在方法签名中声明
            Method method = invoker.getInterface().getMethod(...);
            for (Class<?> exceptionClass : method.getExceptionTypes()) {
                if (exception.getClass().equals(exceptionClass)) {
                    return; // 直接抛出
                }
            }
            
            // 未声明的异常处理
            if (!(exception instanceof RuntimeException)
                    && !(exception instanceof RpcException)) {
                // 转换为 RuntimeException
                appResponse.setException(new RuntimeException(...));
            }
        }
    }
}
```

### 4. ContextFilter - 上下文传递

```java
@Activate(group = CommonConstants.CONSUMER, order = Integer.MIN_VALUE)
public class ContextFilter implements Filter {
    
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 添加上下文信息
        RpcContext context = RpcContext.getServiceContext();
        invocation.setAttachment(REMOTE_APPLICATION_KEY, context.getRemoteApplicationName());
        
        return invoker.invoke(invocation);
    }
}
```

### 5. TokenFilter - 令牌验证

```java
@Activate(group = CommonConstants.PROVIDER)
public class TokenFilter implements Filter {
    
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 获取 token
        String token = invoker.getUrl().getParameter(TOKEN_KEY);
        if (StringUtils.isNotEmpty(token)) {
            String remoteToken = invocation.getAttachment(TOKEN_KEY);
            if (!token.equals(remoteToken)) {
                throw new RpcException(RpcException.FORBIDDEN_EXCEPTION, "Invalid token");
            }
        }
        return invoker.invoke(invocation);
    }
}
```

### 6. ActiveLimitFilter - 限流

```java
@Activate(group = CommonConstants.CONSUMER)
public class ActiveLimitFilter implements Filter, Filter.Listener {
    
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        URL url = invoker.getUrl();
        String methodName = invocation.getMethodName();
        
        int max = url.getMethodParameter(methodName, ACTIVES_KEY, 0);
        if (max > 0) {
            RpcStatus status = RpcStatus.getStatus(url, methodName);
            // 等待或超时
            if (status.getActive() >= max) {
                throw new RpcException(RpcException.LIMIT_EXCEEDED_EXCEPTION, "Max concurrent invocations exceeded");
            }
        }
        return invoker.invoke(invocation);
    }
}
```

## Filter 激活机制

### @Activate 注解

```java
@Activate(
    group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER},  // 激活组
    order = 0,                                                      // 排序
    value = "param",                                                // URL 参数条件
    before = {},                                                    // 在哪些之前
    after = {}                                                      // 在哪些之后
)
public class MyFilter implements Filter {
    // ...
}
```

### 激活条件

```java
// 示例：只在 PROVIDER 端激活
@Activate(group = CommonConstants.PROVIDER)

// 示例：需要特定 URL 参数才激活
@Activate(value = "cache", before = "validation")

// 示例：使用自定义条件
@Activate(condition = "provider")
```

## Filter 链构建

### 1. 构建过程

```java
public class FilterChain {
    
    public static Invoker<?> buildInvokerChain(Invoker<?> invoker, String filterStr, Group group) {
        // 1. 获取所有 Filter
        List<Filter> filters = ExtensionLoader.getExtensionLoader(Filter.class)
                .getActivateExtension(url, keys, group);
        
        // 2. 排序
        filters.sort(Comparator.comparing(Filter::getOrder));
        
        // 3. 构建链
        Invoker<?> last = invoker;
        for (int i = filters.size() - 1; i >= 0; i--) {
            final Filter filter = filters.get(i);
            final Invoker<?> next = last;
            last = new Invoker<T>() {
                @Override
                public Result invoke(Invocation invocation) throws RpcException {
                    return filter.invoke(next, invocation);
                }
                // ...
            };
        }
        
        return last;
    }
}
```

### 2. 使用方式

```java
// Provider 端
Invoker<?> invoker = new DubboExporter<T>(service, key, exporterMap).getInvoker();
invoker = FilterChain.buildInvokerChain(invoker, filterStr, PROVIDER);

// Consumer 端
Invoker<?> invoker = new DubboInvoker<T>(...);
invoker = FilterChain.buildInvokerChain(invoker, filterStr, CONSUMER);
```

## ClusterFilter (3.x 新特性)

### 1. ClusterFilter 接口

```java
@SPI
public interface ClusterFilter extends BaseFilter {
    
    /**
     * 拦截器，在 ClusterInvoker 调用 LoadBalance 之前执行
     */
    @Adaptive("cluster.filter")
    Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException;
    
    default int getPriority() {
        return 0;
    }
}
```

### 2. 与普通 Filter 的区别

| 特性 | Filter | ClusterFilter |
|------|--------|---------------|
| 执行位置 | 每个 Invoker 级别 | Cluster 级别 |
| 3.x 之前 | 支持 | 不支持 |
| 作用范围 | 单个 Provider | 整个集群 |
| 典型用途 | 超时、限流 | 熔断、全局监控 |

## 自定义 Filter

### 1. 开发 Filter

```java
@Activate(group = CommonConstants.PROVIDER, order = 100)
public class MyFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(MyFilter.class);
    
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        long start = System.currentTimeMillis();
        
        try {
            // 调用下一个 Filter 或 Invoker
            Result result = invoker.invoke(invocation);
            return result;
        } finally {
            long cost = System.currentTimeMillis() - start;
            logger.info("Method {} cost {} ms", invocation.getMethodName(), cost);
        }
    }
    
    @Override
    public void onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        // 处理响应
    }
    
    @Override
    public void onError(Throwable t, Invoker<?> invoker, Invocation invocation) {
        // 处理异常
    }
}
```

### 2. 配置使用

```xml
<!-- XML 配置 -->
<dubbo:provider filter="myFilter" />

<!-- 多个 Filter -->
<dubbo:provider filter="myFilter1,myFilter2" />
```

### 3. SPI 注册

在 `META-INF/dubbo/org.apache.dubbo.rpc.Filter` 文件中添加：

```
myFilter=com.example.MyFilter
```

## 与 SimpleRpcFramework 对比

| 特性 | SimpleRpcFramework | Dubbo Filter |
|------|-------------------|--------------|
| 扩展机制 | 无 | SPI + @Activate |
| Filter 链 | 无 | 完整支持 |
| 常见功能 | 无 | 超时/限流/日志/监控 |
| 可扩展性 | 硬编码 | 插件化 |
| 排序机制 | 无 | order 参数 |

## 代码示例

### Filter 实现模板

```java
public class LoggingFilter implements Filter, Filter.Listener {
    
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 1. 前置处理
        String methodName = invocation.getMethodName();
        Object[] args = invocation.getArguments();
        log.info("Calling {}.{} with args: {}", 
                 invoker.getInterface().getName(), methodName, args);
        
        // 2. 调用链
        Result result;
        try {
            result = invoker.invoke(invocation);
        } catch (RpcException e) {
            // 异常处理
            throw e;
        }
        
        // 3. 返回结果（同步）
        return result;
    }
    
    @Override
    public void onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        // 4. 响应后处理
        log.info("Response from {}.{}: {}", 
                 invoker.getInterface().getName(),
                 invocation.getMethodName(),
                 result.getValue());
    }
    
    @Override
    public void onError(Throwable t, Invoker<?> invoker, Invocation invocation) {
        // 5. 错误处理
        log.error("Error calling {}.{}: {}", 
                  invoker.getInterface().getName(),
                  invocation.getMethodName(),
                  t.getMessage(), t);
    }
}
```

## 总结

Dubbo Filter 机制的关键特点：

1. **SPI 扩展** - 基于接口的插件化机制
2. **声明式激活** - @Activate 注解自动加载
3. **双向拦截** - Provider 和 Consumer 端都可拦截
4. **责任链模式** - 灵活组合，无侵入业务
5. **监听器模式** - onResponse/onError 回调
6. **3.x 增强** - ClusterFilter 支持集群级别拦截
