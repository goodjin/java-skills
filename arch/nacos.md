# nacos

## 项目简介

<img src="doc/Nacos_Logo.png" width="50%" syt height="50%" />

# Nacos: Dynamic  *Na*ming and *Co*nfiguration *S*ervice

[![Gitter](https://badges.gitter.im/alibaba/nacos.svg)](https://gitter.im/alibaba/nacos?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)   [![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Gitter](https://travis-ci.org/alibaba/nacos.svg?branch=master)](https://travis-ci.org/alibaba/nacos)
[![](https://img.shields.io/badge/Nacos-Check%20Your%20Contribution-orange)](https://opensource.alibaba.com/contribution_leaderboard/details?projectValue=nacos)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/alibaba/nacos)

-------

## What does it do

Nacos (official site: [nacos.io](https://nacos.io)) is an easy-to-use platform designed for dynamic service discovery and configuration and service management. It helps you to build cloud native applications and microservices platform easily.

Service is a first-class citizen in Nacos. Nacos supports almost all type of services，for example，[Dubbo/gRPC service](https://nacos.io/docs/latest/ecology/use-nacos-with-dubbo/), [Spring Cloud RESTFul service](https://nacos.io/docs/latest/ecology/use-nacos-with-spring-cloud/) or [Kubernetes service](https://nacos.io/docs/latest/quickstart/quick-start-kubernetes/).

Nacos provides four major functions.

* **Service Discovery and Service Health Check** 
    
    Nacos makes it simple for services to register themselves and to discover other services via a DNS or HTTP interface. Nacos also provides real-time health checks of services to prevent sending requests to unhealthy hosts or service instances.

* **Dynamic Configuration Management**
  
    Dynamic Configuration Service allows you to manage configurations of all services in a centralized and dynamic manner across all environments. Nacos eliminates the need to redeploy applications and services when configurations are updated, which makes configuration changes more efficient and agile.

* **Dynamic DNS Service**
    
    Nacos supports weighted routing, making it easier for you to implement mid-tier load balancing, flexible routing policies, flow control, and simple DNS resolution services in the production environment within your data center. It helps you to implement DNS-based service discovery easily and prevent applications from coupling to vendor-specific service discovery APIs.

* **Service and MetaData Management**
	
    Nacos provides an easy-to-use service dashboard to help you manage your services metadata, configuration, kubernetes DNS, service health and metrics statistics.
 

## Quick Start
It is super easy to get started with your first project.

## 整体架构描述
You can also read this online eBook from the [NACOS ARCHITECTURE & PRINCIPLES](https://nacos.io/docs/ebook/kbyo6n/).

All the latest and long-term notice can also be found here from [GitHub notice issue](https://github.com/alibaba/nacos/labels/notice).

## Contributing

Contributors are welcomed to join Nacos project. Please check [CONTRIBUTING](./CONTRIBUTING.md) about how to contribute to this project.

### How can I contribute?

* Take a look at issues with tags marked [`good first issue`](https://github.com/alibaba/nacos/issues?q=is%3Aopen+is%3Aissue+label%3A%22good+first+issue%22) or [`contribution welcome`](https://github.com/alibaba/nacos/issues?q=is%3Aopen+is%3Aissue+label%3A%22contribution+welcome%22).
* Answer questions on [issues](https://github.com/alibaba/nacos/issues).
* Fix bugs reported on [issues](https://github.com/alibaba/nacos/issues), and send us a pull request.
* Review the existing [pull request](https://github.com/alibaba/nacos/pulls).
* Improve the [website](https://github.com/nacos-group/nacos-group.github.io), typically we need
  * blog post
  * translation on documentation
  * use cases around the integration of Nacos in enterprise systems.

## Other Related Project Repositories

* [nacos-spring-project](https://github.com/nacos-group/nacos-spring-project) provides the integration functionality for Spring.
* [nacos-group](https://github.com/nacos-group) is the repository that hosts the eco tools for Nacos, such as SDK, synchronization tool, etc.
* [spring-cloud-alibaba](https://github.com/spring-cloud-incubator/spring-cloud-alibaba) provides the one-stop solution for application development over Alibaba middleware which includes Nacos.

## Contact

* [Gitter](https://gitter.im/alibaba/nacos): Nacos's IM tool for community messaging, collaboration and discovery.
* [Twitter](https://twitter.com/nacos2): Follow along for latest nacos news on Twitter.
* [Weibo](https://weibo.com/u/6574374908): Follow along for latest nacos news on Weibo (Twitter of China version).
* [Nacos Segmentfault](https://segmentfault.com/t/nacos): Get latest notice and prompt help from Segmentfault.

## 核心模块划分


## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
