# RPC 原理与实现

## 问题
如何实现分布式服务调用？

## RPC 核心流程

```
Client                    Server
   │                         │
   │───Request─────────────▶│
   │    interfaceName       │
   │    methodName           │
   │    parameters           │
   │                         │
   │◀───Response────────────│
   │    result/exception     │
```

## 简化实现

### 1. 定义接口

```java
public interface HelloService {
    String sayHello(String name);
}
```

### 2. 服务端 (反射调用)

```java
public class RpcExporter {
    public static void export(int port) throws Exception {
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            Socket socket = serverSocket.accept();
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            
            String interfaceName = input.readUTF();
            String methodName = input.readUTF();
            Class<?>[] paramTypes = (Class<?>[]) input.readObject();
            Object[] args = (Object[]) input.readObject();
            
            // 反射调用
            Class<?> service = Class.forName(interfaceName);
            Method method = service.getMethod(methodName, paramTypes);
            Object result = method.invoke(service.getDeclaredConstructor().newInstance(), args);
            
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.writeObject(result);
        }
    }
}
```

### 3. 客户端 (动态代理)

```java
public class RpcImporter<T> {
    public T importService(Class<T> interfaceClass, String host, int port) {
        return (T) Proxy.newProxyInstance(
            interfaceClass.getClassLoader(),
            new Class[]{interfaceClass},
            (proxy, method, args) -> {
                Socket socket = new Socket(host, port);
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                output.writeUTF(interfaceClass.getName());
                output.writeUTF(method.getName());
                output.writeObject(method.getParameterTypes());
                output.writeObject(args);
                
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                return input.readObject();
            }
        );
    }
}
```

## 最佳实践

- 使用 Netty 替代 BIO (阻塞IO)
- 序列化: Hessian / Protobuf / Kryo
- 注册中心: ZooKeeper / Nacos
- 负载均衡: Random / RoundRobin / LeastActive
