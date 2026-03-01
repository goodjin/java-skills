# Spring Cloud 项目分析

## 项目简介
Spring Cloud 为分布式系统提供开发工具集，包括服务注册发现、配置中心、负载均衡、熔断器、网关等。

## 核心类结构

### 1. 服务注册与发现
- **核心**: `ServiceRegistry` 接口
- **实现**: `EurekaServiceRegistry`, `NacosServiceRegistry`, `ConsulServiceRegistry`
- **客户端**: `DiscoveryClient` 获取服务实例

### 2. 负载均衡
- **核心**: `LoadBalancer` 接口
- **实现**: `RibbonLoadBalancer`, `ReactiveLoadBalancer`
- **拦截**: `LoadBalancerInterceptor` 拦截 RestTemplate

### 3. 熔断器
- **核心**: `CircuitBreaker` 接口
- **实现**: `Resilience4JCircuitBreaker`, `SentinelCircuitBreaker`
- **注解**: `@CircuitBreaker(name = "xxx")`

### 4. 配置中心
- **核心**: `PropertySourceLocator`
- **实现**: `NacosPropertySourceLocator`, `ConsulPropertySourceLocator`
- **刷新**: `@RefreshScope` 动态刷新

### 5. 网关
- **核心**: `GatewayFilter`, `GlobalFilter`
- **路由**: `RouteLocator` 动态路由
- **Predicate**: `RoutePredicateFactory` 请求匹配

### 6. Feign / OpenFeign
- **核心**: `FeignClient` 声明式 HTTP 客户端
- **编译**: `FeignClientsRegistrar` 注册 Feign 接口
- **调用**: `ReflectiveFeign` 动态代理

### 7. 服务调用
- **核心**: `RestTemplate`, `WebClient`
- **集成**: `LoadBalancerClient` 负载均衡

## 设计模式

### 1. 客户端负载均衡
- `LoadBalancerInterceptor` 拦截请求
- `ServiceInstanceChooser` 选择实例

### 2. 熔断器模式
- 关闭 → 打开 → 半开状态转换
- 失败率阈值触发

### 3. 配置中心模式
- 动态推送配置
- 配置变更监听

### 4. 代理模式
- Feign 动态代理
- 透明化远程调用

### 5. 路由模式
- Gateway 路由Predicate
- Filter Chain 处理

## 代码技巧

### 1. 服务注册
```java
// Nacos 服务注册
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848
```

### 2. 负载均衡
```java
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}
// 自动使用 LoadBalancerClient
restTemplate.getForObject("http://service-name/xxx", String.class);
```

### 3. 熔断器
```java
@CircuitBreaker(name = "userService", fallbackMethod = "fallback")
public String call() {
    return restTemplate.getForObject("http://user/xxx", String.class);
}
public String fallback(Exception e) {
    return "fallback";
}
```

### 4. 配置刷新
```java
@RefreshScope
@RestController
public class ConfigController {
    @Value("${config.value}")
    private String value;
}
```

### 5. 网关路由
```yaml
spring.cloud.gateway.routes:
  - id: user-service
    uri: lb://user-service
    predicates:
      - Path=/user/**
```

## 代码规范

### 1. 版本管理
- 使用 Spring Cloud BOM 统一版本
- 兼容性问题需注意

### 2. 配置集中管理
- 公共配置抽离
- 环境隔离

### 3. 服务命名
- 小写字母 + 连字符
- 保持一致性

## 值得学习的地方

1. **微服务架构**: 服务治理模式
2. **服务发现**: 注册中心原理
3. **负载均衡**: 客户端侧负载均衡
4. **熔断降级**: 防止雪崩
5. **配置中心**: 动态配置管理
6. **API 网关**: 统一入口设计
7. **服务调用**: 声明式 REST 客户端
