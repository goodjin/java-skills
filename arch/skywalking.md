# skywalking

## 项目简介
Apache SkyWalking
==========

<img src="http://skywalking.apache.org/assets/logo.svg" alt="Sky Walking logo" height="90px" align="right" />

**SkyWalking**: an APM (Application Performance Monitoring) system, especially designed for
microservices, cloud native and container-based architectures.

[![GitHub stars](https://img.shields.io/github/stars/apache/skywalking.svg?style=for-the-badge&label=Stars&logo=github)](https://github.com/apache/skywalking)
[![X Follow](https://img.shields.io/badge/2K%2B-follow?style=for-the-badge&logo=X&label=%40ASFSKYWALKING)](https://x.com/AsfSkyWalking)

![GitHub Release](https://img.shields.io/github/v/release/apache/skywalking)

# Abstract
**SkyWalking** is an open-source APM system that provides monitoring, tracing and diagnosing capabilities for distributed systems in Cloud Native architectures.


* Distributed Tracing
  * End-to-end distributed tracing. Service topology analysis, service-centric observability and APIs dashboards.
* Agents for your stack
  * Java, .Net Core, PHP, NodeJS, Golang, LUA, Rust, C++, Client JavaScript and Python agents with active development and maintenance.
* eBPF early adoption
  * Rover agent works as a monitor and profiler powered by eBPF to monitor Kubernetes deployments and diagnose CPU and network performance.
* Scaling
  * 100+ billion telemetry data could be collected and analyzed from one SkyWalking cluster.
* Mature Telemetry Ecosystems Supported
  * Metrics, Traces, and Logs from mature ecosystems are supported, e.g. Zipkin, OpenTelemetry, Prometheus, Zabbix, Fluentd
* Native APM Database
  * BanyanDB, an observability database, created in 2022, aims to ingest, analyze and store telemetry/observability data.
* Consistent Metrics Aggregation
  * SkyWalking native meter format and widely known metrics format(OpenTelemetry, Telegraf, Zabbix, e.g.) are processed through the same script pipeline.
* Log Management Pipeline
  * Support log formatting, extract metrics, various sampling policies through script pipeline in high performance.
* Alerting and Telemetry Pipelines
  * Support service-centric, deployment-centric, API-centric alarm rule setting. Support forwarding alarms and all telemetry data to 3rd party.
* AI Power Enabled
  * Machine Learning (ML) and Artificial Intelligence (AI) analyze observability data to identify patterns and enhance capabilities, such as recognizing HTTP URI patterns and automatically calculating metric baselines for intelligent alerting, improving anomaly detection.

<img src="https://skywalking.apache.org/images/home/architecture.svg?t=20220516"/>

## 整体架构描述
microservices, cloud native and container-based architectures.

[![GitHub stars](https://img.shields.io/github/stars/apache/skywalking.svg?style=for-the-badge&label=Stars&logo=github)](https://github.com/apache/skywalking)
[![X Follow](https://img.shields.io/badge/2K%2B-follow?style=for-the-badge&logo=X&label=%40ASFSKYWALKING)](https://x.com/AsfSkyWalking)

![GitHub Release](https://img.shields.io/github/v/release/apache/skywalking)

# Abstract
**SkyWalking** is an open-source APM system that provides monitoring, tracing and diagnosing capabilities for distributed systems in Cloud Native architectures.


* Distributed Tracing
  * End-to-end distributed tracing. Service topology analysis, service-centric observability and APIs dashboards.
* Agents for your stack
  * Java, .Net Core, PHP, NodeJS, Golang, LUA, Rust, C++, Client JavaScript and Python agents with active development and maintenance.
* eBPF early adoption
  * Rover agent works as a monitor and profiler powered by eBPF to monitor Kubernetes deployments and diagnose CPU and network performance.
* Scaling
  * 100+ billion telemetry data could be collected and analyzed from one SkyWalking cluster.
* Mature Telemetry Ecosystems Supported
  * Metrics, Traces, and Logs from mature ecosystems are supported, e.g. Zipkin, OpenTelemetry, Prometheus, Zabbix, Fluentd
* Native APM Database
  * BanyanDB, an observability database, created in 2022, aims to ingest, analyze and store telemetry/observability data.
* Consistent Metrics Aggregation
  * SkyWalking native meter format and widely known metrics format(OpenTelemetry, Telegraf, Zabbix, e.g.) are processed through the same script pipeline.
* Log Management Pipeline
  * Support log formatting, extract metrics, various sampling policies through script pipeline in high performance.
* Alerting and Telemetry Pipelines
  * Support service-centric, deployment-centric, API-centric alarm rule setting. Support forwarding alarms and all telemetry data to 3rd party.
* AI Power Enabled
  * Machine Learning (ML) and Artificial Intelligence (AI) analyze observability data to identify patterns and enhance capabilities, such as recognizing HTTP URI patterns and automatically calculating metric baselines for intelligent alerting, improving anomaly detection.

<img src="https://skywalking.apache.org/images/home/architecture.svg?t=20220516"/>

# Live Demo

## 核心模块划分


## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
