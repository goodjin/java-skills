# Spring Boot 学习总结

## 1. 源码分析

### Spring Boot 启动流程

```
SpringApplication.run()
    │
    ├─▶ createBootstrapContext()      创建引导上下文
    │
    ├─▶ prepareEnvironment()          准备环境 (Profile, PropertySource)
    │
    ├─▶ printBanner()                 打印 Banner
    │
    ├─▶ createApplicationContext()    创建应用上下文 (Web/Reactive/普通)
    │
    ├─▶ prepareContext()               准备上下文 (Bean 扫描、注册)
    │       │
    │       ├─▶ postProcessApplicationContext()
    │       ├─▶ applyInitializers()
    │       └─▶ listeners.contextPrepared()
    │
    ├─▶ refreshContext()              刷新上下文 (核心：Bean 创建)
    │       │
    │       └─▶ AbstractApplicationContext.refresh()
    │               │
    │               ├─▶ prepareBeanFactory()
    │               ├─▶ postProcessBeanFactory()
    │               ├─▶ invokeBeanFactoryPostProcessors()
    │               ├─▶ registerBeanPostProcessors()
    │               ├─▶ initMessageSource()
    │               ├─▶ initApplicationEventMulticaster()
    │               ├─▶ onRefresh()
    │               ├─▶ registerListeners()
    │               ├─▶ finishBeanFactoryInitialization()
    │               └─▶ finishRefresh()
    │
    ├─▶ afterRefresh()                 刷新后处理
    │
    └─▶ callRunners()                  调用 Runner
```

### 关键创新点

1. **自动配置**: `META-INF/spring/*.imports`
2. **条件装配**: `@ConditionalOn*` 系列注解
3. **嵌入式服务器**: 自动选择 Tomcat/Jetty/Undertow
4. ** starters**: 依赖管理简化

---

## 2. 我的实现 vs Spring Boot 源码

| 特性 | 我的实现 | Spring Boot 源码 |
|------|---------|-----------------|
| Bean 扫描 | 简单类名匹配 | ClassPathBeanDefinitionScanner |
| 依赖注入 | 反射 field 注入 | BeanPostProcessor |
| 上下文 | HashMap | DefaultListableBeanFactory |
| 生命周期 | 简化 | 完整的发布-订阅 |
| 自动配置 | 无 | SpringFactoriesLoader |
| Web 支持 | 无 | 自动选择 WebServer |

---

## 3. 差距与改进

### 需要改进

1. **Bean 生命周期**
   - 缺少 BeanPostProcessor
   - 缺少初始化/销毁回调

2. **自动配置**
   - 需要实现 @EnableAutoConfiguration
   - 实现 SpringFactoriesLoader

3. **依赖注入**
   - 支持构造函数注入
   - 支持 @Value 注入

4. **组件扫描**
   - 支持自定义过滤
   - 支持 @Import

---

## 4. 最佳实践

### 自定义 Starter

```java
// 1. 配置类
@Configuration
@ConditionalOnClass(MyService.class)
@EnableConfigurationProperties(MyProperties.class)
public class MyAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public MyService myService(MyProperties props) {
        return new MyService(props);
    }
}

// 2. 注册 (Spring Boot 3.x)
// src/main/resources/META-INF/spring/
// org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

### 配置属性绑定

```java
@ConfigurationProperties(prefix = "my.service")
public class MyProperties {
    private String url;
    private int timeout;
    
    // getter/setter
}
```

---

## 5. 学习资源

- Spring Boot 源码: `core/spring-boot/src/main/java/org/springframework/boot/SpringApplication.java`
- 自动配置: `core/spring-boot-autoconfigure/src/main/java/.../autoconfigure/`
- SpringFactories: `spring-boot/src/main/resources/META-INF/spring.factories`
