# Spring Boot 项目分析

## 项目简介
Spring Boot 是 Spring 生态的快速开发框架，通过自动配置、约定大于配置的理念，让 Java 开发更加简单高效。

## 核心类结构

### 1. SpringApplication
- **位置**: `org.springframework.boot.SpringApplication`
- **职责**: 启动 Spring 应用
- **关键方法**: `run()`, `static run()`
- **功能**: 加载配置、创建应用上下文、启动内嵌服务器

### 2. SpringBootApplication
- **位置**: `org.springframework.boot.SpringBootApplication`
- **职责**: 标注主类入口
- **组合注解**: `@SpringBootConfiguration`, `@EnableAutoConfiguration`, `@ComponentScan`

### 3. AutoConfiguration（自动配置）
- **位置**: `org.springframework.boot.autoconfigure.EnableAutoConfiguration`
- **核心**: `spring.factories` / `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- **机制**: 条件装配 `@ConditionalOnClass`, `@ConditionalOnBean` 等

### 4. ApplicationContext（应用上下文）
- **接口**: `ConfigurableApplicationContext`
- **实现**: `AnnotationConfigServletWebServerApplicationContext`
- **职责**: Bean 容器、管理生命周期

### 5. EmbeddedServletContainer（内嵌容器）
- **接口**: `WebServer`
- **实现**: `TomcatServletWebServerFactory`, `JettyWebServer`, `UndertowWebServer`
- **启动**: `WebServerApplicationContext`

### 6. ConfigurationProperties（配置属性）
- **位置**: `org.springframework.boot.context.properties.ConfigurationProperties`
- **绑定**: `ConfigurationPropertiesBinder`
- **属性类**: `@EnableConfigurationProperties`

### 7. CommandLineRunner / ApplicationRunner
- **接口**: 在应用启动后执行代码
- **执行顺序**: `@Order` 注解控制

## 设计模式

### 1. 约定大于配置
- 默认配置 + 自定义覆盖
- starter 自动依赖配置

### 2. 条件装配
- `@ConditionalOnClass` - 类存在时装配
- `@ConditionalOnBean` - Bean 存在时装配
- `@ConditionalOnProperty` - 配置存在时装配

### 3. 工厂模式
- `WebServerFactory` 创建内嵌服务器
- `ApplicationContextFactory` 创建上下文

### 4. 策略模式
- 多种 `EmbeddedServletContainer` 实现
- 多种 `DataSource` 自动配置

### 5. 事件驱动
- `ApplicationEvent` / `ApplicationListener`
- `SmartApplicationListener` 排序监听

## 代码技巧

### 1. 自动配置
```java
// 自定义 Starter
@Configuration
@ConditionalOnClass(UserService.class)
@ConfigurationProperties(prefix = "user")
public class UserAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public UserService userService() {
        return new UserService();
    }
}
```

### 2. 外部化配置
```java
// application.yml
spring:
  profiles:
    active: dev
  config:
    import: optional:file:./custom.yml
```

### 3. 条件装配
```java
@Bean
@ConditionalOnProperty(name = "feature.enabled", havingValue = "true")
public FeatureService featureService() {
    return new FeatureService();
}
```

### 4. 启动监听
```java
@Component
public class MyListener implements ApplicationListener<ApplicationStartedEvent> {
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        // 启动后执行
    }
}
```

## 代码规范

### 1. 主类位置
- 建议放在根包下
- `@SpringBootApplication` 扫描当前包及子包

### 2. 配置分离
- `application.yml` / `application.properties`
- 多环境: `application-{profile}.yml`

### 3. Starter 命名
- 官方: `spring-boot-starter-*`
- 第三方: `*-spring-boot-starter`

## 值得学习的地方

1. **自动配置**: 理解条件装配原理
2. **约定优于配置**: 平衡灵活性与约定
3. **外部化配置**: 多环境管理
4. **启动流程**: SpringApplication 内部机制
5. **Starter 设计**: 依赖自动配置
6. ** Actuator**: 可观测性端点设计
