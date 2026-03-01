# zipkin

## 项目简介
# zipkin

[![Gitter chat](http://img.shields.io/badge/gitter-join%20chat%20%E2%86%92-brightgreen.svg)](https://gitter.im/openzipkin/zipkin)
[![Build Status](https://github.com/openzipkin/zipkin/actions/workflows/test.yml/badge.svg?branch=master)](https://github.com/openzipkin/zipkin/actions?query=workflow%3Atest+branch%3Amaster)
[![Maven Central](https://img.shields.io/maven-central/v/io.zipkin/zipkin-server.svg)](https://search.maven.org/search?q=g:io.zipkin%20AND%20a:zipkin-server)

[Zipkin](https://zipkin.io) is a distributed tracing system. It helps gather
timing data needed to troubleshoot latency problems in service architectures.
Features include both the collection and lookup of this data.

If you have a trace ID in a log file, you can jump directly to it. Otherwise,
you can query based on attributes such as service, operation name, tags and
duration. Some interesting data will be summarized for you, such as the
percentage of time spent in a service, and whether operations failed.

<img src="https://zipkin.io/public/img/web-screenshot.png" alt="Trace view screenshot" />

The Zipkin UI also presents a dependency diagram showing how many traced
requests went through each application. This can be helpful for identifying
aggregate behavior including error paths or calls to deprecated services.

<img src="https://zipkin.io/public/img/dependency-graph.png" alt="Dependency graph screenshot" />

Application’s need to be “instrumented” to report trace data to Zipkin. This
usually means configuration of a [tracer or instrumentation library](https://zipkin.io/pages/tracers_instrumentation.html). The most
popular ways to report data to Zipkin are via http or Kafka, though many other
options exist, such as Apache ActiveMQ, gRPC, RabbitMQ and Apache Pulsar. The data served to
the UI is stored in-memory, or persistently with a supported backend such as
Apache Cassandra or Elasticsearch.

## Quick-start

The quickest way to get started is to fetch the [latest released server](https://search.maven.org/remote_content?g=io.zipkin&a=zipkin-server&v=LATEST&c=exec) as a self-contained
executable jar. Note that the Zipkin server requires minimum JRE 17+. For example:

```bash
curl -sSL https://zipkin.io/quickstart.sh | bash -s
java -jar zipkin.jar
```

## 整体架构描述
timing data needed to troubleshoot latency problems in service architectures.
Features include both the collection and lookup of this data.

If you have a trace ID in a log file, you can jump directly to it. Otherwise,
you can query based on attributes such as service, operation name, tags and
duration. Some interesting data will be summarized for you, such as the
percentage of time spent in a service, and whether operations failed.

<img src="https://zipkin.io/public/img/web-screenshot.png" alt="Trace view screenshot" />

The Zipkin UI also presents a dependency diagram showing how many traced
requests went through each application. This can be helpful for identifying
aggregate behavior including error paths or calls to deprecated services.

<img src="https://zipkin.io/public/img/dependency-graph.png" alt="Dependency graph screenshot" />

Application’s need to be “instrumented” to report trace data to Zipkin. This
usually means configuration of a [tracer or instrumentation library](https://zipkin.io/pages/tracers_instrumentation.html). The most
popular ways to report data to Zipkin are via http or Kafka, though many other
options exist, such as Apache ActiveMQ, gRPC, RabbitMQ and Apache Pulsar. The data served to
the UI is stored in-memory, or persistently with a supported backend such as
Apache Cassandra or Elasticsearch.

## Quick-start

The quickest way to get started is to fetch the [latest released server](https://search.maven.org/remote_content?g=io.zipkin&a=zipkin-server&v=LATEST&c=exec) as a self-contained
executable jar. Note that the Zipkin server requires minimum JRE 17+. For example:

```bash
curl -sSL https://zipkin.io/quickstart.sh | bash -s
java -jar zipkin.jar

## 核心模块划分
## Storage Component
Zipkin includes a [StorageComponent](zipkin/src/main/java/zipkin2/storage/StorageComponent.java), used to store and query spans and
dependency links. This is used by the server and those making collectors, or span reporters.
For this reason, storage components have minimal dependencies, though require Java 17+.

Ex.
```java
// this won't create network connections
storage = ElasticsearchStorage.newBuilder()
                              .hosts(asList("http://myelastic:9200")).build();

// prepare a call
traceCall = storage.spanStore().getTrace("d3d200866a77cc59");

// execute it synchronously or asynchronously
trace = traceCall.execute();

// clean up any sessions, etc
storage.close();
```

### In-Memory
The [InMemoryStorage](zipkin-server#in-memory-storage) component is packaged in zipkin's core library. It
is neither persistent, nor viable for realistic work loads. Its purpose
is for testing, for example starting a server on your laptop without any

## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
