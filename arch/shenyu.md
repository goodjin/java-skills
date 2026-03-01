# shenyu

## 项目简介
![Light Logo](https://raw.githubusercontent.com/apache/shenyu-website/main/static/img/logo-light.svg#gh-dark-mode-only)
![Dark Logo](https://raw.githubusercontent.com/apache/shenyu-website/main/static/img/logo.svg#gh-light-mode-only)

<p align="center">
  <strong>Scalable, High Performance, Responsive API Gateway Solution for all MicroServices</strong>
</p>
<p align="center">
  <a href="https://shenyu.apache.org/">https://shenyu.apache.org/</a>
</p>

<p align="center">
  <a href="https://shenyu.apache.org/docs/index" >
    <img src="https://img.shields.io/badge/document-English-blue.svg" alt="EN docs" />
  </a>
  <a href="https://shenyu.apache.org/zh/docs/index">
    <img src="https://img.shields.io/badge/文档-简体中文-blue.svg" alt="简体中文文档" />
  </a>
</p>

<p align="center">
    <a target="_blank" href="https://search.maven.org/search?q=g:org.apache.shenyu%20AND%20a:shenyu">
        <img src="https://img.shields.io/maven-central/v/org.apache.shenyu/shenyu.svg?label=maven%20central" />
    </a>
    <a target="_blank" href="https://github.com/apache/shenyu/blob/master/LICENSE">
        <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg?label=license" />
    </a>
    <a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html">
        <img src="https://img.shields.io/badge/JDK-17+-green.svg" />
    </a>
    <a target="_blank" href="https://github.com/apache/shenyu/actions">
        <img src="https://github.com/apache/shenyu/workflows/ci/badge.svg" />
    </a>
   <a target="_blank" href='https://github.com/apache/shenyu'>
        <img src="https://img.shields.io/github/forks/apache/shenyu.svg" alt="github forks"/>
   </a>
   <a target="_blank" href='https://github.com/apache/shenyu'>
        <img src="https://img.shields.io/github/stars/apache/shenyu.svg" alt="github stars"/>
   </a>
   <a target="_blank" href='https://github.com/apache/shenyu'>
        <img src="https://img.shields.io/github/contributors/apache/shenyu.svg" alt="github contributors"/>

## 整体架构描述
# Architecture
 
 ![](https://shenyu.apache.org/img/architecture/shenyu-architecture-3d.png)  
 
---- 

# Why named Apache ShenYu

ShenYu (神禹) is the honorific name of Chinese ancient monarch Xia Yu (also known in later times as Da Yu), 
who left behind the touching story of the three times he crossed the Yellow River for the benefit of the people and successfully managed the flooding of the river. 
He is known as one of the three greatest kings of ancient China, along with Yao and Shun.

   * Firstly, the name ShenYu is to promote the traditional virtues of our Chinese civilisation.

   * Secondly, the most important thing about the gateway is the governance of the traffic.

   * Finally, the community will do things in a fair, just, open and meritocratic way, paying tribute to ShenYu while also conforming to the Apache Way.

--- 

# Features

* Proxy: Support for Apache® Dubbo™, Spring Cloud, gRPC, Motan, SOFA, TARS, WebSocket, MQTT
* Security: Sign, OAuth 2.0, JSON Web Tokens, WAF plugin
* API governance: Request, response, parameter mapping, Hystrix, RateLimiter plugin
* Observability: Tracing, metrics, logging plugin
* Dashboard: Dynamic traffic control, visual backend for user menu permissions
* Extensions: Plugin hot-swapping, dynamic loading
* Cluster: NGINX, Docker, Kubernetes
* Language: provides .NET, Python, Go, Java client for API register
   
---  

## 核心模块划分
  Selector is your first route, It is coarser grained, for example, at the module level.
  
  Rule is your second route and what do you think your request should do. For example a method level in a module.
  
  The selector and the rule match only once, and the match is returned. So the coarsest granularity should be sorted last.
 
---  
   
# Data Caching & Data Sync
 
  Since all data have been cached using ConcurrentHashMap in the JVM, it's very fast.
  
  Apache ShenYu dynamically updates the cache by listening to the ZooKeeper node (or WebSocket push, HTTP long polling) when the user changes configuration information in the background management.
  
  ![](https://shenyu.apache.org/img/shenyu/dataSync/shenyu-config-processor-en.png)
  
  ![](https://shenyu.apache.org/img/shenyu/dataSync/config-strategy-processor-en.png)

---    

# Prerequisite
 
   * JDK 17+

## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
