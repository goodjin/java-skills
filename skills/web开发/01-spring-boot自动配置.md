# Spring Boot 自动配置原理

## 问题
Spring Boot 如何实现自动配置？

## 解决方案

### 核心机制

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// 相当于
@Configuration
@EnableAutoConfiguration
@ComponentScan
```

### 自动配置类

```java
@Configuration
@ConditionalOnClass(WebMvc.class)
@EnableConfigurationProperties(WebMvcProperties.class)
public class WebMvcAutoConfiguration {
    // 自动配置内容
}
```

### 注册机制

```
META-INF/spring.factories (Boot 2.x)
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports (Boot 3.x)
```

### 条件注解

- @ConditionalOnClass - 类存在时生效
- @ConditionalOnMissingBean - Bean 不存在时生效
- @ConditionalOnProperty - 配置属性匹配
- @ConditionalOnWebApplication - Web 应用时生效

## 最佳实践

1. 自定义 Starter: `xxx-spring-boot-starter`
2. 配置属性: `@ConfigurationProperties(prefix = "xxx")`
3. 条件装配: 按需启用功能
