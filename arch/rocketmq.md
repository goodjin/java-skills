# rocketmq

## 项目简介
## Apache RocketMQ

[![Build Status][maven-build-image]][maven-build-url]
[![CodeCov][codecov-image]][codecov-url]
[![Maven Central][maven-central-image]][maven-central-url]
[![Release][release-image]][release-url]
[![License][license-image]][license-url]
[![Average Time to Resolve An Issue][average-time-to-resolve-an-issue-image]][average-time-to-resolve-an-issue-url]
[![Percentage of Issues Still Open][percentage-of-issues-still-open-image]][percentage-of-issues-still-open-url]
[![Twitter Follow][twitter-follow-image]][twitter-follow-url]

**[Apache RocketMQ](https://rocketmq.apache.org) is a distributed messaging and streaming platform with low latency, high performance and reliability, trillion-level capacity and flexible scalability.**


It offers a variety of features:

* Messaging patterns including publish/subscribe, request/reply and streaming
* Financial grade transactional message
* Built-in fault tolerance and high availability configuration options based on [DLedger Controller](docs/en/controller/quick_start.md)
* Built-in message tracing capability, also supports opentracing
* Versatile big-data and streaming ecosystem integration
* Message retroactivity by time or offset
* Reliable FIFO and strict ordered messaging in the same queue
* Efficient pull and push consumption model
* Million-level message accumulation capacity in a single queue
* Multiple messaging protocols like gRPC, MQTT, JMS and OpenMessaging
* Flexible distributed scale-out deployment architecture
* Lightning-fast batch message exchange system
* Various message filter mechanics such as SQL and Tag
* Docker images for isolated testing and cloud isolated clusters
* Feature-rich administrative dashboard for configuration, metrics and monitoring
* Authentication and authorization
* Free open source connectors, for both sources and sinks
* Lightweight real-time computing
----------


## Quick Start

This paragraph guides you through steps of installing RocketMQ in different ways.

## 整体架构描述
* Flexible distributed scale-out deployment architecture
* Lightning-fast batch message exchange system
* Various message filter mechanics such as SQL and Tag
* Docker images for isolated testing and cloud isolated clusters
* Feature-rich administrative dashboard for configuration, metrics and monitoring
* Authentication and authorization
* Free open source connectors, for both sources and sinks
* Lightweight real-time computing
----------


## Quick Start

This paragraph guides you through steps of installing RocketMQ in different ways.
For local development and testing, only one instance will be created for each component.

### Run RocketMQ locally

RocketMQ runs on all major operating systems and requires only a Java JDK version 8 or higher to be installed.
To check, run `java -version`:
```shell
$ java -version
java version "1.8.0_121"
```

For Windows users, click [here](https://dist.apache.org/repos/dist/release/rocketmq/5.4.0/rocketmq-all-5.4.0-bin-release.zip) to download the 5.4.0 RocketMQ binary release,
unpack it to your local disk, such as `D:ocketmq`.
For macOS and Linux users, execute following commands:

```shell
# Download release from the Apache mirror
--
* [RocketMQ MQTT](https://github.com/apache/rocketmq-mqtt): A new MQTT protocol architecture model, based on which Apache RocketMQ can better support messages from terminals such as IoT devices and Mobile APP.
* [RocketMQ EventBridge](https://github.com/apache/rocketmq-eventbridge): EventBridge makes it easier to build an event-driven application.
* [RocketMQ Incubating Community Projects](https://github.com/apache/rocketmq-externals): Incubator community projects of Apache RocketMQ, including [logappender](https://github.com/apache/rocketmq-externals/tree/master/logappender), [rocketmq-ansible](https://github.com/apache/rocketmq-externals/tree/master/rocketmq-ansible), [rocketmq-beats-integration](https://github.com/apache/rocketmq-externals/tree/master/rocketmq-beats-integration), [rocketmq-cloudevents-binding](https://github.com/apache/rocketmq-externals/tree/master/rocketmq-cloudevents-binding), etc.

## 核心模块划分
For local development and testing, only one instance will be created for each component.

### Run RocketMQ locally

RocketMQ runs on all major operating systems and requires only a Java JDK version 8 or higher to be installed.
To check, run `java -version`:
```shell
$ java -version
java version "1.8.0_121"
```

For Windows users, click [here](https://dist.apache.org/repos/dist/release/rocketmq/5.4.0/rocketmq-all-5.4.0-bin-release.zip) to download the 5.4.0 RocketMQ binary release,
unpack it to your local disk, such as `D:ocketmq`.
For macOS and Linux users, execute following commands:

```shell
# Download release from the Apache mirror
$ wget https://dist.apache.org/repos/dist/release/rocketmq/5.4.0/rocketmq-all-5.4.0-bin-release.zip

# Unpack the release
$ unzip rocketmq-all-5.4.0-bin-release.zip

## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
