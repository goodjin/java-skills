# jetty

## 项目简介
# Eclipse Jetty

Eclipse Jetty is a lightweight, highly scalable, Java-based web server and Servlet engine.
Jetty's goal is to support web protocols (HTTP/1, HTTP/2, HTTP/3, WebSocket, etc.) in a high volume low latency way that provides maximum performance while retaining the ease of use and compatibility with years of Servlet development.
Jetty is a modern fully asynchronous web server that has a long history as a component oriented technology, and can be easily embedded into applications while still offering a solid traditional distribution for webapp deployment.

- https://jetty.org
- https://projects.eclipse.org/projects/rt.jetty

## Webapp Example

```shell
$ mkdir jetty-base && cd jetty-base
$ java -jar $JETTY_HOME/start.jar --add-modules=http,ee11-deploy
$ cp ~/src/myproj/target/mywebapp.war webapps
$ java -jar $JETTY_HOME/start.jar 
```

## Multiple Versions Webapp Example

```shell
$ mkdir jetty-base && cd jetty-base
$ java -jar $JETTY_HOME/start.jar --add-modules=http,ee11-deploy,ee8-deploy
$ cp ~/src/myproj/target/mywebapp10.war webapps
$ cp ~/src/myproj/target/mywebapp8.war webapps
$ echo "environment: ee8" > webapps/mywebapp8.properties
$ java -jar $JETTY_HOME/start.jar 
```

## Embedded Jetty Example

```java
Server server = new Server(port);
server.setHandler(new MyHandler());
server.start();
```

## Embedded Servlet Example

```java

## 整体架构描述


## 核心模块划分
Jetty is a modern fully asynchronous web server that has a long history as a component oriented technology, and can be easily embedded into applications while still offering a solid traditional distribution for webapp deployment.

- https://jetty.org
- https://projects.eclipse.org/projects/rt.jetty

## Webapp Example

```shell
$ mkdir jetty-base && cd jetty-base
$ java -jar $JETTY_HOME/start.jar --add-modules=http,ee11-deploy
$ cp ~/src/myproj/target/mywebapp.war webapps
$ java -jar $JETTY_HOME/start.jar 
```

## Multiple Versions Webapp Example

```shell
$ mkdir jetty-base && cd jetty-base
$ java -jar $JETTY_HOME/start.jar --add-modules=http,ee11-deploy,ee8-deploy
$ cp ~/src/myproj/target/mywebapp10.war webapps
$ cp ~/src/myproj/target/mywebapp8.war webapps
$ echo "environment: ee8" > webapps/mywebapp8.properties
$ java -jar $JETTY_HOME/start.jar 
```

## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
