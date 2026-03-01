# junit5

## 项目简介
# <img alt="JUnit" src="https://junit.org/assets/img/junit-logo-adaptive.svg" width="200">

This repository is the home of JUnit Platform, Jupiter, and Vintage.

## Sponsors

[![Support JUnit](https://img.shields.io/badge/%F0%9F%92%9A-Support%20JUnit-brightgreen.svg)](https://junit.org/sponsoring)

* **Gold Sponsors:** [JetBrains](https://jb.gg/junit-logo), [Netflix](https://www.netflix.com/)
* **Silver Sponsors:** [Micromata](https://www.micromata.de), [Quo Card](https://quo-digital.jp)
* **Bronze Sponsors:** [Premium Minds](https://www.premium-minds.com), [codefortynine](https://codefortynine.com), [Info Support](https://www.infosupport.com), [Code Intelligence](https://www.code-intelligence.com), [Route4Me](https://route4me.com/), [Testiny](https://www.testiny.io/), [TestMu AI](https://www.testmuai.com/?utm_medium=sponsor&utm_source=junit)

## Latest Releases

- General Availability (GA): [JUnit 6.0.3](https://github.com/junit-team/junit-framework/releases/tag/r6.0.3) (February 15, 2026)
- Preview (Milestone/Release Candidate): [JUnit 6.1.0-M1](https://github.com/junit-team/junit-framework/releases/tag/r6.1.0-M1) (November 17, 2025)

## Documentation

- [User Guide]
- [Javadoc]
- [Release Notes]
- [Examples]

## Contributing

Contributions to JUnit are both welcomed and appreciated. For specific guidelines
regarding contributions, please see [CONTRIBUTING.md] in the root directory of the
project. Those willing to use milestone or SNAPSHOT releases are encouraged
to file feature requests and bug reports using the project's
[issue tracker](https://github.com/junit-team/junit-framework/issues). Issues marked with an
<a href="https://github.com/junit-team/junit-framework/issues?q=is%3Aissue+is%3Aopen+label%3Aup-for-grabs">`up-for-grabs`</a>
label are specifically targeted for community contributions.

## Getting Help

Ask JUnit-related questions on [StackOverflow] or use the Q&A category on [GitHub Discussions].

## Continuous Integration Builds

## 整体架构描述


## 核心模块划分
All modules can be _built_ and _tested_ with the [Gradle Wrapper] using the following command:

`./gradlew build`

All modules can be _installed_ in a local Maven repository for consumption in other local
projects via the following command:

`./gradlew publishToMavenLocal`

## Dependency Metadata

[![JUnit Jupiter version](https://img.shields.io/maven-central/v/org.junit.jupiter/junit-jupiter/6..svg?color=25a162&label=Jupiter)](https://central.sonatype.com/search?namespace=org.junit.jupiter)
[![JUnit Vintage version](https://img.shields.io/maven-central/v/org.junit.vintage/junit-vintage-engine/6..svg?color=25a162&label=Vintage)](https://central.sonatype.com/search?namespace=org.junit.vintage)
[![JUnit Platform version](https://img.shields.io/maven-central/v/org.junit.platform/junit-platform-commons/6..svg?color=25a162&label=Platform)](https://central.sonatype.com/search?namespace=org.junit.platform)

Consult the [Dependency Metadata] section of the [User Guide] for a list of all artifacts
of the JUnit Platform, JUnit Jupiter, and JUnit Vintage.


[Codecov]: https://codecov.io/gh/junit-team/junit-framework
[CONTRIBUTING.md]: https://github.com/junit-team/junit-framework/blob/HEAD/CONTRIBUTING.md
[Dependency Metadata]: https://docs.junit.org/current/appendix.html#dependency-metadata
[GitHub Discussions]: https://github.com/junit-team/junit-framework/discussions/categories/q-a
[Gradle toolchains]: https://docs.gradle.org/current/userguide/toolchains.html
[Gradle Wrapper]: https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:using_wrapper

## 技术选型
- 构建工具: 未知
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
