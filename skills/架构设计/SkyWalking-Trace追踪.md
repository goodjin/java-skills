# SkyWalking Trace 追踪机制分析

> 本文档深入分析 SkyWalking 的分布式追踪机制

## 1. 追踪模型概述

### 1.1 核心概念

SkyWalking 的分布式追踪基于 **Google Dapper** 论文实现，核心概念：

- **Trace**: 完整的分布式追踪链路，从请求入口到所有下游调用
- **Segment**: 单个服务实例内的追踪单元
- **Span**: 追踪中的基本操作单元，表示一个独立的工作单元
- **SpanKind**: Span 类型 (CLIENT, SERVER, PRODUCER, CONSUMER)

### 1.2 追踪架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Trace                                        │
│  Trace ID: aaaa.bbbb.cccc                                           │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌─────────────┐       ┌─────────────┐       ┌─────────────┐     │
│   │  Service A  │──────▶│  Service B  │──────▶│  Service C  │     │
│   │  Segment 1  │       │  Segment 2  │       │  Segment 3  │     │
│   │ ┌─────────┐ │       │ ┌─────────┐ │       │ ┌─────────┐ │     │
│   │ │Span 1.1 │ │       │ │Span 2.1 │ │       │ │Span 3.1 │ │     │
│   │ └─────────┘ │       │ └─────────┘ │       │ └─────────┘ │     │
│   └─────────────┘       └─────────────┘       └─────────────┘     │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## 2. 源码分析

### 2.1 Segment 核心类

**位置**: `oap-server/server-core/src/main/java/org/apache/skywalking/oap/server/core/source/Segment.java`

```java
@ScopeDeclaration(id = SEGMENT, name = "Segment")
public class Segment extends Source {
    
    private String segmentId;        // Segment ID (本地唯一)
    private String traceId;          // Trace ID (全局唯一)
    private String serviceId;        // 服务 ID
    private String serviceInstanceId;// 实例 ID
    private String endpointId;       // 端点 ID
    private long startTime;          // 开始时间
    private int latency;             // 延迟 (ms)
    private int isError;             // 是否错误
    private byte[] dataBinary;       // 原始二进制数据
    private List<Tag> tags;         // 标签
}
```

### 2.2 追踪分析监听器

#### SegmentAnalysisListener
**位置**: `oap-server/analyzer/agent-analyzer/.../listener/SegmentAnalysisListener.java`

这是核心的追踪数据处理类，负责解析和构建 Segment：

```java
@Slf4j
public class SegmentAnalysisListener 
    implements FirstAnalysisListener, EntryAnalysisListener, SegmentListener {
    
    private final Segment segment = new Segment();
    private SAMPLE_STATUS sampleStatus = SAMPLE_STATUS.UNKNOWN;
    
    @Override
    public void parseSegment(SegmentObject segmentObject) {
        segment.setTraceId(segmentObject.getTraceId());
        
        // 遍历所有 Span，计算时间
        segmentObject.getSpansList().forEach(span -> {
            if (startTimestamp == 0 || startTimestamp > span.getStartTime()) {
                startTimestamp = span.getStartTime();
            }
            if (span.getEndTime() > endTimestamp) {
                endTimestamp = span.getEndTime();
            }
            isError = isError || segmentStatusAnalyzer.isError(span);
        });
        
        // 计算耗时
        final long accurateDuration = endTimestamp - startTimestamp;
        duration = accurateDuration > Integer.MAX_VALUE ? 
            Integer.MAX_VALUE : (int) accurateDuration;
        
        // 采样判断
        if (sampler.shouldSample(segmentObject, duration)) {
            sampleStatus = SAMPLE_STATUS.SAMPLED;
        }
    }
    
    @Override
    public void build() {
        if (sampleStatus.equals(SAMPLE_STATUS.IGNORE)) {
            return;
        }
        // 发送到 SourceReceiver
        sourceReceiver.receive(segment);
    }
}
```

## 3. 追踪机制详解

### 3.1 Span 类型体系

SkyWalking 定义三种 Span 类型：

| 类型 | 说明 | 场景 |
|------|------|------|
| **Entry** | 入口 Span | 接收外部请求的第一个服务 |
| **Exit** | 退出 Span | 发起外部调用的服务 |
| **Local** | 本地 Span | 服务内部的方法调用 |

### 3.2 Trace 上下文传播

```
请求进入 Service A:
┌─────────────────────────────────────────────────────┐
│ Trace ID: abc123                                    │
│ ─────────────────────────────────────────────────── │
│ Span 1 (Entry) - /api/order                         │
│   └─ Span 2 (Local) - orderService.find()          │
│       └─ Span 3 (Exit) - http://stock-service       │
└─────────────────────────────────────────────────────┘
```

### 3.3 跨进程追踪

```
┌──────────────┐ HTTP      ┌──────────────┐  HTTP   ┌──────────────┐
│  Service A   │──────────▶│  Service B   │────────▶│  Service C   │
│  (Client)    │  Header   │  (Server)    │ Header  │  (Server)    │
│              │            │              │         │              │
│ Span 1:Exit  │  ──────▶  │ Span 2:Entry │ ──────▶│ Span 3:Entry │
│ refId=B-1   │            │ parentId=1   │         │ parentId=2   │
└──────────────┘            └──────────────┘         └──────────────┘

Header 传播内容:
  - sw8: TraceId|SegmentId|spanId|parentSpanId|...
  - sw8-correlation: 自定义元数据
```

### 3.4 错误追踪

错误信息通过以下方式记录：

```java
// Span 错误标记
message SpanObject {
    bool isError = 11;              // 是否有错误
    
    // 错误日志
    message Log {
        int64 time = 1;
        message Field {
            string key = 1;
            string value = 2;
        }
        repeated Field fields = 2;  // 通常包含 error.stack, error.message
    }
    repeated Log logs = 10;         // 错误堆栈
}
```

## 4. 追踪数据处理流程

### 4.1 完整处理流程图

```
┌─────────────────────────────────────────────────────────────────────┐
│                         接收阶段                                     │
│  ┌─────────────────┐                                               │
│  │ gRPC Handler    │ TraceSegmentReportServiceHandler              │
│  │ HTTP Handler    │ TraceSegmentReportHandler                     │
│  └────────┬────────┘                                               │
│           ↓                                                         │
│  ┌─────────────────┐                                               │
│  │ Segment Parser  │ TraceAnalyzer.doAnalysis()                   │
│  └────────┬────────┘                                               │
│           ↓                                                         │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                    Listener 通知链                            │   │
│  │  SegmentListener → FirstListener → Entry/Exit/LocalListener │   │
│  └─────────────────────────────────────────────────────────────┘   │
│           ↓                                                         │
└───────────┼─────────────────────────────────────────────────────────┘
            ↓
┌─────────────────────────────────────────────────────────────────────┐
│                         分析阶段                                     │
│  ┌─────────────────┐     ┌─────────────────┐                        │
│  │ Sampling       │     │ Segment Status │                        │
│  │ 采样判断        │     │ 分析(错误状态)  │                        │
│  └─────────────────┘     └─────────────────┘                        │
│           ↓                                                         │
│  ┌─────────────────┐     ┌─────────────────┐                        │
│  │ SourceReceiver │     │ 搜索标签收集     │                        │
│  │ .receive()      │     │                 │                        │
│  └────────┬────────┘     └─────────────────┘                        │
└───────────┼─────────────────────────────────────────────────────────┘
            ↓
┌─────────────────────────────────────────────────────────────────────┐
│                         分发阶段                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ DispatcherManager.forward()                                │   │
│  │   - 根据 scopeId 查找对应的 Dispatcher 列表                   │   │
│  │   - 遍历执行所有 SourceDispatcher                            │   │
│  └─────────────────────────────────────────────────────────────┘   │
└───────────┼─────────────────────────────────────────────────────────┘
            ↓
┌─────────────────────────────────────────────────────────────────────┐
│                         指标计算阶段                                  │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                    SourceDispatcher                          │   │
│  │  (由 OAL 脚本生成)                                           │   │
│  │  - ServiceResponseTimeDispatcher                            │   │
│  │  - EndpointAvgDispatcher                                    │   │
│  │  - ServiceRelationServerDispatcher                          │   │
│  └─────────────────────────────────────────────────────────────┘   │
│           ↓                                                         │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ MetricsStreamProcessor.in()                                │   │
│  │   - L1 聚合 (内存中)                                          │   │
│  │   - 周期刷新到 L2                                             │   │
│  └─────────────────────────────────────────────────────────────┘   │
└───────────┼─────────────────────────────────────────────────────────┘
            ↓
┌─────────────────────────────────────────────────────────────────────┐
│                         持久化阶段                                   │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ MetricsPersistentWorker                                    │   │
│  │   - 按时间分桶 (Minute/Hour/Day)                            │   │
│  │   - 批量写入存储                                              │   │
│  │   - TTL 管理                                                  │   │
│  └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

### 4.2 核心分发机制

**DispatcherManager** 负责将 Source 分发到对应的处理器：

```java
// 位置: server-core/.../analysis/DispatcherManager.java
public class DispatcherManager implements DispatcherDetectorListener {
    
    private Map<Integer, List<SourceDispatcher<ISource>>> dispatcherMap;
    
    public void forward(ISource source) {
        List<SourceDispatcher<ISource>> dispatchers = dispatcherMap.get(source.scope());
        
        if (dispatchers != null) {
            source.prepare();
            for (SourceDispatcher<ISource> dispatcher : dispatchers) {
                dispatcher.dispatch(source);  // 执行 OAL 生成的代码
            }
        }
    }
}
```

## 5. 追踪数据模型

### 5.1 Scope 定义

**位置**: `oap-server/server-core/.../source/DefaultScopeDefine.java`

```java
public class DefaultScopeDefine {
    public static final int SEGMENT = 0;
    public static final int SERVICE = 1;
    public static final int SERVICE_INSTANCE = 2;
    public static final int ENDPOINT = 3;
    public static final int SERVICE_RELATION = 4;
    public static final int ENDPOINT_RELATION = 5;
    // ... 更多 Scope
}
```

### 5.2 关系图谱

```
                    ┌──────────────────┐
                    │     Service      │
                    │   (scope: 1)     │
                    └────────┬─────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
          ↓                  ↓                  ↓
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ServiceInstance  │  │    Endpoint     │  │ServiceRelation  │
│  (scope: 2)     │  │   (scope: 3)    │  │  (scope: 4)     │
└─────────────────┘  └─────────────────┘  └─────────────────┘
                             │
                             ↓
                  ┌─────────────────────┐
                  │ EndpointRelation    │
                  │   (scope: 5)        │
                  └─────────────────────┘
```

## 6. 关键处理类一览

| 类名 | 职责 |
|------|------|
| `TraceSegmentReportServiceHandler` | gRPC 接收 trace 数据 |
| `SegmentParserServiceImpl` | Segment 解析服务入口 |
| `TraceAnalyzer` | 分析入口，触发监听器 |
| `SegmentAnalysisListener` | Segment 级别分析 |
| `EntryAnalysisListener` | Entry Span 分析 |
| `ExitAnalysisListener` | Exit Span 分析 |
| `SourceReceiver` | Source 数据接收接口 |
| `SourceReceiverImpl` | Source 接收实现 |
| `DispatcherManager` | 分发管理器 |
| `SourceDispatcher` | OAL 生成的处理器基类 |

## 7. 性能优化

### 7.1 L1 内存聚合

**MetricsAggregateWorker** 在内存中做 L1 聚合：

```java
// 位置: server-core/.../worker/MetricsAggregateWorker.java
public class MetricsAggregateWorker extends AbstractWorker<Metrics> {
    
    private final MergableBufferedData<Metrics> mergeDataCache;
    private final long l1FlushPeriod = 500;  // 500ms 刷新
    
    @Override
    public void in(final Metrics metrics) {
        // 相同 entity + time bucket 的 metrics 合并
        mergeDataCache.accept(metrics);
    }
    
    private void flush() {
        // 定期将合并后的数据发送到下游
        mergeDataCache.read().forEach(nextWorker::in);
    }
}
```

### 7.2 批量写入

**MetricsPersistentWorker** 使用批量写入优化：

```java
// 位置: server-core/.../worker/MetricsPersistentWorker.java
// 周期: 默认 15 秒
// 批量大小: 可配置
```

## 8. 总结

SkyWalking 的 Trace 追踪机制核心要点：

1. **三层模型**: Trace → Segment → Span
2. **上下文传播**: 通过 Header 传递 TraceId/SegmentId
3. **Listener 模式**: TraceAnalyzer 通过监听器模式处理不同类型 Span
4. **OAL 驱动**: 基于观测分析语言自动生成指标计算逻辑
5. **流式处理**: L1 内存聚合 + L2 批量持久化
6. **采样支持**: 服务端采样 + Agent 端采样

---

*文档基于 SkyWalking OAP 服务端源码分析*
