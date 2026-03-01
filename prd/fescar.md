# Fescar PRD 分析

## 项目概述

**项目名称**: Fescar (Seata)  
**类型**: 分布式事务解决方案  
**开源协议**: Apache License 2.0

## 简介

Fescar(Fast Easy Commit Atomic Transaction)是阿里巴巴开源的分布式事务解决方案，后更名为Seata( Simple Extensible Autonomous Transaction Architecture)。它为微服务架构提供了高性能和易用的分布式事务服务。

## 核心特性

- **AT模式**: 自动补偿模式，对业务无侵入
- **TCC模式**: Try-Confirm-Cancel三阶段提交
- **Saga模式**: 长事务解决方案
- **XA模式**: 标准XA分布式事务
- **高可用**: 支持集群部署
- **Seata Server**: 事务协调中心

## 适用场景

- 微服务分布式事务
- 跨库数据一致性
- 金融交易系统
- 电商订单处理

## 技术栈

- Java为核心
- 支持Spring Boot
- MySQL、Oracle、PostgreSQL等数据库

---

*分析时间: 2026-02-27*
*注: Fescar已演进为Apache Seata项目*
