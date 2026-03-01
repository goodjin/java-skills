# dubbo-2

## 项目简介

# Apache Dubbo Project

[![Build and Test For PR](https://github.com/apache/dubbo/actions/workflows/build-and-test-pr.yml/badge.svg)](https://github.com/apache/dubbo/actions/workflows/build-and-test-pr.yml)
[![Codecov](https://codecov.io/gh/apache/dubbo/branch/3.3/graph/badge.svg)](https://codecov.io/gh/apache/dubbo)
[![Maven](https://img.shields.io/github/v/release/apache/dubbo.svg?sort=semver)](https://github.com/apache/dubbo/releases)
[![License](https://img.shields.io/github/license/apache/dubbo.svg)](https://github.com/apache/dubbo/blob/3.3/LICENSE)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/apache/dubbo.svg)](http://isitmaintained.com/project/apache/dubbo)
[![Percentage of issues still open](http://isitmaintained.com/badge/open/apache/dubbo.svg)](http://isitmaintained.com/project/apache/dubbo)

Apache Dubbo is a powerful and user-friendly Web and RPC framework. It supports multiple language implementations such as Java, [Go](https://github.com/apache/dubbo-go), [Python](https://github.com/dubbo/py-client-for-apache-dubbo), [PHP](https://github.com/apache/dubbo-php-framework), [Erlang](https://github.com/apache/dubbo-erlang), [Rust](https://github.com/apache/dubbo-rust), and [Node.js/Web](https://github.com/apache/dubbo-js).

Dubbo provides solutions for communication, service discovery, traffic management, observability, security, tooling, and best practices for building enterprise-grade microservices.

> 🚀 We're collecting user info to improve Dubbo. Help us out here: [Who's using Dubbo](https://github.com/apache/dubbo/discussions/13842)

---

## 🧱 Architecture

![Architecture](https://dubbo.apache.org/imgs/architecture.png)

- Communication between consumers and providers is done via RPC protocols like Triple, TCP, REST, etc.
- Consumers dynamically discover provider instances from registries (e.g., Zookeeper, Nacos) and manage traffic using defined strategies.
- Built-in support for dynamic config, metrics, tracing, security, and a visualized console.

---

## 🚀 Getting Started

### 📦 Lightweight RPC API

Start quickly with our [5-minute guide](https://cn.dubbo.apache.org/zh-cn/overview/mannual/java-sdk/tasks/framework/lightweight-rpc/)

Dubbo allows you to build RPC services using a minimal codebase and a lightweight SDK. It supports protocols like:

- [Triple (gRPC-compatible)](https://dubbo.apache.org/zh-cn/overview/reference/protocols/triple/)
- Dubbo2 (TCP)
- REST
- Custom protocols

## 整体架构描述
## 🧱 Architecture

![Architecture](https://dubbo.apache.org/imgs/architecture.png)

- Communication between consumers and providers is done via RPC protocols like Triple, TCP, REST, etc.
- Consumers dynamically discover provider instances from registries (e.g., Zookeeper, Nacos) and manage traffic using defined strategies.
- Built-in support for dynamic config, metrics, tracing, security, and a visualized console.

---

## 🚀 Getting Started

### 📦 Lightweight RPC API

Start quickly with our [5-minute guide](https://cn.dubbo.apache.org/zh-cn/overview/mannual/java-sdk/tasks/framework/lightweight-rpc/)

Dubbo allows you to build RPC services using a minimal codebase and a lightweight SDK. It supports protocols like:

- [Triple (gRPC-compatible)](https://dubbo.apache.org/zh-cn/overview/reference/protocols/triple/)
- Dubbo2 (TCP)
- REST
- Custom protocols

### 🌱 Microservices with Spring Boot

Kickstart your project using [Spring Boot Starter](https://cn.dubbo.apache.org/zh-cn/overview/mannual/java-sdk/tasks/develop/springboot/).

Using just a dependency and a YAML config, you can unlock the full power of Dubbo: service discovery, observability, tracing, etc.

➡️ Learn how to [deploy](https://dubbo.apache.org/zh-cn/overview/tasks/deploy/), [monitor](https://dubbo.apache.org/zh-cn/overview/tasks/observability/), and [manage traffic](https://dubbo.apache.org/zh-cn/overview/tasks/traffic-management/) for Dubbo services.

---

## 核心模块划分


## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
