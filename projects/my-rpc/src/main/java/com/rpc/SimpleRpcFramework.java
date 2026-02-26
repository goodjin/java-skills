package com.rpc;

import java.io.*;
import java.lang.reflect.*;

/**
 * 简化版 RPC 框架 - 对比 Dubbo
 * 
 * 差距分析:
 * | 特性        | 我的实现   | Dubbo           |
 * |-------------|-----------|-----------------|
 * | 传输        | BIO Socket| Netty           |
 * | 序列化      | Java      | Hessian/ProtoBuf|
 * | 注册中心    | 无        | ZK/Nacos        |
 * | 负载均衡   | 无        | 多种策略        |
 * | 集群容错   | 无        | Failover等      |
 * | Filter链   | 无        | 完整支持        |
 * | 超时重试   | 无        | 支持            |
 */
public class SimpleRpcFramework {

    // ============ 服务端 ============
    
    /**
     * 服务导出 (对应 Dubbo Protocol.export)
     */
    public static void export(Object service, int port) throws Exception {
        ServerSocket server = new ServerSocket(port);
        System.out.println("RPC Server started on port " + port);
        
        while (true) {
            Socket client = server.accept();
            // 处理请求
            new Thread(() -> handleRequest(client, service)).start();
        }
    }
    
    private static void handleRequest(Socket client, Object service) {
        try (ObjectInputStream in = new ObjectInputStream(client.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream())) {
            
            // 读取请求
            String methodName = in.readUTF();
            Class<?>[] paramTypes = (Class<?>[]) in.readObject();
            Object[] args = (Object[]) in.readObject();
            
            // 反射调用 (对应 Dubbo Invoker.invoke)
            Method method = service.getClass().getMethod(methodName, paramTypes);
            Object result = method.invoke(service, args);
            
            // 返回结果
            out.writeObject(result);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // ============ 客户端 ============
    
    /**
     * 获取远程代理 (对应 Dubbo Cluster + Invoker)
     */
    @SuppressWarnings("unchecked")
    public static <T> T refer(Class<T> interfaceClass, String host, int port) {
        return (T) Proxy.newProxyInstance(
            interfaceClass.getClassLoader(),
            new Class[]{interfaceClass},
            (proxy, method, args) -> {
                // 建立连接
                Socket socket = new Socket(host, port);
                try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                     ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                    
                    // 发送请求
                    out.writeUTF(method.getName());
                    out.writeObject(method.getParameterTypes());
                    out.writeObject(args);
                    out.flush();
                    
                    // 接收结果
                    return in.readObject();
                } finally {
                    socket.close();
                }
            }
        );
    }
    
    // ============ 使用示例 ============
    
    public interface HelloService {
        String sayHello(String name);
    }
    
    public static class HelloServiceImpl implements HelloService {
        @Override
        public String sayHello(String name) {
            return "Hello, " + name + "!";
        }
    }
    
    // 服务端启动
    public static void startServer() throws Exception {
        export(new HelloServiceImpl(), 8080);
    }
    
    // 客户端调用
    public static void clientCall() {
        HelloService service = refer(HelloService.class, "localhost", 8080);
        System.out.println(service.sayHello("World"));
    }
}
