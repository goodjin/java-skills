# Helios PRD 分析

## 项目概述

**项目名称**: Helios  
**类型**: Docker容器编排平台  
**开源协议**: Apache License 2.0

## 简介

Helios是Spotify开源的Docker编排平台，用于在服务器集群中部署和管理容器化应用。该项目提供了HTTP API和命令行客户端，已被标记为停止维护(项目状态: Sunset)。

## 核心特性

- **HTTP API**: 完整的RESTful API接口
- **命令行客户端**: 便捷的helios命令行工具
- **服务发现**: 内置服务发现机制
- **部署管理**: 容器版本管理和滚动更新
- **事件追踪**: 记录集群事件历史

## 适用场景

- Docker容器集群管理(历史项目)
- 了解容器编排演进

## 技术状态

**⚠️ 已停止维护**: 由于Kubernetes等开源容器编排框架的出现，Spotify已停止使用Helios并转向Kubernetes。该项目不再接受PR。

## 技术栈

- Java开发
- Docker Engine
- ZooKeeper(服务发现)

---

*分析时间: 2026-02-27*
*注: 此项目已停止维护，不建议用于新项目*
