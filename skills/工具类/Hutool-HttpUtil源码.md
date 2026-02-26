# Hutool HttpUtil 源码分析

## 概述

`HttpUtil` 是 Hutool 工具库中的 HTTP 请求工具类，位于 `cn.hutool.http` 包下。它对 JDK 原生的 `HttpURLConnection` 进行了封装，提供了简洁易用的 HTTP 请求 API，同时支持文件上传下载、参数编码等功能。

## 核心设计

### 1. 架构设计

```
HttpUtil (工具入口类)
├── HttpRequest (请求构建器)
├── HttpResponse (响应封装)
├── HttpDownloader (下载器)
├── HttpGlobalConfig (全局配置)
└── CookieManager (Cookie 管理)
```

### 2. HttpRequest 请求构建

`HttpRequest` 是 Hutool HTTP 模块的核心类，采用链式构建模式：

```java
public class HttpRequest extends HttpBase<HttpRequest> implements Closeable {
    private String url;
    private Method method = Method.GET;
    private Map<String, Object> form;     // 表单参数
    private Map<String, Object> pathVariables; // Path 变量
    private Body body;                     // 请求体
    private boolean followRedirects;       // 是否跟随重定向
    private int timeout = -1;              // 超时时间
    // ... 更多配置
    
    // 链式调用
    public HttpRequest url(String url) { ... }
    public HttpRequest method(Method method) { ... }
    public HttpRequest header(String name, String value) { ... }
    public HttpRequest form(Map<String, Object> formMap) { ... }
    public HttpRequest body(String body) { ... }
    public HttpResponse execute() { ... }
}
```

### 3. 请求执行流程

```java
public HttpResponse execute() {
    // 1. 构建 URL (包含 query 参数)
    String url = buildUrl();
    
    // 2. 创建连接
    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    
    // 3. 设置请求头
    conn.setRequestMethod(method.name());
    conn.setConnectTimeout(timeout);
    conn.setReadTimeout(timeout);
    
    // 4. 设置请求体 (POST/PUT)
    if (body != null) {
        conn.setDoOutput(true);
        Writer writer = new OutputStreamWriter(conn.getOutputStream(), charset);
        writer.write(body);
    }
    
    // 5. 获取响应
    int status = conn.getResponseCode();
    Map<String, List<String>> headers = conn.getHeaderFields();
    InputStream in = (status >= 200 && status < 300) 
        ? conn.getInputStream() 
        : conn.getErrorStream();
    
    // 6. 封装响应
    return new HttpResponse(in, charset, status, headers);
}
```

### 4. 参数编码处理

```java
// 将 Map 参数转为 URL 查询字符串
public static String toParams(Map<String, ?> paramMap, Charset charset) {
    return UrlQuery.of(paramMap).build(charset);
}

// URL 编码
public static String encodeParams(String urlWithParams, Charset charset) {
    // 分离 URL 和参数部分
    int pathEndPos = urlWithParams.indexOf('?');
    String urlPart = ...; // URL 部分
    String paramPart = ...; // 参数部分
    
    // 标准化参数并编码
    paramPart = normalizeParams(paramPart, charset);
    
    return urlPart + "?" + paramPart;
}

// 参数标准化
private static String normalizeParams(String paramPart, Charset charset) {
    // 处理键值对
    for (int i = 0; i < len; i++) {
        if (c == '=') { // 键值对分界
            name = paramPart.substring(pos, i);
        } else if (c == '&') { // 参数对分界
            // 编码 name 和 value
           3986.QUERY_PARAM_NAME.encode(name builder.append(RFC, charset))
                   .append('=')
                   .append(RFC3986.QUERY_PARAM_VALUE.encode(value, charset));
        }
    }
}
```

### 5. 文件下载

```java
// 下载文件
public static long downloadFile(String url, File destFile) {
    return HttpDownloader.downloadFile(url, destFile, -1, null);
}

// 内部实现
public static long downloadFile(String url, File destFile, int timeout, StreamProgress progress) {
    HttpResponse response = HttpRequest.get(url).execute();
    
    // 获取 Content-Length
    long contentLength = response.headerLong("Content-Length");
    
    // 写入文件
    try (FileOutputStream fos = new FileOutputStream(destFile)) {
        InputStream in = response.bodyStream();
        byte[] buffer = new byte[8192];
        long total = 0;
        int len;
        while ((len = in.read(buffer)) != -1) {
            fos.write(buffer, 0, len);
            total += len;
            // 进度回调
            if (progress != null) {
                progress.progress(total, contentLength);
            }
        }
    }
    return total;
}
```

### 6. 字符集检测

```java
// 从 Content-Type 获取字符集
public static String getCharset(String contentType) {
    if (StrUtil.isBlank(contentType)) {
        return null;
    }
    // 使用正则提取 charset
    return ReUtil.get(CHARSET_PATTERN, contentType, 1);
}

// 从响应内容中检测字符集
public static String getString(byte[] contentBytes, Charset charset, 
        boolean isGetCharsetFromContent) {
    String content = new String(contentBytes, charset);
    
    if (isGetCharsetFromContent) {
        // 从 meta 标签获取编码
        String charsetInContent = ReUtil.get(META_CHARSET_PATTERN, content, 1);
        if (StrUtil.isNotBlank(charsetInContent)) {
            // 转换编码
            content = new String(contentBytes, Charset.forName(charsetInContent));
        }
    }
    return content;
}
```

## 常用 API

### GET 请求
```java
// 简单 GET
String html = HttpUtil.get("https://example.com");

// 带参数
String html = HttpUtil.get("https://example.com", params);

// 带超时
String html = HttpUtil.get("https://example.com", 5000);

// 带自定义字符集
String html = HttpUtil.get("https://example.com", CharsetUtil.CHARSET_UTF_8);
```

### POST 请求
```java
// 表单 POST
String result = HttpUtil.post("https://api.example.com", params);

// 字符串 body (JSON)
String result = HttpUtil.post("https://api.example.com", jsonString);

// 带超时
String result = HttpUtil.post("https://api.example.com", params, 5000);
```

### 请求构建器
```java
// 链式构建请求
String result = HttpRequest.post("https://api.example.com")
    .header("Authorization", "Bearer token")
    .contentType("application/json")
    .body(jsonBody)
    .execute()
    .body();
```

### 文件下载
```java
// 下载文件
long size = HttpUtil.downloadFile(url, "D:/download/file.pdf");

// 下载到指定目录
long size = HttpUtil.downloadFile(url, new File("D:/download/"));

// 带进度
HttpUtil.downloadFile(url, file, new StreamProgress() {
    @Override
    public void progress(long total, long progress) {
        System.out.println("进度: " + (progress * 100 / total) + "%");
    }
});

// 下载字符串
String content = HttpUtil.downloadString(url, CharsetUtil.CHARSET_UTF_8);
```

### 参数处理
```java
// Map 转 URL 参数
String params = HttpUtil.toParams(map);
// "key1=value1&key2=value2"

// URL 参数编码
String encoded = HttpUtil.encodeParams(url, StandardCharsets.UTF_8);

// URL 参数解码
Map<String, String> decoded = HttpUtil.decodeParamMap(params, StandardCharsets.UTF_8);
```

### 其他工具
```java
// 判断 HTTPS
boolean isHttps = HttpUtil.isHttps(url);

// 获取 MimeType
String mimeType = HttpUtil.getMimeType("file.pdf");

// 构建 Basic Auth
String auth = HttpUtil.buildBasicAuth("username", "password", Charset.UTF_8);
// "Basic dXNlcm5hbWU6cGFzc3dvcmQ="
```

## Hutool vs JDK vs Guava 对比

| 特性 | JDK | Hutool | Guava |
|------|-----|--------|-------|
| **请求方式** | `HttpURLConnection` | `HttpRequest` | `com.google.api.client.http` |
| **链式调用** | 否 | 是 | 是 |
| **同步请求** | 是 | 是 | 是 |
| **异步请求** | 需手动实现 | 需结合线程池 | `HttpRequest.executeAsync()` |
| **文件上传** | 手动拼接 | `multiPart()` | 支持 |
| **文件下载** | 手动流处理 | 简洁 API | 支持 |
| **重定向** | 默认不跟随 | 可配置 | 默认跟随 |
| **Cookie** | 手动管理 | 全局管理 | 自动管理 |
| **SSL** | 手动配置 | 兼容 | 支持 |

### 详细对比

**1. 发送 GET 请求**

```java
// JDK - 繁琐
URL url = new URL("https://api.example.com?id=1");
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("GET");
int code = conn.getResponseCode();
BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
String line;
StringBuilder sb = new StringBuilder();
while ((line = reader.readLine()) != null) {
    sb.append(line);
}

// Hutool - 简洁
String result = HttpUtil.get("https://api.example.com?id=1");

// Guava
HttpRequest request = HttpRequest.get("https://api.example.com?id=1");
HttpResponse response = request.execute();
String result = response.parseAsString();
```

**2. 发送 POST 请求**

```java
// JDK
HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
conn.setDoOutput(true);
conn.setRequestMethod("POST");
OutputStream os = conn.getOutputStream();
os.write("name=value".getBytes());
os.flush();

// Hutool
String result = HttpUtil.post(url, params);
// 或链式
String result = HttpRequest.post(url)
    .form(params)
    .execute()
    .body();
```

**3. 文件上传**

```java
// JDK - 复杂的多部分编码

// Hutool - 简洁支持
HttpRequest request = HttpRequest.post(url)
    .form("file", new File("test.pdf"))
    .form("name", "文档");
    
// Guava - 支持
HttpRequest request = HttpRequest.post(url)
    .addFormDataPart("file", "test.pdf", 
        new FileContent("application/pdf", new File("test.pdf")));
```

**4. 文件下载**

```java
// JDK
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
InputStream in = conn.getInputStream();
FileOutputStream out = new FileOutputStream("file");
byte[] buf = new byte[4096];
int len;
while ((len = in.read(buf)) != -1) {
    out.write(buf, 0, len);
}

// Hutool - 一行代码
HttpUtil.downloadFile(url, "D:/file.pdf");

// 带进度
HttpUtil.downloadFile(url, file, new StreamProgress() { ... });
```

**5. 特性对比**

| 特性 | Hutool HttpUtil | Apache HttpClient | OkHttp |
|------|-----------------|-------------------|--------|
| **体积** | 小 | 大 | 中 |
| **API 简洁** | ★★★★★ | ★★★☆☆ | ★★★★☆ |
| **连接池** | 需自行实现 | 支持 | 支持 |
| **HTTP/2** | 不支持 | 支持 | 支持 |
| **WebSocket** | 不支持 | 支持 | 支持 |
| **异步** | 需配合线程池 | 支持 | 支持 |

## 代码示例

```java
import cn.hutool.http.*;

// 1. 简单 GET 请求
String html = HttpUtil.get("https://api.github.com/users/octocat");

// 2. 带参数的 GET
Map<String, Object> params = new HashMap<>();
params.put("page", 1);
params.put("size", 10);
String result = HttpUtil.get("https://api.example.com/list", params);

// 3. POST 表单
Map<String, Object> formData = new HashMap<>();
formData.put("username", "admin");
formData.put("password", "123456");
String result = HttpUtil.post("https://login.example.com", formData);

// 4. POST JSON
String json = "{\"name\":\"张三\",\"age\":20}";
String result = HttpRequest.post("https://api.example.com/user")
    .contentType("application/json")
    .body(json)
    .execute()
    .body();

// 5. 链式构建复杂请求
String result = HttpRequest.post("https://api.example.com/user")
    .header("Authorization", "Bearer " + token)
    .header("Accept", "application/json")
    .contentType("application/json")
    .body(userJson)
    .timeout(10000) // 10秒超时
    .execute()
    .body();

// 6. 处理响应
HttpResponse response = HttpRequest.get("https://api.example.com/user/1").execute();
int status = response.getStatus();
String body = response.body();
Map<String, List<String>> headers = response.headers();

// 7. 文件下载
// 下载到文件
long size = HttpUtil.downloadFile(url, "D:/download/test.pdf");

// 下载到目录 (自动取文件名)
long size = HttpUtil.downloadFile(url, new File("D:/download/"));

// 带进度显示
HttpUtil.downloadFile(url, new File("D:/download/test.pdf"), 
    new StreamProgress() {
        public void progress(long total, long current) {
            System.out.printf("下载进度: %.2f%%\n", 
                (current * 100.0) / total);
        }
    });

// 8. 下载字符串内容
String html = HttpUtil.downloadString(url, CharsetUtil.CHARSET_UTF_8);

// 9. 参数编码
String params = HttpUtil.toParams(paramsMap); // a=1&b=2
String encoded = HttpUtil.encodeParams(url + "?" + params, StandardCharsets.UTF_8);

// 10. 处理 Cookie (全局)
HttpUtil.closeCookie(); // 关闭 Cookie
```

## 总结

**Hutool HttpUtil 优势**：
1. **API 简洁** - 链式调用，代码量少
2. **功能完整** - GET/POST/文件上传下载
3. **参数处理** - 自动编码/解码
4. **零依赖** - 不依赖第三方 HTTP 库
5. **中文优化** - 完善的中文编码支持

**局限性**：
- 不支持连接池
- 不支持 HTTP/2
- 不支持 WebSocket
- 异步需要自行实现

**适用场景**：
- 简单的 HTTP 调用
- 爬虫/数据抓取
- API 接口测试
- 文件上传下载
- 微服务内部调用 (简单场景)
