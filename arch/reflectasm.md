# reflectasm

## 项目简介
![](https://raw.github.com/wiki/EsotericSoftware/reflectasm/images/logo.png)

[![Build Status](https://travis-ci.org/EsotericSoftware/reflectasm.png?branch=master)](https://travis-ci.org/EsotericSoftware/reflectasm)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.esotericsoftware/reflectasm/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.esotericsoftware%22%20AND%20a%3Areflectasm)

Please use the [ReflectASM discussion group](http://groups.google.com/group/reflectasm-users) for support.

## Overview

ReflectASM is a very small Java library that provides high performance reflection by using code generation. An access class is generated to set/get fields, call methods, or create a new instance. The access class uses bytecode rather than Java's reflection, so it is much faster. It can also access primitive fields via bytecode to avoid boxing.

## Performance

![](http://chart.apis.google.com/chart?chma=100&chtt=Field%20Set/Get&chs=700x62&chd=t:1402081,11339107&chds=0,11339107&chxl=0:|Java%20Reflection|FieldAccess&cht=bhg&chbh=10&chxt=y&chco=6600FF)

![](http://chart.apis.google.com/chart?chma=100&chtt=Method%20Call&chs=700x62&chd=t:97390,208750&chds=0,208750&chxl=0:|Java%20Reflection|MethodAccess&cht=bhg&chbh=10&chxt=y&chco=6600AA)

![](http://chart.apis.google.com/chart?chma=100&chtt=Constructor&chs=700x62&chd=t:2853063,5828993&chds=0,5828993&chxl=0:|Java%20Reflection|ConstructorAccess&cht=bhg&chbh=10&chxt=y&chco=660066)

The source code for these benchmarks is included in the project. The above charts were generated on Oracle's Java 7u3, server VM.

## Installation

To use reflectasm with maven, please use the following snippet in your pom.xml

```xml
    <dependency>
        <groupId>com.esotericsoftware</groupId>
        <artifactId>reflectasm</artifactId>
        <version>1.11.9</version>
    </dependency>
```

## Usage

Method reflection with ReflectASM:

```java
SomeClass someObject = ...
MethodAccess access = MethodAccess.get(SomeClass.class);

## 整体架构描述


## 核心模块划分


## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
