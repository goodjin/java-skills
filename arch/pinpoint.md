# pinpoint

## 项目简介


![Pinpoint](web/psd/logo.png)

[![Maven](https://img.shields.io/github/actions/workflow/status/pinpoint-apm/pinpoint/maven.yml?branch=master&label=build&logo=github)](https://github.com/pinpoint-apm/pinpoint/actions?query=workflow%3AMaven)
[![codecov](https://codecov.io/gh/pinpoint-apm/pinpoint/branch/master/graph/badge.svg)](https://codecov.io/gh/pinpoint-apm/pinpoint)

## Latest Release (2025/06/12)

We're happy to announce the release of Pinpoint v3.0.4.
Please check the release note at (https://github.com/pinpoint-apm/pinpoint/releases/tag/v3.0.4).

The current stable version is [v3.0.4](https://github.com/pinpoint-apm/pinpoint/releases/tag/v3.0.4).

## Live Demo

Take a quick look at Pinpoint with our [demo](http://223.130.142.103:8080/main/ApiGateway@SPRING_BOOT/5m?inbound=1&outbound=4&wasOnly=false&bidirectional=false)!

## PHP, PYTHON

Pinpoint also supports application written in PHP, Python. [Check-out our agent repository](https://github.com/pinpoint-apm/pinpoint-c-agent).

## About Pinpoint

**Pinpoint** is an APM (Application Performance Management) tool for large-scale distributed systems written in Java / [PHP](https://github.com/pinpoint-apm/pinpoint-c-agent)/[PYTHON]((https://github.com/pinpoint-apm/pinpoint-c-agent)).
Inspired by [Dapper](http://research.google.com/pubs/pub36356.html "Google Dapper"),
Pinpoint provides a solution to help analyze the overall structure of the system and how components within them are interconnected by tracing transactions across distributed applications.

You should definitely check **Pinpoint** out If you want to

* understand your *[application topology](https://pinpoint-apm.gitbook.io/pinpoint/want-a-quick-tour/overview)* at a glance
* monitor your application in *Real-Time*
* gain *code-level visibility* to every transaction
* install APM Agents *without changing a single line of code*
* have minimal impact on the performance (approximately 3% increase in resource usage)

## Getting Started
 * [Quick-start guide](https://pinpoint-apm.gitbook.io/pinpoint/getting-started/quickstart) for simple test run of Pinpoint
 * [Installation guide](https://pinpoint-apm.gitbook.io/pinpoint/getting-started/installation) for further instructions.

## 整体架构描述


## 核心模块划分
Pinpoint provides a solution to help analyze the overall structure of the system and how components within them are interconnected by tracing transactions across distributed applications.

You should definitely check **Pinpoint** out If you want to

* understand your *[application topology](https://pinpoint-apm.gitbook.io/pinpoint/want-a-quick-tour/overview)* at a glance
* monitor your application in *Real-Time*
* gain *code-level visibility* to every transaction
* install APM Agents *without changing a single line of code*
* have minimal impact on the performance (approximately 3% increase in resource usage)

## Getting Started
 * [Quick-start guide](https://pinpoint-apm.gitbook.io/pinpoint/getting-started/quickstart) for simple test run of Pinpoint
 * [Installation guide](https://pinpoint-apm.gitbook.io/pinpoint/getting-started/installation) for further instructions.

## Deploying Pinpoint to Kubernetes
 * [pinpoint-kubernetes](https://github.com/pinpoint-apm/pinpoint-kubernetes) 
 
## Overview
Services nowadays often consist of many different components, communicating amongst themselves as well as making API calls to external services. How each and every transaction gets executed is often left as a blackbox. Pinpoint traces transaction flows between these components and provides a clear view to identify problem areas and potential bottlenecks.<br/>
For a more intimate guide, please check out our *[Introduction to Pinpoint](https://pinpoint-apm.gitbook.io/pinpoint/#want-a-quick-tour)* video clip.

* **ServerMap** - Understand the topology of any distributed systems by visualizing how their components are interconnected. Clicking on a node reveals details about the component, such as its current status, and transaction count.
* **Realtime Active Thread Chart** - Monitor active threads inside applications in real-time.
* **Request/Response Scatter Chart** - Visualize request count and response patterns over time to identify potential problems. Transactions can be selected for additional detail by **dragging over the chart**.

## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
