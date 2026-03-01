# Seata 项目分析

## 项目简介
Seata 是阿里巴巴开源的分布式事务解决方案，提供高性能和简单易用的分布式事务服务。

## 核心类结构

### 1. TransactionManager（事务管理器）
- **职责**: 管理全局事务
- **接口**: `io.seata.tm.TransactionManager`
- **功能**: 事务提交/回滚、状态管理

### 2. ResourceManager（资源管理器）
- **位置**: `io.seata.rm.ResourceManager`
- **职责**: 管理分支事务和资源

### 3. TC（Transaction Coordinator）
- **职责**: 事务协调器
- **核心**: 处理全局事务和分支事务

### 4. TM（Transaction Manager）
- **职责**: 事务管理器（应用侧）
- **入口**: `GlobalTransaction`

### 5. RM（Resource Manager）
- **职责**: 资源管理器（数据库侧）

### 6. AT 模式核心
- `UndoLogManager` - 回滚日志
- `TableMetaCache` - 表元数据缓存

## 设计模式

### 1. TC 架构模式
- Server/Client 架构

### 2. 代理模式
- `DataSourceProxy` 数据源代理

### 3. 策略模式
- 多种事务模式（AT, TCC, SAGA, XA）

### 4. 责任链模式
- `TransactionHook` 钩子链

### 5. 观察者模式
- 事务事件监听

## 代码技巧

### 1. TCC 模式
```java
// Try-Confirm-Cancel
@LocalTCC
public interface TccService {
    @TwoPhaseBusinessAction(name = "tryService")
    boolean tryService(BusinessActionContext context);
    
    @BusinessActionContextParameter(paramName = "param")
    boolean confirm(BusinessActionContext context);
    
    boolean cancel(BusinessActionContext context);
}
```

### 2. AT 模式
```java
// 自动生成回滚 SQL
// 解析前镜像 -> 执行 SQL -> 解析后镜像
```

### 3. 事务日志
```java
// 写入 undo log
UndoLogManager.flushUndoLogs(connection);
```

### 4. 锁机制
```java
// 分支锁
LockManager lockManager = new LockManagerImpl();
lockManager.acquireLock(xid, rows);
```

### 5. 会话管理
```java
// GlobalSession / BranchSession
SessionHolder.getRootSessionManager().addGlobalSession(globalSession);
```

## 代码规范

### 1. 模块化设计
- `seata-server` - 服务端
- `seata-tm` - 事务管理器
- `seata-rm` - 资源管理器

### 2. SPI 机制
- 扩展点设计
- 多种实现切换

### 3. 配置中心
- 支持 Nacos, Zookeeper, Apollo 等

### 4. 高可用
- 支持集群部署

## 值得学习的地方

1. **分布式事务**: 理解 CAP 和 BASE 理论
2. **AT 模式**: 自动回滚日志
3. **TCC 模式**: 补偿事务
4. **Saga 模式**: 长事务处理
5. **XA 模式**: 强一致性
6. **锁机制**: 分布式锁设计
7. **高可用**: TC 集群部署
