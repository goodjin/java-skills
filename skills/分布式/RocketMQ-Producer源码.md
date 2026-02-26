# RocketMQ Producer 生产者源码分析

## 概述

Producer（生产者）是 RocketMQ 消息发送的核心组件，负责将消息发送到 Broker。本文深入分析其源码实现，涵盖核心类、发送流程、消息队列选择、容错机制等。

## 核心类

### 1. DefaultMQProducer

**位置**: `client/src/main/java/org/apache/rocketmq/client/producer/DefaultMQProducer.java`

这是生产者的主要入口类，提供了多种消息发送方法。

```java
public class DefaultMQProducer extends ClientConfig implements MQProducer {
    protected final transient DefaultMQProducerImpl defaultMQProducerImpl;
    
    // 生产者组
    private String producerGroup;
    
    // 消息发送超时时间（毫秒）
    private int sendMsgTimeout = 3000;
    
    // 同步发送失败重试次数
    private int retryTimesWhenSendFailed = 2;
    
    // 异步发送失败重试次数
    private int retryTimesWhenSendAsyncFailed = 2;
    
    // 是否在发送失败时重试另一个 Broker
    private boolean retryAnotherBrokerWhenNotStoreOK = false;
    
    // 最大消息大小（默认4M）
    private int maxMessageSize = 1024 * 1024 * 4;
    
    // 消息追踪
    private TraceDispatcher traceDispatcher = null;
    
    // 批量消息累积器
    private boolean autoBatch = false;
    private ProduceAccumulator produceAccumulator = null;
    
    // 异步发送背压控制
    private boolean enableBackpressureForAsyncMode = false;
    private int backPressureForAsyncSendNum = 1024;
    private int backPressureForAsyncSendSize = 100 * 1024 * 1024;
}
```

**主要发送方法**:
- `send(Message msg)` - 同步发送
- `send(Message msg, SendCallback sendCallback)` - 异步发送
- `sendOneway(Message msg)` - 单向发送（不等待响应）
- `send(Message msg, MessageQueueSelector selector, Object arg)` - 消息队列选择发送

### 2. DefaultMQProducerImpl

**位置**: `client/src/main/java/org/apache/rocketmq/client/impl/producer/DefaultMQProducerImpl.java`

这是生产者的核心实现类，包含所有发送逻辑。

```java
public class DefaultMQProducerImpl implements MQProducerInner {
    private final DefaultMQProducer defaultMQProducer;
    
    // Topic 发布信息表
    private final ConcurrentMap<String, TopicPublishInfo> topicPublishInfoTable;
    
    // 消息发送钩子列表
    private final ArrayList<SendMessageHook> sendMessageHookList;
    
    // 客户端实例
    private MQClientInstance mQClientFactory;
    
    // 故障策略
    private MQFaultStrategy mqFaultStrategy;
    
    // 异步发送线程池
    private ExecutorService asyncSenderExecutor;
    
    // 背压信号量
    private Semaphore semaphoreAsyncSendNum;
    private Semaphore semaphoreAsyncSendSize;
}
```

### 3. TopicPublishInfo

**位置**: `client/src/main/java/org/apache/rocketmq/client/impl/producer/TopicPublishInfo.java`

存储 Topic 的发布信息，包括消息队列列表等。

```java
public class TopicPublishInfo {
    // 是否从 NameServer 成功获取路由信息
    private boolean orderTopic = false;
    private boolean haveTopicRouterInfo = false;
    
    // 消息队列列表
    private List<MessageQueue> messageQueueList;
    
    // 当前选择的消息队列索引（轮询）
    private volatile int sendWhichQueue = 0;
    
    // Topic 路由信息
    private TopicRouteData topicRouteData;
}
```

## 发送流程

### 1. 整体发送流程

```
Application
    ↓
DefaultMQProducer.send()
    ↓
DefaultMQProducerImpl.sendDefaultImpl()
    ↓
tryToFindTopicPublishInfo() // 获取 Topic 路由信息
    ↓
selectOneMessageQueue() // 选择消息队列
    ↓
sendKernelImpl() // 发送到 Broker
    ↓
MQClientAPIImpl.sendMessage() // 网络通信
    ↓
Broker 存储消息
    ↓
返回 SendResult
```

### 2. 核心发送方法实现

```java
private SendResult sendDefaultImpl(
    Message msg,
    final CommunicationMode communicationMode,
    final SendCallback sendCallback,
    final long timeout
) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
    
    // 1. 检查 Producer 状态
    this.makeSureStateOK();
    Validators.checkMessage(msg, this.defaultMQProducer);
    
    // 2. 获取 Topic 路由信息
    TopicPublishInfo topicPublishInfo = this.tryToFindTopicPublishInfo(msg.getTopic());
    
    if (topicPublishInfo != null && topicPublishInfo.ok()) {
        // 3. 计算重试次数
        int timesTotal = communicationMode == CommunicationMode.SYNC 
            ? 1 + this.defaultMQProducer.getRetryTimesWhenSendFailed() : 1;
        
        // 4. 遍历重试
        for (int times = 0; times < timesTotal; times++) {
            // 5. 选择消息队列
            MessageQueue mqSelected = this.selectOneMessageQueue(
                topicPublishInfo, lastBrokerName, resetIndex);
            
            // 6. 发送消息到 Broker
            sendResult = this.sendKernelImpl(msg, mq, communicationMode, 
                sendCallback, topicPublishInfo, curTimeout);
            
            // 7. 根据发送模式处理结果
            switch (communicationMode) {
                case ASYNC:
                    return null;  // 异步直接返回
                case ONEWAY:
                    return null;  // 单向发送直接返回
                case SYNC:
                    if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
                        // 失败重试
                        if (this.defaultMQProducer.isRetryAnotherBrokerWhenNotStoreOK()) {
                            continue;
                        }
                    }
                    return sendResult;
            }
        }
    }
    
    throw new MQClientException("No route info of this topic...");
}
```

### 3. 获取 Topic 路由信息

```java
private TopicPublishInfo tryToFindTopicPublishInfo(final String topic) {
    // 1. 从本地缓存获取
    TopicPublishInfo topicPublishInfo = this.topicPublishInfoTable.get(topic);
    
    if (null == topicPublishInfo || !topicPublishInfo.ok()) {
        // 2. 缓存不存在，创建新的
        this.topicPublishInfoTable.putIfAbsent(topic, new TopicPublishInfo());
        
        // 3. 从 NameServer 更新路由信息
        this.mQClientFactory.updateTopicRouteInfoFromNameServer(topic);
        topicPublishInfo = this.topicPublishInfoTable.get(topic);
    }
    
    // 4. 如果路由信息不完整，强制刷新
    if (topicPublishInfo.isHaveTopicRouterInfo() || topicPublishInfo.ok()) {
        return topicPublishInfo;
    } else {
        this.mQClientFactory.updateTopicRouteInfoFromNameServer(topic, true, 
            this.defaultMQProducer);
        return this.topicPublishInfoTable.get(topic);
    }
}
```

### 4. 消息队列选择策略

```java
public MessageQueue selectOneMessageQueue(final TopicPublishInfo info, 
    final String lastBrokerName, boolean resetIndex) {
    
    // 如果启用了故障回避，使用故障策略选择
    if (this.mqFaultStrategy.isEnableFaultLatency()) {
        return this.mqFaultStrategy.selectOneMessageQueue(info, lastBrokerName);
    }
    
    // 默认轮询选择
    int index = info.getSendWhichQueue().incrementAndGet();
    
    // 取模获取队列
    int pos = Math.abs(index) % info.getMessageQueueList().size();
    
    // 重置索引
    if (resetIndex) {
        info.getSendWhichQueue().set(0);
        pos = Math.abs(random.nextInt()) % info.getMessageQueueList().size();
    }
    
    return info.getMessageQueueList().get(pos);
}
```

### 5. 发送核心实现 sendKernelImpl

```java
private SendResult sendKernelImpl(final Message msg, final MessageQueue mq,
    final CommunicationMode communicationMode, ...) 
    throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
    
    // 1. 获取 Broker 地址
    String brokerName = this.mQClientFactory.getBrokerNameFromMessageQueue(mq);
    String brokerAddr = this.mQClientFactory.findBrokerAddressInPublish(brokerName);
    
    // 2. 构建请求头
    SendMessageRequestHeader requestHeader = new SendMessageRequestHeader();
    requestHeader.setTopic(msg.getTopic());
    requestHeader.setQueueId(mq.getQueueId());
    requestHeader.setSysFlag(sysFlag);
    requestHeader.setBornTimestamp(System.currentTimeMillis());
    requestHeader.setFlag(msg.getFlag());
    requestHeader.setProperties(MessageEncoder.messageProperties2String(msg.getProperties()));
    requestHeader.setMsgId(msg.getMsgId());
    requestHeader.setWaitStoreMsgOK(true);
    requestHeader.setBatch( msg instanceof MessageBatch);
    
    // 3. 执行发送钩子
    SendMessageContext context = new SendMessageContext();
    context.setProducer(this);
    context.setTopic(msg.getTopic());
    context.setMessageQueue(mq);
    this.sendMessageHookList.forEach(hook -> hook.sendMessageBefore(context));
    
    // 4. 根据发送模式选择发送方式
    switch (communicationMode) {
        case SYNC:
            return this.mQClientFactory.getMQClientAPIImpl().sendMessage(
                brokerAddr, mq.getBrokerName(), msg, requestHeader, timeout);
            
        case ASYNC:
            this.mQClientFactory.getMQClientAPIImpl().sendMessage(
                brokerAddr, mq.getBrokerName(), msg, requestHeader, 
                new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        // 处理成功
                    }
                    
                    @Override
                    public void onException(Throwable e) {
                        // 处理异常
                    }
                }, timeout);
            return null;
            
        case ONEWAY:
            this.mQClientFactory.getMQClientAPIImpl().sendMessageOneway(
                brokerAddr, mq.getBrokerName(), msg, requestHeader);
            return null;
    }
    
    return null;
}
```

## 消息发送模式

### 1. 同步发送 (SYNC)

```java
// 阻塞等待 Broker 响应
SendResult sendResult = producer.send(msg);
System.out.println("Message ID: " + sendResult.getMsgId());
```

**特点**:
- 阻塞直到收到 Broker 响应
- 可配置重试次数（默认2次）
- 适用于可靠性要求高的场景

### 2. 异步发送 (ASYNC)

```java
// 不阻塞，通过回调处理结果
producer.send(msg, new SendCallback() {
    @Override
    public void onSuccess(SendResult sendResult) {
        System.out.println("Send success: " + sendResult.getMsgId());
    }
    
    @Override
    public void onException(Throwable e) {
        System.out.println("Send failed: " + e.getMessage());
    }
});
```

**特点**:
- 立即返回，不阻塞
- 通过回调通知发送结果
- 适用于对响应时间敏感的场景

### 3. 单向发送 (ONEWAY)

```java
// 发送后立即返回，不等待任何响应
producer.sendOneway(msg);
```

**特点**:
- 发送后立即返回，不等待响应
- 不保证消息一定能到达 Broker
- 适用于日志收集等对可靠性要求不高的场景

## 容错机制

### 1. 失败重试

```java
// 同步发送失败重试逻辑
int timesTotal = 1 + this.defaultMQProducer.getRetryTimesWhenSendFailed(); // 默认3次

for (int times = 0; times < timesTotal; times++) {
    try {
        sendResult = this.sendKernelImpl(msg, mq, ...);
        
        // 如果发送成功
        if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
            return sendResult;
        }
        
        // 如果失败且允许重试另一个 Broker
        if (this.defaultMQProducer.isRetryAnotherBrokerWhenNotStoreOK()) {
            continue;  // 尝试下一个 Broker
        }
        
    } catch (MQClientException e) {
        // 客户端异常，继续重试
        continue;
    } catch (RemotingException e) {
        // 网络异常，更新故障信息并重试
        this.updateFaultItem(mq.getBrokerName(), ...);
        continue;
    } catch (MQBrokerException e) {
        // Broker 异常
        if (this.defaultMQProducer.getRetryResponseCodes().contains(e.getResponseCode())) {
            continue;  // 可重试的错误码
        }
        throw e;
    }
}
```

### 2. 故障回避策略 (Fault Tolerance)

```java
// MQFaultStrategy 故障回避策略
public class MQFaultStrategy {
    // 是否启用故障回避
    private boolean enableFaultLatency = false;
    
    // 故障持续时间（毫秒）
    private long latencyMax = 50L;
    
    // 不可用时间（毫秒）
    private long notAvailableDuration = 0L;
    
    // 选择消息队列时考虑 Broker 延迟
    public MessageQueue selectOneMessageQueue(final TopicPublishInfo info, 
        final String lastBrokerName) {
        
        // 如果当前 Broker 不可用，选择其他 Broker
        if (latencyFaultItem.isAvailable()) {
            return this.scrollTo(index, info);
        }
        
        // 选择一个可用的 Broker
        for (int i = 0; i < info.getMessageQueueList().size(); i++) {
            int index = Math.abs(this.sendWhichQueue.incrementAndGet()) 
                % info.getMessageQueueList().size();
            MessageQueue mq = info.getMessageQueueList().get(index);
            
            if (latencyFaultItemTable.containsKey(mq.getBrokerName())) {
                LatencyFaultItem latencyFaultItem = 
                    latencyFaultItemTable.get(mq.getBrokerName());
                
                if (latencyFaultItem.isAvailable()) {
                    return mq;
                }
            } else {
                return mq;
            }
        }
        
        // 没有可用 Broker，选择一个
        return info.getMessageQueueList().get(
            Math.abs(this.sendWhichQueue.incrementAndGet()) 
                % info.getMessageQueueList().size());
    }
}
```

### 3. 背压机制

```java
// 异步发送背压控制
class BackpressureSendCallBack implements SendCallback {
    @Override
    public void onSuccess(SendResult sendResult) {
        semaphoreProcessor();  // 释放信号量
        sendCallback.onSuccess(sendResult);
    }
    
    @Override
    public void onException(Throwable e) {
        semaphoreProcessor();  // 释放信号量
        sendCallback.onException(e);
    }
    
    public void semaphoreProcessor() {
        // 释放信号量，允许更多异步发送
        if (isSemaphoreAsyncSizeAcquired) {
            semaphoreAsyncSendSize.release(msgLen);
        }
        if (isSemaphoreAsyncNumAcquired) {
            semaphoreAsyncSendNum.release();
        }
    }
}
```

## 消息队列选择器

### 1. 自定义消息队列选择

```java
// 使用 MessageQueueSelector 实现顺序消息发送
producer.send(msg, new MessageQueueSelector() {
    @Override
    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
        // 根据订单ID选择同一个队列，保证同一订单的消息顺序
        Integer orderId = (Integer) arg;
        int index = orderId % mqs.size();
        return mqs.get(index);
    }
}, orderId);
```

### 2. 内置选择器

RocketMQ 提供了多种内置选择器:

- **SelectMessageQueueByHash**: 使用 Hash 值选择队列
- **SelectMessageQueueByRandom**: 随机选择队列
- **SelectMessageQueueByMachineRoom**: 按机器房间选择

## 事务消息发送

### 1. 事务消息发送流程

```java
// TransactionMQProducer 事务消息发送
TransactionMQProducer producer = new TransactionMQProducer("producerGroup");
producer.setTransactionListener(new TransactionListener() {
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        // 执行本地事务
        return LocalTransactionState.COMMIT_MESSAGE;  // 或 ROLLBACK_MESSAGE
    }
    
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        // 检查本地事务状态
        return LocalTransactionState.COMMIT_MESSAGE;
    }
});

// 发送事务消息
TransactionSendResult result = producer.sendMessageInTransaction(msg, null);
```

### 2. 事务消息状态

```java
public enum LocalTransactionState {
    COMMIT_MESSAGE,    // 提交事务，消息可被消费
    ROLLBACK_MESSAGE, // 回滚事务，消息被删除
    UNKNOW            // 未知状态，需要 Broker 回调检查
}
```

## 代码示例

### 1. 基本同步发送

```java
// 创建生产者
DefaultMQProducer producer = new DefaultMQProducer("my-producer-group");
producer.setNamesrvAddress("localhost:9876");
producer.start();

// 创建消息
Message msg = new Message("my-topic", "tagA", 
    "Hello RocketMQ".getBytes(RemotingHelper.DEFAULT_CHARSET));

// 发送消息
SendResult sendResult = producer.send(msg);

// 打印结果
System.out.println("Send Status: " + sendResult.getSendStatus());
System.out.println("Message ID: " + sendResult.getMsgId());
System.out.println("Queue ID: " + sendResult.getMessageQueue().getQueueId());

// 关闭生产者
producer.shutdown();
```

### 2. 异步发送

```java
producer.send(msg, new SendCallback() {
    @Override
    public void onSuccess(SendResult sendResult) {
        System.out.println("Send Success: " + sendResult.getMsgId());
    }
    
    @Override
    public void onException(Throwable e) {
        System.out.println("Send Failed: " + e.getMessage());
    }
});
```

### 3. 批量发送

```java
List<Message> messages = new ArrayList<>();
messages.add(new Message("my-topic", "tagA", "msg1".getBytes()));
messages.add(new Message("my-topic", "tagA", "msg2".getBytes()));
messages.add(new Message("my-topic", "tagA", "msg3".getBytes()));

List<SendResult> results = producer.send(messages);
```

### 4. 顺序消息发送

```java
// 保证同一订单的消息发送到同一队列
producer.send(msg, new MessageQueueSelector() {
    @Override
    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
        Long orderId = (Long) arg;
        int index = (int)(orderId % mqs.size());
        return mqs.get(index);
    }
}, orderId);
```

## 流程图

```
┌─────────────────────────────────────────────────────────────────┐
│                        Producer 发送流程                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐                                              │
│  │   发送消息    │ ──→ send(msg)                               │
│  └──────┬───────┘                                              │
│         │                                                       │
│         ▼                                                       │
│  ┌──────────────────────────────────┐                           │
│  │  1. tryToFindTopicPublishInfo() │                           │
│  │     从本地缓存获取 Topic 路由    │                           │
│  │     缓存不存在则从 NameServer 获取 │                          │
│  └────────────────┬─────────────────┘                           │
│                   │                                             │
│                   ▼                                             │
│  ┌──────────────────────────────────┐                           │
│  │  2. selectOneMessageQueue()     │                           │
│  │     选择消息队列（轮询/故障回避）  │                           │
│  └────────────────┬─────────────────┘                           │
│                   │                                             │
│                   ▼                                             │
│  ┌──────────────────────────────────┐                           │
│  │  3. sendKernelImpl()            │                           │
│  │     构建请求头                   │                           │
│  │     执行发送钩子                 │                           │
│  └────────────────┬─────────────────┘                           │
│                   │                                             │
│                   ▼                                             │
│  ┌──────────────────────────────────┐                           │
│  │  4. MQClientAPIImpl.sendMessage │                           │
│  │     网络通信发送消息             │                           │
│  └────────────────┬─────────────────┘                           │
│                   │                                             │
│         ┌────────┴────────┐                                    │
│         │                 │                                     │
│    [SYNC 模式]       [ASYNC 模式]                               │
│         │                 │                                     │
│         ▼                 ▼                                     │
│  ┌──────────────┐   ┌──────────────┐                             │
│  │ 等待响应     │   │ 回调处理     │                             │
│  │ + 重试逻辑   │   │ + 重试逻辑   │                             │
│  └──────┬───────┘   └──────┬───────┘                             │
│         │                 │                                     │
│         └────────┬────────┘                                     │
│                  ▼                                             │
│         ┌──────────────┐                                        │
│         │ 返回结果     │                                        │
│         └──────────────┘                                        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## 总结

RocketMQ Producer 的设计要点:

1. **多种发送模式**: 支持同步、异步、单向三种模式，满足不同场景需求
2. **故障回避**: 自动检测 Broker 可用性，避开不可用的 Broker
3. **重试机制**: 支持失败后重试另一个 Broker，提高发送成功率
4. **背压控制**: 异步发送时防止瞬时流量过大导致系统崩溃
5. **消息队列选择**: 支持轮询、Hash、随机等多种选择策略
6. **扩展性**: 提供丰富的钩子和接口，便于定制扩展
