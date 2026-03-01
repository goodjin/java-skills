# ReflectASM PRD 分析

## 项目概述

**项目名称**: ReflectASM  
**类型**: 高性能反射库  
**开源协议**: BSD 3-Clause License

## 简介

ReflectASM是一个极小的Java库，通过代码生成提供高性能反射能力。它生成访问类来set/get字段、调用方法或创建实例，这些访问类使用字节码而非Java反射，因此性能极高。

## 核心特性

- **高性能**: 比标准反射快10-100倍
- **代码生成**: 编译时生成访问类
- **无装箱开销**: 原始类型直接访问
- **线程安全**: 生成的访问类线程安全
- **极小体积**: 轻量级依赖

## 适用场景

- 高性能框架底层
- 游戏开发(需要频繁反射)
- 序列化/反序列化框架
- 任何对性能敏感的反射使用

## 性能对比

- Field Set/Get: 比Java Reflection快约8倍
- Method Call: 比Java Reflection快约2倍
- Constructor: 比Java Reflection快约2倍

## 技术栈

- 纯Java实现
- ASM字节码库
- JDK 6+

---

*分析时间: 2026-02-27*
