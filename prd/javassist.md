# Javassist PRD 分析

## 项目概述

**项目名称**: Javassist  
**类型**: Java字节码操作工具  
**开源协议**: MPL 1.1 / LGPL 2.1 / Apache License 2.0

## 简介

Javassist(JAVA programming ASSISTant)是一个Java字节码工程工具库，使得Java字节码操作变得简单。它允许程序在运行时定义新类或修改类文件。

## 核心特性

- **两层API**: 源码级和字节码级API
- **无 JVM 知识要求**: 可用Java语言语法编辑字节码
- **运行时类定义**: 动态创建和修改类
- **CtClass API**: 完整的类操作接口
- **字节码转换**: AOP、代理等场景支持

## 适用场景

- AOP框架实现
- 运行时字节码增强
- 动态代理生成
- 框架底层开发
- 性能监控工具

## 技术栈

- 纯Java实现
- 无额外运行时依赖
- JDK 8+

---

*分析时间: 2026-02-27*
