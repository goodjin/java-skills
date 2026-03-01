# powermock

## 项目简介
![PowerMock](powermock.png)

[![Build Status](https://travis-ci.org/powermock/powermock.svg?branch=master)](https://travis-ci.org/powermock/powermock)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.powermock/powermock-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.powermock/powermock-core)
[ ![Download](https://api.bintray.com/packages/powermock/maven/powermock/images/download.svg) ](https://bintray.com/powermock/maven/powermock/_latestVersion)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/org.powermock/powermock-core/badge.svg)](http://www.javadoc.io/doc/org.powermock/powermock-core)

Writing unit tests can be hard and sometimes good design has to be sacrificed for the sole purpose of testability. Often testability corresponds to good design, but this is not always the case. For example final classes and methods cannot be used, private methods sometimes need to be protected or unnecessarily moved to a collaborator, static methods should be avoided completely and so on simply because of the limitations of existing frameworks.

PowerMock is a framework that extends other mock libraries such as EasyMock with more powerful capabilities. PowerMock uses a custom classloader and bytecode manipulation to enable mocking of static methods, constructors, final classes and methods, private methods, removal of static initializers and more. By using a custom classloader no changes need to be done to the IDE or continuous integration servers which simplifies adoption. Developers familiar with the supported mock frameworks will find PowerMock easy to use, since the entire expectation API is the same, both for static methods and constructors. PowerMock aims to extend the existing API's with a small number of methods and annotations to enable the extra features. Currently PowerMock supports EasyMock and Mockito.

When writing unit tests it is often useful to bypass encapsulation and therefore PowerMock includes several features that simplifies reflection specifically useful for testing. This allows easy access to internal state, but also simplifies partial and private mocking.

Please note that PowerMock is mainly intended for people with expert knowledge in unit testing. Putting it in the hands of junior developers may cause more harm than good.

## News
* 2019-04-21: PowerMock 2.0.2 has been released and is avaliable in Maven Central. The release includes fixes for [issue](https://github.com/powermock/powermock/issues/979) with PowerMock JavaAgent and the latest JDK and a [security issue](https://github.com/powermock/powermock/issues/973) with the build script. 
* 2019-01-07: PowerMock 2.0.0 has been released. Main changes: offical supporting Mockito 2.x and dropping supporting Mockito 1.x. This release also supports Java 9. Other change read in [release notes](https://github.com/powermock/powermock/releases/tag/powermock-2.0.0). 
* 2017-08-12: PowerMock 1.7.1 has been released with one, but significant change: the old API for verifying static mock has been deprecated and a new one has been added. Old API will be removed in version PowerMock 2.0 due to incompatibility with Mockito Public API.
* 2017-06-16: PowerMock 1.7.0 has been released with support for Mockito 2 (not only beta versions) and new features such as global `@PowerMockIgnore` as well as bug fixes and other improvements. See [release notes](https://github.com/powermock/powermock/releases/tag/powermock-1.7.0) and [change log](https://raw.githubusercontent.com/powermock/powermock/master/docs/changelog.txt) for details. 
* 2017-02-03: Johan blogs about how to mock slf4j with PowerMock at his [blog](http://code.haleby.se/2017/02/03/a-case-for-powermock/)

[Older News](https://github.com/powermock/powermock/wiki/OldNews)

## Documentation
* [Getting Started](https://github.com/powermock/powermock/wiki/Getting-Started)
* [Downloads](https://github.com/powermock/powermock/wiki/Downloads)
* [Motivation](https://github.com/powermock/powermock/wiki/Motivation)
* Javadoc
  * [EasyMock API extension](http://www.javadoc.io/doc/org.powermock/powermock-api-easymock/1.7.0) ([PowerMock class](http://static.javadoc.io/org.powermock/powermock-api-easymock/1.7.0/org/powermock/api/easymock/PowerMock.html))
  * [Mockito API extension](http://www.javadoc.io/doc/org.powermock/powermock-api-mockito/1.7.0) ([PowerMockito class](http://static.javadoc.io/org.powermock/powermock-api-mockito/1.7.0/org/powermock/api/mockito/PowerMockito.html))
  * [Mockito2 API extension](http://www.javadoc.io/doc/org.powermock/powermock-api-mockito2/1.7.0) ([PowerMockito class](http://static.javadoc.io/org.powermock/powermock-api-mockito2/1.7.0/org/powermock/api/mockito/PowerMockito.html))
  * [PowerMock Reflect](http://www.javadoc.io/doc/org.powermock/powermock-reflect/1.7.0) ([Whitebox class](http://static.javadoc.io/org.powermock/powermock-reflect/1.7.0/org/powermock/reflect/Whitebox.html))
* Common
  * [PowerMock Configuration](https://github.com/powermock/powermock/wiki/PowerMock-Configuration)
  * [Bypass Encapsulation](https://github.com/powermock/powermock/wiki/Bypass-Encapsulation)
  * [Suppress Unwanted Behavior](https://github.com/powermock/powermock/wiki/Suppress-Unwanted-Behavior)
  * [Test Listeners](https://github.com/powermock/powermock/wiki/Test-Listeners)
  * [Mock Policies](https://github.com/powermock/powermock/wiki/Mock-Policies)
  * [Mock system classes](https://github.com/powermock/powermock/wiki/Mock-System)

## 整体架构描述


## 核心模块划分


## 技术选型
- 构建工具: Gradle
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
