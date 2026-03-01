# seata

## 项目简介
<!--
    Licensed to the Apache Software Foundation (ASF) under one or more
    contributor license agreements.  See the NOTICE file distributed with
    this work for additional information regarding copyright ownership.
    The ASF licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
-->
<div style="align: center">
<img src="https://img.alicdn.com/imgextra/i1/O1CN011z0JfQ2723QgDiWuH_!!6000000007738-2-tps-1497-401.png"  height="100" width="426"/>
</div>

# Apache Seata (incubating): Simple Extensible Autonomous Transaction Architecture

[![build](https://github.com/apache/incubator-seata/actions/workflows/build.yml/badge.svg)](https://github.com/apache/incubator-seata/actions/workflows/build.yml)
[![codecov](https://codecov.io/gh/apache/incubator-seata/graph/badge.svg?token=tbmHt2ZfxO)](https://codecov.io/gh/apache/incubator-seata)
[![license](https://img.shields.io/github/license/apache/incubator-seata.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![maven](https://img.shields.io/maven-central/v/org.apache.seata/seata-all?versionSuffix=2.5.0)](https://central.sonatype.com/search?q=org.apache.seata%3Aseata-all)

## What is Apache Seata (incubating)?

A **distributed transaction solution** with high performance and ease of use for **microservices** architecture.
### Distributed Transaction Problem in Microservices

Let's imagine a traditional monolithic application. Its business is built up with 3 modules. They use a single local data source.

Naturally, data consistency will be guaranteed by the local transaction.

![Monolithic App](https://img.alicdn.com/imgextra/i3/O1CN01FTtjyG1H4vvVh1sNY_!!6000000000705-0-tps-1106-678.jpg) 

Things have changed in a microservices architecture. The 3 modules mentioned above are designed to be 3 services on top of 3 different data sources ([Pattern: Database per service](http://microservices.io/patterns/data/database-per-service.html)). Data consistency within every single service is naturally guaranteed by the local transaction. 

## 整体架构描述
# Apache Seata (incubating): Simple Extensible Autonomous Transaction Architecture

[![build](https://github.com/apache/incubator-seata/actions/workflows/build.yml/badge.svg)](https://github.com/apache/incubator-seata/actions/workflows/build.yml)
[![codecov](https://codecov.io/gh/apache/incubator-seata/graph/badge.svg?token=tbmHt2ZfxO)](https://codecov.io/gh/apache/incubator-seata)
[![license](https://img.shields.io/github/license/apache/incubator-seata.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![maven](https://img.shields.io/maven-central/v/org.apache.seata/seata-all?versionSuffix=2.5.0)](https://central.sonatype.com/search?q=org.apache.seata%3Aseata-all)

## What is Apache Seata (incubating)?

A **distributed transaction solution** with high performance and ease of use for **microservices** architecture.
### Distributed Transaction Problem in Microservices

Let's imagine a traditional monolithic application. Its business is built up with 3 modules. They use a single local data source.

Naturally, data consistency will be guaranteed by the local transaction.

![Monolithic App](https://img.alicdn.com/imgextra/i3/O1CN01FTtjyG1H4vvVh1sNY_!!6000000000705-0-tps-1106-678.jpg) 

Things have changed in a microservices architecture. The 3 modules mentioned above are designed to be 3 services on top of 3 different data sources ([Pattern: Database per service](http://microservices.io/patterns/data/database-per-service.html)). Data consistency within every single service is naturally guaranteed by the local transaction. 

**But how about the whole business logic scope?**

![Microservices Problem](https://img.alicdn.com/imgextra/i1/O1CN01DXkc3o1te9mnJcHOr_!!6000000005926-0-tps-1268-804.jpg) 

## Architecture

Apache Seata offers a robust solution to the distributed transaction challenges in microservices.

![Seata solution](https://img.alicdn.com/imgextra/i1/O1CN01FheliH1k5VHIRob3p_!!6000000004632-0-tps-1534-908.jpg)

### Core Concepts

A **Distributed Transaction** is a **Global Transaction** composed of a batch of **Branch Transactions**, where a **Branch Transaction** is typically a **Local Transaction**.

![Global & Branch](https://cdn.nlark.com/lark/0/2018/png/18862/1545015454979-a18e16f6-ed41-44f1-9c7a-bd82c4d5ff99.png) 

## 核心模块划分
Let's imagine a traditional monolithic application. Its business is built up with 3 modules. They use a single local data source.

Naturally, data consistency will be guaranteed by the local transaction.

![Monolithic App](https://img.alicdn.com/imgextra/i3/O1CN01FTtjyG1H4vvVh1sNY_!!6000000000705-0-tps-1106-678.jpg) 

Things have changed in a microservices architecture. The 3 modules mentioned above are designed to be 3 services on top of 3 different data sources ([Pattern: Database per service](http://microservices.io/patterns/data/database-per-service.html)). Data consistency within every single service is naturally guaranteed by the local transaction. 

**But how about the whole business logic scope?**

![Microservices Problem](https://img.alicdn.com/imgextra/i1/O1CN01DXkc3o1te9mnJcHOr_!!6000000005926-0-tps-1268-804.jpg) 

## Architecture

Apache Seata offers a robust solution to the distributed transaction challenges in microservices.

![Seata solution](https://img.alicdn.com/imgextra/i1/O1CN01FheliH1k5VHIRob3p_!!6000000004632-0-tps-1534-908.jpg)

### Core Concepts

A **Distributed Transaction** is a **Global Transaction** composed of a batch of **Branch Transactions**, where a **Branch Transaction** is typically a **Local Transaction**.

![Global & Branch](https://cdn.nlark.com/lark/0/2018/png/18862/1545015454979-a18e16f6-ed41-44f1-9c7a-bd82c4d5ff99.png) 

### Roles

## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
