# lettuce-core

## 项目简介
<img src="https://avatars2.githubusercontent.com/u/25752188?v=4" width="50" height="50"> Lettuce - Advanced Java Redis client
===============================

 [![Integration](https://github.com/redis/lettuce/actions/workflows/integration.yml/badge.svg?branch=main)](https://github.com/redis/lettuce/actions/workflows/integration.yml)
 [![codecov](https://codecov.io/gh/redis/lettuce/branch/main/graph/badge.svg?token=pAstxAAjYo)](https://codecov.io/gh/redis/lettuce)
 [![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](./LICENSE.txt)
 [![Maven Central](https://img.shields.io/maven-central/v/io.lettuce/lettuce-core?versionSuffix=RELEASE&logo=redis
 )](https://maven-badges.herokuapp.com/maven-central/io.lettuce/lettuce-core)
 [![Javadocs](https://www.javadoc.io/badge/io.lettuce/lettuce-core.svg)](https://www.javadoc.io/doc/io.lettuce/lettuce-core)

[![Discord](https://img.shields.io/discord/697882427875393627.svg?style=social&logo=discord)](https://discord.gg/redis)
[![Twitch](https://img.shields.io/twitch/status/redisinc?style=social)](https://www.twitch.tv/redisinc)
[![YouTube](https://img.shields.io/youtube/channel/views/UCD78lHSwYqMlyetR0_P4Vig?style=social)](https://www.youtube.com/redisinc)
[![Twitter](https://img.shields.io/twitter/follow/redisinc?style=social)](https://twitter.com/redisinc)
[![Stack Exchange questions](https://img.shields.io/stackexchange/stackoverflow/t/lettuce?style=social&logo=stackoverflow&label=Stackoverflow)](https://stackoverflow.com/questions/tagged/lettuce)


Lettuce is a scalable thread-safe Redis client for synchronous,
asynchronous and reactive usage. Multiple threads may share one connection if they avoid blocking and transactional
operations such as `BLPOP` and  `MULTI`/`EXEC`.
Lettuce is built with [netty](https://github.com/netty/netty).
Supports advanced Redis features such as Sentinel, Cluster, Pipelining, Auto-Reconnect and Redis data models.

This version of Lettuce has been tested against the latest Redis source-build.

* [synchronous](https://redis.github.io/lettuce/user-guide/connecting-redis/#basic-usage), [asynchronous](https://redis.github.io/lettuce/user-guide/async-api/) and [reactive](https://redis.github.io/lettuce/user-guide/reactive-api/) usage
* [Redis Sentinel](https://redis.github.io/lettuce/ha-sharding/#redis-sentinel_1)
* [Redis Cluster](https://redis.github.io/lettuce/ha-sharding/#redis-cluster)
* [SSL](https://redis.github.io/lettuce/advanced-usage/#ssl-connections) and [Unix Domain Socket](https://redis.github.io/lettuce/advanced-usage/#unix-domain-sockets) connections
* [Streaming API](https://redis.github.io/lettuce/advanced-usage/#streaming-api)
* [Codecs](https://redis.github.io/lettuce/integration-extension/#codecss) (for UTF8/bit/JSON etc. representation of your data)
* multiple [Command Interfaces](https://github.com/redis/lettuce/wiki/Command-Interfaces-%284.0%29)
* Support for [Native Transports](https://redis.github.io/lettuce/advanced-usage/#native-transports)
* Support [RediSearch](https://redis.github.io/lettuce/user-guide/redis-search/), [RedisJSON](https://redis.github.io/lettuce/user-guide/redis-json/) and [Redis Vector Sets](https://redis.github.io/lettuce/user-guide/vector-sets/)
* Compatible with Java 8++ (implicit automatic module w/o descriptors)

See the [reference documentation](https://redis.github.io/lettuce/) and [API Reference](https://www.javadoc.io/doc/io.lettuce/lettuce-core/latest/index.html) for more details.

## How do I Redis?

## 整体架构描述


## 核心模块划分
* Compatible with Java 8++ (implicit automatic module w/o descriptors)

See the [reference documentation](https://redis.github.io/lettuce/) and [API Reference](https://www.javadoc.io/doc/io.lettuce/lettuce-core/latest/index.html) for more details.

## How do I Redis?

[Learn for free at Redis University](https://university.redis.io/academy)

[Try the Redis Cloud](https://redis.io/try-free/)

[Dive in developer tutorials](https://redis.io/learn/)

[Join the Redis community](https://redis.io/community/)

[Work at Redis](https://redis.io/careers/jobs/)

Documentation
---------------

* [Reference documentation](https://redis.github.io/lettuce/)
* [Javadoc](https://www.javadoc.io/doc/io.lettuce/lettuce-core/latest/index.html)

## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
