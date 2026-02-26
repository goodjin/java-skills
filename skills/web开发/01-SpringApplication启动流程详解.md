# SpringApplication 启动流程详解

## 概述

Spring Boot 应用启动的核心入口是 `SpringApplication.run()` 方法。这个方法负责整个 Spring Boot 应用的启动流程，包括环境准备、上下文创建、bean 加载、服务器启动等关键步骤。

## 源码分析

### 1. 启动入口

```java
// SpringApplication.java
public static void main(String[] args) throws Exception {
    SpringApplication.run(new Class<?>[0], args);
}

public static ConfigurableApplicationContext run(Class<?>[] primarySources, String[] args) {
    return new SpringApplication(primarySources).run(args);
}
```

### 2. SpringApplication 构造函数

```java
public SpringApplication(Class<?>... primarySources) {
    this(null, primarySources);
}

public SpringApplication(@Nullable ResourceLoader resourceLoader, Class<?>... primarySources) {
    this.resourceLoader = resourceLoader;
    this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
    
    // 1. 推断 Web 应用类型
    this.properties.setWebApplicationType(WebApplicationType.deduce());
    
    // 2. 获取 BootstrapRegistryInitializer 实例
    this.bootstrapRegistryInitializers = new ArrayList<>(
        getSpringFactoriesInstances(BootstrapRegistryInitializer.class));
    
    // 3. 获取 ApplicationContextInitializer 实例
    setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
    
    // 4. 获取 ApplicationListener 实例
    setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
    
    // 5. 推断主应用类
    this.mainApplicationClass = deduceMainApplicationClass();
}
```

### 3. run() 方法核心流程

```java
public ConfigurableApplicationContext run(String... args) {
    // 3.1 创建启动计时器
    Startup startup = Startup.create();
    
    // 3.2 启用关闭钩子
    if (this.properties.isRegisterShutdownHook()) {
        SpringApplication.shutdownHook.enableShutdownHookAddition();
    }
    
    // 3.3 创建 BootstrapContext
    DefaultBootstrapContext bootstrapContext = createBootstrapContext();
    
    // 3.4 配置 headless 模式
    configureHeadlessProperty();
    
    // 3.5 获取并启动 SpringApplicationRunListeners
    SpringApplicationRunListeners listeners = getRunListeners(args);
    listeners.starting(bootstrapContext, this.mainApplicationClass);
    
    try {
        // 3.6 准备环境
        ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        ConfigurableEnvironment environment = prepareEnvironment(listeners, bootstrapContext, applicationArguments);
        
        // 3.7 打印 Banner
        Banner printedBanner = printBanner(environment);
        
        // 3.8 创建 ApplicationContext
        context = createApplicationContext();
        context.setApplicationStartup(this.applicationStartup);
        
        // 3.9 准备 Context
        prepareContext(bootstrapContext, context, environment, listeners, applicationArguments, printedBanner);
        
        // 3.10 刷新 Context（核心！）
        refreshContext(context);
        
        // 3.11 后置处理
        afterRefresh(context, applicationArguments);
        
        // 3.12 记录启动时间
        Duration timeTakenToStarted = startup.started();
        
        // 3.13 打印启动信息
        if (this.properties.isLogStartupInfo()) {
            new StartupInfoLogger(this.mainApplicationClass, environment).logStarted(getApplicationLog(), startup);
        }
        
        // 3.14 通知启动完成
        listeners.started(context, timeTakenToStarted);
        
        // 3.15 调用 Runner
        callRunners(context, applicationArguments);
    }
    catch (Throwable ex) {
        throw handleRunFailure(context, ex, listeners);
    }
    
    try {
        if (context.isRunning()) {
            listeners.ready(context, startup.ready());
        }
    }
    catch (Throwable ex) {
        throw handleRunFailure(context, ex, null);
    }
    return context;
}
```

### 4. 环境准备流程

```java
private ConfigurableEnvironment prepareEnvironment(SpringApplicationRunListeners listeners,
        DefaultBootstrapContext bootstrapContext, ApplicationArguments applicationArguments) {
    
    // 4.1 创建或获取 Environment
    ConfigurableEnvironment environment = getOrCreateEnvironment();
    
    // 4.2 配置 Environment
    configureEnvironment(environment, applicationArguments.getSourceArgs());
    
    // 4.3 附加配置属性源
    ConfigurationPropertySources.attach(environment);
    
    // 4.4 通知环境准备完成
    listeners.environmentPrepared(bootstrapContext, environment);
    
    // 4.5 移动属性源
    ApplicationInfoPropertySource.moveToEnd(environment);
    DefaultPropertiesPropertySource.moveToEnd(environment);
    
    // 4.6 绑定环境到 SpringApplication
    bindToSpringApplication(environment);
    
    // 4.7 转换环境（如需要）
    if (!this.isCustomEnvironment) {
        EnvironmentConverter environmentConverter = new EnvironmentConverter(getClassLoader());
        environment = environmentConverter.convertEnvironmentIfNecessary(environment, deduceEnvironmentClass());
    }
    
    ConfigurationPropertySources.attach(environment);
    return environment;
}
```

### 5. Context 准备流程

```java
private void prepareContext(DefaultBootstrapContext bootstrapContext, ConfigurableApplicationContext context,
        ConfigurableEnvironment environment, SpringApplicationRunListeners listeners,
        ApplicationArguments applicationArguments, @Nullable Banner printedBanner) {
    
    // 5.1 设置环境
    context.setEnvironment(environment);
    
    // 5.2 后处理 ApplicationContext
    postProcessApplicationContext(context);
    
    // 5.3 添加 AOT 生成的初始化器
    addAotGeneratedInitializerIfNecessary(this.initializers);
    
    // 5.4 应用初始化器
    applyInitializers(context);
    
    // 5.5 通知 Context 准备完成
    listeners.contextPrepared(context);
    
    // 5.6 关闭 BootstrapContext
    bootstrapContext.close(context);
    
    // 5.7 打印启动信息
    if (this.properties.isLogStartupInfo()) {
        logStartupInfo(context);
        logStartupProfileInfo(context);
    }
    
    // 5.8 注册单例 Bean
    ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
    beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
    if (printedBanner != null) {
        beanFactory.registerSingleton("springBootBanner", printedBanner);
    }
    
    // 5.9 配置循环引用和 Bean 定义覆盖
    if (beanFactory instanceof AbstractAutowireCapableBeanFactory autowireCapableBeanFactory) {
        autowireCapableBeanFactory.setAllowCircularReferences(this.properties.isAllowCircularReferences());
        if (beanFactory instanceof DefaultListableBeanFactory listableBeanFactory) {
            listableBeanFactory.setAllowBeanDefinitionOverriding(this.properties.isAllowBeanDefinitionOverriding());
        }
    }
    
    // 5.10 懒加载处理
    if (this.properties.isLazyInitialization()) {
        context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());
    }
    
    // 5.11 加载 Bean 定义
    if (!AotDetector.useGeneratedArtifacts()) {
        Set<Object> sources = getAllSources();
        Assert.state(!ObjectUtils.isEmpty(sources), "No sources defined");
        load(context, sources.toArray(new Object[0]));
    }
    
    // 5.12 通知 Context 加载完成
    listeners.contextLoaded(context);
}
```

## 核心类解释

### SpringApplication

- **作用**: Spring Boot 应用的主启动类，负责整个应用的启动流程
- **关键方法**: `run()`、`createApplicationContext()`、`prepareEnvironment()`

### SpringApplicationRunListeners

- **作用**: 管理多个 `SpringApplicationRunListener`，用于在启动各个阶段发布事件
- **关键方法**: `starting()`、`environmentPrepared()`、`contextPrepared()`、`contextLoaded()`、`started()`、`ready()`

### ApplicationContextFactory

- **作用**: 创建 `ApplicationContext` 的策略接口
- **默认实现**: `DefaultApplicationContextFactory`，根据 `WebApplicationType` 创建相应上下文

### WebApplicationType

- **作用**: 枚举类，用于推断 Web 应用类型
- **取值**:
  - `NONE`: 非 Web 应用
  - `SERVLET`: Servlet Web 应用
  - `REACTIVE`: 响应式 Web 应用

## 启动流程图

```
┌─────────────────────────────────────────────────────────────┐
│                     SpringApplication.run()                  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  1. 创建 SpringApplication 实例                               │
│     - 推断 WebApplicationType                               │
│     - 加载 BootstrapRegistryInitializer                     │
│     - 加载 ApplicationContextInitializer                    │
│     - 加载 ApplicationListener                              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  2. 创建 BootstrapContext                                    │
│     - 创建 DefaultBootstrapContext                          │
│     - 调用 BootstrapRegistryInitializer.initialize()       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  3. 配置 Headless 模式                                       │
│     - 设置 java.awt.headless=true                           │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  4. 准备 Environment                                         │
│     - 创建/获取 ConfigurableEnvironment                      │
│     - 配置属性源                                             │
│     - 绑定到 SpringApplication                              │
│     - 发布 environmentPrepared 事件                          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  5. 打印 Banner                                              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  6. 创建 ApplicationContext                                  │
│     - 根据 WebApplicationType 创建对应上下文                 │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  7. 准备 Context                                             │
│     - 设置 Environment                                       │
│     - 应用 Initializers                                      │
│     - 加载 Bean 定义                                         │
│     - 注册单例                                               │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  8. 刷新 Context（核心）                                      │
│     - 执行 BeanFactoryPostProcessor                          │
│     - 实例化所有单例 Bean                                    │
│     - 启动嵌入式服务器（如果是 Web 应用）                    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  9. 调用 Runner                                              │
│     - 执行 ApplicationRunner                                 │
│     - 执行 CommandLineRunner                                 │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  10. 应用启动完成                                            │
│      - 发布 ready 事件                                       │
└─────────────────────────────────────────────────────────────┘
```

## 代码示例

### 基本的 Spring Boot 应用

```java
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 自定义 SpringApplication

```java
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MyApplication.class);
        
        // 设置Banner模式
        app.setBannerMode(Banner.Mode.OFF);
        
        // 设置Web应用类型
        app.setWebApplicationType(WebApplicationType.SERVLET);
        
        // 设置懒加载
        app.setLazyInitialization(true);
        
        // 添加自定义Initializer
        app.addInitializers(new MyApplicationContextInitializer());
        
        // 添加自定义Listener
        app.addListeners(event -> System.out.println("Event: " + event.getClass().getSimpleName()));
        
        app.run(args);
    }
}
```

### 自定义 ApplicationRunner

```java
@Component
public class MyRunner implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("Application started!");
        args.getOptionNames().forEach(name -> 
            System.out.println(name + " = " + args.getOptionValues(name)));
    }
}
```

## 最佳实践

### 1. 理解启动阶段

- **环境准备阶段**: 配置属性源、处理配置文件
- **Context 创建阶段**: 创建 BeanFactory、注册 Bean 定义
- **Bean 加载阶段**: 实例化单例 Bean、执行后置处理器
- **服务器启动阶段**: 启动嵌入式服务器

### 2. 性能优化

```java
// 懒加载优化
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MyApplication.class);
        // 启用懒加载
        app.setLazyInitialization(true);
        app.run(args);
    }
}
```

### 3. 启动失败处理

```java
@Component
public class FailureReporter implements SpringBootExceptionHandler {
    @Override
    public void registerLoggedException(Throwable exception) {
        // 记录已记录的异常
    }
    
    @Override
    public void registerExitCode(int exitCode) {
        // 设置退出码
        System.exit(exitCode);
    }
}
```

### 4. 调试启动问题

- 使用 `DEBUG` 日志级别查看详细启动信息
- 查看 `ConditionEvaluationReport` 了解条件装配结果
- 使用 Spring Boot Actuator 的 `/startup` 端点

### 5. 关闭钩子配置

```java
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MyApplication.class);
        // 禁用关闭钩子
        app.setRegisterShutdownHook(false);
        app.run(args);
    }
}
```

## 总结

Spring Boot 的启动流程是一个精心设计的复杂过程，涉及多个阶段的协调配合。理解这个流程对于调试启动问题、优化应用性能、自定义启动行为都非常重要。核心是通过 `SpringApplicationRunListeners` 和 `ApplicationContextInitializer` 提供扩展点，让开发者可以在各个阶段插入自定义逻辑。
