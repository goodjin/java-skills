# Async Http Client 项目需求分析

## 项目定位（一句话）

异步HTTP客户端库，用于Java应用执行HTTP请求和处理异步响应。

## 核心功能列表

- 异步HTTP请求执行
- 异步响应处理
- WebSocket协议支持
- 基于Netty构建
- 支持Java 11+
- 连接池管理
- Dsl便捷方法

## 快速开始要点

```xml
<dependency>
    <groupId>org.asynchttpclient</groupId>
    <artifactId>async-http-client</artifactId>
    <version>3.0.7</version>
</dependency>
```

```java
import static org.asynchttpclient.Dsl.*;
AsyncHttpClient asyncHttpClient = asyncHttpClient();
// 使用后需要关闭
```

## 文档结构特点

- 基于Netty
- 官方GitHub有详细文档

## 资源链接

- GitHub: https://github.com/AsyncHttpClient/async-http-client
