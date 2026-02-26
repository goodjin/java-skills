# RocketMQ Consumer 消费者源码分析

## 概述

Consumer（消费者）是 RocketMQ 消息消费的核心组件，负责从 Broker 拉取消息并进行处理。RocketMQ 支持推（Push）和拉（Pull）两种消费模式，本文深入分析其源码实现。

## 核心类

### 1. DefaultMQPushConsumer

**位置**: `client/src/main/java/org/apache/rocketmq/client/consumer/DefaultMQPushConsumer.java`

推模式消费者，主要特点是消息自动推送给消费者。

```java
public class DefaultMQPushConsumer extends ClientConfig implements MQPushConsumer {
    // 消费者组
    private String consumerGroup;
    
    // 消息模型：集群模式 / 广播模式
    private MessageModel messageModel = MessageModel.CLUSTERING;
    
    // 消费起始位置
    private ConsumeFromWhere consumeFromWhere = 
        ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;
    
    // 订阅关系
    private Map<String, String> subscription = new HashMap<>();
    
    // 消息监听器
    private MessageListener messageListener;
    
    // 队列分配策略
    private AllocateMessageQueueStrategy allocateMessageQueueStrategy;
    
    // 消费线程数
    private int consumeThreadMin = 20;
    private int consumeThreadMax = 20;
    
    // 消费并发最大跨度
    private int consumeConcurrentlyMaxSpan = 2000;
    
    // 拉取阈值
    private int pullThresholdForQueue = 1000;
    private int pullThresholdSizeForQueue = 100;
    
    // 内部实现
    protected final transient DefaultMQPushConsumerImpl defaultMQPushConsumerImpl;
}
```

### 2. DefaultMQPushConsumerImpl

**位置**: `client/src/main/java/org/apache/rocketmq/client/impl/consumer/DefaultMQPushConsumerImpl.java`

推模式消费者的核心实现类。

```java
public class DefaultMQPushConsumerImpl extends MQConsumerInner {
    private final DefaultMQPushConsumer defaultMQPushConsumer;
    
    // 消费消息服务
    private ConsumeMessageService consumeMessageService;
    
    // 拉取消息服务
    private PullMessageService pullMessageService;
    
    // 消费进度存储
    private OffsetStore offsetStore;
    
    // 负载均衡实现
    private RebalanceImpl rebalanceImpl;
    
    // 消费者内部状态
    private ServiceState serviceState = ServiceState.CREATE_JUST;
}
```

### 3. DefaultLitePullConsumer

**位置**: `client/src/main/java/org/apache/rocketmq/client/consumer/DefaultLitePullConsumer.java`

拉模式消费者，主动从 Broker 拉取消息。

```java
public class DefaultLitePullConsumer extends ClientConfig implements LitePullConsumer {
    private String consumerGroup;
    private MessageModel messageModel = MessageModel.CLUSTERING;
    private ConsumeFromWhere consumeFromWhere = 
        ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;
    
    // 主题订阅
    private Set<String> subscription = new HashSet<>();
    
    // 自动拉取
    private boolean autoPoll = true;
    
    // 拉取线程数
    private int pullThreadNums = 20;
    
    // 拉取批量大小
    private int pullBatchSize = 10;
    
    // 消费者实现
    private DefaultLitePullConsumerImpl defaultLitePullConsumerImpl;
}
```

### 4. PullAPIWrapper

**位置**: `client/src/main/java/org/apache/rocketmq/client/impl/consumer/PullAPIWrapper.java`

拉取消息的封装类，负责与 Broker 通信获取消息。

```java
public class PullAPIWrapper {
    private final MQClientInstance mQClientFactory;
    private final String consumerGroup;
    
    // 记录从哪个 Broker 拉取消息
    private ConcurrentMap<MessageQueue, AtomicLong> pullFromWhichNodeTable;
    
    // 拉取消息方法
    public PullResult pullMessage(String brokerAddr, 
        PullMessageRequestHeader requestHeader, 
        PullCallback pullCallback, ...) 
}
```

### 5. RebalanceImpl

**位置**: `client/src/main/java/org/apache/rocketmq/client/impl/consumer/RebalanceImpl.java`

负责消费者负载均衡，将消息队列分配给消费者实例。

```java
public abstract class RebalanceImpl {
    // 消费者组
    protected String consumerGroup;
    
    // 消息模型
    protected MessageModel messageModel;
    
    // 分配策略
    protected AllocateMessageQueueStrategy allocateMessageQueueStrategy;
    
    // Topic 订阅数据
    protected ConcurrentMap<String, SubscriptionData> subscriptionInner;
    
    // 分配到的消息队列
    protected ConcurrentMap<MessageQueue, ProcessQueue> processQueueTable;
    
    // 消息队列变更监听器
    protected MessageQueueListener messageQueueListener;
}
```

### 6. ProcessQueue

**位置**: `client/src/main/java/org/apache/rocketmq/client/impl/consumer/ProcessQueue.java`

处理队列，管理拉取到的消息。

```java
public class ProcessQueue {
    // 消息树Map，用于存储消息
    private final TreeMap<Long, MessageExt> msgTreeMap;
    
    // 消息数量
    private AtomicInteger msgCount = new AtomicInteger();
    
    // 消息总大小
    private AtomicLong msgSize = new AtomicLong();
    
    // 队列最大偏移量
    private long queueMaxOffset;
    
    // 队列最小偏移量
    private long queueMinOffset;
    
    // 是否被丢弃
    private volatile boolean dropped = false;
    
    // 最后拉取时间
    private long lastPullTimestamp;
    
    // 最后消费时间
    private long lastConsumeTimestamp;
}
```

## 消息消费流程

### 1. Push 模式消费流程

```
Broker
    ↓
PullMessageService (拉取服务)
    ↓
PullAPIWrapper.pullMessage() (网络拉取)
    ↓
ProcessQueue.putMessage() (存入处理队列)
    ↓
ConsumeMessageService.submitConsumeRequest() (提交消费请求)
    ↓
MessageListener.onMessage() (业务处理)
    ↓
OffsetStore.updateOffset() (更新消费进度)
```

### 2. 消息拉取流程

```java
// PullMessageService 核心方法
public void pullMessage(final PullRequest pullRequest) {
    // 1. 获取消费者处理队列
    ProcessQueue processQueue = pullRequest.getProcessQueue();
    
    // 2. 如果队列被丢弃，停止拉取
    if (processQueue.isDropped()) {
        return;
    }
    
    // 3. 更新拉取时间
    processQueue.setLastPullTimestamp(System.currentTimeMillis());
    
    // 4. 获取消费线程池
    ...
    
    // 5. 执行拉取
    this.pullAPIWrapper.pullMessage(
        brokerAddr,
        requestHeader,
        new PullCallback() {
            @Override
            public void onSuccess(PullResult pullResult) {
                // 处理拉取结果
                handlePullResult(pullRequest, pullResult);
                
                // 提交消费请求
                submitConsumeRequest(pullRequest, pullResult);
            }
            
            @Override
            public void onException(Throwable e) {
                // 拉取失败，延迟重新拉取
                scheduleNextPull(pullRequest, delay);
            }
        },
        ...
    );
}
```

### 3. 拉取结果处理

```java
private void handlePullResult(final PullRequest request, final PullResult pullResult) {
    switch (pullResult.getPullStatus()) {
        case FOUND:
            // 有新消息
            List<MessageExt> msgs = pullResult.getMsgFoundList();
            long nextBeginOffset = pullResult.getNextBeginOffset();
            
            // 更新 ProcessQueue
            ProcessQueue pq = request.getProcessQueue();
            pq.putMessage(msgs);
            
            // 更新拉取偏移量
            pq.setLastPullOffset(nextBeginOffset);
            break;
            
        case NO_NEW_MSG:
            // 没有新消息
            break;
            
        case NO_MATCHED_MSG:
            // 没有匹配的消息
            break;
            
        case OFFSET_ILLEGAL:
            // 偏移量非法
            break;
    }
}
```

### 4. 消费请求提交

```java
// ConsumeMessageConcurrentlyService
public void submitConsumeRequest(final PullRequest pullRequest, 
    final PullResult pullResult) {
    
    // 1. 获取消息列表
    List<MessageExt> msgs = pullResult.getMsgFoundList();
    
    // 2. 批量提交消费请求
    int consumeBatchSize = Math.min(msgs.size(), 
        this.defaultMQPushConsumer.getConsumeMessageBatchMaxSize());
    
    for (int i = 0; i < msgs.size(); i += consumeBatchSize) {
        List<MessageExt> msgThis = msgs.subList(i, i + consumeBatchSize);
        
        // 3. 提交到消费线程池
        ConsumeRequest consumeRequest = new ConsumeRequest(msgThis, ...);
        this.defaultMQPushConsumer.getConsumeExecutor().submit(consumeRequest);
    }
}
```

### 5. 消息消费执行

```java
// ConsumeRequest 实现
class ConsumeRequest implements Runnable {
    private final List<MessageExt> msgs;
    private final ProcessQueue processQueue;
    private final MessageQueue messageQueue;
    
    @Override
    public void run() {
        // 1. 调用消息监听器处理消息
        ConsumeConcurrentlyStatus status = 
            messageListener.consumeMessage(mesgs, context);
        
        // 2. 处理消费结果
        if (status == ConsumeConcurrentlyStatus.CONSUME_SUCCESS) {
            // 消费成功
            offsetStore.updateOffset(messageQueue, 
                processQueue.getMaxOffset(), false);
        } else if (status == ConsumeConcurrentlyStatus.RECONSUME_LATER) {
            // 消费失败，稍后重试
            // 消息会被重新投递
        }
        
        // 3. 处理完成，清理 ProcessQueue
        processQueue.removeMessage(msgs);
    }
}
```

## 负载均衡

### 1. 负载均衡流程

```
RebalanceService (定时任务)
    ↓
RebalanceImpl.doRebalance()
    ↓
对每个 Topic 进行重新分配
    ↓
allocateMessageQueue() (分配队列)
    ↓
更新 ProcessQueueTable
    ↓
创建/移除 PullRequest
```

### 2. 队列分配策略

```java
// 默认平均分配策略
public class AllocateMessageQueueAveragately implements AllocateMessageQueueStrategy {
    @Override
    public List<MessageQueue> allocate(String consumerGroup, 
        List<MessageQueue> mqAll, List<String> cidAll) {
        
        List<MessageQueue> result = new ArrayList<>();
        
        if (mqAll.isEmpty() || cidAll.isEmpty()) {
            return result;
        }
        
        // 平均分配
        int averageSize = mqAll.size() / cidAll.size();
        int mod = mqAll.size() % cidAll.size();
        
        int currentIndex = cidAll.indexOf(currentCID);
        
        // 计算起始位置
        int startIndex = (mod > currentIndex) ? 
            currentIndex * (averageSize + 1) : 
            currentIndex * averageSize + mod;
        
        // 分配队列
        int size = (mod > currentIndex) ? 
            averageSize + 1 : averageSize;
        
        for (int i = 0; i < size; i++) {
            result.add(mqAll.get((startIndex + i) % mqAll.size()));
        }
        
        return result;
    }
}
```

### 3. 消费进度管理

```java
// OffsetStore 接口
public interface OffsetStore {
    // 读取消费进度
    long readOffset(final MessageQueue mq, final ReadOffsetType type);
    
    // 更新消费进度
    void updateOffset(final MessageQueue mq, final long offset, final boolean increaseOnly);
    
    // 持久化消费进度
    void persistAll(Set<MessageQueue> mqs);
    
    // 移除消费进度
    void removeOffset(MessageQueue mq);
}
```

**实现类**:
- **LocalFileOffsetStore**: 本地文件存储
- **RemoteBrokerOffsetStore**: Broker 端存储

## 消息处理模式

### 1. 并发消费

```java
// MessageListenerConcurrently
public interface MessageListenerConcurrently extends MessageListener {
    ConsumeConcurrentlyStatus consumeMessage(
        List<MessageExt> msgs, ConsumeConcurrentlyContext context);
}

// 使用示例
consumer.registerMessageListener(new MessageListenerConcurrently() {
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(
        List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        
        for (MessageExt msg : msgs) {
            System.out.println("Receive: " + new String(msg.getBody()));
        }
        
        // 返回消费成功
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
});
```

### 2. 顺序消费

```java
// MessageListenerOrderly
public interface MessageListenerOrderly extends MessageListener {
    ConsumeOrderlyStatus consumeMessage(
        List<MessageExt> msgs, ConsumeOrderlyContext context);
}

// 使用示例
consumer.registerMessageListener(new MessageListenerOrderly() {
    @Override
    public ConsumeOrderlyStatus consumeMessage(
        List<MessageExt> msgs, ConsumeOrderlyContext context) {
        
        // 队列加锁，保证同一队列消息顺序消费
        context.setAutoCommit(false);
        
        for (MessageExt msg : msgs) {
            // 处理消息
        }
        
        // 手动提交
        return ConsumeOrderlyStatus.SUCCESS;
    }
});
```

### 3. 拉取消费 (LitePull)

```java
// 使用 LitePullConsumer
DefaultLitePullConsumer consumer = new DefaultLitePullConsumer("group");
consumer.subscribe("topic", "*");
consumer.start();

// 手动拉取
while (running) {
    List<MessageExt> msgs = consumer.poll();
    for (MessageExt msg : msgs) {
        System.out.println(new String(msg.getBody()));
    }
}
```

## 消息ACK机制

### 1. 消息确认

```java
// ConsumeMessageConcurrentlyService 处理消费结果
public ProcessResult consumeMessageInner(
    List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
    
    ConsumeConcurrentlyStatus status = 
        this.messageListener.consumeMessage(msgs, context);
    
    if (status == ConsumeConcurrentlyStatus.CONSUME_SUCCESS) {
        return new ProcessResult(ProcessResult.SUCCESS);
    }
    
    if (status == ConsumeConcurrentlyStatus.RECONSUME_LATER) {
        // 消费失败，设置重试
        if (checkReconsumeTimes(msgs)) {
            // 延迟重试
            this.sendMessagestoBackQueue(msgs);
            return new ProcessResult(ProcessResult.SUCCESS);
        }
    }
    
    return new ProcessResult(ProcessResult.SUCCESS);
}
```

### 2. 消费进度持久化

```java
// RemoteBrokerOffsetStore 持久化
public void persistAll(Set<MessageQueue> mqs) {
    // 构建消息
    HashMap<MessageQueue, Long> offsets = new HashMap<>();
    
    for (MessageQueue mq : mqs) {
        Long offset = offsetTable.get(mq);
        if (offset != null) {
            offsets.put(mq, offset);
        }
    }
    
    // 调用 Broker 接口更新
    this.mQClientFactory.getMQClientAPIImpl().updateConsumerOffset(
        brokerAddr, requestHeader, commitOffset);
}
```

## 流量控制

### 1. 队列级流量控制

```java
// PullMessageService 拉取前检查
public void pullMessage(final PullRequest pullRequest) {
    ProcessQueue pq = pullRequest.getProcessQueue();
    
    // 1. 检查队列消息数量
    if (pq.getMsgCount().get() > 
        defaultMQPushConsumer.getPullThresholdForQueue()) {
        // 暂停拉取
        return;
    }
    
    // 2大小
    if (pq.getMsgSize().get() > 
        default. 检查队列消息MQPushConsumer.getPullThresholdSizeForQueue() * 1024 * 1024) {
        // 暂停拉取
        return;
    }
}
```

### 2. 消费进度控制

```java
// 延迟提交消费请求
public void submitConsumeRequest(PullRequest pullRequest, PullResult pullResult) {
    List<MessageExt> msgs = pullResult.getMsgFoundList();
    
    // 检查消息数量
    if (msgs.size() > 
        defaultMQPushConsumer.getConsumeMessageBatchMaxSize()) {
        // 分批提交
        for (int i = 0; i < msgs.size(); 
             i += defaultMQPushConsumer.getConsumeMessageBatchMaxSize()) {
            // 提交消费请求
        }
    } else {
        // 直接提交
    }
}
```

## 代码示例

### 1. Push 模式消费者

```java
// 创建消费者
DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("my-consumer-group");
consumer.setNamesrvAddress("localhost:9876");
consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

// 订阅主题
consumer.subscribe("my-topic", "*");

// 注册消息监听器
consumer.registerMessageListener(new MessageListenerConcurrently() {
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(
        List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        
        for (MessageExt msg : msgs) {
            System.out.println("Receive: " + new String(msg.getBody()));
        }
        
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
});

// 启动消费者
consumer.start();
```

### 2. 顺序消息消费

```java
consumer.registerMessageListener(new MessageListenerOrderly() {
    @Override
    public ConsumeOrderlyStatus consumeMessage(
        List<MessageExt> msgs, ConsumeOrderlyContext context) {
        
        context.setAutoCommit(false);
        
        for (MessageExt msg : msgs) {
            System.out.println("Order: " + msg.getKeys() + 
                ", Body: " + new String(msg.getBody()));
        }
        
        return ConsumeOrderlyStatus.SUCCESS;
    }
});
```

### 3. 广播模式消费

```java
// 设置为广播模式
consumer.setMessageModel(MessageModel.BROADCASTING);

// 广播模式下，每个消费者实例都会消费全量消息
consumer.registerMessageListener(new MessageListenerConcurrently() {
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(
        List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        
        System.out.println("Receive: " + msgs.size() + " messages");
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
});
```

### 4. 拉取模式消费

```java
// 创建拉取消费者
DefaultLitePullConsumer consumer = new DefaultLitePullConsumer("group");
consumer.subscribe("my-topic", "*");
consumer.setAutoPoll(false);  // 手动拉取
consumer.start();

try {
    while (running) {
        // 手动拉取消息
        List<MessageExt> msgs = consumer.poll(5000);
        for (MessageExt msg : msgs) {
            System.out.println(new String(msg.getBody()));
        }
        
        // 提交消费进度
        consumer.commit();
    }
} finally {
    consumer.shutdown();
}
```

### 5. 自定义负载均衡

```java
// 使用自定义分配策略
consumer.setAllocateMessageQueueStrategy(
    new AllocateMessageQueueByMachineRoom());
    
// 机房级别分配策略
public class AllocateMessageQueueByMachineRoom 
    implements AllocateMessageQueueStrategy {
    
    private Set<String> consumerMachines = new HashSet<>();
    
    @Override
    public List<MessageQueue> allocate(String consumerGroup,
        List<MessageQueue> mqAll, List<String> cidAll) {
        
        List<MessageQueue> result = new ArrayList<>();
        
        for (MessageQueue mq : mqAll) {
            String brokerName = mq.getBrokerName();
            
            // 只分配本机房的消息队列
            if (isSameMachineRoom(brokerName)) {
                result.add(mq);
            }
        }
        
        return result;
    }
}
```

## 流程图

```
┌─────────────────────────────────────────────────────────────────┐
│                     Consumer 消费流程                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                    PullMessageService                       ││
│  │                   (定时拉取消息服务)                          ││
│  └──────────────────────────┬──────────────────────────────────┘│
│                             │                                    │
│                             ▼                                    │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                 PullAPIWrapper.pullMessage()                ││
│  │                    (从 Broker 拉取消息)                       ││
│  └──────────────────────────┬──────────────────────────────────┘│
│                             │                                    │
│                             ▼                                    │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                   ProcessQueue.putMessage()                 ││
│  │                    (消息存入处理队列)                         ││
│  └──────────────────────────┬──────────────────────────────────┘│
│                             │                                    │
│                             ▼                                    │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │              ConsumeMessageService.submitConsumeRequest()   ││
│  │                   (提交消费请求到线程池)                        ││
│  └──────────────────────────┬──────────────────────────────────┘│
│                             │                                    │
│                             ▼                                    │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                    ConsumeRequest.run()                     ││
│  │               (消费者线程执行消息处理)                         ││
│  └──────────────────────────┬──────────────────────────────────┘│
│                             │                                    │
│                             ▼                                    │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │               MessageListener.onMessage()                  ││
│  │                    (业务逻辑处理)                            ││
│  └──────────────────────────┬──────────────────────────────────┘│
│                             │                                    │
│              ┌──────────────┴──────────────┐                    │
│              │         结果处理              │                    │
│              ▼                              ▼                    │
│   ┌──────────────────┐            ┌──────────────────┐          │
│   │ CONSUME_SUCCESS  │            │ RECONSUME_LATER  │          │
│   │   更新消费进度    │            │   延迟重试投递    │          │
│   └──────────────────┘            └──────────────────┘          │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      负载均衡流程                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐                                               │
│  │ Rebalance    │ ──→ 定时触发 (每20秒)                         │
│  │ Service      │                                               │
│  └──────┬───────┘                                               │
│         │                                                       │
│         ▼                                                       │
│  ┌──────────────────────────────────┐                           │
│  │  1. 从 NameServer 获取 Topic    │                           │
│  │     路由信息                      │                           │
│  └────────────────┬─────────────────┘                           │
│                   │                                             │
│                   ▼                                             │
│  ┌──────────────────────────────────┐                           │
│  │  2. 获取消费者组内所有消费者       │                           │
│  └────────────────┬─────────────────┘                           │
│                   │                                             │
│                   ▼                                             │
│  ┌──────────────────────────────────┐                           │
│  │  3. 分配消息队列策略              │                           │
│  │     (平均分配/机房分配/Hash分配)   │                           │
│  └────────────────┬─────────────────┘                           │
│                   │                                             │
│                   ▼                                             │
│  ┌──────────────────────────────────┐                           │
│  │  4. 更新本地 ProcessQueue 表     │                           │
│  └────────────────┬─────────────────┘                           │
│                   │                                             │
│         ┌────────┴────────┐                                    │
│         │                 │                                    │
│    [新增队列]          [移除队列]                                 │
│         │                 │                                    │
│         ▼                 ▼                                    │
│  ┌──────────────┐  ┌──────────────┐                             │
│  │ 启动拉取任务  │  │ 停止拉取任务  │                             │
│  └──────────────┘  └──────────────┘                             │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## 总结

RocketMQ Consumer 的设计要点:

1. **双消费模式**: Push 模式自动推送，Pull 模式主动拉取
2. **负载均衡**: 自动将消息队列分配给消费者实例，支持多种分配策略
3. **消费进度管理**: 支持本地和 Broker 端两种存储方式
4. **消息顺序性**: 支持并发和顺序两种消费模式
5. **流量控制**: 多级流量控制，防止消息积压
6. **重试机制**: 消费失败后自动重试，支持延迟投递
7. **消息ACK**: 消费成功后自动更新进度，失败后可以重试
8. **广播消费**: 支持广播模式，所有实例消费全量消息
