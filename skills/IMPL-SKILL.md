# Java 开源项目代码实现经验总结

> 基于 HikariCP、Netty、Dubbo、Disruptor、Fastjson2、Sentinel、RxJava、Seata、Shardingsphere、Arthas 等约 100 个开源项目的代码分析总结

## 目录

1. [常见设计模式及其使用场景](#1-常见设计模式及其使用场景)
2. [代码技巧和最佳实践](#2-代码技巧和最佳实践)
3. [结构设计方法](#3-结构设计方法)
4. [代码规范建议](#4-代码规范建议)

---

## 1. 常见设计模式及其使用场景

### 1.1 责任链模式 (Chain of Responsibility)

**核心思想**：将请求沿着处理者链传递，每个处理者负责特定功能，链上的每个节点都可以决定是否处理请求或传递给下一个节点。

**使用场景**：
- 处理器的顺序执行（如 Web 过滤器、拦截器）
- 多阶段数据处理（如 SQL 解析 → 优化 → 执行）
- 事件传播机制

**典型应用**：
```java
// Netty - ChannelPipeline 中的 ChannelHandler 链
ChannelPipeline pipeline = ch.pipeline();
pipeline.addLast("decoder", new ByteToMessageDecoder());
pipeline.addLast("encoder", new MessageToByteEncoder<>());
pipeline.addLast("handler", new BusinessHandler());

// Dubbo - Filter 链
ProtocolFilterWrapper.buildInvokerChain(invoker, REF_KEY, constants);

// Sentinel - ProcessorSlotChain
ProcessorSlotChain chain = new ProcessorSlotChain();
chain.addFirst(new FlowSlot());
chain.addFirst(new DegradeSlot());
```

---

### 1.2 装饰器模式 (Decorator)

**核心思想**：动态地给对象添加额外功能，比继承更灵活。

**使用场景**：
- 为对象动态添加功能
- 需要组合多个行为
- 扩展第三方类的功能

**典型应用**：
```java
// Netty - ChannelHandler 包装器
public class CombinedChannelDuplexHandler<I extends ChannelInboundHandler, 
                                           O extends ChannelOutboundHandler> 
    extends ChannelDuplexHandler

// Dubbo - ProtocolFilterWrapper 添加过滤功能
public class ProtocolFilterWrapper implements Protocol {
    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) {
        return new ProtocolFilterWrapper<>(protocol.export(invoker));
    }
}

// Fastjson2 - Filter 链
JSONWriter writer = JSONWriter.of(features);
writer.write(object);
```

---

### 1.3 策略模式 (Strategy)

**核心思想**：定义一系列算法，把它们一个个封装起来，使它们可以互相替换。

**使用场景**：
- 多种算法/策略可选
- 需要在运行时切换算法
- 消除大量条件语句

**典型应用**：
```java
// HikariCP - 多种等待策略
public interface WaitStrategy {
    long waitFor(long sequence, Sequence cursor, 
                 Sequence barrier, SequenceDependentCallback callback);
}
enum BlockingWaitStrategy implements WaitStrategy {}
enum BusySpinWaitStrategy implements WaitStrategy {}
enum YieldingWaitStrategy implements WaitStrategy {}

// Dubbo - 多种集群容错策略
Cluster cluster = Cluster.getCluster("failover"); // 失败自动切换
Cluster cluster = Cluster.getCluster("failfast"); // 快速失败

// Disruptor - 等待策略
WaitStrategy waitStrategy = new BlockingWaitStrategy();
WaitStrategy waitStrategy = new PhasedBackoffWaitStrategy();
```

---

### 1.4 工厂模式 (Factory)

**核心思想**：创建对象的最佳方式，将对象的创建与使用分离。

**使用场景**：
- 创建过程复杂
- 需要根据配置创建不同实现
- 统一管理对象创建

**典型应用**：
```java
// Dubbo - SPI 扩展加载
ExtensionLoader<Protocol> loader = ExtensionLoader.getExtensionLoader(Protocol.class);
Protocol dubboProtocol = loader.getExtension("dubbo");

// Netty - ChannelFactory 创建 Channel
ChannelFactory<NioSocketChannel> factory = new NioSocketChannelFactory();

// Fastjson2 - Reader/Writer 工厂
JSONReader reader = JSONReaderFactory.getJSONReader(input);
JSONWriter writer = JSONWriterFactory.getJSONWriter(output);
```

---

### 1.5 适配器模式 (Adapter)

**核心思想**：将不兼容的接口转换为兼容的接口。

**使用场景**：
- 整合第三方库
- 兼容旧版本 API
- 统一不同实现的外观

**典型应用**：
```java
// Netty - ChannelHandlerAdapter 提供默认实现
public class ByteToMessageDecoder extends ChannelInboundHandlerAdapter {
    // 提供默认的空实现，子类只需重写需要的方法
}

// Fastjson2 - 兼容 Fastjson 1.x API
public class JSON extends JSON2 {
    // 适配器方法
    public static <T> T parseObject(String text, Class<T> clazz) {
        return JSON2.parseObject(text, clazz);
    }
}
```

---

### 1.6 观察者模式 (Observer)

**核心思想**：定义对象间的一对多依赖关系，当对象状态改变时，所有依赖它的对象都会收到通知。

**使用场景**：
- 事件监听系统
- 消息订阅发布
- 状态变化通知

**典型应用**：
```java
// RxJava - Observable/Observer
Observable<String> observable = Observable.just("Hello");
observable.subscribe(new Observer<String>() {
    @Override
    public void onNext(String s) { System.out.println(s); }
    @Override
    public void onError(Throwable e) {}
    @Override
    public void onComplete() {}
});

// Sentinel - 指标采集观察者
MetricFetcher fetcher = new MetricFetcher();
fetcher.addListener((metrics) -> { /* 处理指标 */ });
```

---

### 1.7 代理模式 (Proxy)

**核心思想**：为其他对象提供一种代理以控制对这个对象的访问。

**使用场景**：
- 远程代理
- 虚拟代理（延迟加载）
- 保护代理（权限控制）
- 智能引用

**典型应用**：
```java
// Seata - DataSourceProxy 数据源代理
public class DataSourceProxy extends DataSource implements Wrapper {
    private final ConnectionFactory connectionFactory;
    @Override
    public Connection getConnection() {
        return connectionFactory.getConnection();
    }
}

// Arthas - 动态代理方法拦截
Enhancer.enhance(classLoader, className, classInfo);
```

---

### 1.8 建造者模式 (Builder)

**核心思想**：将一个复杂对象的构建与它的表示分离，使得同样的构建过程可以创建不同的表示。

**使用场景**：
- 构造方法参数过多
- 对象创建过程复杂
- 希望创建不可变对象

**典型应用**：
```java
// MyBatis - ResultMap 构建
ResultMap resultMap = new ResultMap.Builder(configuration, id, type, mappings)
    .build();

// Caffeine - Cache 构建
Cache<String, Object> cache = Caffeine.newBuilder()
    .maximumSize(10000)
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .build();
```

---

### 1.9 享元模式 (Flyweight)

**核心思想**：运用共享技术有效地支持大量细粒度对象。

**使用场景**：
- 大量相似对象
- 对象创建代价高
- 需要缓存复用

**典型应用**：
```java
// HikariCP - PoolEntry 复用
public class PoolEntry {
    final Connection connection;
    // 连接被复用而非每次创建新的
}

// Disruptor - 预分配事件对象
private void fill(EventFactory<E> eventFactory) {
    for (int i = 0; i < bufferSize; i++) {
        entries[BUFFER_PAD + i] = eventFactory.newInstance();
    }
}
```

---

### 1.10 门面模式 (Facade)

**核心思想**：提供一个统一的接口，用来访问子系统中的一群接口。

**使用场景**：
- 简化复杂系统调用
- 提供统一入口
- 解耦子系统

**典型应用**：
```java
// Sentinel - 统一入口
public class Sph {
    // 提供简洁的 API，隐藏内部复杂逻辑
    public static Entry entry(String name) {
        return CtSph.entryWithType(name, ResourceNodeType.Common);
    }
}
```

---

## 2. 代码技巧和最佳实践

### 2.1 性能优化技巧

#### 2.1.1 CPU 缓存行对齐

**问题**：伪共享（False Sharing）导致缓存行失效

**解决方案**：使用填充避免不同变量的缓存行竞争
```java
// Disruptor - 避免伪共享
abstract class RingBufferPad {
    protected byte p000, p001, p002, p003, p004, p005, p006, p007;
    protected byte p008, p009, p010, p011, p012, p013, p014, p015;
    // ... 更多填充
}

// HikariCP - RingBufferFields
private static final int BUFFER_PAD = 32;
this.entries = (E[]) new Object[bufferSize + 2 * BUFFER_PAD];
```

#### 2.1.2 无锁设计

**问题**：锁竞争导致性能瓶颈

**解决方案**：使用 CAS 和 ThreadLocal 减少锁
```java
// HikariCP - ConcurrentBag 使用 ThreadLocal
final var list = threadLocalList.get();
for (var i = list.size() - 1; i >= 0; i--) {
    final var entry = list.remove(i);
    if (bagEntry.compareAndSet(STATE_NOT_IN_USE, STATE_IN_USE)) {
        return entry;
    }
}
```

#### 2.1.3 内存预分配

**问题**：运行时分配内存导致 GC 压力

**解决方案**：预先分配对象，复用对象
```java
// Disruptor - RingBuffer 预分配
private void fill(EventFactory<E> eventFactory) {
    for (int i = 0; i < bufferSize; i++) {
        entries[BUFFER_PAD + i] = eventFactory.newInstance();
    }
}
```

#### 2.1.4 零拷贝

**问题**：数据在内存中多次拷贝

**解决方案**：使用组合缓冲区直接操作多个缓冲区
```java
// Netty - CompositeByteBuf
CompositeByteBuf composite = Unpooled.compositeBuffer();
composite.addComponent(true, byteBuf1, byteBuf2);
// 直接操作，无需拷贝
```

#### 2.1.5 位运算优化

**问题**：取模运算开销大

**解决方案**：使用 2 的幂次方和位运算
```java
// Disruptor - 快速取模
this.indexMask = bufferSize - 1; // bufferSize 必须是 2 的幂
return entries[BUFFER_PAD + (int) (sequence & indexMask)];
```

---

### 2.2 并发处理技巧

#### 2.2.1 引用计数

**问题**：手动内存管理复杂

**解决方案**：使用引用计数自动管理
```java
// Netty - 引用计数
ReferenceCountUtil.release(msg);
// 或使用 SimpleChannelInboundHandler 自动释放
public abstract class SimpleChannelInboundHandler<I> 
    extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        boolean release = true;
        try {
            if (acceptInboundMessage(msg)) {
                I imsg = (I) msg;
                channelRead0(ctx, imsg);
            } else {
                release = false;
                ctx.fireChannelRead(msg);
            }
        } finally {
            if (release) {
                ReferenceCountUtil.release(msg);
            }
        }
    }
}
```

#### 2.2.2 内存池

**问题**：频繁分配/释放内存

**解决方案**：使用内存池复用内存块
```java
// Netty - 内存池
PooledByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;
ByteBuf buf = allocator.directBuffer(1024);
```

#### 2.2.3 滑动窗口算法

**问题**：需要统计一段时间内的请求情况

**解决方案**：使用滑动窗口进行平滑统计
```java
// Sentinel - 滑动窗口
private final MetricBucket[] data;
private final int windowLengthInMs;

public long getSum(long timeMillis) {
    int idx = calculateIdx(timeMillis);
    MetricBucket bucket = data[idx];
    return bucket.pass() + bucket.block();
}
```

#### 2.2.4 令牌桶算法

**问题**：需要限制速率

**解决方案**：使用令牌桶实现平滑限流
```java
// Sentinel - 令牌桶
double newestToken = maxToken - (curTime - lastFillTime) * rate;
if (newestToken >= 1) {
    lastToken = Math.max(0, newestToken);
    return true;
}
return false;
```

---

### 2.3 高级技巧

#### 2.3.1 SPI 机制

**问题**：需要可插拔的扩展

**解决方案**：使用 SPI 动态加载扩展
```java
// Dubbo - SPI 加载
@SPI("dubbo")
public interface Protocol {
    <T> Exporter<T> export(Invoker<T> invoker);
    <T> Invoker<T> refer(Class<T> type, URL url);
}

// META-INF/services/org.apache.dubbo.rpc.Protocol
dubbo=org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol
```

#### 2.3.2 URL 驱动设计

**问题**：配置分散，难以传递

**解决方案**：统一使用 URL 传递配置
```java
// Dubbo - URL 驱动
URL url = new URL("dubbo", "localhost", 20880, 
    "/org.example.DemoService?timeout=3000&retries=2");
```

#### 2.3.3 字节码增强

**问题**：运行时需要动态修改类行为

**解决方案**：使用 ASM/Javassist 字节码修改
```java
// Arthas - 运行时增强
Enhancer.enhance(classLoader, className, classInfo);

// Fastjson2 - 编译期/运行期生成代码
```

#### 2.3.4 异步编程

**问题**：同步阻塞影响性能

**解决方案**：使用 CompletableFuture/响应式编程
```java
// Dubbo - 异步调用
CompletableFuture<String> future = invoker.invokeMethodAsync();
future.thenApply(result -> process(result));

// RxJava - 响应式编程
Observable.from(list)
    .subscribeOn(Schedulers.io())
    .observeOn(Schedulers.computation())
    .map(this::process)
    .subscribe(this::handle);
```

---

## 3. 结构设计方法

### 3.1 模块化架构

**原则**：高内聚、低耦合

**实践**：
```
// Dubbo 模块划分
dubbo-common      - 公共模块（URL、配置、工具类）
dubbo-rpc        - RPC 相关（协议、调用）
dubbo-cluster    - 集群相关（负载均衡、容错）
dubbo-registry   - 注册中心
dubbo-monitor    - 监控

// ShardingSphere 模块划分
features         - 功能特性（分片、读写分离）
infra            - 基础设施
kernel           - 内核
proxy            - 数据库代理
```

### 3.2 分层设计

**典型分层**：
```
接口层 (Interface)
    ↓
服务层 (Service)
    ↓
领域层 (Domain)
    ↓
基础设施层 (Infrastructure)
```

### 3.3 接口设计原则

**原则**：
- 单一职责：接口职责单一
- 最小接口：接口方法尽量少
- 可扩展：使用策略模式支持扩展

```java
// 清晰接口定义示例
public interface WaitStrategy {
    long waitFor(long sequence, Sequence cursor, 
                 Sequence barrier, SequenceDependentCallback callback);
    void signalAllWhenBlocking();
}
```

### 3.4 不可变对象设计

**优点**：
- 线程安全
- 易于缓存
- 易于推理

```java
// Dubbo - URL 不可变对象
public final class URL implements Serializable {
    private final String protocol;
    private final String host;
    private final int port;
    private final String path;
    // 无 setter，所有字段 final
}
```

---

## 4. 代码规范建议

### 4.1 命名规范

**类名**：
- 使用完整单词，避免缩写（如 `Connection` 而非 `Conn`）
- 接口以 `Handler`、`Factory`、`Processor` 等结尾
- 抽象类以 `Abstract` 开头

**方法名**：
- 动宾结构（如 `getConnection()`, `processRequest()`）
- 布尔方法以 `is`、`has`、`can` 开头

**常量**：
- 全大写下划线分隔（如 `MAX_POOL_SIZE`）

### 4.2 包结构规范

**按功能分包**：
```
com.example.project
├── common        # 公共组件
├── config        # 配置
├── domain        # 领域模型
├── service       # 服务层
├── repository    # 数据访问层
├── controller    # 控制器层
└── util          # 工具类
```

**按模块分包**（大型项目）：
```
io.netty.channel    # Channel 相关
io.netty.buffer     # 缓冲区相关
io.netty.handler    # 处理器相关
```

### 4.3 注释规范

**类注释**：
```java
/**
 * 连接池核心类，负责管理数据库连接的生命周期。
 * 
 * @author authorName
 * @since 1.0
 */
public class HikariPool {
}
```

**方法注释**：
```java
/**
 * 从连接池获取一个连接。
 *
 * @return 可用的数据库连接
 * @throws SQLException 如果获取失败
 */
public Connection getConnection() throws SQLException {
}
```

### 4.4 代码格式

**建议**：
- 缩进：4 空格
- 大括号：K&R 风格
- 行长度：不超过 120 字符
- 空格：操作符前后有空格

### 4.5 异常处理

**原则**：
- 具体的异常类型
- 有意义的错误信息
- 避免吞掉异常

```java
// 推荐
try {
    connection = dataSource.getConnection();
} catch (SQLException e) {
    throw new DataSourceException("Failed to get connection from " + dataSource, e);
}

// 避免
try {
    connection = dataSource.getConnection();
} catch (Exception e) {
    // 永远不要这样
}
```

### 4.6 日志规范

**原则**：
- 使用合适的日志级别
- 包含上下文信息
- 避免过度日志

```java
// 错误日志
logger.error("Failed to process request: {}", requestId, e);

// 调试日志
logger.debug("Connection acquired from pool: {}", poolEntry);
```

---

## 附录：项目分析维度参考

### 核心类结构
- 核心类/接口的职责
- 类之间的关系
- 关键字段和方法

### 设计模式
- 常用设计模式的应用场景
- 模式带来的优势

### 代码技巧
- 性能优化技巧
- 并发处理技巧
- 内存管理技巧

### 代码规范
- 命名规范
- 注释规范
- 包结构

### 值得学习的地方
- 核心技术的理解
- 工程实践
