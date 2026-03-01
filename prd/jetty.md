# Jetty 项目需求分析

## 项目定位（一句话）

轻量级、高度可扩展的Java Web服务器和Servlet引擎。

## 核心功能列表

- HTTP/1、HTTP/2、HTTP/3支持
- WebSocket支持
- 异步处理
- 嵌入式部署
- Servlet容器
- 低延迟
- 高吞吐量

## 快速开始要点

- 下载jar直接运行
- 嵌入式：
```java
Server server = new Server(port);
server.setHandler(new MyHandler());
server.start();
```

## 文档结构特点

- Eclipse项目
- 官方文档详细
- 模块化设计

## 资源链接

- 官网: https://jetty.org
- GitHub: https://github.com/eclipse/jetty.project
