# fastjson2

## 项目简介
[![Java CI](https://img.shields.io/github/actions/workflow/status/alibaba/fastjson2/ci.yaml?branch=main&logo=github&logoColor=white)](https://github.com/alibaba/fastjson2/actions/workflows/ci.yaml)
[![Codecov](https://img.shields.io/codecov/c/github/alibaba/fastjson2/main?logo=codecov&logoColor=white)](https://codecov.io/gh/alibaba/fastjson2/branch/main)
[![Maven Central](https://img.shields.io/maven-central/v/com.alibaba.fastjson2/fastjson2?logo=apache-maven&logoColor=white)](https://search.maven.org/artifact/com.alibaba.fastjson2/fastjson2)
[![GitHub release](https://img.shields.io/github/release/alibaba/fastjson2)](https://github.com/alibaba/fastjson2/releases)
[![Java support](https://img.shields.io/badge/Java-8+-green?logo=java&logoColor=white)](https://openjdk.java.net/)
[![License](https://img.shields.io/github/license/alibaba/fastjson2?color=4D7A97&logo=apache)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Gitpod Ready-to-Code](https://img.shields.io/badge/Gitpod-ready--to--code-green?label=gitpod&logo=gitpod&logoColor=white)](https://gitpod.io/#https://github.com/alibaba/fastjson2)
[![Last SNAPSHOT](https://img.shields.io/nexus/snapshots/https/oss.sonatype.org/com.alibaba.fastjson2/fastjson2?label=latest%20snapshot)](https://oss.sonatype.org/content/repositories/snapshots/com/alibaba/fastjson2/)
[![GitHub Stars](https://img.shields.io/github/stars/alibaba/fastjson2)](https://github.com/alibaba/fastjson2/stargazers)
[![GitHub Forks](https://img.shields.io/github/forks/alibaba/fastjson2)](https://github.com/alibaba/fastjson2/fork)
[![user repos](https://badgen.net/github/dependents-repo/alibaba/fastjson2?label=user%20repos)](https://github.com/alibaba/fastjson2/network/dependents)
[![GitHub Contributors](https://img.shields.io/github/contributors/alibaba/fastjson2)](https://github.com/alibaba/fastjson2/graphs/contributors)

##### 📖 English Documentation | 📖 [中文文档](README_cn.md)
#####  The issues of fastjson will be also posted on [Alibaba Cloud Developer Community](https://developer.aliyun.com/ask/)

# FASTJSON v2

`FASTJSON v2` is an upgrade of the `FASTJSON`, with the goal of providing a highly optimized `JSON` library for the next ten years.

- Supports the JSON and JSONB Protocols.
- Supports full parsing and partial parsing.
- Supports Java servers and Android Clients, and has big data applications.
- Supports Kotlin [https://alibaba.github.io/fastjson2/Kotlin/kotlin_en](https://alibaba.github.io/fastjson2/Kotlin/kotlin_en)
- Supports Android 8+ 
- Supports `JSON Schema` [https://alibaba.github.io/fastjson2/JSONSchema/json_schema_en](https://alibaba.github.io/fastjson2/JSONSchema/json_schema_en)

![fastjson logo](https://user-images.githubusercontent.com/1063891/233821110-0c912009-4de3-4664-a27e-25274f2fa9c1.jpg)

Related Documents:

- `JSONB` format documentation:  
  [https://alibaba.github.io/fastjson2/JSONB/jsonb_format_en](https://alibaba.github.io/fastjson2/JSONB/jsonb_format_en)
- `FASTJSON v2`'s performance has been significantly improved. For the benchmark, see here:  
  [https://github.com/alibaba/fastjson2/wiki/fastjson_benchmark](https://github.com/alibaba/fastjson2/wiki/fastjson_benchmark)

# 1. Prepare

## 1.1 Download

## 整体架构描述


## 核心模块划分
## 1.2 Other modules

### Compatible dependence of fastjson-v1

If you are using `fastjson 1.2.x`, you can use the compatibility package. The compatibility package cannot guarantee 100% compatibility. Please test  it yourself and report any problems.

`Maven`:

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>2.0.61</version>
</dependency>
```

`Gradle`:

```groovy
dependencies {
    implementation 'com.alibaba:fastjson:2.0.61'
--
### `Kotlin` integration module `fastjson-kotlin`

If your project uses `kotlin`, you can use the `Fastjson-Kotlin` module, and use the characteristics of `kotlin`.

## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
