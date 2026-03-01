# grpc-java

## 项目简介
gRPC-Java - An RPC library and framework
========================================

<table>
  <tr>
    <td><b>Homepage:</b></td>
    <td><a href="https://grpc.io/">grpc.io</a></td>
  </tr>
  <tr>
    <td><b>Mailing List:</b></td>
    <td><a href="https://groups.google.com/forum/#!forum/grpc-io">grpc-io@googlegroups.com</a></td>
  </tr>
</table>

[![Join the chat at https://gitter.im/grpc/grpc](https://badges.gitter.im/grpc/grpc.svg)](https://gitter.im/grpc/grpc?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![GitHub Actions Linux Testing](https://github.com/grpc/grpc-java/actions/workflows/testing.yml/badge.svg?branch=master)](https://github.com/grpc/grpc-java/actions/workflows/testing.yml?branch=master)
[![Line Coverage Status](https://coveralls.io/repos/grpc/grpc-java/badge.svg?branch=master&service=github)](https://coveralls.io/github/grpc/grpc-java?branch=master)
[![Branch-adjusted Line Coverage Status](https://codecov.io/gh/grpc/grpc-java/branch/master/graph/badge.svg)](https://codecov.io/gh/grpc/grpc-java)

Supported Platforms
-------------------

gRPC-Java supports Java 8 and later. Android minSdkVersion 21 (Lollipop) and
later are supported with [Java 8 language desugaring][android-java-8].

TLS usage on Android typically requires Play Services Dynamic Security Provider.
Please see the [Security Readme](SECURITY.md).

Older Java versions are not directly supported, but a branch remains available
for fixes and releases. See [gRFC P5 JDK Version Support
Policy][P5-jdk-version-support].

Java version | gRPC Branch
------------ | -----------
7            | 1.41.x

[android-java-8]: https://developer.android.com/studio/write/java8-support#supported_features
[P5-jdk-version-support]: https://github.com/grpc/proposal/blob/master/P5-jdk-version-support.md#proposal

Getting Started

## 整体架构描述


## 核心模块划分
High-level Components
---------------------

At a high level there are three distinct layers to the library: *Stub*,
*Channel*, and *Transport*.

### Stub

The Stub layer is what is exposed to most developers and provides type-safe
bindings to whatever datamodel/IDL/interface you are adapting. gRPC comes with
a [plugin](https://github.com/google/grpc-java/blob/master/compiler) to the
protocol-buffers compiler that generates Stub interfaces out of `.proto` files,
but bindings to other datamodel/IDL are easy and encouraged.

### Channel

The Channel layer is an abstraction over Transport handling that is suitable for
interception/decoration and exposes more behavior to the application than the
Stub layer. It is intended to be easy for application frameworks to use this
layer to address cross-cutting concerns such as logging, monitoring, auth, etc.

## 技术选型
- 构建工具: Gradle
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
