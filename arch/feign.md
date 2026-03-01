# feign

## 项目简介
# Feign simplifies the process of writing Java HTTP clients

[![Join the chat at https://gitter.im/OpenFeign/feign](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/OpenFeign/feign?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![CI](https://github.com/OpenFeign/feign/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/OpenFeign/feign/actions/workflows/build.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.openfeign/feign-core/badge.png)](https://search.maven.org/artifact/io.github.openfeign/feign-core/)

Feign is a Java to HTTP client binder inspired by [Retrofit](https://github.com/square/retrofit), [JAXRS-2.0](https://jax-rs-spec.java.net/nonav/2.0/apidocs/index.html), and [WebSocket](http://www.oracle.com/technetwork/articles/java/jsr356-1937161.html).  Feign's first goal was reducing the complexity of binding [Denominator](https://github.com/Netflix/Denominator) uniformly to HTTP APIs regardless of [ReSTfulness](http://www.slideshare.net/adrianfcole/99problems).

---
### Why Feign and not X?

Feign uses tools like Jersey and CXF to write Java clients for ReST or SOAP services. Furthermore, Feign allows you to write your own code on top of http libraries such as Apache HC. Feign connects your code to http APIs with minimal overhead and code via customizable decoders and error handling, which can be written to any text-based http API.

### How does Feign work?

Feign works by processing annotations into a templatized request. Arguments are applied to these templates in a straightforward fashion before output.  Although Feign is limited to supporting text-based APIs, it dramatically simplifies system aspects such as replaying requests. Furthermore, Feign makes it easy to unit test your conversions knowing this.

### Java Version Compatibility

Feign 10.x and above are built on Java 8 and should work on Java 9, 10, and 11.  For those that need JDK 6 compatibility, please use Feign 9.x

## Feature overview

This is a map with current key features provided by feign:

![MindMap overview](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/OpenFeign/feign/master/src/docs/overview-mindmap.iuml)

# Roadmap
## Feign 11 and beyond
Making _API_ clients easier

Short Term - What we're working on now. ⏰
---
* Response Caching
  * Support caching of api responses.  Allow for users to define under what conditions a response is eligible for caching and what type of caching mechanism should be used.
  * Support in-memory caching and external cache implementations (EhCache, Google, Spring, etc...)
* Complete URI Template expression support
  * Support [level 1 through level 4](https://tools.ietf.org/html/rfc6570#section-1.2) URI template expressions.
  * Use [URI Templates TCK](https://github.com/uri-templates/uritemplate-test) to verify compliance.
* `Logger` API refactor

## 整体架构描述


## 核心模块划分


## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
