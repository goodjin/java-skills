# jedis

## 项目简介
# Jedis

[![Release](https://img.shields.io/github/release/redis/jedis.svg?sort=semver)](https://github.com/redis/jedis/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/redis.clients/jedis.svg)](https://central.sonatype.com/artifact/redis.clients/jedis)
[![Javadocs](https://www.javadoc.io/badge/redis.clients/jedis.svg)](https://www.javadoc.io/doc/redis.clients/jedis)
[![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/redis/jedis/blob/master/LICENSE)
[![codecov](https://codecov.io/gh/redis/jedis/branch/master/graph/badge.svg?token=pAstxAAjYo)](https://codecov.io/gh/redis/jedis)
[![Discord](https://img.shields.io/discord/697882427875393627?style=flat-square)](https://discord.gg/redis)

## What is Jedis?

Jedis is a Java client for [Redis](https://github.com/redis/redis "Redis") designed for performance and ease of use.

Are you looking for a high-level library to handle object mapping? See [redis-om-spring](https://github.com/redis/redis-om-spring)!

## How do I Redis?

[Learn for free at Redis University](https://university.redis.io/academy/)

[Try the Redis Cloud](https://redis.io/try-free/)

[Dive in developer tutorials](https://redis.io/learn/)

[Join the Redis community](https://redis.io/community/)

[Work at Redis](https://redis.io/careers/jobs/)

## Supported Redis versions

The most recent version of this library supports redis version 
[7.2](https://github.com/redis/redis/blob/7.2/00-RELEASENOTES),
[7.4](https://github.com/redis/redis/blob/7.4/00-RELEASENOTES),
[8.0](https://github.com/redis/redis/blob/8.0/00-RELEASENOTES),
[8.2](https://github.com/redis/redis/blob/8.2/00-RELEASENOTES) and
[8.4](https://github.com/redis/redis/blob/8.4/00-RELEASENOTES).

The table below highlights version compatibility of the most-recent library versions with Redis and JDK versions. Compatibility means communication features, and Redis command capabilities.


| Jedis version | Supported Redis versions              | JDK Compatibility |

## 整体架构描述


## 核心模块划分
Jedis supports Token-Based authentication (TBA) starting with 5.3.0 GA release. This feature is complemented by an extension library that enhances the developer experience and provides most of the components required for TBA functionality.

Notably, the extension library includes built-in support for **Microsoft EntraID**, offering a seamless integration as part of the generic solution.

For more details and examples, please refer to the [Advanced Usage](https://redis.github.io/jedis/advanced-usage/) documentation.

## Documentation

The [Jedis documentation site](https://redis.github.io/jedis/) contains several useful articles for using Jedis.

You can also check the [latest Jedis Javadocs](https://www.javadoc.io/doc/redis.clients/jedis/latest/index.html).

Some specific use-case examples can be found in [`redis.clients.jedis.examples`
package](https://github.com/redis/jedis/tree/master/src/test/java/redis/clients/jedis/examples/) of the test source codes.

## Troubleshooting

If you run into trouble or have any questions, we're here to help!

Hit us up on the [Redis Discord Server](http://discord.gg/redis) or 
[Jedis GitHub Discussions](https://github.com/redis/jedis/discussions).

## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
