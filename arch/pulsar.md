# pulsar

## 项目简介
<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->

![logo](https://pulsar.apache.org/img/pulsar.svg)

[![docker pull](https://img.shields.io/docker/pulls/apachepulsar/pulsar-all.svg)](https://hub.docker.com/r/apachepulsar/pulsar)
[![contributors](https://img.shields.io/github/contributors-anon/apache/pulsar)](https://github.com/apache/pulsar/graphs/contributors)
[![last commit](https://img.shields.io/github/last-commit/apache/pulsar)](https://github.com/apache/pulsar/commits/master)
[![release](https://img.shields.io/github/v/release/apache/pulsar?sort=semver)](https://pulsar.apache.org/download/)
[![downloads](https://img.shields.io/github/downloads/apache/pulsar/total)](https://pulsar.apache.org/download/)

Pulsar is a distributed pub-sub messaging platform with a very
flexible messaging model and an intuitive client API.

Learn more about Pulsar at https://pulsar.apache.org

## Main features

- Horizontally scalable (Millions of independent topics and millions
  of messages published per second)
- Strong ordering and consistency guarantees
- Low latency durable storage

## 整体架构描述


## 核心模块划分
components in the Pulsar ecosystem, including connectors, adapters, and other language clients.

- [Pulsar Core](https://github.com/apache/pulsar)

### Helm Chart

- [Pulsar Helm Chart](https://github.com/apache/pulsar-helm-chart)

### Ecosystem

- [Pulsar Adapters](https://github.com/apache/pulsar-adapters)

### Clients

- [.NET/C# Client](https://github.com/apache/pulsar-dotpulsar)
- [C++ Client](https://github.com/apache/pulsar-client-cpp)
- [Go Client](https://github.com/apache/pulsar-client-go)
- [NodeJS Client](https://github.com/apache/pulsar-client-node)
- [Python Client](https://github.com/apache/pulsar-client-python)
- [Reactive Java Client](https://github.com/apache/pulsar-client-reactive)

--
| Component       | Java Version |
|-----------------|:------------:|
| Broker          |      21      |

## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
