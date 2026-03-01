# reactor

## 项目简介
# Reactor Project

[![Join the chat at https://gitter.im/reactor/reactor](	https://img.shields.io/gitter/room/reactor/reactor.svg)](https://gitter.im/reactor/reactor?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

 [![Download](https://img.shields.io/maven-central/v/io.projectreactor/reactor-bom.svg) ](https://img.shields.io/maven-central/v/io.projectreactor/reactor-bom.svg)

Project Reactor is organized into multiple projects:
 - [`reactor-core`](https://github.com/reactor/reactor-core/)
 - [`reactor-netty`](https://github.com/reactor/reactor-netty/)
 - addons like [`reactor-extra`](https://github.com/reactor/reactor-addons/) or [`reactor-pool`](https://github.com/reactor/reactor-pool/)

A set of compatible versions for all these projects is curated under a BOM ("Bill of Materials") hosted under this very repository.

## Using the BOM with Maven
In Maven, you need to import the bom first:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.projectreactor</groupId>
            <artifactId>reactor-bom</artifactId>
            <version>2025.0.3</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
Notice we use the `<dependencyManagement>` section and the `import` scope.

Next, add your dependencies to the relevant reactor projects as usual, except without a `<version>`:

```xml
<dependencies>
    <dependency>
        <groupId>io.projectreactor</groupId>
        <artifactId>reactor-core</artifactId>
    </dependency>
    <dependency>

## 整体架构描述


## 核心模块划分


## 技术选型
- 构建工具: Gradle
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
