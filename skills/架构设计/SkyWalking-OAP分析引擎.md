# SkyWalking OAP 分析引擎

> 本文档深入分析 SkyWalking OAP (Observability Analysis Platform) 服务端的分析引擎架构

## 1. OAP 架构概述

### 1.1 整体架构

OAP 是 SkyWalking 的后端分析平台，负责接收、存储和分析来自 Agent 的遥测数据。

```
┌─────────────────────────────────────────────────────────────────────┐
│                          OAP Server                                  │
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                    Data Ingestion                            │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │   │
│  │  │  gRPC    │  │  HTTP    │  │  Kafka   │  │  RabbitMQ│  │   │
│  │  │ Receiver │  │ Receiver │  │ Fetcher  │  │ Fetcher  │  │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                              ↓                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                    Analysis Engine                           │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │   │
│  │  │ Trace        │  │ Metrics      │  │ Log          │     │   │
│  │  │ Analyzer     │  │ Stream       │  │ Analyzer     │     │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘     │   │
│  │         ↓                ↓                  ↓                │   │
│  │  ┌─────────────────────────────────────────────────────┐   │   │
│  │  │              SourceDispatcher (OAL 引擎)            │   │   │
│  │  └─────────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                              ↓                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                    Storage Layer                             │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │   │
│  │  │Elastic   │  │ BanyanDB │  │ MySQL    │  │ PGSQL    │  │   │
│  │  │search    │  │          │  │          │  │          │  │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                    Query Layer                               │   │
│  │  ┌──────────────────────────────────────────────────────┐  │   │
│  │  │              GraphQL Query API                        │  │   │
│  │  └──────────────────────────────────────────────────────┘  │   │
│  └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

## 2. 核心组件

### 2.1 模块系统

SkyWalking 使用自定义模块系统：

```
┌────────────────────────────────────────────────────┐
│              ModuleDefine (模块定义)                │
│  - 声明模块名                                       │
│  - 声明依赖的其他模块                                │
│  - 声明提供的服务                                   │
└──────────────────────┬───────────────────────────┘
                       ↓
┌────────────────────────────────────────────────────┐
│            ModuleProvider (模块提供者)               │
│  - 实现具体功能                                     │
│  - 配置管理                                        │
└────────────────────────────────────────────────────┘
```

**示例 - CoreModule**:

```java
// 位置: server-core/.../CoreModule.java
public class CoreModule extends ModuleDefine {
    
    @Override
    public Class[] services() {
        return new Class[] {
            SourceReceiver.class,     // Source 接收器
            ConfigService.class,       // 配置服务
            NamingControl.class,       // 命名控制
            ModelCreator.class,        // 存储模型创建
            // ...
        };
    }
}
```

### 2.2 数据流架构

```
                    ┌─────────────────┐
                    │   Source Data   │
                    │ (Trace/Metrics) │
                    └────────┬────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│                      Analysis Pipeline                           │
│                                                                     │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐        │
│  │   Receiver  │───▶│   Analyzer  │───▶│  Processor  │        │
│  │ (gRPC/HTTP) │    │  (解析/计算)  │    │ (聚合/持久化) │        │
│  └─────────────┘    └─────────────┘    └─────────────┘        │
│                            ↓                                     │
│                    ┌─────────────┐                               │
│                    │  Dispatcher │                               │
│                    │  (OAL 引擎)  │                               │
│                    └─────────────┘                               │
└─────────────────────────────────────────────────────────────────┘
                             ↓
                    ┌─────────────┐
                    │   Storage   │
                    └─────────────┘
```

## 3. 源码分析

### 3.1 Source 接收机制

**SourceReceiver** 是所有数据的统一入口：

```java
// 位置: server-core/.../source/SourceReceiver.java
public interface SourceReceiver {
    void receive(ISource source);  // 接收 Source 数据
    DispatcherDetectorListener getDispatcherDetectorListener();
}
```

**SourceReceiverImpl** 实现：

```java
// 位置: server-core/.../source/SourceReceiverImpl.java
public class SourceReceiverImpl implements SourceReceiver {
    
    private final DispatcherManager dispatcherManager;
    private final SourceDecoratorManager sourceDecoratorManager;
    
    @Override
    public void receive(ISource source) {
        // 分发到所有注册的 SourceDispatcher
        dispatcherManager.forward(source);
    }
    
    // 扫描并注册所有 Dispatcher
    public void scan() throws IOException {
        // 从 classpath 扫描所有 SourceDispatcher 实现
        // 自动注册到 DispatcherManager
    }
}
```

### 3.2 流处理机制

#### StreamProcessor 接口

```java
// 位置: server-core/.../analysis/StreamProcessor.java
public interface StreamProcessor<STREAM> {
    void in(STREAM stream);  // 流处理入口
}
```

#### MetricsStreamProcessor

**位置**: `server-core/.../analysis/worker/MetricsStreamProcessor.java`

```java
public class MetricsStreamProcessor implements StreamProcessor<Metrics> {
    
    // 单例
    private final static MetricsStreamProcessor PROCESSOR = new MetricsStreamProcessor();
    
    // 入口 Workers 表
    private Map<Class<? extends Metrics>, MetricsAggregateWorker> entryWorkers = new HashMap<>();
    
    // 持久化 Workers
    private List<MetricsPersistentWorker> persistentWorkers = new ArrayList<>();
    
    @Override
    public void in(Metrics metrics) {
        // 找到对应的聚合 Worker 并发送
        MetricsAggregateWorker worker = entryWorkers.get(metrics.getClass());
        if (worker != null) {
            worker.in(metrics);
        }
    }
    
    // 创建 Metrics 处理流程
    public void create(ModuleDefineHolder moduleDefineHolder,
                       Stream stream,
                       Class<? extends Metrics> metricsClass) {
        
        // 1. 创建存储 DAO
        IMetricsDAO metricsDAO = storageDAO.newMetricsDao(...);
        
        // 2. 创建分钟级持久化 Worker
        MetricsPersistentWorker minutePersistentWorker = 
            minutePersistentWorker(...);
        
        // 3. 创建小时/天级持久化 Worker (可选)
        if (supportDownSampling) {
            MetricsPersistentWorker hourPersistentWorker = 
                downSamplingWorker(...);
        }
        
        // 4. 创建远程接收 Worker
        MetricsRemoteWorker remoteWorker = 
            new MetricsRemoteWorker(...);
        
        // 5. 创建聚合 Worker (L1)
        MetricsAggregateWorker aggregateWorker = 
            new MetricsAggregateWorker(...);
        
        // 注册入口
        entryWorkers.put(metricsClass, aggregateWorker);
    }
}
```

### 3.3 Worker 体系

```
                    ┌─────────────────────┐
                    │ AbstractWorker<T>   │
                    │  - in(T input)      │
                    └──────────┬──────────┘
                               │
        ┌──────────────────────┼──────────────────────┐
        ↓                      ↓                      ↓
┌───────────────┐    ┌─────────────────┐    ┌─────────────────┐
│MetricsAggregate│    │MetricsPersistent│    │ MetricsRemote   │
│   Worker      │    │    Worker       │    │    Worker       │
│ (L1 聚合)      │    │  (持久化)       │    │  (远程接收)      │
└───────────────┘    └─────────────────┘    └─────────────────┘
        ↓                      ↓
┌───────────────┐    ┌─────────────────┐
│MetricsTrans   │    │ AlarmNotify     │
│   Worker      │    │    Worker       │
│ (L2 转发)      │    │  (告警通知)     │
└───────────────┘    └─────────────────┘
```

#### MetricsAggregateWorker (L1 聚合)

```java
// 位置: server-core/.../worker/MetricsAggregateWorker.java
public class MetricsAggregateWorker extends AbstractWorker<Metrics> {
    
    private final MergableBufferedData<Metrics> mergeDataCache;
    private final BatchQueue<Metrics> l1Queue;
    private final long l1FlushPeriod = 500;  // 500ms
    
    @Override
    public void in(final Metrics metrics) {
        // 加入队列
        l1Queue.produce(metrics);
    }
    
    // L1 聚合处理
    private void onWork(final List<Metrics> metricsList) {
        for (final Metrics metrics : metricsList) {
            // 相同 entity + time bucket 合并
            mergeDataCache.accept(metrics);
        }
        // 定期刷新到下游
        flush();
    }
}
```

#### MetricsPersistentWorker (持久化)

```java
// 位置: server-core/.../worker/MetricsPersistentWorker.java
public class MetricsPersistentWorker extends AbstractWorker<Metrics> {
    
    // 时间分桶
    private Map<Long, Map<String, Metrics>> currentBatch;
    
    // 批量写入周期
    private static final long FLUSH_PERIOD = 15;  // 15秒
    
    @Override
    public void in(Metrics metrics) {
        // 按 timeBucket 和 entityId 索引
        long timeBucket = metrics.getTimeBucket();
        String entityId = metrics.getEntityId();
        
        // 相同时间桶+实体的数据合并
        Metrics existing = currentBatch.computeIfAbsent(timeBucket, k -> new HashMap())
            .get(entityId);
        
        if (existing != null) {
            // 合并
            existing.merge(metrics);
        } else {
            currentBatch.get(timeBucket).put(entityId, metrics);
        }
        
        // 检查是否需要刷新
        if (shouldFlush(timeBucket)) {
            flush(timeBucket);
        }
    }
}
```

## 4. OAL 引擎

### 4.1 OAL 概述

OAL (Observability Analysis Language) 是 SkyWalking 的领域特定语言，用于定义指标计算规则。

```oal
// 示例 OAL 脚本
// 计算服务响应时间
from All
  .filter(span.kind == SERVER)
  .responseTime as responseTime
  .avg() as avgResponseTime;

// 计算服务调用次数
from ServiceRelationServer
  .count() as serviceCallCount;
```

### 4.2 OAL 编译流程

```
┌─────────────┐    ┌──────────────┐    ┌─────────────┐    ┌──────────────┐
│  OAL 脚本   │───▶│  语法解析    │───▶│  代码生成   │───▶│  编译加载    │
│  (.oal)     │    │ (ANTLR)     │    │ (模板)      │    │  (ClassLoader)│
└─────────────┘    └──────────────┘    └─────────────┘    └──────────────┘
```

### 4.3 SourceDispatcher 生成

OAL 编译器会为每个指标生成对应的 SourceDispatcher：

```java
// OAL 生成的代码示例
// 指标: ServiceResponseTime
public class ServiceResponseTimeDispatcher implements SourceDispatcher<ServiceRelation> {
    
    @Override
    public void dispatch(ServiceRelation source) {
        // 构建 ServiceResponseTime 指标
        ServiceResponseTime metrics = new ServiceResponseTime();
        metrics.setTimeBucket(source.getTimeBucket());
        metrics.setServiceId(source.getSourceServiceId());
        metrics.setServiceName(source.getSourceServiceName());
        metrics.setResponseTime(source.getLatency());
        
        // 发送到流处理器
        MetricsStreamProcessor.getInstance().in(metrics);
    }
}
```

## 5. 分析引擎详解

### 5.1 Trace 分析流程

```
┌─────────────────────────────────────────────────────────────────┐
│                    Trace Analysis Pipeline                       │
│                                                                  │
│  1. Receiver Layer                                              │
│     ┌──────────────────────────────────────────────────────┐  │
│     │  TraceSegmentReportServiceHandler (gRPC)              │  │
│     │  TraceSegmentReportHandler (HTTP)                     │  │
│     └──────────────────────────────────────────────────────┘  │
│                            ↓                                     │
│  2. Parser Layer                                               │
│     ┌──────────────────────────────────────────────────────┐  │
│     │  SegmentParserServiceImpl                             │  │
│     │    └── TraceAnalyzer.doAnalysis()                    │  │
│     └──────────────────────────────────────────────────────┘  │
│                            ↓                                     │
│  3. Listener Layer                                             │
│     ┌──────────────────────────────────────────────────────┐  │
│     │  SegmentAnalysisListener (Segment 级别)              │  │
│     │  EntryAnalysisListener   (Entry Span)                 │  │
│     │  ExitAnalysisListener   (Exit Span)                  │  │
│     │  LocalAnalysisListener  (Local Span)                 │  │
│     │  NetworkAddressAliasMappingListener                  │  │
│     │  EndpointDependencyBuilder                            │  │
│     └──────────────────────────────────────────────────────┘  │
│                            ↓                                     │
│  4. Source Layer                                               │
│     ┌──────────────────────────────────────────────────────┐  │
│     │  SourceReceiver.receive()                            │  │
│     │    └── Segment, Service, ServiceInstance, Endpoint   │  │
│     └──────────────────────────────────────────────────────┘  │
│                            ↓                                     │
│  5. Dispatch Layer                                             │
│     ┌──────────────────────────────────────────────────────┐  │
│     │  DispatcherManager.forward()                         │  │
│     │    └── [OAL 生成的 Dispatcher 列表]                  │  │
│     └──────────────────────────────────────────────────────┘  │
│                            ↓                                     │
│  6. Metrics Layer                                              │
│     ┌──────────────────────────────────────────────────────┐  │
│     │  MetricsStreamProcessor.in()                         │  │
│     │    └── MetricsAggregateWorker (L1)                   │  │
│     │        └── MetricsPersistentWorker (L2)              │  │
│     └──────────────────────────────────────────────────────┘  │
│                            ↓                                     │
│  7. Storage Layer                                              │
│     ┌──────────────────────────────────────────────────────┐  │
│     │  StorageDAO / IMetricsDAO                            │  │
│     │    └── Elasticsearch / BanyanDB / ...               │  │
│     └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 指标计算流程

```
┌─────────────────────────────────────────────────────────────────┐
│                    Metrics Calculation Flow                      │
│                                                                  │
│  Source (Segment/ServiceInstance/Endpoint)                       │
│         ↓                                                        │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ SourceDispatcher.dispatch(source)                        │   │
│  │                                                            │   │
│  │ // 伪代码: from ServiceRelationServer.count()             │   │
│  │ ServiceRelationServerMetrics metrics = new ...();         │   │
│  │ metrics.setTimeBucket(source.getTimeBucket());            │   │
│  │ metrics.setServiceId(source.getSourceServiceId());       │   │
│  │ metrics.increment();  // 计数 +1                           │   │
│  └─────────────────────────────────────────────────────────┘   │
│         ↓                                                        │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ MetricsStreamProcessor.in(metrics)                      │   │
│  │                                                            │   │
│  │ // 找到对应 MetricsClass 的 Worker                       │   │
│  │ MetricsAggregateWorker worker = entryWorkers.get(...);   │   │
│  │ worker.in(metrics);                                      │   │
│  └─────────────────────────────────────────────────────────┘   │
│         ↓                                                        │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ MetricsAggregateWorker.in()                             │   │
│  │                                                            │   │
│  │ // L1 内存聚合 (500ms 周期)                                │   │
│  │ mergeDataCache.accept(metrics);                           │   │
│  │ flush();  // 发送到 L2                                    │   │
│  └─────────────────────────────────────────────────────────┘   │
│         ↓                                                        │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ MetricsPersistentWorker.in()                             │   │
│  │                                                            │   │
│  │ // 按 TimeBucket 缓存                                     │   │
│  │ currentBatch.get(timeBucket).put(entityId, metrics);     │   │
│  │                                                            │   │
│  │ // 15秒 周期批量写入                                       │   │
│  │ if (shouldFlush()) { batchWrite(); }                    │   │
│  └─────────────────────────────────────────────────────────┘   │
│         ↓                                                        │
│  Storage (Elasticsearch / BanyanDB / ...)                       │
└─────────────────────────────────────────────────────────────────┘
```

## 6. 核心类一览

### 6.1 接收层

| 类名 | 职责 |
|------|------|
| `TraceSegmentReportServiceHandler` | gRPC trace 数据接收 |
| `TraceSegmentReportHandler` | HTTP trace 数据接收 |
| `ISegmentParserService` | Segment 解析服务接口 |
| `SegmentParserServiceImpl` | Segment 解析服务实现 |

### 6.2 分析层

| 类名 | 职责 |
|------|------|
| `TraceAnalyzer` | Trace 入口分析器 |
| `SegmentParserListenerManager` | 监听器管理器 |
| `SegmentAnalysisListener` | Segment 级别分析 |
| `EntryAnalysisListener` | Entry Span 分析 |
| `ExitAnalysisListener` | Exit Span 分析 |

### 6.3 核心处理层

| 类名 | 职责 |
|------|------|
| `SourceReceiver` | Source 统一入口 |
| `DispatcherManager` | Dispatcher 分发管理 |
| `SourceDispatcher` | OAL 生成的处理器基类 |
| `MetricsStreamProcessor` | Metrics 流处理器 |

### 6.4 Worker 层

| 类名 | 职责 |
|------|------|
| `AbstractWorker` | Worker 基类 |
| `MetricsAggregateWorker` | L1 内存聚合 |
| `MetricsPersistentWorker` | 持久化处理 |
| `MetricsTransWorker` | 跨级转发 (分钟→小时) |
| `MetricsRemoteWorker` | 远程接收 |
| `AlarmNotifyWorker` | 告警通知 |

## 7. 数据存储

### 7.1 多级降采样

SkyWalking 支持多级时间粒度存储：

```
┌─────────────────────────────────────────────────────────────┐
│                    Time Bucket Hierarchy                     │
│                                                              │
│  Minute (1分钟) ──────────────────────────────────────▶ Time │
│       │                                                       │
│       │  MetricsPersistentMinWorker                         │
│       │                                                     │
│       ↓                                                      │
│  Hour (1小时)  ──────────────────────────────────────▶ Time  │
│       │                                                       │
│       │  MetricsPersistentWorker (DownSampling)             │
│       │                                                     │
│       ↓                                                      │
│  Day (1天)    ──────────────────────────────────────▶ Time   │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 7.2 存储模型

```java
// 位置: server-core/.../storage/model/Model.java
public class Model {
    private Class<? extends StorageData> dataClass;  // 数据类
    private String name;                              // 模型名
    private DownSampling downsamping;                 // 降采样级别
    private boolean isTimeRelativeID;                 // 是否使用时间相关 ID
}
```

## 8. 配置与扩展

### 8.1 模块配置

```yaml
# 核心模块
core:
  default:
    # 数据 TTL
    metricsDataTTL: 3    # 天
    recordDataTTL: 7     # 天
    
    # 降采样配置
    enableDownSampling: true
    
    # 搜索标签配置
    searchableTracesTags: http.method,http.status_code,error

# 分析模块
agent-analyzer:
  default:
    # 采样率 (0-10000)
    sampleRate: 10000
    # 错误 Trace 强制采样
    forceSampleErrorSegment: true

# 存储模块  
storage:
  elasticsearch:
    clusterNodes: localhost:9200
    indexShardsNumber: 2
    indexReplicasNumber: 1
```

### 8.2 自定义 OAL

用户可以通过添加自定义 OAL 脚本来扩展指标：

```oal
// custom.oal
from HTTPRequest
  .filter(uri like "/api/%")
  .responseTime as apiLatency
  .avg() as avgApiLatency;
```

## 9. 总结

SkyWalking OAP 分析引擎的核心架构：

1. **模块化设计**: 使用 ModuleDefine/ModuleProvider 架构
2. **流式处理**: 基于 Stream Processor 的流式计算
3. **多级聚合**: L1 内存聚合 + L2 批量持久化
4. **OAL 驱动**: DSL 声明式指标定义，自动代码生成
5. **Dispatcher 模式**: 基于 Scope 的事件分发
6. **时间分桶**: 支持分钟/小时/天多级降采样
7. **可扩展存储**: 支持多种存储后端

---

*文档基于 SkyWalking OAP 服务端源码分析*
