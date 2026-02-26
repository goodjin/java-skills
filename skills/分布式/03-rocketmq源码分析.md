# RocketMQ 源码分析

## 核心架构

```
Producer → NameServer → Broker → Consumer
              ↑              ↑
          Topic         Queue/CommitLog
```

## 核心组件

| 组件 | 作用 |
|------|------|
| Producer | 消息生产者 |
| Consumer | 消息消费者 |
| NameServer | 路由注册中心 |
| Broker | 消息存储转发 |

## 消息存储

- **CommitLog**: 顺序写入
- **ConsumeQueue**: 消费队列
- **IndexFile**: 索引文件

## 核心特性

1. **顺序消息**: 单队列有序
2. **事务消息**: 半消息 + 回查
3. **延迟消息**: 消息延迟投递
4. **消息过滤**: Tag/SQL 过滤

## 最佳实践

```java
// 生产者
DefaultMQProducer producer = new DefaultMQProducer("producer_group");
producer.start();
Message msg = new Message("TopicTest", "TagA", "Keys", "Hello".getBytes());
SendResult result = producer.send(msg);

// 消费者
DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumer_group");
consumer.subscribe("TopicTest", "*");
consumer.registerMessageListener((msgs, context) -> {
    for (MessageExt msg : msgs) {
        System.out.println(new String(msg.getBody()));
    }
    return ConsumeOrderlyStatus.SUCCESS;
});
consumer.start();
```
