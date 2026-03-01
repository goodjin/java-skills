# canal

## 项目简介
[![build status](https://travis-ci.com/alibaba/canal.svg?branch=master)](https://travis-ci.com/alibaba/canal)
[![codecov](https://codecov.io/gh/alibaba/canal/branch/master/graph/badge.svg)](https://codecov.io/gh/alibaba/canal)
![maven](https://img.shields.io/maven-central/v/com.alibaba.otter/canal.svg)
![license](https://img.shields.io/github/license/alibaba/canal.svg)
[![average time to resolve an issue](http://isitmaintained.com/badge/resolution/alibaba/canal.svg)](http://isitmaintained.com/project/alibaba/canal "average time to resolve an issue")
[![percentage of issues still open](http://isitmaintained.com/badge/open/alibaba/canal.svg)](http://isitmaintained.com/project/alibaba/canal "percentage of issues still open")
[![Leaderboard](https://img.shields.io/badge/Canal-%E6%9F%A5%E7%9C%8B%E8%B4%A1%E7%8C%AE%E6%8E%92%E8%A1%8C%E6%A6%9C-orange)](https://opensource.alibaba.com/contribution_leaderboard/details?projectValue=canal)


## 简介

![](https://img-blog.csdnimg.cn/20191104101735947.png)

**canal [kə'næl]**，译意为水道/管道/沟渠，主要用途是基于 MySQL 数据库增量日志解析，提供增量数据订阅和消费

早期阿里巴巴因为杭州和美国双机房部署，存在跨机房同步的业务需求，实现方式主要是基于业务 trigger 获取增量变更。从 2010 年开始，业务逐步尝试数据库日志解析获取增量变更进行同步，由此衍生出了大量的数据库增量订阅和消费业务。

基于日志增量订阅和消费的业务包括
- 数据库镜像
- 数据库实时备份
- 索引构建和实时维护(拆分异构索引、倒排索引等)
- 业务 cache 刷新
- 带业务逻辑的增量数据处理

当前的 canal 支持源端 MySQL 版本包括 5.1.x , 5.5.x , 5.6.x , 5.7.x , 8.0.x

## 工作原理

#### MySQL主备复制原理
![](http://dl.iteye.com/upload/attachment/0080/3086/468c1a14-e7ad-3290-9d3d-44ac501a7227.jpg)

- MySQL master 将数据变更写入二进制日志( binary log, 其中记录叫做二进制日志事件binary log events，可以通过 show binlog events 进行查看)
- MySQL slave 将 master 的 binary log events 拷贝到它的中继日志(relay log)
- MySQL slave 重放 relay log 中事件，将数据变更反映它自己的数据

#### canal 工作原理

- canal 模拟 MySQL slave 的交互协议，伪装自己为 MySQL slave ，向 MySQL master 发送dump 协议
- MySQL master 收到 dump 请求，开始推送 binary log 给 slave (即 canal )
- canal 解析 binary log 对象(原始为 byte 流)

## 整体架构描述


## 核心模块划分


## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
