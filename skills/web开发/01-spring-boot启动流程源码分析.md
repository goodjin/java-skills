# Spring Boot 启动流程源码分析

## 核心流程 (Spring Boot 3.x)

```java
public ConfigurableApplicationContext run(String... args) {
    // 1. 创建引导上下文
    DefaultBootstrapContext bootstrapContext = createBootstrapContext();
    
    // 2. 准备环境
    ConfigurableEnvironment environment = prepareEnvironment(...);
    
    // 3. 打印 Banner
    Banner printedBanner = printBanner(environment);
    
    // 4. 创建应用上下文
    context = createApplicationContext();
    
    // 5. 准备上下文 (Bean 扫描、注册)
    prepareContext(...);
    
    // 6. 刷新上下文 (核心：Bean 创建)
    refreshContext(context);
    
    // 7. 启动完成回调
    afterRefresh(context, applicationArguments);
    
    // 8. 调用 Runner
    callRunners(context, applicationArguments);
}
```

## 关键组件

### 1. SpringApplicationRunListeners
- 负责发布生命周期事件
- starting → environmentPrepared → contextPrepared → contextLoaded → started → ready

### 2. ApplicationContext
- 根据 classpath 自动选择:
  - AnnotationConfigApplicationContext (普通)
  - ServletWebServerApplicationContext (Web)
  - ReactiveWebServerApplicationContext (Reactive)

### 3. 自动配置机制
```java
// META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
// Spring Boot 3.x 使用 imports 文件
```

### 4. 条件装配
- @ConditionalOnClass
- @ConditionalOnMissingBean
- @ConditionalOnProperty

## 最佳实践

1. **自定义 Starter**
```
my-starter/
├── src/main/java/.../
│   └── MyAutoConfiguration.java
└── src/main/resources/
    └── META-INF/spring/
        └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

2. **配置属性**
```java
@ConfigurationProperties(prefix = "my.config")
public class MyProperties {
    private String name;
    // getter/setter
}
```

3. **环境后置处理器**
```java
public class MyEnvPostProcessor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication app) {
        // 自定义处理
    }
}
```
