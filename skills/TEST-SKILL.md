# Java 测试策略 SKILL

基于100个Java开源项目测试代码分析的实战经验总结。

---

## 1. 测试框架选择

### 主流框架

| 框架 | 使用场景 | 示例项目 |
|------|----------|----------|
| **JUnit 4** | 成熟项目、存量代码 | HikariCP, Druid, RxJava |
| **JUnit 5 (Jupiter)** | 新项目、现代Java | Caffeine, Arthas, Shardingsphere |
| **TestNG** | 特殊需求、TCK测试 | RxJava (兼容性测试) |
| **Spring Boot Test** | Spring生态项目 | Apollo, Activiti |

### 断言库选择

- **AssertJ**: 主流选择，流畅API，RxJava/Caffeine/Arthas都在用
- **Google Truth**: Caffeine 专用，简洁易读
- **JUnit Assert**: 传统但够用

### Mock框架

- **Mockito**: 绝对主流，几乎所有项目都在用
- **自定义Mock**: HikariCP 使用 `com.zaxxer.hikari.mocks` 包
- **少用Mock**: RxJava、AsyncHttpClient 倾向真实流/服务器测试

---

## 2. 测试策略模式

### 核心库/基础架构 → 全面覆盖

```
RxJava: 970测试文件, 28万+行测试代码
AssertJ: 4328测试文件, 100万+行测试代码
ShardingSphere: 1591测试文件
```

策略：每个操作符/方法都有数十到数百个测试用例，覆盖边界条件和错误场景。

### 工具库 → 重点覆盖

```
Caffeine: 75测试文件，专注缓存核心逻辑
HikariCP: 64测试文件，专注连接池核心功能
```

策略：测试核心功能、并发安全、边界条件，不测性能（单独基准测试）。

### 框架/中间件 → 集成测试为主

```
Apollo: 191测试文件，Spring Boot Test + H2
Druid: 805测试文件，真实数据库测试
AsyncHttpClient: 163测试文件，Embedded Jetty
```

策略：单元测试 + 集成测试，使用内存数据库/嵌入式服务器模拟真实场景。

---

## 3. 覆盖率取舍原则

### ✅ 必须测试

| 场景 | 原因 | 典型项目 |
|------|------|----------|
| 核心业务逻辑 | 项目价值所在 | 所有项目 |
| 并发/异步代码 | bug难以重现 | HikariCP, Caffeine, RxJava |
| 配置校验 | 错误配置导致严重问题 | HikariCP |
| 边界条件 | 80% bug来自边界 | AssertJ |
| 错误处理路径 | 生产环境常见 | RxJava |

### ⚠️ 视情况测试

| 场景 | 取舍理由 | 典型处理 |
|------|----------|----------|
| 第三方集成 | 测试成本高 | 用Mock隔离 |
| 真实数据库 | 速度慢 | H2内存数据库 / Testcontainers |
| 极端并发 (1000+) | 发生概率低 | 理论验证 + 常规并发测试 |
| 性能基准 | 单独模块 | 独立性能测试套件 |
| OSGi/特殊容器 | 环境复杂 | 少测或跳过 |

### ❌ 通常不测

- 第三方库的内部实现
- 极端内存压力场景
- 真实生产环境完整集成
- UI渲染（如果是纯后端项目）

---

## 4. 测试工具与技巧

### 时间模拟

```java
// Caffeine 使用 Google FakeTicker
FakeTicker ticker = new FakeTicker();
ticker.advance(10, TimeUnit.MINUTES);  // 模拟时间流逝
```

### 嵌入式服务器

```java
// AsyncHttpClient 使用 Embedded Jetty
HttpTest 基类提供测试服务器
```

### 参数化测试

```java
// Caffeine 示例
@ParameterizedTest
@MethodSource("cacheSpecs")
void should_evict_when_full(CacheSpec spec) { }
```

### 测试基类模式

```java
// HikariCP: TestElf.java 共享工具
// Apollo: AbstractIntegrationTest 基类
// AsyncHttpClient: HttpTest 基类
```

### 自定义测试扩展

```java
// ShardingSphere: AutoMockExtension
// RxJava: RxJavaTest 基类
// Caffeine: CacheSpec 配置组合
```

---

## 5. 测试组织结构

### 按模块分组

```
test/
├── src/test/java/
│   ├── pool/          # 连接池测试
│   ├── sql/           # SQL解析测试
│   ├── metrics/       # 指标测试
│   └── util/          # 工具类测试
```

### 命名规范

- 测试类: `*Test.java` 或 `Test*.java` 或 `*TestCase.java`
- 测试方法: `test*()` 或 `should*()` (AssertJ风格)
- 集成测试: `*IntegrationTest.java`

---

## 6. 测试金字塔

```
        /\
       /  \      端到端测试 (E2E)
      /----\     少量，关键路径验证
     /      \
    /--------\   集成测试
   /          \  框架集成、数据库交互
  /------------\ 单元测试
 /              \ 核心逻辑、边界条件
```

**实践建议**:
- 单元测试占70%，专注核心逻辑
- 集成测试占20%，验证组件交互
- E2E测试占10%，验证关键路径

---

## 7. 关键测试场景

### 并发测试

```java
// HikariCP: ConnectionRaceConditionTest
// Caffeine: MultiThreadedTest
// 验证: 竞态条件、线程安全、资源泄露
```

### 异步/响应式测试

```java
// RxJava: 每个操作符都有异步测试
// 使用: CountDownLatch, async assertions
// 验证: 线程调度、背压、取消行为
```

### 熔断/限流测试

```java
// Sentinel: 限流规则、熔断降级、系统自适应
// 验证: 规则生效、多场景配置
```

---

## 8. 反模式警示

### ❌ 不要这样做

1. **过度Mock**: Mock一切，失去真实行为验证
2. **测试顺序依赖**: 测试间相互依赖，无法并行
3. **忽略边界**: 只测"happy path"
4. **断言不足**: `assertTrue(true)` 类无意义断言
5. **忽视性能**: 测试太慢导致没人跑

### ✅ 最佳实践

1. **测试独立**: 每个测试可单独运行
2. **AAA模式**: Given(Arrange) / When(Act) / Then(Assert)
3. **清晰命名**: 从测试名知道测什么
4. **适当覆盖**: 核心高覆盖，边缘适度
5. **持续集成**: 每次提交自动运行

---

## 9. 框架特定建议

### Spring 项目

```java
@SpringBootTest
@MockBean
使用 H2 内存数据库
```

### 数据库相关

- 连接池: 真实数据库测试 + Mock隔离
- ORM: Repository 层完整测试
- 使用 Testcontainers 模拟真实数据库

### 异步/响应式

- 优先真实流测试，减少Mock
- 使用专门的测试工具 (RxJava TestHelper, Project Reactor StepVerifier)
- 超时保护防止测试挂起

### 基础设施库

- 全面覆盖每个public方法
- 边界条件和错误场景重点测
- 并发测试不可或缺

---

## 10. 总结

测试策略取决于项目性质：

| 项目类型 | 测试重点 | 覆盖率目标 |
|----------|----------|------------|
| 核心库/基础架构 | 全面覆盖、边界条件 | 80%+ |
| 工具库 | 核心功能、并发安全 | 60-70% |
| 框架/中间件 | 集成场景、组件交互 | 50-60% |
| 应用层 | 关键业务路径 | 按需 |

**核心理念**: 测试是为了信任代码，盲目追求100%覆盖率不如聚焦核心逻辑和风险点。

---

*基于 HikariCP, RxJava, Sentinel, Apollo, Druid, Caffeine, AsyncHttpClient, Shardingsphere, Spring Security, AssertJ 等100个Java开源项目的测试分析总结。*
