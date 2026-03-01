# spring-data-jpa

## 项目简介
= Spring Data JPA image:https://img.shields.io/badge/Revved%20up%20by-Develocity-06A0CE?logo=Gradle&labelColor=02303A["Revved up by Develocity", link="https://ge.spring.io/scans?search.rootProjectNames=Spring Data JPA Parent"]

Spring Data JPA, part of the larger https://projects.spring.io/spring-data[Spring Data] family, makes it easy to implement JPA-based repositories.
This module deals with enhanced support for JPA-based data access layers.
It makes it easier to build Spring-powered applications that use data access technologies.

Implementing a data access layer of an application has been cumbersome for quite a while.
Too much boilerplate code has to be written to execute simple queries as well as perform pagination, and auditing.
Spring Data JPA aims to significantly improve the implementation of data access layers by reducing the effort to the amount that’s actually needed.
As a developer you write your repository interfaces, including custom finder methods, and Spring will provide the implementation automatically.

== Features

* Implementation of CRUD methods for JPA Entities
* Dynamic query generation from query method names
* Transparent triggering of JPA NamedQueries by query methods
* Implementation domain base classes providing basic properties
* Support for transparent auditing (created, last changed)
* Possibility to integrate custom repository code
* Easy Spring integration with custom namespace

== Code of Conduct

This project is governed by the https://github.com/spring-projects/.github/blob/e3cc2ff230d8f1dca06535aa6b5a4a23815861d4/CODE_OF_CONDUCT.md[Spring Code of Conduct]. By participating, you are expected to uphold this code of conduct. Please report unacceptable behavior to spring-code-of-conduct@pivotal.io.

== Getting Started

Here is a quick teaser of an application using Spring Data Repositories in Java:

[source,java]
----
public interface PersonRepository extends CrudRepository<Person, Long> {

  List<Person> findByLastname(String lastname);

  List<Person> findByFirstnameLike(String firstname);
}

@Service
public class MyService {

## 整体架构描述


## 核心模块划分
This module deals with enhanced support for JPA-based data access layers.
It makes it easier to build Spring-powered applications that use data access technologies.

Implementing a data access layer of an application has been cumbersome for quite a while.
Too much boilerplate code has to be written to execute simple queries as well as perform pagination, and auditing.
Spring Data JPA aims to significantly improve the implementation of data access layers by reducing the effort to the amount that’s actually needed.
As a developer you write your repository interfaces, including custom finder methods, and Spring will provide the implementation automatically.

== Features

* Implementation of CRUD methods for JPA Entities
* Dynamic query generation from query method names
* Transparent triggering of JPA NamedQueries by query methods
* Implementation domain base classes providing basic properties
* Support for transparent auditing (created, last changed)
* Possibility to integrate custom repository code
* Easy Spring integration with custom namespace

== Code of Conduct

This project is governed by the https://github.com/spring-projects/.github/blob/e3cc2ff230d8f1dca06535aa6b5a4a23815861d4/CODE_OF_CONDUCT.md[Spring Code of Conduct]. By participating, you are expected to uphold this code of conduct. Please report unacceptable behavior to spring-code-of-conduct@pivotal.io.

## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
