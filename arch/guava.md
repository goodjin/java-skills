# guava

## 项目简介
# Guava: Google Core Libraries for Java

[![GitHub Release](https://img.shields.io/github/v/release/google/guava)](https://github.com/google/guava/releases/latest)
[![CI](https://github.com/google/guava/actions/workflows/ci.yml/badge.svg)](https://github.com/google/guava/actions/workflows/ci.yml)
[![OpenSSF Best Practices](https://www.bestpractices.dev/projects/7197/badge)](https://www.bestpractices.dev/projects/7197)



Guava is a set of core Java libraries from Google that includes new collection
types (such as multimap and multiset), immutable collections, a graph library,
and utilities for concurrency, I/O, hashing, primitives, strings, and more! It
is widely used on most Java projects within Google, and widely used by many
other companies as well.



Guava comes in two flavors:

*   The JRE flavor requires JDK 1.8 or higher.
*   If you need support for Android, use
    [the Android flavor](https://github.com/google/guava/wiki/Android). You can
    find the Android Guava source in the [`android` directory].

[`android` directory]: https://github.com/google/guava/tree/master/android

## Adding Guava to your build

Guava's Maven group ID is `com.google.guava`, and its artifact ID is `guava`.
Guava provides two different "flavors": one for use on a (Java 8+) JRE and one
for use on Android or by any library that wants to be compatible with Android.
These flavors are specified in the Maven version field as either `33.5.0-jre` or
`33.5.0-android`. For more about depending on Guava, see
[using Guava in your build].

To add a dependency on Guava using Maven, use the following:

```xml
<dependency>
  <groupId>com.google.guava</groupId>
  <artifactId>guava</artifactId>

## 整体架构描述


## 核心模块划分


## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
