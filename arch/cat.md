# cat

## 项目简介
<img src="https://github.com/dianping/cat/raw/master/cat-home/src/main/webapp/images/logo/cat_logo03.png" width="50%">

**CAT**
==========
[![GitHub stars](https://img.shields.io/github/stars/dianping/cat.svg?style=flat-square&label=Star&)](https://github.com/dianping/cat/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/dianping/cat.svg?style=flat-square&label=Fork&)](https://github.com/dianping/cat/fork)
![Maven Central](https://img.shields.io/maven-central/v/org.unidal.framework/dal-jdbc)
![License](https://img.shields.io/github/license/dianping/cat.svg)
[![Build](https://github.com/dianping/cat/actions/workflows/build.yml/badge.svg)](https://github.com/dianping/cat/actions/workflows/build.yml)

### CAT 简介 

- CAT 是基于 Java 开发的实时应用监控平台，为美团点评提供了全面的实时监控告警服务。
- CAT 作为服务端项目基础组件，提供了 Java, C/C++, Node.js, Python, Go 等多语言客户端，已经在美团点评的基础架构中间件框架（MVC框架，RPC框架，数据库框架，缓存框架等，消息队列，配置系统等）深度集成，为美团点评各业务线提供系统丰富的性能指标、健康状况、实时告警等。
- CAT 很大的优势是它是一个实时系统，CAT 大部分系统是分钟级统计，但是从数据生成到服务端处理结束是秒级别，秒级定义是48分钟40秒，基本上看到48分钟38秒数据，整体报表的统计粒度是分钟级；第二个优势，监控数据是全量统计，客户端预计算；链路数据是采样计算。

### Cat 产品价值

- 减少故障发现时间
- 降低故障定位成本
- 辅助应用程序优化

### Cat 优势

- 实时处理：信息的价值会随时间锐减，尤其是事故处理过程中
- 全量数据：全量采集指标数据，便于深度分析故障案例
- 高可用：故障的还原与问题定位，需要高可用监控来支撑
- 故障容忍：故障不影响业务正常运转、对业务透明
- 高吞吐：海量监控数据的收集，需要高吞吐能力做保证
- 可扩展：支持分布式、跨 IDC 部署，横向扩展的监控系统

> 由于仓库的git历史记录众多，对于不关注历史，只关注最新版本或者基于最新版本贡献的新用户，可以在第一次克隆代码时增加--depth=1参数以加快下载速度，如
```bash
git clone --depth=1 https://github.com/dianping/cat.git
```

### 更新日志

- [**最新版本特性一览**](https://github.com/dianping/cat/wiki/new)

## 整体架构描述


## 核心模块划分


## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
