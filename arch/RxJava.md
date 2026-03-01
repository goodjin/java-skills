# RxJava 架构文档分析

## 项目概述
- **项目类型**: 响应式编程库
- **核心功能**: JVM上的响应式扩展库，支持异步和基于事件的程序

## 架构文档位置
- 主文档: `README.md`
- 设计文档: `DESIGN.md`
- 额外文档: `docs/` 目录

## 文档结构分析

### README.md 章节结构
1. **版本信息** - v3.x, v2.x, v1.x
2. **Getting started** - 入门指南
   - Setting up the dependency
   - Hello World 示例
   - Base classes (Flowable, Observable, Single, Maybe, Completable)
3. **术语解释**
   - Upstream, downstream
   - Objects in motion
   - Backpressure (背压)
   - Assembly time, Subscription time, Runtime
4. **操作符文档** - 链接到Wiki
5. **测试支持**
6. **Backpressure策略**
7. **调度器(Schedulers)**
8. **错误处理**
9. **Hot vs Cold Observables**

### DESIGN.md 章节结构 (架构设计专项文档)
1. **Terminology & Definitions** - 术语定义
   - Interactive vs Reactive
   - Hot vs Cold
   - Reactive/Push
   - Sync Pull vs Async Pull
   - Flow Control
   - Eager vs Lazy
2. **RxJava & Related Types** - 核心类型设计
   - Observable 类型签名和契约
   - Flowable 类型签名
   - Single, Maybe, Completable

## 描述风格
- **概念驱动**: 以概念和术语为核心
- **示例驱动**: 大量代码示例
- **分层解释**: 从简单到复杂 (Hello World → 高级操作符)
- **契约明确**: 明确接口契约和行为

## 与PRD文档的对应关系
| README/DESIGN章节 | PRD可能对应 |
|------------------|------------|
| Base classes | 产品功能 - 核心类型定义 |
| Terminology | 术语表 - 领域概念定义 |
| Backpressure | 技术规格 - 流控策略 |
| Schedulers | 技术选型 - 线程模型 |
| Type contracts | 接口设计 - 类型契约 |

## 架构信息提取

### 核心模块
- Flowable (支持背压)
- Observable (不支持背压)
- Single (单一结果)
- Maybe (0或1结果)
- Completable (无结果)

### 技术选型
- 依赖: Reactive-Streams
- Java版本: Java 8+ 或 Android API 21+
- 异步模型: 非阻塞响应式

### 设计原则
- 非阻塞优先
- 背压支持
- 虚时间和调度器用于测试
- 操作符组合声明式
