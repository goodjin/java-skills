# RocketMQ 消息存储机制 CommitLog 源码分析

## 概述

CommitLog 是 RocketMQ 的核心存储组件，负责存储所有的消息数据。RocketMQ 采用混合存储机制，所有主题的消息都存储在同一个 CommitLog 文件中，通过 ConsumeQueue 作为索引来快速定位消息。本文深入分析其源码实现。

## 核心组件

### 1. CommitLog

**位置**: `store/src/main/java/org/apache/rocketmq/store/CommitLog.java`

消息存储的核心类，负责消息的写入和读取。

```java
public class CommitLog implements Swappable {
    // CommitLog 文件魔数
    public final static int MESSAGE_MAGIC_CODE = -626843481;
    // 文件结束空白魔数
    public final static int BLANK_MAGIC_CODE = -875286124;
    
    // MappedFile 队列
    protected final MappedFileQueue mappedFileQueue;
    
    // 消息存储服务
    protected final DefaultMessageStore defaultMessageStore;
    
    // 消息追加回调
    private final AppendMessageCallback appendMessageCallback;
    
    // 消息编码线程本地变量
    private final ThreadLocal<PutMessageThreadLocal> putMessageThreadLocal;
    
    // 写入锁
    protected final PutMessageLock putMessageLock;
    
    // Topic 队列锁（支持并发写入不同队列）
    protected final TopicQueueLock topicQueueLock;
}
```

### 2. DefaultMessageStore

**位置**: `store/src/main/java/org/apache/rocketmq/store/DefaultMessageStore.java`

消息存储的顶层入口，管理所有存储组件。

```java
public class DefaultMessageStore implements MessageStore {
    // CommitLog 存储
    private final CommitLog commitLog;
    
    // 消费队列存储
    private final ConsumeQueueInterface consumeQueue;
    
    // 索引服务
    private final IndexService indexService;
    
    // 消息存储配置
    private final MessageStoreConfig messageStoreConfig;
    
    // 存储检查点
    private final StoreCheckpoint storeCheckpoint;
    
    // 消息分配 MappedFile 服务
    private final AllocateMappedFileService allocateMappedFileService;
}
```

### 3. MappedFileQueue

**位置**: `store/src/main/java/org/apache/rocketmq/store/MappedFileQueue.java`

MappedFile 队列管理，负责文件的创建、读写。

```java
public class MappedFileQueue {
    // 存储路径
    private final String storePath;
    
    // 文件大小
    private final int fileSize;
    
    // MappedFile 列表
    private final CopyOnWriteArrayList<MappedFile> mappedFiles;
    
    // MappedFile 创建服务
    private final AllocateMappedFileService allocateMappedFileService;
    
    // 刷盘服务
    private FlushManager flushManager;
}
```

### 4. MappedFile

**位置**: `store/src/main/java/org/apache/rocketmq/store/logfile/MappedFile.java`

内存映射文件，代表一个 CommitLog 文件。

```java
public class MappedFile extends ReferenceResource {
    // 文件大小 (默认 1GB)
    private int fileSize;
    
    // 文件通道
    private FileChannel fileChannel;
    
    // 写入缓冲区 (Transient Store)
    private ByteBuffer writeBuffer;
    
    // 内存映射缓冲区
    private MappedByteBuffer mappedByteBuffer;
    
    // 文件起始偏移量
    private long fileFromOffset;
    
    // 文件名称
    private String fileName;
    
    // 最后更新时间
    private volatile long lastFlushedTime;
    
    // 是否正在写入
    private volatile boolean writing = true;
}
```

### 5. ConsumeQueue

**位置**: `store/src/main/java/org/apache/rocketmq/store/ConsumeQueue.java`

消费队列索引，存储消息在 CommitLog 中的位置。

```java
public class ConsumeQueue implements ConsumeQueueInterface {
    // Topic
    private final String topic;
    
    // 队列ID
    private final int queueId;
    
    // MappedFile 队列
    private final MappedFileQueue mappedFileQueue;
    
    // 消息过滤器
    private final MessageFilter messageFilter;
    
    // 单元大小 (20字节)
    // 8字节: CommitLog 偏移量
    // 4字节: 消息大小
    // 8字节: 消息 Tag HashCode
}
```

## 消息存储结构

### 1. 文件结构

```
┌─────────────────────────────────────────────────────────────────┐
│                    RocketMQ 存储目录结构                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ${ROCKETMQ_HOME}/store/                                        │
│  ├── commitlog/                    (消息存储目录)                 │
│  │   ├── 00000000000000000000     (第一个 CommitLog 文件)        │
│  │   ├── 00000000001073741824     (第二个 CommitLog 文件)        │
│  │   └── ...                                                    │
│  │                                                                │
│  ├── consumequeue/                  (消费队列目录)                │
│  │   ├── TopicA/                                                │
│  │   │   ├── 0/                    (Queue 0)                    │
│  │   │   │   ├── 00000000000000000000.cq                        │
│  │   │   │   └── 00000000000000001000.cq                        │
│  │   │   └── 1/                    (Queue 1)                    │
│  │   └── TopicB/                                                │
│  │                                                                │
│  ├── index/                        (索引目录)                    │
│  │   └── 20240101120000                                    │
│  │                                                                │
│  └── checkpoint                    (检查点文件)                   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 2. CommitLog 消息格式

```
┌─────────────────────────────────────────────────────────────────┐
│                    CommitLog 消息格式                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────────────────────────────────────────────────┐│
│  │ 4 bytes      │ 消息总长度 (4 bytes)                           ││
│  ├──────────────────────────────────────────────────────────────┤
│  │ 8 bytes      │ 物理偏移量 (CommitLog Offset)                 ││
│  ├──────────────────────────────────────────────────────────────┤
│  │ 4 bytes      │ 消息大小                                       ││
│  ├──────────────────────────────────────────────────────────────┤
│  │ 8 bytes      │ 消息魔数 (MESSAGE_MAGIC_CODE)                 ││
│  ├──────────────────────────────────────────────────────────────┤
│  │ 8 bytes      │ 消息体质时间戳                                 ││
│  ├──────────────────────────────────────────────────────────────┤
│  │ 8 bytes      │ 消息出生时间 (Born Timestamp)                 ││
│  ├──────────────────────────────────────────────────────────────┤
│  │ 8 bytes      │ 存储时间 (Store Timestamp)                   ││
│  ├──────────────────────────────────────────────────────────────┤
│  │ 4 bytes      │ 消息体 CRC                                     ││
│  ├──────────────────────────────────────────────────────────────┤
│  │ 1 byte       │ 消息系统标志 (SysFlag)                        ││
│  ├──────────────────────────────────────────────────────────────┤
│  │ 4 bytes      │ 队列ID                                         ││
│  ├──────────────────────────────────────────────────────────────┤
│  │ 4 bytes      │ 队列偏移 (Queue Offset)                       ││
│  ├──────────────────────────────────────────────────────────────┤
│  │ 4 bytes      │ 系统标志 (SysFlag)                           ││
│  ├──────────────────────────────────────────────────────────────┤
│  │ 8 bytes      │ 事务ID                                        ││
│  ├──────────────────────────────────────────────────────────────┤
│  │ 4 bytes      │ Topic 长度                                    ││
│  ├──────────────┼───────────────────────────────────────────────┤
│  │ n bytes      │ Topic 内容                                    ││
│  ├──────────────┼───────────────────────────────────────────────┤
│  │ 8 bytes      │ 消息ID (8字节唯一ID)                          ││
│  ├──────────────┼───────────────────────────────────────────────┤
│  │ 4 bytes      │ 索引列表长度                                   ││
│  ├──────────────┼───────────────────────────────────────────────┤
│  │ n bytes      │ 索引列表内容                                   ││
│  ├──────────────┼───────────────────────────────────────────────┤
│  │ 4 bytes      │ 属性长度                                      ││
│  ├──────────────┼───────────────────────────────────────────────┤
│  │ n bytes      │ 属性内容                                      ││
│  ├──────────────┼───────────────────────────────────────────────┤
│  │ 4 bytes      │ 消息体长度                                     ││
│  ├──────────────┼───────────────────────────────────────────────┤
│  │ n bytes      │ 消息体内容                                     ││
│  └──────────────┴───────────────────────────────────────────────┘│
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 3. ConsumeQueue 格式

```
┌─────────────────────────────────────────────────────────────────┐
│                    ConsumeQueue 索引格式                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  每条记录 20 字节:                                                │
│  ┌──────────────────────────────────────────────────────────────┐│
│  │ 8 bytes  │  CommitLog 物理偏移量                             ││
│  ├──────────┼───────────────────────────────────────────────────┤│
│  │ 4 bytes  │  消息大小                                         ││
│  ├──────────┼───────────────────────────────────────────────────┤
│  │ 8 bytes  │  消息 Tag HashCode (用于消息过滤)                 ││
│  └──────────┴───────────────────────────────────────────────────┘│
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## 消息写入流程

### 1. 整体写入流程

```
Producer 发送消息
    ↓
Broker 接收消息
    ↓
CommitLog.asyncPutMessage() 异步写入
    ↓
获取 TopicQueueLock (并发控制)
    ↓
获取 PutMessageLock (串行写入)
    ↓
MappedFileQueue.appendMessage() 追加消息
    ↓
消息编码 & 写入 ByteBuffer
    ↓
刷盘 (根据配置同步/异步)
    ↓
更新 ConsumeQueue 索引
    ↓
更新 IndexService 索引 (可选)
    ↓
返回 PutMessageResult
```

### 2. 消息写入核心实现

```java
// CommitLog.java
public PutMessageResult asyncPutMessage(final MessageExtBrokerInner msg) {
    // 1. 获取 Topic 对应的锁 (支持并发写入不同 Topic)
    TopicQueueLock topicQueueLock = this.getTopicQueueLock(msg.getTopic());
    topicQueueLock.lock();
    try {
        // 2. 获取写入锁
        this.putMessageLock.lock();
        try {
            // 3. 获取或创建 MappedFile
            MappedFile mappedFile = this.mappedFileQueue.getLastMappedFile(
                msg.getTopic(), msg.getTagsCode());
            
            // 4. 追加消息
            PutMessageResult result = mappedFile.appendMessage(
                msg, 
                this.appendMessageCallback,
                putMessageContext);
            
            // 5. 更新相关统计
            ...
            
            return result;
            
        } finally {
            this.putMessageLock.unlock();
        }
    } finally {
        topicQueueLock.unlock();
    }
}
```

### 3. 消息追加实现

```java
// DefaultAppendMessageCallback.doAppend()
public AppendMessageResult doAppend(final long fileFromOffset,
    final ByteBuffer byteBuffer, final int maxBlank, 
    final MessageExtBrokerInner msgInner) {
    
    // 1. 获取消息编码器
    PutMessageThreadLocal putMessageThreadLocal = 
        CommitLog.this.putMessageThreadLocal.get();
    
    // 2. 编码消息
    Encoder encoder = putMessageThreadLocal.encoder;
    ByteBuffer preEncodeBuffer = encoder.encode(msgInner);
    int totalSize = preEncodeBuffer.limit();
    
    // 3. 检查文件空间
    if (fileFromOffset + totalSize > 
        (fileFromOffset + maxBlank)) {
        return AppendMessageResult.PARTIAL_FLUSH;
    }
    
    // 4. 写入消息
    byteBuffer.put(preEncodeBuffer.array(), 0, totalSize);
    
    // 5. 返回结果
    AppendMessageResult result = new AppendMessageResult(
        AppendMessageStatus.PUT_OK);
    result.setWroteOffset(fileFromOffset + totalSize);
    result.setWroteBytes(totalSize);
    result.setLogicsOffset(msgInner.getQueueOffset());
    
    return result;
}
```

### 4. 消息刷盘机制

```java
// FlushManager 接口
public interface FlushManager {
    void flush(final CommitLog commitLog, final long flushCommitLogLeastPages);
    void wakeupFlush();
    void shutdown();
}

// 同步刷盘实现
class同步刷盘 {
    @Override
    public void flush(CommitLog commitLog, long flushLeastPages) {
        // 强制刷盘
        MappedFile mappedFile = commitLog.getMappedFileQueue()
            .getLastMappedFile();
        
        if (mappedFile != null) {
            // 刷新到磁盘
            mappedFile.flush(flushLeastPages);
        }
    }
}

// 异步刷盘实现
class AsyncFlush implements FlushManager {
    @Override
    public void flush(CommitLog commitLog, long flushLeastPages) {
        // 提交到线程池异步刷盘
        CommitLog.this.flushExecutor.submit(() -> {
            // 刷新到磁盘
            mappedFile.flush(flushLeastPages);
        });
    }
}
```

## 消息读取流程

### 1. 消息读取流程

```
Consumer 拉取消息请求
    ↓
DefaultMessageStore.getMessage()
    ↓
根据 Topic + QueueId 获取 ConsumeQueue
    ↓
从 ConsumeQueue 读取消息索引
    ↓
根据索引从 CommitLog 读取完整消息
    ↓
返回消息列表
```

### 2. 消息读取核心实现

```java
// DefaultMessageStore.getMessage()
public GetMessageResult getMessage(String group, String topic, 
    int queueId, long offset, int maxMsgNums, MessageFilter messageFilter) {
    
    // 1. 验证参数
    ...
    
    // 2. 获取 ConsumeQueue
    ConsumeQueueInterface consumeQueue = 
        this.getConsumeQueue(topic, queueId);
    
    // 3. 读取 ConsumeQueue 索引
    SelectMappedBufferResult result = 
        consumeQueue.getIndexBuffer(offset);
    
    // 4. 遍历读取消息
    List<MessageExt> msgList = new ArrayList<>();
    long nextOffset = offset;
    
    while (result != null && msgList.size() < maxMsgNums) {
        // 解析索引
        long commitLogOffset = 
            ByteBuffer.wrap(result.getBuffer()).getLong();
        int size = 
            ByteBuffer.wrap(result.getBuffer()).getInt();
        
        // 从 CommitLog 读取消息
        MessageExt messageExt = 
            this.commitLog.getMessage(commitLogOffset, size);
        
        // 消息过滤
        if (messageFilter.isMessageMatched(
            subscriptionData, messageExt)) {
            msgList.add(messageExt);
        }
        
        nextOffset++;
    }
    
    // 5. 构建返回结果
    GetMessageResult getResult = new GetMessageResult();
    getResult.setMsgList(msgList);
    getResult.setNextBeginOffset(nextOffset);
    
    return getResult;
}
```

### 3. 从 CommitLog 读取消息

```java
// CommitLog.getMessage()
public GetMessageResult getMessage(final long offset, final int size) {
    // 1. 根据偏移量计算 MappedFile
    MappedFile mappedFile = 
        this.mappedFileQueue.findMappedFileByOffset(offset);
    
    if (mappedFile == null) {
        return null;
    }
    
    // 2. 读取消息
    SelectMappedBufferResult result = 
        mappedFile.selectMappedBuffer((int)(offset - mappedFile.getFileFromOffset()));
    
    // 3. 解码消息
    MessageExt messageExt = MessageDecoder.decode(result.getByteBuffer());
    
    return messageExt;
}
```

## 消息索引机制

### 1. ConsumeQueue 索引

```java
// ConsumeQueue.java
public SelectMappedBufferResult getIndexBuffer(final long offset) {
    // 1. 找到对应的 MappedFile
    MappedFile mappedFile = 
        this.mappedFileQueue.findMappedFileByOffset(offset);
    
    if (mappedFile == null) {
        return null;
    }
    
    // 2. 计算在文件中的位置
    int pos = (int)(offset % this.mappedFileQueue.getMappedFileSize());
    
    // 3. 读取索引数据
    return mappedFile.selectMappedBuffer(pos);
}
```

### 2. IndexService 索引

```java
// IndexService.java
public void buildIndex(MessageExtBrokerInner msg) {
    // 1. 创建索引头
    IndexHeader indexHeader = this.indexFile.getIndexHeader();
    
    // 2. 构建索引项
    IndexFile indexFile = this.getIndexFile();
    
    // 消息 Key 索引
    String key = msg.getKeys();
    if (key != null && key.length() > 0) {
        String[] keys = key.split(MessageConst.KEY_SEPARATOR);
        for (String k : keys) {
            indexFile.addIndex(
                k,                      // 索引 Key
                msg.getCommitLogOffset(), // 物理偏移量
                msg.getStoreTimestamp()   // 存储时间
            );
        }
    }
    
    // 消息 Topic 索引
    indexFile.addIndex(
        msg.getTopic(),
        msg.getCommitLogOffset(),
        msg.getStoreTimestamp());
}
```

## 消息分发机制

### 1. 消息分发流程

```
消息写入 CommitLog 成功后
    ↓
CommitLogDispatcher 分发消息
    ↓
    ├── 1. 更新 ConsumeQueue 索引
    ├── 2. 更新 IndexService 索引 (如果启用)
    └── 3. 其他分发器 (HA, 事务等)
    ↓
通知消息到达
```

### 2. 分发器实现

```java
// CommitLogDispatcher.java
public interface CommitLogDispatcher {
    void dispatch(DispatchRequest request);
}

// ConsumeQueue 分发器
public class CommitLogDispatcherConsumeQueue implements CommitLogDispatcher {
    @Override
    public void dispatch(DispatchRequest request) {
        // 1. 为每个队列创建索引
        final int topicSysFlag = request.getSysFlag();
        final String topic = request.getTopic();
        final String tags = request.getTags();
        
        // 2. 写入 ConsumeQueue
        ConsumeQueueInterface consumeQueue = 
            this.defaultMessageStore.getConsumeQueue(
                topic, request.getQueueId());
        
        consumeQueue.putMessagePositionInfo(
            request.getCommitLogOffset(),
            request.getMsgSize(),
            tagsCode,
            request.getQueueOffset());
    }
}
```

## 消息恢复机制

### 1. 启动恢复

```java
// DefaultMessageStore.load()
public boolean load() {
    // 1. 加载 CommitLog
    boolean result = this.commitLog.load();
    
    // 2. 加载 ConsumeQueue
    result = result && this.consumeQueue.load();
    
    // 3. 加载 Index
    result = result && this.indexService.load();
    
    // 4. 恢复消息
    this.recover();
    
    return result;
}
```

### 2. 消息恢复

```java
// DefaultMessageStore.recover()
private void recover() {
    // 1. 恢复 ConsumeQueue
    this.recoverConsumeQueue();
    
    // 2. 恢复 CommitLog
    this.recoverCommitLog();
}

// 恢复 CommitLog
private void recoverCommitLog() {
    // 1. 获取最后一个 MappedFile
    MappedFile mappedFile = 
        mappedFileQueue.getLastMappedFile();
    
    // 2. 检查文件完整性
    int readSize = 0;
    while (true) {
        // 3. 读取消息
        SelectMappedBufferResult result = 
            mappedFile.selectMappedBuffer(readSize);
        
        if (result == null) {
            break;
        }
        
        // 4. 验证消息
        if (isMessageValid(result)) {
            readSize += getMessageSize(result);
        } else {
            // 消息损坏，截断文件
            mappedFile.setWrotePosition(readSize);
            break;
        }
    }
}
```

## 消息存储配置

### 1. 存储配置类

```java
// MessageStoreConfig.java
public class MessageStoreConfig {
    // 存储路径
    private String storePathRootDir = System.getProperty("user.home") + "/store";
    private String storePathCommitLog;
    
    // 文件大小
    private int mappedFileSizeCommitLog = 1024 * 1024 * 1024; // 1GB
    private int mappedFileSizeConsumeQueue = 300000 * 20; // 20字节 * 30万
    
    // 刷盘方式
    private FlushDiskType flushDiskType = FlushDiskType.ASYNC_FLUSH;
    private BrokerRole brokerRole = BrokerRole.SLAVE;
    
    // 刷盘页数
    private int flushCommitLogLeastPages = 4;
    private int flushConsumeQueueLeastPages = 2;
    
    // 消息最大大小
    private int maxMessageSize = 1024 * 1024 * 4; // 4M
    
    // 是否使用 mmap
    private boolean writeWithoutMmap = false;
}
```

### 2. 刷盘配置

```java
// 同步刷盘
flushDiskType = FlushDiskType.SYNC_FLUSH
// 适合对可靠性要求高的场景，每次写入都等待刷盘完成

// 异步刷盘 (默认)
flushDiskType = FlushDiskType.ASYNC_FLUSH
// 适合高吞吐量场景，批量刷盘提高性能
```

## 代码示例

### 1. 消息存储配置

```java
MessageStoreConfig config = new MessageStoreConfig();

// 设置存储路径
config.setStorePathRootDir("/data/rocketmq/store");
config.setStorePathCommitLog("/data/rocketmq/store/commitlog");

// 设置文件大小
config.setMappedFileSizeCommitLog(1024 * 1024 * 1024); // 1GB
config.setMappedFileSizeConsumeQueue(300000 * 20);

// 设置刷盘方式
config.setFlushDiskType(FlushDiskType.ASYNC_FLUSH);
config.setBrokerRole(BrokerRole.MASTER);

// 设置消息最大大小
config.setMaxMessageSize(1024 * 1024 * 4);

// 创建消息存储
DefaultMessageStore store = new DefaultMessageStore(config, ...);
```

### 2. 消息写入

```java
// Broker 端处理消息发送
public PutMessageResult putMessage(MessageExtBrokerInner msg) {
    // 1. 消息校验
    Validators.checkMessage(msg, this);
    
    // 2. 获取 topic 配置
    TopicConfig topicConfig = this.getTopicConfig(msg.getTopic());
    
    // 3. 设置队列偏移量
    msg.setQueueOffset(
        this.getMaxOffsetInQueue(msg.getTopic(), msg.getQueueId()));
    
    // 4. 设置消息ID
    msg.setMsgId(MessageClientIDSetter.createUniqID());
    
    // 5. 写入 CommitLog
    PutMessageResult result = this.commitLog.putMessage(msg);
    
    // 6. 如果是同步模式且成功，分发消息到 ConsumeQueue
    if (result.getPutMessageStatus() == PutMessageStatus.PUT_OK) {
        this.defaultMessageStore.doDispatch(dispatchRequest);
    }
    
    return result;
}
```

### 3. 消息读取

```java
// Broker 端处理消息拉取
public GetMessageResult getMessage(String group, String topic,
    int queueId, long offset, int maxMsgNums, int sysFlag) {
    
    // 1. 权限校验
    if (!this.brokerController.getBrokerConfig().isAuthorizeRead()) {
        ...
    }
    
    // 2. 获取消息
    GetMessageResult result = 
        this.messageStore.getMessage(
            group, topic, queueId, offset, maxMsgNums, 
            new DefaultMessageFilter(subscriptionData));
    
    return result;
}
```

## 流程图

```
┌─────────────────────────────────────────────────────────────────┐
│                      消息写入流程                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐                                                │
│  │  Producer   │ ──→ 发送消息                                    │
│  └──────┬───────┘                                                │
│         │                                                        │
│         ▼                                                        │
│  ┌──────────────────────────────────┐                          │
│  │ 1. 获取 TopicQueueLock           │                          │
│  │    (不同 Topic 可并发写入)         │                          │
│  └────────────────┬─────────────────┘                          │
│                   │                                              │
│                   ▼                                              │
│  ┌──────────────────────────────────┐                          │
│  │ 2. 获取 PutMessageLock           │                          │
│  │    (同一文件内串行写入)            │                          │
│  └────────────────┬─────────────────┘                          │
│                   │                                              │
│                   ▼                                              │
│  ┌──────────────────────────────────┐                          │
│  │ 3. 获取/创建 MappedFile          │                          │
│  │    (1GB 大文件)                  │                          │
│  └────────────────┬─────────────────┘                          │
│                   │                                              │
│                   ▼                                              │
│  ┌──────────────────────────────────┐                          │
│  │ 4. 消息编码 & 写入                │                          │
│  │    ByteBuffer                    │                          │
│  └────────────────┬─────────────────┘                          │
│                   │                                              │
│         ┌────────┴────────┐                                     │
│         │                 │                                      │
│    [同步刷盘]        [异步刷盘]                                   │
│         │                 │                                      │
│         ▼                 ▼                                      │
│  ┌──────────────┐  ┌──────────────┐                            │
│  │ 等待刷盘完成  │  │ 提交刷盘任务  │                            │
│  └──────┬───────┘  └──────┬───────┘                            │
│         │                 │                                      │
│         └────────┬────────┘                                     │
│                  ▼                                               │
│  ┌──────────────────────────────────┐                          │
│  │ 5. 分发消息到 ConsumeQueue       │                          │
│  │    (更新索引)                    │                          │
│  └────────────────┬─────────────────┘                          │
│                   │                                              │
│                   ▼                                              │
│  ┌──────────────────────────────────┐                          │
│  │ 6. 返回 PutMessageResult         │                          │
│  └──────────────────────────────────┘                          │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      消息读取流程                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐                                                │
│  │  Consumer    │ ──→ 拉取消息                                    │
│  └──────┬───────┘                                                │
│         │                                                        │
│         ▼                                                        │
│  ┌──────────────────────────────────┐                          │
│  │ 1. 根据 Topic+QueueId 获取       │                          │
│  │    ConsumeQueue                  │                          │
│  └────────────────┬─────────────────┘                          │
│                   │                                              │
│                   ▼                                              │
│  ┌──────────────────────────────────┐                          │
│  │ 2. 读取 ConsumeQueue 索引        │                          │
│  │    (20字节/条)                   │                          │
│  └────────────────┬─────────────────┘                          │
│                   │                                              │
│                   ▼                                              │
│  ┌──────────────────────────────────┐                          │
│  │ 3. 根据索引读取 CommitLog        │                          │
│  │    (获取完整消息)                │                          │
│  └────────────────┬─────────────────┘                          │
│                   │                                              │
│                   ▼                                              │
│  ┌──────────────────────────────────┐                          │
│  │ 4. 消息过滤 (Tag)                │                          │
│  └────────────────┬─────────────────┘                          │
│                   │                                              │
│                   ▼                                              │
│  ┌──────────────────────────────────┐                          │
│  │ 5. 返回消息列表                   │                          │
│  └──────────────────────────────────┘                          │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## 总结

RocketMQ 消息存储机制的设计要点:

1. **混合存储**: 所有主题消息存储在同一个 CommitLog，通过 ConsumeQueue 索引实现高效读取
2. **内存映射**: 使用 MappedByteBuffer 实现高效的顺序写入和随机读取
3. **文件预分配**: MappedFile 文件预分配，减少文件创建开销
4. **并发写入**: TopicQueueLock 支持不同 Topic 并发写入
5. **多种刷盘策略**: 支持同步和异步刷盘，平衡性能和数据可靠性
6. **索引机制**: ConsumeQueue 作为消息索引，IndexService 支持消息检索
7. **消息恢复**: 启动时自动检查和恢复损坏的消息
8. **高吞吐量**: 异步写入 + 批量刷盘，提供高性能消息存储
