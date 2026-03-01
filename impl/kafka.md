# Kafka 项目分析

## 项目简介
Kafka 是分布式流处理平台，提供高吞吐量的持久化消息队列，支持发布-订阅模式和消息持久化。

## 核心类结构

### 1. KafkaProducer（生产者）
- **位置**: `org.apache.kafka.clients.producer.KafkaProducer`
- **核心**: `send()` 异步发送
- **配置**: `ProducerConfig` 各类参数

### 2. KafkaConsumer（消费者）
- **位置**: `org.apache.kafka.clients.consumer.KafkaConsumer`
- **核心**: `poll()` 拉取消息
- **提交**: `commitSync()` / `commitAsync()`

### 3. Broker（代理）
- **位置**: `kafka.server.KafkaBroker`
- **核心**: `kafka.server.KafkaApis` 处理请求
- **副本**: `ReplicaManager` 管理副本

### 4. Topic（主题）
- **核心**: 分区 + 副本
- **分区**: `Partition` 物理存储单元
- **副本**: `Replica` 数据副本

### 5. Log（日志）
- **位置**: `kafka.log.Log`
- **存储**: `LogSegment` 分段存储
- **索引**: 偏移量索引 + 时间索引

### 6. Controller（控制器）
- **位置**: `kafka.controller.KafkaController`
- **职责**: 集群元数据管理、分区选举
- **选举**: ZK / KRaft 模式

### 7. Coordinator（协调者）
- **位置**: `kafka.coordinator.group.GroupCoordinator`
- **职责**: 消费者组管理、offset 提交
- **再平衡**: `rebalance` 机制

## 设计模式

### 1. 发布-订阅模式
- Producer → Topic → Consumer Group
- 同一消费组内只有一个消费者消费

### 2. 追加写日志
- 顺序写入磁盘
- LogSegment 分段 + 索引

### 3. 零拷贝
- `FileChannel.transferTo()` 直接传输
- 减少内核态/用户态拷贝

### 4. 批量处理
- 批量发送 `batch.size`
- 批量拉取 `fetch.min.bytes`

### 5. 副本同步
- ISR (In-Sync Replicas) 机制
- AR (Assigned Replicas) 分区副本

## 代码技巧

### 1. 生产者发送
```java
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("acks", "all");
props.put("retries", 3);
props.put("batch.size", 16384);
props.put("linger.ms", 1);
props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

KafkaProducer<String, String> producer = new KafkaProducer<>(props);
producer.send(new ProducerRecord<>("topic", "key", "value"));
```

### 2. 消费者订阅
```java
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("group.id", "my-group");
props.put("enable.auto.commit", "true");
props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
consumer.subscribe(Arrays.asList("topic"));
while (true) {
    ConsumerRecords<String, String> records = consumer.poll(100);
    for (ConsumerRecord<String, String> record : records) {
        System.out.println(record.value());
    }
}
```

### 3. 精确一次语义
```java
// 生产者幂等性
props.put("enable.idempotence", "true");

// 事务
producer.initTransactions();
producer.beginTransaction();
producer.send(record1);
producer.send(record2);
producer.commitTransaction();
```

### 4. 拦截器
```java
public class MyProducerInterceptor implements ProducerInterceptor<K, V> {
    @Override
    public ProducerRecord<K, V> onSend(ProducerRecord<K, V> record) {
        return record;
    }
    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {}
}
```

## 代码规范

### 1. 主题命名
- 业务前缀: `order-`, `payment-`
- 分区数合理设置

### 2. 消费者组
- 同一业务使用同一组
- 避免频繁重平衡

### 3. 消息key
- 使用有业务意义的 key
- 有利于分区策略

## 值得学习的地方

1. **高吞吐量**: 顺序写 + 零拷贝 + 批量处理
2. **持久化**: 多副本 + 磁盘顺序写
3. **扩展性**: 分区 + 副本机制
4. **容错**: ISR 副本同步
5. **消费者组**: 组内负载均衡
6. **事务**: 精确一次语义
7. **流处理**: Kafka Streams
