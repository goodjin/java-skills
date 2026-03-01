# Sentinel 架构文档分析

## 项目概述
- **项目类型**: 流量控制/熔断降级组件
- **核心功能**: 微服务可靠性保护，流量控制、熔断、系统自适应保护

## 架构文档位置
- 主文档: `README.md`
- 额外文档: `doc/` 目录

## 文档结构分析

### README.md 章节结构
1. **Introduction** - 项目介绍
   - 分布式系统可靠性重要性
   - 核心功能: 流量控制、流量整形、并发限制、熔断、系统自适应保护
2. **特性概览**
   - 丰富适用场景 (双11场景)
   - 实时监控
   - 开源生态集成
   - 多语言支持 (Java, Go, C++, Rust)
   - SPI扩展
3. **Ecosystem Landscape** - 生态系统图
4. **Quick Start** - 快速开始 (5步)
   - 1. Add Dependency
   - 2. Define Resource (定义资源)
   - 3. Define Rules (定义规则)
   - 4. Check Result (查看结果)
   - 5. Start Dashboard (启动控制台)
5. **Trouble Shooting and Logs** - 故障排查
6. **Bugs and Feedback** - 反馈

## 描述风格
- **场景驱动**: 通过双11、秒杀等场景介绍
- **步骤驱动**: 5步快速开始
- **结果导向**: 展示实际运行结果(metrics日志)
- **产品化**: 包含Dashboard控制台

## 与PRD文档的对应关系
| README章节 | PRD可能对应 |
|-----------|------------|
| Introduction | 产品愿景 - 微服务保护平台 |
| 核心特性 | 功能规格 - 流量控制/熔断/降级 |
| Quick Start | 使用指南 - 接入流程 |
| Dashboard | UI设计 - 控制台功能 |
| Rules | 配置规格 - 规则定义 |

## 架构信息提取

### 核心模块
- sentinel-core (核心引擎)
- sentinel-adapter (框架适配器)
- sentinel-dashboard (控制台)
- sentinel-transport (传输模块)
- sentinel-cluster (集群流控)
- sentinel-extension (扩展)
- sentinel-logging (日志)

### 技术选型
- Java版本: JDK 1.8+
- 集成: Spring Cloud, Dubbo, gRPC, Quarkus
- 监控: 实时监控 (单机+集群)
- SPI扩展机制

### 设计理念
- 以流量为突破口
- 保障微服务可靠性
- 低侵入接入
- 规则动态配置
