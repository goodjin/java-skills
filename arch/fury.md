# fury

## 项目简介
<div align="center">
  <img width="65%" alt="Apache Fory logo" src="docs/images/logo/fory-horizontal.png"><br>
</div>

[![Build Status](https://img.shields.io/github/actions/workflow/status/apache/fory/ci.yml?branch=main&style=for-the-badge&label=GITHUB%20ACTIONS&logo=github)](https://github.com/apache/fory/actions/workflows/ci.yml)
[![Slack Channel](https://img.shields.io/badge/slack-join-3f0e40?logo=slack&style=for-the-badge)](https://join.slack.com/t/fory-project/shared_invite/zt-36g0qouzm-kcQSvV_dtfbtBKHRwT5gsw)
[![X](https://img.shields.io/badge/@ApacheFory-follow-blue?logo=x&style=for-the-badge)](https://x.com/ApacheFory)
[![Maven Version](https://img.shields.io/maven-central/v/org.apache.fory/fory-core?style=for-the-badge)](https://search.maven.org/#search|gav|1|g:"org.apache.fory"%20AND%20a:"fory-core")
[![Crates.io](https://img.shields.io/crates/v/fory.svg?style=for-the-badge)](https://crates.io/crates/fory)
[![PyPI](https://img.shields.io/pypi/v/pyfory.svg?logo=PyPI&style=for-the-badge)](https://pypi.org/project/pyfory/)

**Apache Fory™** is a blazingly-fast multi-language serialization framework powered by **JIT compilation**, **zero-copy** techniques, and **advanced code generation**, achieving up to **170x performance improvement** while maintaining simplicity and ease of use.

<https://fory.apache.org>

> [!IMPORTANT]
> **Apache Fory™ was previously named as Apache Fury. For versions before 0.11, please use "fury" instead of "fory" in package names, imports, and dependencies, see [Fury Docs](https://fory.apache.org/docs/0.10/docs/introduction/) for how to use Fury in older versions**.

## Key Features

### High-Performance Serialization

Apache Fory™ delivers excellent performance through advanced optimization techniques:

- **JIT Compilation**: Runtime code generation for Java eliminates virtual method calls and inlines hot paths
- **Static Code Generation**: Compile-time code generation for Rust, C++, and Go delivers peak performance without runtime overhead
- **Meta Packing & Sharing**: Class metadata packing and sharing reduces redundant type information across objects on one stream

### Cross-Language Serialization

The **[xlang serialization format](docs/specification/xlang_serialization_spec.md)** enables seamless data exchange across programming languages:

- **Reference Preservation**: Shared and circular references work correctly across languages
- **Polymorphism**: Objects serialize/deserialize with their actual runtime types
- **Schema Evolution**: Optional forward/backward compatibility for evolving schemas
- **Automatic Serialization**: Serialize domain objects automatically, no IDL or schema definitions required

### Row Format

A cache-friendly **[row format](docs/specification/row_format_spec.md)** optimized for analytics workloads:

## 整体架构描述


## 核心模块划分


## 技术选型
- 构建工具: 未知
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
