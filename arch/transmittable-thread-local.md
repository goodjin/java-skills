# transmittable-thread-local

## 项目简介
# <div align="center"><a href="#dummy"><img src="https://user-images.githubusercontent.com/1063891/233595946-4493119e-4e0c-4081-a382-0a20731c578e.png" alt="📌 TransmittableThreadLocal(TTL)"></a></div>

> [!IMPORTANT]
> 🚧 这个分支是`TransmittableThreadLocal(TTL) v3`，在开发中还没有发布。  
> `v3`的版本说明、工作项列表及其进展，参见 [issue 432](https://github.com/alibaba/transmittable-thread-local/issues/432)。
>
> 👉 目前使用中的稳定发布版本`v2.x`在 [**分支`2.x`**](https://github.com/alibaba/transmittable-thread-local/tree/2.x)上。

<p align="center">
<a href="https://github.com/alibaba/transmittable-thread-local/actions/workflows/ci.yaml"><img src="https://img.shields.io/github/actions/workflow/status/alibaba/transmittable-thread-local/ci.yaml?branch=master&logo=github&logoColor=white&label=fast ci" alt="Fast CI"></a>
<a href="https://github.com/alibaba/transmittable-thread-local/actions/workflows/strong_ci.yaml"><img src="https://img.shields.io/github/actions/workflow/status/alibaba/transmittable-thread-local/strong_ci.yaml?branch=master&logo=github&logoColor=white&label=strong ci" alt="Strong CI"></a>
<a href="https://app.codecov.io/gh/alibaba/transmittable-thread-local/tree/master"><img src="https://img.shields.io/codecov/c/github/alibaba/transmittable-thread-local/master?logo=codecov&logoColor=white" alt="Coverage Status"></a>
<a href="https://openjdk.java.net/"><img src="https://img.shields.io/badge/Java-6+-339933?logo=openjdk&logoColor=white" alt="JDK support"></a>
<a href="https://www.apache.org/licenses/LICENSE-2.0.html"><img src="https://img.shields.io/github/license/alibaba/transmittable-thread-local?color=4D7A97&logo=apache" alt="License"></a>
<a href="https://alibaba.github.io/transmittable-thread-local/apidocs/"><img src="https://img.shields.io/github/release/alibaba/transmittable-thread-local?label=javadoc&color=339933&logo=read-the-docs&logoColor=white" alt="Javadocs"></a>
<a href="https://repo1.maven.org/maven2/com/alibaba/transmittable-thread-local/maven-metadata.xml"><img src="https://img.shields.io/maven-central/v/com.alibaba/transmittable-thread-local?logo=apache-maven&logoColor=white" alt="Maven Central"></a>
<a href="https://github.com/alibaba/transmittable-thread-local/releases"><img src="https://img.shields.io/github/release/alibaba/transmittable-thread-local" alt="GitHub release"></a>
<a href="https://github.com/alibaba/transmittable-thread-local/stargazers"><img src="https://img.shields.io/github/stars/alibaba/transmittable-thread-local?style=flat" alt="GitHub Stars"></a>
<a href="https://github.com/alibaba/transmittable-thread-local/fork"><img src="https://img.shields.io/github/forks/alibaba/transmittable-thread-local?style=flat" alt="GitHub Forks"></a>
<a href="https://github.com/alibaba/transmittable-thread-local/network/dependents"><img src="https://badgen.net/github/dependents-repo/alibaba/transmittable-thread-local?label=user%20repos" alt="user repos"></a>
<a href="https://github.com/alibaba/transmittable-thread-local/issues"><img src="https://img.shields.io/github/issues/alibaba/transmittable-thread-local" alt="GitHub issues"></a>
<a href="https://github.com/alibaba/transmittable-thread-local/graphs/contributors"><img src="https://img.shields.io/github/contributors/alibaba/transmittable-thread-local" alt="GitHub Contributors"></a>
<a href="https://github.com/alibaba/transmittable-thread-local"><img src="https://img.shields.io/github/repo-size/alibaba/transmittable-thread-local" alt="GitHub repo size"></a>
<a href="https://gitpod.io/#https://github.com/alibaba/transmittable-thread-local"><img src="https://img.shields.io/badge/Gitpod-ready to code-339933?label=gitpod&logo=gitpod&logoColor=white" alt="gitpod: Ready to Code"></a>
</p>

[📖 English Documentation](README-EN.md) | 📖 中文文档

----------------------------------------

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

- [🔧 功能](#-%E5%8A%9F%E8%83%BD)
- [🎨 需求场景](#-%E9%9C%80%E6%B1%82%E5%9C%BA%E6%99%AF)
- [👥 User Guide](#-user-guide)
    - [1. 简单使用](#1-%E7%AE%80%E5%8D%95%E4%BD%BF%E7%94%A8)
    - [2. 保证线程池中传递值](#2-%E4%BF%9D%E8%AF%81%E7%BA%BF%E7%A8%8B%E6%B1%A0%E4%B8%AD%E4%BC%A0%E9%80%92%E5%80%BC)
        - [2.1 修饰`Runnable`和`Callable`](#21-%E4%BF%AE%E9%A5%B0runnable%E5%92%8Ccallable)
            - [整个过程的完整时序图](#%E6%95%B4%E4%B8%AA%E8%BF%87%E7%A8%8B%E7%9A%84%E5%AE%8C%E6%95%B4%E6%97%B6%E5%BA%8F%E5%9B%BE)

## 整体架构描述


## 核心模块划分


## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
