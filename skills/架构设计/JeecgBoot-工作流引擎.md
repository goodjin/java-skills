# JeecgBoot 工作流引擎分析

## 概述

JeecgBoot 工作流引擎基于 **Flowable** 实现，是企业级 BPM 流程管理解决方案。Flowable 是 Activiti 的开源分支，提供了更现代的架构和更好的性能。JeecgBoot 将 Flowable 深度集成到低代码平台中，实现只需在页面配置流程转向，极大简化了 BPM 工作流的开发。

## 架构设计

### 1. 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                     JeecgBoot 工作流架构                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐         │
│   │  流程设计器  │   │  流程配置   │   │  表单配置   │         │
│   │ (Web Modeler)│   │ (BPMN XML)  │   │(Online Form)│         │
│   └──────┬──────┘   └──────┬──────┘   └──────┬──────┘         │
│          │                 │                 │                 │
│          +-----------------+-----------------+                 │
│                          │                                       │
│                    ┌─────▼─────┐                                 │
│                    │  Flowable │                                 │
│                    │  引擎核心  │                                 │
│                    └─────┬─────┘                                 │
│                          │                                       │
│     ┌────────────────────┼────────────────────┐                 │
│     │                    │                    │                 │
│ ┌───▼───┐          ┌─────▼─────┐        ┌─────▼─────┐         │
│ │ 任务  │          │  历史记录  │        │  流程变量  │         │
│ │ 管理  │          │  (History) │        │ (Variables)│         │
│ └───────┘          └───────────┘        └───────────┘         │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 2. 核心特性

- **可视化流程设计** - Web 流程设计器，在线画流程图
- **自定义表单** - 支持表单设计器、Online 表单、编码表单
- **业务与流程分离** - 表单挂靠机制，松耦合设计
- **多种流程模式** - 会签、或签、顺序审批等
- **流程监控** - 实时查看流程状态和历史

## 核心概念

### Flowable 核心概念

| 概念 | 说明 |
|------|------|
| `ProcessDefinition` | 流程定义（部署的 BPMN 流程） |
| `ProcessInstance` | 流程实例（正在执行的流程） |
| `Task` | 任务（需要处理的工作项） |
| `Execution` | 执行（流程执行的路径） |
| `IdentityLink` | 身份关联（用户/角色与流程的关系） |
| `Variable` | 流程变量（流程中的数据） |
| `History` | 历史数据（已完成的流程记录） |

### JeecgBoot 扩展概念

| 概念 | 说明 |
|------|------|
| `SysCategory` | 流程分类（按业务分类管理流程） |
| `SysFlow` | 流程配置（Jeecg 流程管理实体） |
| `SysFlowCategory` | 分类配置 |
| `BpmLinked` | 业务表单关联（流程与业务数据绑定） |

## 流程生命周期

```
┌─────────────────────────────────────────────────────────────────┐
│                     流程生命周期                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   ┌─────────┐                                                   │
│   │  设计   │  -> 创建 BPMN 流程图                              │
│   └────┬────┘                                                   │
│        │                                                        │
│   ┌────▼────┐                                                   │
│   │  部署   │  -> 部署流程定义到引擎                            │
│   └────┬────┘                                                   │
│        │                                                        │
│   ┌────▼────┐                                                   │
│   │  发起   │  -> 启动流程实例                                │
│   └────┬────┘                                                   │
│        │                                                        │
│   ┌────▼────┐                                                   │
│   │  审批   │  -> 处理任务（通过/拒绝/转办/委派）               │
│   └────┬────┘                                                   │
│        │                                                        │
│   ┌────▼────┐                                                   │
│   │  完成   │  -> 流程正常结束                                  │
│   └────┬────┘                                                   │
│        │                                                        │
│   ┌────▼────┐                                                   │
│   │  归档   │  -> 历史数据存储                                  │
│   └─────────┘                                                   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 核心 API

### 流程定义

```java
// 部署流程
RepositoryService deploymentService = processEngine.getRepositoryService();
deploymentService.createDeployment()
    .addClasspathResource("processes/myflow.bpmn20.xml")
    .deploy();

// 获取流程定义
ProcessDefinition processDefinition = repositoryService
    .createProcessDefinitionQuery()
    .processDefinitionKey("myProcess")
    .latestVersion()
    .singleResult();
```

### 流程实例

```java
// 启动流程
RuntimeService runtimeService = processEngine.getRuntimeService();
ProcessInstance processInstance = runtimeService
    .startProcessInstanceByKey("myProcess", "businessKey", variables);

// 完成任务
TaskService taskService = processEngine.getTaskService();
taskService.complete(taskId, variables);
```

### 任务查询

```java
// 查询待办任务
List<Task> tasks = taskService.createTaskQuery()
    .processDefinitionKeyProcess")
    .("mytaskAssignee(userId)
    .orderByTaskCreateTime().desc()
    .list();
```

## 流程设计器

JeecgBoot 提供 Web 端流程设计器，支持：

1. **拖拽式建模** - 可视化设计流程图
2. **节点配置** - 配置审批人、条件、动作
3. **表单挂靠** - 关联业务表单
4. **规则引擎** - 配置审批规则
5. **多实例支持** - 会签、或签配置

### 节点类型

| 节点类型 | 说明 |
|----------|------|
| `StartEvent` | 开始事件 |
| `EndEvent` | 结束事件 |
| `UserTask` | 用户任务（审批节点） |
| `ServiceTask` | 服务任务（自动处理） |
| `ExclusiveGateway` | 排他网关（条件分支） |
| `ParallelGateway` | 并行网关（并行处理） |
| `InclusiveGateway` | 包含网关（混合分支） |

## 表单集成

JeecgBoot 支持三种表单方案：

### 1. 表单设计器表单
- 使用 JeecgBoot 表单设计器配置
- 支持拖拽布局
- 所见即所得

### 2. Online 配置表单
- Online 表单配置
- 无需编码

### 3. 编码表单
- 自定义 JSP/Vue 页面
- 适合复杂业务场景

## 审批动作

| 动作 | 说明 |
|------|------|
| `通过` | 审批同意，进入下一节点 |
| `拒绝` | 审批拒绝，流程结束 |
| `转办` | 转给其他人处理 |
| `委派` | 委托他人办理 |
| `退回` | 退回上一节点 |
| `驳回` | 驳回到指定节点 |
| `抄送` | 通知相关人员（不参与审批） |

## 业务整合

### 流程与业务绑定

```java
// 发起流程时绑定业务ID
Map<String, Object> variables = new HashMap<>();
variables.put("businessKey", "order_123");

ProcessInstance instance = runtimeService
    .startProcessInstanceByKey("orderApproval", "order_123", variables);

// 查询业务关联的流程
ProcessInstance instance = runtimeService
    .createProcessInstanceQuery()
    .processInstanceBusinessKey("order_123")
    .singleResult();
```

## 权限控制

- **数据权限** - 控制用户可见的流程数据
- **节点权限** - 控制用户可处理的审批节点
- **字段权限** - 控制审批表单的字段可见性

## 总结

JeecgBoot 工作流引擎特点：

1. **Flowable 引擎** - 企业级流程引擎，稳定可靠
2. **可视化设计** - Web 流程设计器，易于使用
3. **低代码集成** - 与 Online 表单深度集成
4. **灵活扩展** - 支持自定义任务监听器
5. **完整监控** - 流程监控和历史追溯
