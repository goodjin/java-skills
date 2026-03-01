# shardingsphere

## 项目简介
## [Apache ShardingSphere - Enterprise Distributed Database Ecosystem](https://shardingsphere.apache.org/)

Building the standards and ecosystem on top of heterogeneous databases, empowering enterprise data architecture transformation

**Official Website:** [https://shardingsphere.apache.org/](https://shardingsphere.apache.org/)

[![GitHub Release](https://img.shields.io/github/release/apache/shardingsphere.svg)](https://github.com/apache/shardingsphere/releases)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=apache_shardingsphere&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=apache_shardingsphere)

[![CI](https://github.com/apache/shardingsphere/actions/workflows/ci.yml/badge.svg)](https://github.com/apache/shardingsphere/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=apache_shardingsphere&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=apache_shardingsphere)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=apache_shardingsphere&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=apache_shardingsphere)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=apache_shardingsphere&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=apache_shardingsphere)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=apache_shardingsphere&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=apache_shardingsphere)
[![codecov](https://codecov.io/gh/apache/shardingsphere/branch/master/graph/badge.svg)](https://codecov.io/gh/apache/shardingsphere)

[![OpenSSF Best Practices](https://bestpractices.coreinfrastructure.org/projects/5394/badge)](https://bestpractices.coreinfrastructure.org/projects/5394)

[![Slack](https://img.shields.io/badge/%20Slack-ShardingSphere%20Channel-blueviolet)](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)
[![Gitter](https://badges.gitter.im/shardingsphere/shardingsphere.svg)](https://gitter.im/shardingsphere/Lobby)

[![X](https://img.shields.io/twitter/url/https/twitter.com/ShardingSphere.svg?style=social&label=Follow%20%40ShardingSphere)](https://x.com/ShardingSphere)

<table style="width:100%">
    <tr>
        <th>
            <a href="https://next.ossinsight.io/widgets/official/analyze-repo-stars-map?activity=stars&repo_id=49876476" target="_blank" style="display: block" align="center">
                <picture>
                    <source media="(prefers-color-scheme: dark)" srcset="https://next.ossinsight.io/widgets/official/analyze-repo-stars-map/thumbnail.png?activity=stars&repo_id=49876476&image_size=auto&color_scheme=dark" width="721" height="auto">
                    <img alt="Star Geographical Distribution of apache/shardingsphere" src="https://next.ossinsight.io/widgets/official/analyze-repo-stars-map/thumbnail.png?activity=stars&repo_id=49876476&image_size=auto&color_scheme=light" width="721" height="auto">
                </picture>
            </a>
        </th>
        <th>
            <a href="https://next.ossinsight.io/widgets/official/analyze-repo-stars-map?activity=pull-request-creators&repo_id=49876476" target="_blank" style="display: block" align="center">
                <picture>
                    <source media="(prefers-color-scheme: dark)" srcset="https://next.ossinsight.io/widgets/official/analyze-repo-stars-map/thumbnail.png?activity=pull-request-creators&repo_id=49876476&image_size=auto&color_scheme=dark" width="721" height="auto">
                    <img alt="Pull Request Creator Geographical Distribution of apache/shardingsphere" src="https://next.ossinsight.io/widgets/official/analyze-repo-stars-map/thumbnail.png?activity=pull-request-creators&repo_id=49876476&image_size=auto&color_scheme=light" width="721" height="auto">
                </picture>
            </a>

## 整体架构描述
Building the standards and ecosystem on top of heterogeneous databases, empowering enterprise data architecture transformation

**Official Website:** [https://shardingsphere.apache.org/](https://shardingsphere.apache.org/)

[![GitHub Release](https://img.shields.io/github/release/apache/shardingsphere.svg)](https://github.com/apache/shardingsphere/releases)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=apache_shardingsphere&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=apache_shardingsphere)

[![CI](https://github.com/apache/shardingsphere/actions/workflows/ci.yml/badge.svg)](https://github.com/apache/shardingsphere/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=apache_shardingsphere&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=apache_shardingsphere)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=apache_shardingsphere&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=apache_shardingsphere)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=apache_shardingsphere&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=apache_shardingsphere)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=apache_shardingsphere&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=apache_shardingsphere)
[![codecov](https://codecov.io/gh/apache/shardingsphere/branch/master/graph/badge.svg)](https://codecov.io/gh/apache/shardingsphere)

[![OpenSSF Best Practices](https://bestpractices.coreinfrastructure.org/projects/5394/badge)](https://bestpractices.coreinfrastructure.org/projects/5394)

[![Slack](https://img.shields.io/badge/%20Slack-ShardingSphere%20Channel-blueviolet)](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)
[![Gitter](https://badges.gitter.im/shardingsphere/shardingsphere.svg)](https://gitter.im/shardingsphere/Lobby)

[![X](https://img.shields.io/twitter/url/https/twitter.com/ShardingSphere.svg?style=social&label=Follow%20%40ShardingSphere)](https://x.com/ShardingSphere)

<table style="width:100%">
    <tr>
        <th>
            <a href="https://next.ossinsight.io/widgets/official/analyze-repo-stars-map?activity=stars&repo_id=49876476" target="_blank" style="display: block" align="center">
                <picture>
                    <source media="(prefers-color-scheme: dark)" srcset="https://next.ossinsight.io/widgets/official/analyze-repo-stars-map/thumbnail.png?activity=stars&repo_id=49876476&image_size=auto&color_scheme=dark" width="721" height="auto">
                    <img alt="Star Geographical Distribution of apache/shardingsphere" src="https://next.ossinsight.io/widgets/official/analyze-repo-stars-map/thumbnail.png?activity=stars&repo_id=49876476&image_size=auto&color_scheme=light" width="721" height="auto">
                </picture>
            </a>
        </th>
--
**Database Plus Core Concept**: By building a standardized and scalable enhancement layer above databases, it makes heterogeneous databases as simple to use as a single database, providing unified governance capabilities and distributed computing capabilities for enterprise data architectures.

**Connect, Enhance, and Pluggable** are the three core pillars of Apache ShardingSphere:

## 核心模块划分
- **Pluggable:** Adopting a micro-kernel + 3-layer pluggable architecture to achieve complete decoupling of kernel, functional components, and ecosystem integration. Developers can flexibly customize unique data architecture solutions that meet enterprise needs, just like building with LEGO blocks.

**Differentiation Advantages**:
- **vs Distributed Databases**: More lightweight, protecting existing investments, avoiding vendor lock-in
- **vs Traditional Middleware**: Richer features, more complete ecosystem, more flexible architecture
- **vs Cloud Vendor Solutions**: Support multi-cloud deployment, avoid technology binding, autonomous and controllable

ShardingSphere became an [Apache](https://apache.org/index.html#projects-list) Top-Level Project on April 16, 2020, and has been adopted by [19,000+ projects](https://github.com/search?l=Maven+POM&q=shardingsphere+language%3A%22Maven+POM%22&type=Code) worldwide.

### DUAL-ACCESS ARCHITECTURE DESIGN

<hr>

ShardingSphere adopts a unique dual-access architecture design, providing two access ends - JDBC and Proxy - that can be deployed independently or in hybrid deployment, meeting diverse requirements for different scenarios.

#### ShardingSphere-JDBC: Lightweight Access End

**Positioning**: Lightweight Java framework, enhanced JDBC driver

**Core Features**:
- **Client-side direct connection**: Shares resources with applications, decentralized architecture
--
Apache ShardingSphere adopts a micro-kernel + 3-layer pluggable architecture, achieving complete decoupling of the kernel, functional components, and ecosystem integration, providing developers with ultimate flexibility and extensibility.

#### Micro-Kernel + 3-Layer Pluggable Model

## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
