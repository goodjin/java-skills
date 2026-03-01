# swagger-core

## 项目简介
# Swagger Core <img src="https://raw.githubusercontent.com/swagger-api/swagger.io/wordpress/images/assets/SW-logo-clr.png" height="50" align="right">

**NOTE:** If you're looking for Swagger Core 1.5.X and OpenAPI 2.0, please refer to [1.5 branch](https://github.com/swagger-api/swagger-core/tree/1.5).

**NOTE:** Since version 2.1.7, Swagger Core also supports the Jakarta namespace. There are a parallel set of artifacts with the `-jakarta` suffix, providing the same functionality as the unsuffixed (i.e.: `javax`) artifacts.
Please see the [Wiki](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Getting-started) for more details.

**NOTE:** Since version 2.2.0 Swagger Core supports OpenAPI 3.1; see [this page](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---OpenAPI-3.1) for details

![Build Test Deploy](https://github.com/swagger-api/swagger-core/workflows/Build%20Test%20Deploy%20master/badge.svg?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.swagger.core.v3/swagger-project/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/io.swagger.core.v3/swagger-project)

Swagger Core is a Java implementation of the OpenAPI Specification. Current version supports *JAX-RS2* (`javax` and `jakarta` namespaces).

## Get started with Swagger Core!
See the guide on [getting started with Swagger Core](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Getting-started) to get started with adding Swagger to your API.

## See the Wiki!
The [github wiki](https://github.com/swagger-api/swagger-core/wiki) contains documentation, samples, contributions, etc. Start there.

## Compatibility
The OpenAPI Specification has undergone several revisions since initial creation in 2010.  The Swagger Core project has the following compatibilities with the OpenAPI Specification:

Swagger core Version      | Release Date | OpenAPI Spec compatibility | Notes | Status
------------------------- | ------------ | -------------------------- | ----- | ----
2.2.43 (**current stable**)| 2026-02-17   | 3.x           | [tag v2.2.43](https://github.com/swagger-api/swagger-core/tree/v2.2.43) | Supported
2.2.42                     | 2026-01-19   | 3.x           | [tag v2.2.42](https://github.com/swagger-api/swagger-core/tree/v2.2.42) | Supported
2.2.41                     | 2025-11-24   | 3.x           | [tag v2.2.41](https://github.com/swagger-api/swagger-core/tree/v2.2.41) | Supported
2.2.40                     | 2025-10-28   | 3.x           | [tag v2.2.40](https://github.com/swagger-api/swagger-core/tree/v2.2.40) | Supported
2.2.39                     | 2025-10-13   | 3.x           | [tag v2.2.39](https://github.com/swagger-api/swagger-core/tree/v2.2.39) | Supported
2.2.38                     | 2025-09-29   | 3.x           | [tag v2.2.38](https://github.com/swagger-api/swagger-core/tree/v2.2.38) | Supported
2.2.37                     | 2025-09-16   | 3.x           | [tag v2.2.37](https://github.com/swagger-api/swagger-core/tree/v2.2.37) | Supported
2.2.36                     | 2025-08-18   | 3.x           | [tag v2.2.36](https://github.com/swagger-api/swagger-core/tree/v2.2.36) | Supported
2.2.35                     | 2025-07-31   | 3.x           | [tag v2.2.35](https://github.com/swagger-api/swagger-core/tree/v2.2.35) | Supported
2.2.34                     | 2025-06-20   | 3.x           | [tag v2.2.34](https://github.com/swagger-api/swagger-core/tree/v2.2.34) | Supported
2.2.33                     | 2025-06-12   | 3.x           | [tag v2.2.33](https://github.com/swagger-api/swagger-core/tree/v2.2.33) | Supported
2.2.32                     | 2025-05-14   | 3.x           | [tag v2.2.32](https://github.com/swagger-api/swagger-core/tree/v2.2.32) | Supported
2.2.31                     | 2025-05-13   | 3.x           | [tag v2.2.31](https://github.com/swagger-api/swagger-core/tree/v2.2.31) | Supported
2.2.30                     | 2025-04-07   | 3.x           | [tag v2.2.30](https://github.com/swagger-api/swagger-core/tree/v2.2.30) | Supported
2.2.29                     | 2025-03-10   | 3.x           | [tag v2.2.29](https://github.com/swagger-api/swagger-core/tree/v2.2.29) | Supported

## 整体架构描述


## 核心模块划分
This will build the modules.

Of course if you don't want to build locally you can grab artifacts from maven central:

`https://repo1.maven.org/maven2/io/swagger/core/`

## Sample Apps
The samples have moved to [a new repository](https://github.com/swagger-api/swagger-samples/tree/2.0) and contain various integrations and configurations.

## Security contact

Please disclose any security-related issues or vulnerabilities by emailing [security@swagger.io](mailto:security@swagger.io), instead of using the public issue tracker.

## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
