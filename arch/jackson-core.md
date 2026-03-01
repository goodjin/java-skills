# jackson-core

## 项目简介
# Overview

This project contains core low-level incremental ("streaming") parser and generator abstractions used by
[Jackson Data Processor](https://github.com/FasterXML/jackson).
It also includes the default implementation of handler types (parser, generator) that handle JSON format.
The core abstractions are not JSON specific, although naming does contain 'JSON' in many places, due to historical reasons. Only packages that specifically contain word 'json' are JSON-specific.

This package is the base on which [Jackson data-binding](https://github.com/FasterXML/jackson-databind) package builds on.
It is licensed under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

Alternate data format implementations (like
[Smile (binary JSON)](https://github.com/FasterXML/jackson-dataformats-binary/tree/3.x/smile),
[XML](https://github.com/FasterXML/jackson-dataformat-xml/),
[CSV](https://github.com/FasterXML/jackson-dataformats-text/tree/3.x/csv),
[YAML](https://github.com/FasterXML/jackson-dataformats-text/tree/3.x/yaml),
[Protobuf](https://github.com/FasterXML/jackson-dataformats-binary/tree/3.x/protobuf),
and [CBOR](https://github.com/FasterXML/jackson-dataformats-binary/tree/3.x/cbor))
also build on this base package, implementing the core interfaces,
making it possible to use standard [data-binding package](https://github.com/FasterXML/jackson-databind) regardless of underlying data format.

Project contains versions 2.0 and above: source code for earlier (1.x) versions can be found from
[Jackson-1](../../../jackson-1) github repo.

## Status

| Type | Status |
| ---- | ------ |
| Build (CI) | [![Build (github)](https://github.com/FasterXML/jackson-core/actions/workflows/main.yml/badge.svg)](https://github.com/FasterXML/jackson-core/actions/workflows/main.yml) |
| Artifact | ![Maven Central](https://img.shields.io/maven-central/v/tools.jackson.core/jackson-core) |
| OSS Sponsorship | [![Tidelift](https://tidelift.com/badges/package/maven/com.fasterxml.jackson.core:jackson-core)](https://tidelift.com/subscription/pkg/maven-com-fasterxml-jackson-core-jackson-core?utm_source=maven-com-fasterxml-jackson-core-jackson-core&utm_medium=referral&utm_campaign=readme) |
| Javadocs | [![Javadoc](https://javadoc.io/badge/tools.jackson.core/jackson-core.svg)](https://javadoc.io/doc/tools.jackson.core/jackson-core) |
| Code coverage (3.0) | [![codecov.io](https://codecov.io/github/FasterXML/jackson-core/coverage.svg?branch=3.x)](https://codecov.io/github/FasterXML/jackson-core?branch=3.x) |
| OpenSSF Score | [![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/FasterXML/jackson-core/badge)](https://securityscorecards.dev/viewer/?uri=github.com/FasterXML/jackson-core) |

# Get it!

## Maven

Functionality of this package is contained in 
Java package

## 整体架构描述


## 核心模块划分
Jackson 2.10 and above include `module-info.class` definitions so the jar is also a proper Java module (JPMS).

Jackson 2.12 and above include additional Gradle 6 Module Metadata for version alignment with Gradle.

-----
# Use it!

## General

Usage typically starts with creation of a reusable (and thread-safe, once configured) `JsonFactory` instance:

```java
// Builder-style since 2.10:
JsonFactory factory = JsonFactory.builder()
// configure, if necessary:
     .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
     .build();
```

Alternatively, you have an `ObjectMapper` (from [Jackson Databind package](https://github.com/FasterXML/jackson-databind)) handy; if so, you can do:

```java
JsonFactory factory = objectMapper.tokenStreamFactory();
--
Starting with Jackson 2.15, releases of this module was meant be [SLSA](https://slsa.dev/) compliant: see issue #844 for details.

## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
