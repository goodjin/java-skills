# Netty 项目分析

## 项目简介
Netty 是一个异步事件驱动的网络应用程序框架，用于快速开发可维护的高性能协议服务器和客户端。

## 核心类结构

### 1. Channel（通道）
- **位置**: `io.netty.channel.Channel`
- **职责**: 网络连接的抽象
- **实现**: `NioSocketChannel`, `NioServerSocketChannel`, `EpollSocketChannel` 等

### 2. EventLoop（事件循环）
- **位置**: `io.netty.channel.EventLoop`
- **职责**: 处理 Channel 的所有 I/O 事件
- **核心**: 单线程处理避免锁竞争

### 3. ChannelPipeline（通道管道）
- **职责**: 存储 ChannelHandler 的链式结构
- **功能**:  Inbound/Outbound 事件传播

### 4. ChannelHandler（通道处理器）
- **接口**: `ChannelInboundHandler`, `ChannelOutboundHandler`
- **实现**: `ChannelInitializer`, `ByteToMessageDecoder`, `MessageToByteEncoder`

### 5. Bootstrap / ServerBootstrap
- **职责**: 引导客户端/服务端启动

### 6. ByteBuf（字节缓冲区）
- **位置**: `io.netty.buffer.ByteBuf`
- **优势**: 比 ByteBuffer 更高效、更易用

## 设计模式

### 1. 责任链模式（Chain of Responsibility）
- `ChannelPipeline` 中的 `ChannelHandler` 链
- 每个 Handler 处理特定功能

### 2. 装饰器模式（Decorator）
- `ChannelHandler` 的包装器
- 如 `CombinedChannelDuplexHandler`

### 3. 工厂模式（Factory）
- `ChannelFactory` 创建 Channel 实例
- `ByteBufAllocator` 分配缓冲区

### 4. 策略模式（Strategy）
- 多种 `ByteBufAllocator` 实现
- 多种 `EpollMode` 实现

### 5. 适配器模式（Adapter）
- `ChannelHandlerAdapter` 提供默认实现

### 6. 回调模式（Callback）
- `ChannelFutureListener` 异步操作回调

## 代码技巧

### 1. 零拷贝
```java
// CompositeByteBuf 组合多个缓冲区
CompositeByteBuf compositeByteBuf = Unpooled.compositeBuffer();
compositeByteBuf.addComponent(true, byteBuf1, byteBuf2);
```

### 2. 引用计数
```java
// 自动/手动内存管理
ReferenceCountUtil.release(msg);
// 或使用 SimpleChannelInboundHandler 自动释放
```

### 3. 内存池
```java
// PooledByteBufAllocator 高效分配
PooledByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;
```

### 4. 预编码
```java
// Encoder 预编码提高性能
public class MyEncoder extends MessageToByteEncoder<MyMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, MyMessage msg, ByteBuf out) {
        // 编码逻辑
    }
}
```

### 5. 流量控制
```io.netty.channel.ChannelOption#WRITE_BUFFER_WATER_MARK```

## 代码规范

### 1. 包结构清晰
- `io.netty.channel` - Channel 相关
- `io.netty.buffer` - 缓冲区相关
- `io.netty.handler` - 处理器相关

### 2. 命名规范
- 类名清晰表达功能
- 接口以 `Handler`、`Encoder`、`Decoder` 结尾

### 3. 注释规范
- 关键类有详细 Javadoc
- 重要方法有使用示例

## 值得学习的地方

1. **高性能网络编程**: 理解 NIO/Epoll 机制
2. **内存管理**: 引用计数和内存池
3. **责任链设计**: 灵活的 Handler 链
4. **事件驱动**: 事件循环模型
5. **背压机制**: 流量控制
6. **协议设计**: 自定义协议的实现方式
7. **线程模型**: EventLoopGroup 的使用
