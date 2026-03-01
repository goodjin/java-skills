# spring-boot

## 项目简介
= Spring Boot image:https://github.com/spring-projects/spring-boot/actions/workflows/build-and-deploy-snapshot.yml/badge.svg?branch=main["Build Status", link="https://github.com/spring-projects/spring-boot/actions/workflows/build-and-deploy-snapshot.yml?query=branch%3Amain"] image:https://img.shields.io/badge/Revved%20up%20by-Develocity-06A0CE?logo=Gradle&labelColor=02303A["Revved up by Develocity", link="https://ge.spring.io/scans?&search.rootProjectNames=Spring%20Boot%20Build&search.rootProjectNames=spring-boot-build"]

:docs: https://docs.spring.io/spring-boot
:github: https://github.com/spring-projects/spring-boot

Spring Boot helps you to create Spring-powered, production-grade applications and services with absolute minimum fuss.
It takes an opinionated view of the Spring platform so that new and existing users can quickly get to the bits they need.

You can use Spring Boot to create stand-alone Java applications that can be started using `java -jar` or more traditional WAR deployments.
We also provide a command-line tool that runs Spring scripts.

Our primary goals are:

* Provide a radically faster and widely accessible getting started experience for all Spring development.
* Be opinionated, but get out of the way quickly as requirements start to diverge from the defaults.
* Provide a range of non-functional features common to large classes of projects (for example, embedded servers, security, metrics, health checks, externalized configuration).
* Absolutely no code generation and no requirement for XML configuration.



== Installation and Getting Started

The {docs}[reference documentation] includes detailed {docs}/installing.html[installation instructions] as well as a comprehensive {docs}/tutorial/first-application/index.html[``getting started``] guide.

Here is a quick teaser of a complete Spring Boot application in Java:

[source,java]
----
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

@RestController
@SpringBootApplication
public class Example {

	@RequestMapping("/")
	String home() {
		return "Hello World!";
	}

## 整体架构描述


## 核心模块划分
This command builds all modules and publishes them to your local Maven cache.
It won't run any of the tests.
If you want to build everything, use the `build` task:

[source,shell]
----
$ ./gradlew build
----



== Guides

The https://spring.io/[spring.io] site contains several guides that show how to use Spring Boot step-by-step:

* https://spring.io/guides/gs/spring-boot/[Building an Application with Spring Boot] is an introductory guide that shows you how to create an application, run it, and add some management services.
* https://spring.io/guides/gs/actuator-service/[Building a RESTful Web Service with Spring Boot Actuator] is a guide to creating a REST web service and also shows how the server can be configured.



== License

## 技术选型
- 构建工具: Gradle
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
