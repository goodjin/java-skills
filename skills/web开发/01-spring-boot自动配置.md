# Spring Boot 自动配置原理

> 基于源码分析 v3.4.x

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

// @SpringBootApplication 相当于
@Configuration
@EnableAutoConfiguration
@ComponentScan
```

### 源码分析

#### 1. @EnableAutoConfiguration 注解

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@AutoConfigurationPackage
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {
    // ...
}
```

关键点：
- 导入了 `AutoConfigurationImportSelector`
- 实现了 `DeferredImportSelector` 接口

#### 2. AutoConfigurationImportSelector

```java
public class AutoConfigurationImportSelector implements DeferredImportSelector {
    
    @Override
    public String[] selectImports(AnnotationMetadata metadata) {
        AutoConfigurationEntry entry = getAutoConfigurationEntry(metadata);
        return entry.getConfigurations();
    }
    
    protected List<String> getCandidateConfigurations() {
        // 核心：使用 ImportCandidates 加载
        ImportCandidates.load(
            AutoConfiguration.class,
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
        );
    }
}
```

#### 3. 自动配置文件

**Spring Boot 3.x 使用:**
```
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

内容示例：
```properties
org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration
org.springframework.boot.autoconfigure.aop.AopAutoConfiguration
org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
# ... 共 140+ 自动配置类
```

#### 4. 条件注解

Spring Boot 使用条件注解来决定是否启用配置：

| 注解 | 作用 |
|------|------|
| @ConditionalOnClass | 类路径存在时生效 |
| @ConditionalOnMissingBean | Bean 不存在时生效 |
| @ConditionalOnProperty | 配置属性匹配时生效 |
| @ConditionalOnWebApplication | Web 应用时生效 |
| @ConditionalOnBean | 特定 Bean 存在时生效 |

#### 5. 自动配置示例

```java
@Configuration
@ConditionalOnClass(WebMvc.class)
@EnableConfigurationProperties(WebMvcProperties.class)
public class WebMvcAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public WelcomePageHandlerMapping welcomePageHandlerMapping(
            ApplicationContext applicationContext,
            FormattingConversionService mvcConversionService,
            ResourceUrlProvider mvcResourceUrlProvider) {
        // 自动配置内容
    }
}
```

## 最佳实践

### 1. 自定义 Starter

```
my-starter/
├── src/main/java/
│   └── com/example/autoconfigure/
│       └── MyAutoConfiguration.java
└── src/main/resources/
    └── META-INF/spring/
        └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

```java
@Configuration
@ConditionalOnClass(MyService.class)
@ConditionalOnMissingBean
public class MyAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public MyService myService() {
        return new MyService();
    }
}
```

### 2. 配置属性

```java
@ConfigurationProperties(prefix = "my.service")
public class MyProperties {
    private String name = "default";
    private int timeout = 30;
    // getters/setters
}

@Configuration
@EnableConfigurationProperties(MyProperties.class)
public class MyAutoConfiguration {
    // 使用 MyProperties
}
```

### 3. 排除自动配置

```java
// 方式1: 注解排除
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)

// 方式2: 配置文件排除
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

## 与我的 Demo 对比

| 方面 | 源码 | 我的实现 |
|------|------|----------|
| 配置加载 | ImportCandidates | 简单反射 |
| 条件注解 | 完整实现 | 简化版 |
| 配置属性 | @ConfigurationProperties | 无 |
| 启动流程 | 完整的 SpringApplication | 简化版 |

## 总结

Spring Boot 自动配置核心：
1. `EnableAutoConfiguration` 导入 `AutoConfigurationImportSelector`
2. `ImportCandidates` 读取 `META-INF/spring/xxx.imports`
3. `@Conditional*` 条件注解决定是否生效
4. `@ConfigurationProperties` 绑定配置属性
