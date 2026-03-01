# caffeine

## 项目简介
[![Build Status](https://github.com/ben-manes/caffeine/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/ben-manes/caffeine/actions?query=workflow%3Abuild+branch%3Amaster)
[![Test Count](https://gist.githubusercontent.com/ben-manes/c20eb418f0e0bd6dfe1c25beb35faae4/raw/badge.svg)](https://github.com/ben-manes/caffeine/actions?query=workflow%3Abuild+branch%3Amaster)
[![Coverage Status](https://img.shields.io/coveralls/ben-manes/caffeine.svg?color=31c653)](https://coveralls.io/r/ben-manes/caffeine?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.ben-manes.caffeine/caffeine?color=31c653&label=maven%20central)](https://central.sonatype.com/artifact/com.github.ben-manes.caffeine/caffeine)
[![JavaDoc](https://www.javadoc.io/badge/com.github.ben-manes.caffeine/caffeine.svg?color=31c653)](https://www.javadoc.io/doc/com.github.ben-manes.caffeine/caffeine)
[![License](https://img.shields.io/:license-apache-31c653.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Revved up by Develocity](https://img.shields.io/badge/Revved%20up%20by-Develocity-06A0CE?logo=Gradle&labelColor=02303A)](https://caffeine.gradle-enterprise.cloud/scans)
<a href="https://github.com/ben-manes/caffeine/wiki">
<img align="right" height="90px" src="https://raw.githubusercontent.com/ben-manes/caffeine/master/wiki/logo.png">
</a>

Caffeine is a [high performance][benchmarks], [near optimal][efficiency] caching library. For more
details, see our [user's guide][users-guide] and browse the [API docs][javadoc] for the latest
release.

### Cache

Caffeine provides an in-memory cache using a Google Guava inspired API. The improvements draw on our
experience designing [Guava's cache][guava-cache] and [ConcurrentLinkedHashMap][clhm].

```java
LoadingCache<Key, Graph> graphs = Caffeine.newBuilder()
    .maximumSize(10_000)
    .expireAfterWrite(Duration.ofMinutes(5))
    .refreshAfterWrite(Duration.ofMinutes(1))
    .build(key -> createExpensiveGraph(key));
```

#### Features at a Glance

Caffeine provides flexible construction to create a cache with a combination of the following
optional features:

* [automatic loading of entries][population] into the cache, optionally asynchronously
* [size-based eviction][size] when a maximum is exceeded based on [frequency and recency][efficiency]
* [time-based expiration][time] of entries, measured since last access or last write
* [asynchronously refresh][refresh] when the first stale request for an entry occurs
* keys automatically wrapped in [weak references][reference]
* values automatically wrapped in [weak or soft references][reference]
* [notification][listener] of evicted (or otherwise removed) entries

## 整体架构描述
* An in-depth description of Caffeine's architecture.
  * [Design of a Modern Cache: part #1][modern-cache-1], [part #2][modern-cache-2]
    ([slides][modern-cache-slides]) at [HighScalability][]
* Caffeine is presented as part of research papers evaluating its novel eviction policy.
  * [TinyLFU: A Highly Efficient Cache Admission Policy][tinylfu]
    by Gil Einziger, Roy Friedman, Ben Manes
  * [Adaptive Software Cache Management][adaptive-tinylfu]
    by Gil Einziger, Ohad Eytan, Roy Friedman, Ben Manes
  * [Lightweight Robust Size Aware Cache Management][size-tinylfu]
    by Gil Einziger, Ohad Eytan, Roy Friedman, Ben Manes

### Download

Download from [Maven Central][maven] or depend via Gradle:

```gradle
implementation("com.github.ben-manes.caffeine:caffeine:3.2.3")

// Optional extensions
implementation("com.github.ben-manes.caffeine:guava:3.2.3")
implementation("com.github.ben-manes.caffeine:jcache:3.2.3")
```

For Java 11 or above, use `3.x` otherwise use `2.x`.

See the [release notes][releases] for details of the changes.

Snapshots of the development version are available in
[Sonatype's snapshots repository][snapshots].

[benchmarks]: https://github.com/ben-manes/caffeine/wiki/Benchmarks

## 核心模块划分
[camel]: https://github.com/apache/camel/blob/master/components/camel-caffeine/src/main/docs/caffeine-cache-component.adoc
[coherence]: https://docs.oracle.com/en/middleware/standalone/coherence/14.1.1.2206/develop-applications/implementing-storage-and-backing-maps.html#GUID-260228C2-371A-4B91-9024-8D6514DD4B78
[corfu]: https://github.com/CorfuDB/CorfuDB
[micronaut]: https://docs.micronaut.io/latest/guide/index.html#caching
[play]: https://www.playframework.com/documentation/latest/JavaCache
[redisson]: https://github.com/redisson/redisson
[accumulo]: https://accumulo.apache.org
[dropwizard]: https://www.dropwizard.io
[grails]: https://grails.org
[quarkus]: https://quarkus.io
[aedile]: https://github.com/sksamuel/aedile
[bootique]: https://bootique.io/
[caffeine-coroutines]: https://github.com/be-hase/caffeine-coroutines

## 技术选型
- 构建工具: 未知
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
