# liquibase

## 项目简介
# Liquibase [![Build and Test](https://github.com/liquibase/liquibase/actions/workflows/run-tests.yml/badge.svg)](https://github.com/liquibase/liquibase/actions/workflows/run-tests.yml) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=liquibase&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=liquibase)
<p align="center"><img src="https://github.com/liquibase/liquibase/blob/master/Liquibase.png" width="30%" height="30%"></p>

Liquibase helps millions of developers track, version, and deploy database schema changes. It will help you to:
- Control database schema changes for specific versions
- Eliminate errors and delays when releasing databases
- Automatically order scripts for deployment
- Easily rollback changes
- Collaborate with tools you already use

This repository contains the main source code for Liquibase Community. For more information about the product, see the [Liquibase website](https://www.liquibase.com/).

## Liquibase Automation and Integrations

Liquibase Community has built-in support for a variety of databases. Databases that are not part of Liquibase Community require extensions that you can download for free. Here is the full list of [supported databases](https://www.liquibase.com/supported-databases).

Liquibase can be integrated with Maven, Ant, Gradle, Spring Boot, and other CI/CD tools. For a full list, see [Liquibase Tools & Integrations](https://docs.liquibase.com/tools-integrations/home.html). You can use Liquibase with [GitHub Actions](https://github.com/liquibase/liquibase-github-action-example), [Spinnaker](https://github.com/liquibase/liquibase-spinnaker-plugin), and many different [workflows](https://docs.liquibase.com/workflows/home.html).


## Install and Run Liquibase

### System Requirements
Liquibase system requirements can be found on the [Download Liquibase](https://www.liquibase.com/download) page.

### An H2 in-memory database example for CLI
1. [Download and run the appropriate installer](https://www.liquibase.com/download). 
2. Make sure to add Liquibase to your PATH.
3. Copy the included `examples` directory to the needed location.
4. Open your CLI and navigate to your `examples/sql` or `examples/xml` directory.
5. Start the included H2 database with the `liquibase init start-h2` command.
6. Run the `liquibase update` command.
7. Run the `liquibase history` command to see what has executed!

See also how to [get started with Liquibase in minutes](https://docs.liquibase.com/start/home.html) or refer to our [Installing Liquibase](https://docs.liquibase.com/start/install/home.html) documentation page for more details.

## Documentation

Visit the [Liquibase Documentation](https://docs.liquibase.com/home.html) website to find the information on how Liquibase works.

## Courses

## 整体架构描述


## 核心模块划分


## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
