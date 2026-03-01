# hibernate-orm

## 项目简介
== Hibernate ORM

image:https://img.shields.io/maven-central/v/org.hibernate.orm/hibernate-core.svg?label=Maven%20Central&style=for-the-badge[Maven Central,link=https://central.sonatype.com/search?namespace=org.hibernate.orm&sort=name]
image:https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.hibernate.org%2Fjob%2Fhibernate-orm-pipeline%2Fjob%2Fmain%2F&style=for-the-badge[Build Status,link=https://ci.hibernate.org/job/hibernate-orm-pipeline/job/main/]
image:https://img.shields.io/badge/Revved%20up%20by-Develocity-06A0CE?style=for-the-badge&logo=gradle[Develocity,link=https://develocity.commonhaus.dev/scans?search.rootProjectNames=Hibernate%20ORM]
image:https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/jvm-repo-rebuild/reproducible-central/master/content/org/hibernate/orm/hibernate-core/badge.json&style=for-the-badge[Reproducible Builds,link=https://github.com/jvm-repo-rebuild/reproducible-central/blob/master/content/org/hibernate/orm/hibernate-core/README.md]
image:https://testpilot.oracle.com/ords/testpilot/badges/github/hibernate/hibernate-orm[Oracle Test Pilot,link=https://testpilot.oracle.com/]

Hibernate ORM is a powerful object/relational mapping solution for Java, and makes it easy to develop persistence logic for applications, libraries, and frameworks.

Hibernate implements JPA, the standard API for object/relational persistence in Java, but also offers an extensive set of features and APIs which go beyond the specification.

See https://hibernate.org/orm/[Hibernate.org] for more information.

== Continuous Integration

See link:MAINTAINERS.md#ci[MAINTAINERS.md] for information about CI.

== Building from sources

The build requires at least JDK 25, and produces Java 17 bytecode.

Hibernate uses https://gradle.org[Gradle] as its build tool. See the _Gradle Primer_ section below if you are new to
Gradle.

Contributors should read the link:CONTRIBUTING.md[Contributing Guide].

See the guides for setting up https://hibernate.org/community/contribute/intellij-idea/[IntelliJ] or
https://hibernate.org/community/contribute/eclipse-ide/[Eclipse] as your development environment.

== Gradle Primer

The Gradle build tool has amazing documentation.  2 in particular that are indispensable:

* https://docs.gradle.org/current/userguide/userguide_single.html[Gradle User Guide] is a typical user guide in that
it follows a topical approach to describing all of the capabilities of Gradle.
* https://docs.gradle.org/current/dsl/index.html[Gradle DSL Guide] is unique and excellent in quickly
getting up to speed on certain aspects of Gradle.

We will cover the basics developers and contributors new to Gradle need to know to get productive quickly.

## 整体架构描述


## 核心模块划分
To execute a task across all modules, simply perform that task from the root directory. Gradle will visit each
sub-project and execute that task if the sub-project defines it. To execute a task in a specific module you can
either:

. `cd` into that module directory and execute the task
. name the "task path". For example, to run the tests for the _hibernate-core_ module from the root directory
you could say `gradle hibernate-core:test`

=== Common tasks

The common tasks you might use in building Hibernate include:

* _build_ - Assembles (jars) and tests this project
* _compile_ - Performs all compilation tasks including staging resources from both main and test
* _jar_ - Generates a jar archive with all the compiled classes
* _test_ - Runs the tests
* _publishToMavenLocal_ - Installs the project jar to your local maven cache (aka ~/.m2/repository). Note that Gradle
never uses this, but it can be useful for testing your build with other local Maven-based builds.
* _clean_ - Cleans the build directory

== Testing and databases

Testing against a specific database can be achieved in 2 different ways:

=== Using the "Matrix Testing Plugin" for Gradle.

## 技术选型
- 构建工具: Gradle
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
