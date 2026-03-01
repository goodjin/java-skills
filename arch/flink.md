# flink

## 项目简介
# Apache Flink

Apache Flink is an open source stream processing framework with powerful stream- and batch-processing capabilities.

Learn more about Flink at [https://flink.apache.org/](https://flink.apache.org/)


### Features

* A streaming-first runtime that supports both batch processing and data streaming programs

* Elegant and fluent APIs in Java

* A runtime that supports very high throughput and low event latency at the same time

* Support for *event time* and *out-of-order* processing in the DataStream API, based on the *Dataflow Model*

* Flexible windowing (time, count, sessions, custom triggers) across different time semantics (event time, processing time)

* Fault-tolerance with *exactly-once* processing guarantees

* Natural back-pressure in streaming programs

* Libraries for Graph processing (batch), Machine Learning (batch), and Complex Event Processing (streaming)

* Custom memory management for efficient and robust switching between in-memory and out-of-core data processing algorithms

* Compatibility layers for Apache Hadoop MapReduce

* Integration with YARN, HDFS, HBase, and other components of the Apache Hadoop ecosystem


### Streaming Example
```java
// pojo class WordWithCount
public class WordWithCount {
    public String word;
    public int count;

    public WordWithCount() {}

## 整体架构描述


## 核心模块划分
* Integration with YARN, HDFS, HBase, and other components of the Apache Hadoop ecosystem


### Streaming Example
```java
// pojo class WordWithCount
public class WordWithCount {
    public String word;
    public int count;

    public WordWithCount() {}
    
    public WordWithCount(String word, int count) {
        this.word = word;
        this.count = count;
    }
}

// main method
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
DataStreamSource<String> text = env.socketTextStream(host, port);

## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
