# redisson

## 项目简介
# Redisson - Valkey & Redis Java client.<br/>Real-Time Data Platform.

[Quick start](https://redisson.org/docs/getting-started/) | [Documentation](https://redisson.org/docs/) | [Changelog](https://github.com/redisson/redisson/blob/master/CHANGELOG.md) | [Code examples](https://github.com/redisson/redisson-examples) | [JavaDocs](https://www.javadoc.io/doc/org.redisson/redisson/latest/index.html)

Redisson is the Java Client and Real-Time Data Platform for Valkey and Redis. Providing the most convenient and easiest way to work with Valkey or Redis. Redisson objects provide an abstraction layer between Valkey or Redis and your Java code, which allowing maintain focus on data modeling and application logic. 

Redisson greatly extends the capabilities of Valkey and Redis by providing additional services and data structures not natively available in either platform. This enhancement includes distributed Java collections, objects, and service implementations.

## Features

* Thread-safe implementation
* JDK 1.8+ up to the latest version compatible
* Android compatible
* [Redis](https://redis.io) compatible - from 3.0 up to the latest version
* [Valkey](https://valkey.io) compatible - from 7.2.5 up to the latest version
* Supported Valkey and Redis deployment types
    * [Proxy](https://redisson.org/docs/configuration/#proxy-mode)
    * [Multi-Cluster](https://redisson.org/docs/configuration/#multi-cluster-mode)
    * [Multi-Sentinel](https://redisson.org/docs/configuration/#multi-sentinel-mode)
    * [Single](https://redisson.org/docs/configuration/#single-mode)
    * [Cluster](https://redisson.org/docs/configuration/#cluster-mode)
    * [Sentinel](https://redisson.org/docs/configuration/#sentinel-mode)
    * [Replicated](https://redisson.org/docs/configuration/#replicated-mode)
    * [Master and Slaves](https://redisson.org/docs/configuration/#master-slave-mode)
* Supports auto-reconnection  
* Supports failed to send command auto-retry  
* Supports OSGi  
* Supports SSL  
* Asynchronous connection pool  
* Lua scripting  
* [RediSearch](https://redisson.org/docs/data-and-services/services/#redisearch-service)
* [JSON datatype](https://redisson.org/docs/data-and-services/objects/#json-object-holder)
* [JSON Store](https://redisson.org/docs/data-and-services/collections/#json-store) 
* [Reactive Streams](https://redisson.org/docs/api-models/#reactive-api) API  
* [RxJava3](https://redisson.org/docs/api-models/#rxjava-api) API  
* [Asynchronous](https://redisson.org/docs/api-models/#synchronous-and-asynchronous-api) API  
* Local cache support including [Caffeine](https://github.com/ben-manes/caffeine)-based implementation
* [Cache API implementations](https://redisson.org/docs/cache-api-implementations)  
    Spring Cache, JCache API (JSR-107), Hibernate Cache, MyBatis Cache, Quarkus Cache, Micronaut Cache
* [Distributed Objects](https://redisson.org/docs/data-and-services/objects)  

## 整体架构描述


## 核心模块划分


## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
