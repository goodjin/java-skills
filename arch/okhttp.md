# okhttp

## 项目简介
OkHttp
======

See the [project website][okhttp] for documentation and APIs.

HTTP is the way modern applications network. It’s how we exchange data & media. Doing HTTP
efficiently makes your stuff load faster and saves bandwidth.

OkHttp is an HTTP client that’s efficient by default:

 * HTTP/2 support allows all requests to the same host to share a socket.
 * Connection pooling reduces request latency (if HTTP/2 isn’t available).
 * Transparent GZIP shrinks download sizes.
 * Response caching avoids the network completely for repeat requests.

OkHttp perseveres when the network is troublesome: it will silently recover from common connection
problems. If your service has multiple IP addresses, OkHttp will attempt alternate addresses if the
first connect fails. This is necessary for IPv4+IPv6 and services hosted in redundant data
centers. OkHttp supports modern TLS features (TLS 1.3, ALPN, certificate pinning). It can be
configured to fall back for broad connectivity.

Using OkHttp is easy. Its request/response API is designed with fluent builders and immutability. It
supports both synchronous blocking calls and async calls with callbacks.

A well behaved user agent
-------------------------

OkHttp follows modern HTTP specifications such as

* HTTP Semantics - [RFC 9110](https://datatracker.ietf.org/doc/html/rfc9110)
* HTTP Caching- [RFC 9111](https://datatracker.ietf.org/doc/html/rfc9111)
* HTTP/1.1 - [RFC 9112](https://datatracker.ietf.org/doc/html/rfc9112)
* HTTP/2 - [RFC 9113](https://datatracker.ietf.org/doc/html/rfc9113)
* Websockets - [RFC 6455](https://datatracker.ietf.org/doc/html/rfc6455)
* SSE - [Server-sent events](https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events)

Where the spec is ambiguous, OkHttp follows modern user agents such as popular Browsers or common HTTP Libraries.

OkHttp is principled and avoids being overly configurable, especially when such configuration is
to workaround a buggy server, test invalid scenarios or that contradict the relevant RFC.

## 整体架构描述


## 核心模块划分
See the okcurl module for an example build.

```shell
$ ./gradlew okcurl:nativeImage
$ ./okcurl/build/graal/okcurl https://httpbin.org/get
```

Java Modules
------------

OkHttp (5.2+) implements Java 9 Modules.

With this in place Java builds should fail if apps attempt to use internal packages.

```
error: package okhttp3.internal.platform is not visible
    okhttp3.internal.platform.Platform.get();
                    ^
  (package okhttp3.internal.platform is declared in module okhttp3,
    which does not export it to module com.bigco.sdk)
```

The stable public API is based on the list of defined modules:

- okhttp3

## 技术选型
- 构建工具: 未知
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
