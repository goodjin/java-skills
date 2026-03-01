# hystrix

## 项目简介
<img src="https://netflix.github.io/Hystrix/images/hystrix-logo-tagline-850.png">

# Hystrix: Latency and Fault Tolerance for Distributed Systems

[![NetflixOSS Lifecycle](https://img.shields.io/osslifecycle/Netflix/hystrix.svg)]()
[![][travis img]][travis]
[![][maven img]][maven]
[![][license img]][license]

# Hystrix Status
Hystrix is no longer in active development, and is currently in maintenance mode.

Hystrix (at version 1.5.18) is stable enough to meet the needs of Netflix for our existing applications. Meanwhile, our focus has shifted towards more adaptive implementations that react to an application’s real time performance rather than pre-configured settings (for example, through [adaptive concurrency limits](https://medium.com/@NetflixTechBlog/performance-under-load-3e6fa9a60581)). For the cases where something like Hystrix makes sense, we intend to continue using Hystrix for existing applications, and to leverage open and active projects like [resilience4j](https://github.com/resilience4j/resilience4j) for new internal projects. We are beginning to recommend others do the same.

Netflix Hystrix is now officially in maintenance mode, with the following expectations to the greater community: 
Netflix will no longer actively review issues, merge pull-requests, and release new versions of Hystrix. 
We have made a final release of Hystrix (1.5.18) per [issue 1891](https://github.com/Netflix/Hystrix/issues/1891) so that the latest version in Maven Central is aligned with the last known stable version used internally at Netflix (1.5.11). 
If members of the community are interested in taking ownership of Hystrix and moving it back into active mode, please reach out to hystrixoss@googlegroups.com.

Hystrix has served Netflix and the community well over the years, and the transition to maintenance mode is in no way an indication that the concepts and ideas from Hystrix are no longer valuable. On the contrary, Hystrix has inspired many great ideas and projects. We thank everyone at Netflix, and in the greater community, for all the contributions made to Hystrix over the years.

## Introduction

Hystrix is a latency and fault tolerance library designed to isolate points of access to remote systems, services and 3rd party libraries, stop cascading failure and enable resilience in complex distributed systems where failure is inevitable.

## Full Documentation

See the [Wiki](https://github.com/Netflix/Hystrix/wiki/) for full documentation, examples, operational details and other information.

See the [Javadoc](http://netflix.github.com/Hystrix/javadoc) for the API.

## Communication

- Google Group: [HystrixOSS](http://groups.google.com/d/forum/hystrixoss)
- Twitter: [@HystrixOSS](http://twitter.com/HystrixOSS)
- [GitHub Issues](https://github.com/Netflix/Hystrix/issues)

## What does it do?

#### 1) Latency and Fault Tolerance

## 整体架构描述


## 核心模块划分
Example source code can be found in the [hystrix-examples](https://github.com/Netflix/Hystrix/tree/master/hystrix-examples/src/main/java/com/netflix/hystrix/examples) module.

## Binaries

Binaries and dependency information for Maven, Ivy, Gradle and others can be found at [http://search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.netflix.hystrix%22%20AND%20a%3A%22hystrix-core%22).

Change history and version numbers => [CHANGELOG.md](https://github.com/Netflix/Hystrix/blob/master/CHANGELOG.md)

Example for Maven:

```xml
<dependency>
    <groupId>com.netflix.hystrix</groupId>
    <artifactId>hystrix-core</artifactId>
    <version>x.y.z</version>
</dependency>
```
and for Ivy:

```xml
<dependency org="com.netflix.hystrix" name="hystrix-core" rev="x.y.z" />
--
The hystrix-dashboard component of this project has been deprecated and moved to [Netflix-Skunkworks/hystrix-dashboard](https://github.com/Netflix-Skunkworks/hystrix-dashboard). Please see the README there for more details including important security considerations.

## 技术选型
- 构建工具: Gradle
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
