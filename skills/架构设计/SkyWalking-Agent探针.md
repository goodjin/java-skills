# SkyWalking Agent 探针原理分析

> 本文档基于 SkyWalking OAP 服务端源码分析 Agent 探针的工作原理

## 1. 概述

SkyWalking Agent 是运行在目标应用 JVM 中的探针，负责采集运行时数据并发送给 OAP 服务端。Agent 通过字节码增强技术（ByteBuddy）在运行时instrumentation目标方法，实现无侵入式监控。

## 2. 核心架构

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        Application JVM                           │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                   SkyWalking Agent                       │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │   │
│  │  │   Plugin     │  │   Context    │  │   Reporter   │   │   │
│  │  │  Instrument │  │   Manager    │  │   (gRPC/HTTP)│   │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘   │   │
│  │         ↓                 ↓                 ↓            │   │
│  │  ┌─────────────────────────────────────────────────┐    │   │
│  │  │              ByteBuddy Instrumentation           │    │   │
│  │  └─────────────────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              ↓                                  │
│                    Network (gRPC/HTTP)                          │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               ↓
┌──────────────────────────────────────────────────────────────────┐
│                     OAP Server                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │            Trace Receiver (gRPC/HTTP)                    │   │
│  │  TraceSegmentReportServiceHandler                        │   │
│  └──────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
```

### 2.2 数据采集流程

```
Method Execution
      ↓
ByteBuddy Interceptor
      ↓
TracingContext (创建/传递 Trace)
      ↓
SpanBuilder (构建 Span)
      ↓
Span (完成 Span)
      ↓
Segment (一个请求一个 Segment)
      ↓
Reporter 发送至 OAP
```

## 3. 核心类分析

### 3.1 OAP 接收端核心类

#### TraceSegmentReportServiceHandler
**位置**: `oap-server/server-receiver-plugin/skywalking-trace-receiver-plugin/.../handler/v8/grpc/TraceSegmentReportServiceHandler.java`

```java
public class TraceSegmentReportServiceHandler 
    extends TraceSegmentReportServiceGrpc.TraceSegmentReportServiceImplBase 
    implements GRPCHandler {
    
    private ISegmentParserService segmentParserService;
    
    // 流式接收 Segment
    @Override
    public StreamObserver<SegmentObject> collect(StreamObserver<Commands> responseObserver) {
        return new StreamObserver<SegmentObject>() {
            @Override
            public void onNext(SegmentObject segment) {
                segmentParserService.send(segment);  // 发送给分析引擎
            }
            // ...
        };
    }
    
    // 同步接收 Segment
    @Override
    public void collectInSync(final SegmentCollection request, 
                              final StreamObserver<Commands> responseObserver) {
        request.getSegmentsList().forEach(segment -> {
            segmentParserService.send(segment);
        });
    }
}
```

#### ISegmentParserService
**位置**: `oap-server/analyzer/agent-analyzer/.../parser/ISegmentParserService.java`

```java
public interface ISegmentParserService extends Service {
    void send(SegmentObject segment);  // 接收 Segment 并触发解析
}
```

#### SegmentParserServiceImpl
**位置**: `oap-server/analyzer/agent-analyzer/.../parser/SegmentParserServiceImpl.java`

```java
public class SegmentParserServiceImpl implements ISegmentParserService {
    @Override
    public void send(SegmentObject segment) {
        // 创建 TraceAnalyzer 进行分析
        final TraceAnalyzer traceAnalyzer = new TraceAnalyzer(moduleManager, listenerManager, config);
        traceAnalyzer.doAnalysis(segment);
    }
}
```

### 3.2 Segment 数据结构

**SegmentObject** 是跨进程追踪的基本单元：

```protobuf
// 来自 apm-network 协议定义
message SegmentObject {
    string traceId = 1;           // 全局追踪 ID
    string traceSegmentId = 2;     // Segment ID (当前进程)
    repeated SpanObject spans = 3; // Span 列表
    string service = 4;             // 服务名
    string serviceInstance = 5;    // 实例名
}

message SpanObject {
    int32 spanId = 1;              // Span 序号
    int32 parentSpanId = 2;        // 父 Span ID
    string operationName = 3;      // 操作名 (如方法名/接口名)
    SpanType spanType = 4;         // Span 类型 (Entry/Exit/Local)
    int64 startTime = 5;           // 开始时间
    int64 endTime = 6;             // 结束时间
    string peerId = 7;             // 远程服务地址
    string peer = 8;               // 远程服务名
    repeated KeyStringValuePair tags = 9;  // 标签
    repeated Log logs = 10;        // 日志
    bool isError = 11;             // 是否错误
    int32 layer = 12;              // 层级 (HTTP/DB/RPC...)
}
```

## 4. 探针工作原理

### 4.1 字节码增强机制

Agent 使用 ByteBuddy 在运行时修改字节码：

1. **Plugin 加载**: Agent 扫描 `agent/plugins/` 目录下的插件
2. **类匹配**: 通过 `@ InstrumentationClass` 注解匹配. **方法目标类
3拦截**: 使用 `@ Garner` 和 `@ Wrap` 注解定义拦截点
4. **Interceptor 执行**: 在方法前后执行自定义逻辑

### 4.2 上下文传播

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Service A   │────▶│  Service B   │────▶│  Service C   │
│  (Entry)     │     │   (Exit)     │     │   (Entry)    │
└──────────────┘     └──────────────┘     └──────────────┘
      ↓                    ↓                    ↓
  Span A-1              Span B-1              Span C-1
      │                    │                    │
      └────────────────────┴────────────────────┘
                         ↓
                    Trace ID
```

### 4.3 采样策略

Agent 支持多种采样策略：

1. **固定采样**: 按固定比例采样
2. **尾部采样**: 100% 采样错误请求
3. **自适应采样**: 基于服务负载动态调整

OAP 服务端的采样在 `SegmentAnalysisListener` 中实现：

```java
// 位置: agent-analyzer/.../listener/SegmentAnalysisListener.java
if (sampler.shouldSample(segmentObject, duration)) {
    sampleStatus = SAMPLE_STATUS.SAMPLED;
} else if (isError && forceSampleErrorSegment) {
    sampleStatus = SAMPLE_STATUS.SAMPLED;  // 强制采样错误请求
} else {
    sampleStatus = SAMPLE_STATUS.IGNORE;
}
```

## 5. 数据流处理

### 5.1 完整处理流程

```
Agent 发送
    ↓
gRPC/HTTP Receiver
    ↓
TraceSegmentReportServiceHandler
    ↓
ISegmentParserService.send()
    ↓
TraceAnalyzer.doAnalysis()
    ↓
SegmentParserListenerManager (创建多个 Listener)
    ↓
各 Listener 分析 Span
    ↓
SourceReceiver.receive()
    ↓
DispatcherManager 转发
    ↓
SourceDispatcher (OAL 生成的代码)
    ↓
StreamProcessor.in()
    ↓
MetricsPersistentWorker (持久化)
```

### 5.2 Span 监听器模式

**TraceAnalyzer** 使用观察者模式处理 Span：

```java
public void doAnalysis(SegmentObject segmentObject) {
    // 通知 Segment 级别监听器
    notifySegmentListener(segmentObject);
    
    // 遍历所有 Span
    segmentObject.getSpansList().forEach(spanObject -> {
        if (spanObject.getSpanId() == 0) {
            // 首个 Span - 通知 FirstListener
            notifyFirstListener(spanObject, segmentObject);
        }
        
        if (SpanType.Exit.equals(spanObject.getSpanType())) {
            // 退出 Span - 通知 ExitListener
            notifyExitListener(spanObject, segmentObject);
        } else if (SpanType.Entry.equals(spanObject.getSpanType())) {
            // 入口 Span - 通知 EntryListener
            notifyEntryListener(spanObject, segmentObject);
        } else if (SpanType.Local.equals(spanObject.getSpanType())) {
            // 本地 Span - 通知 LocalListener
            notifyLocalListener(spanObject, segmentObject);
        }
    });
    
    // 通知所有 Listener 构建数据
    notifyListenerToBuild();
}
```

## 6. 关键配置

### 6.1 Agent 配置 (agent.config)

```properties
# Collector 地址
collector.backend_service=127.0.0.1:11800

# 采样配置
agent.sample_rate_per_3_secs=1000  # 每3秒采样数

# 插件配置
agent.plugin.exclude_suffixes=.jpg,.jpeg,.css,.js

# 最大_span_limit=300
```

### 6.2 OAP 服务端配置

```yaml
receiver-trace:
  default:
    # 采样策略
    sampleRate: 1000  # 0-10000, 千分之一
    # 错误 Trace 强制采样
    forceSampleErrorSegment: true
```

## 7. 总结

SkyWalking Agent 探针的核心原理：

1. **无侵入**: 通过 Java Agent + ByteBuddy 在运行时加载
2. **字节码增强**: 在方法入口/出口自动插入监控代码
3. **上下文传递**: 通过 TraceContext 在线程间传递追踪信息
4. **标准化协议**: 使用 gRPC/HTTP 发送数据至 OAP
5. **OAP 处理**: OAP 服务端接收后通过 Listener 模式解析处理

---

*文档基于 SkyWalking OAP 服务端源码分析*
