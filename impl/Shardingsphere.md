# ShardingSphere 项目分析

## 项目简介
ShardingSphere 是 Apache 软件基金会的分布式数据库中间件生态，提供数据分片、分布式事务、数据库治理等功能。

## 核心类结构

### 1. ShardingSphere（主类）
- **位置**: `org.apache.shardingsphere` 
- **职责**: 分布式数据库中间件入口

### 2. ShardingRule（分片规则）
- **职责**: 数据分片核心逻辑
- **功能**: 分片算法、分片键

### 3. ShardingAlgorithm（分片算法）
- **接口**: `org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm`
- **实现**: `ModShardingAlgorithm`, `RangeShardingAlgorithm`

### 4. KeyGenerateAlgorithm（主键生成）
- **接口**: `org.apache.shardingsphere.infra.algorithm.keygen.KeyGenerateAlgorithm`
- **实现**: `SnowflakeKeyGenerateAlgorithm`

### 5. TransactionManager（事务管理）
- **职责**: 分布式事务管理
- **实现**: `XATransactionManager`, `BASETransactionManager`

### 6.读写分离
- `ReadWriteSplittingRule`, `LoadBalanceAlgorithm`

## 设计模式

### 1. 装饰器模式
- `ShardingDataSourceWrapper` 包装数据源

### 2. 责任链模式
- SQL 解析 -> 优化 -> 执行

### 3. 策略模式
- 多种分片算法
- 多种负载均衡策略

### 4. 责任链模式
- `ShardingInterceptor` 链

### 5. 享元模式
- 共享规则对象

## 代码技巧

### 1. SQL 解析
```java
// 解析 SQL 获取分片键
SQLStatementContext context = new SQLStatementContextVisitor().visit(statement);
```

### 2. 分布式 ID
```java
// 雪花算法
long id = snowflake.nextId();
```

### 3. SQL 改写
```java
// 添加分片条件
SQLRewriter.rewrite(logicalSQL, shardingCondition);
```

### 4. 路由执行
```java
// 分片路由
Collection<ActualSQLGroup> routes = route(shardingRule, shardingCondition);
```

### 5. 结果合并
```java
// 归并排序
MergeEngine.merge(queryResults, orderByColumns);
```

## 代码规范

### 1. 模块化架构
- `features` - 功能特性
- `infra` - 基础设施
- `kernel` - 内核
- `proxy` - 数据库代理

### 2. SPI 扩展
- `META-INF/services/` 扩展点

### 3. 配置驱动
- YAML/Properties 配置

## 值得学习的地方

1. **数据分片**: 理解分片算法和路由
2. **分布式事务**: XA 和柔性事务
3. **读写分离**: 负载均衡
4. **SQL 解析**: 完整 SQL 解析流程
5. **结果归并**: 排序、分页、聚合
6. **分布式 ID**: 雪花算法
7. **数据库治理**: 流量治理、熔断
