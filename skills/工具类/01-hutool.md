# Hutool 常用工具

## 问题
Java 开发中常用的工具方法有哪些？

## 解决方案

### 日期时间

```java
// 格式化
DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss")

// 解析
DateUtil.parse("2024-01-01")

// 计算
DateUtil.offsetDay(date, 1)  // 加1天
DateUtil.beginOfDay(date)    // 开始
DateUtil.endOfDay(date)      // 结束
```

### 字符串

```java
// 判断
StrUtil.isBlank(str)
StrUtil.isNotBlank(str)

// 转换
StrUtil.toUnderlineCase("UserName")  // user_name
StrUtil.toCamelCase("user_name")     // userName

// 模板
StrUtil.format("Hello, {}", name)
```

### JSON

```java
// 序列化
String json = JSONUtil.toJsonStr(obj);

// 反序列化
User user = JSONUtil.toBean(json, User.class);

// 创建
JSONObject obj = JSONUtil.createObj()
    .set("name", "张三")
    .set("age", 18);
```

### HTTP

```java
// GET
String result = HttpUtil.get("https://api.example.com/data");

// POST
String result = HttpUtil.post("url", HttpUtil.newParams()
    .set("username", "admin")
    .set("password", "123"));
```

## 最佳实践

- 优先使用 Hutool，减少重复造轮子
- 注意空指针: 用 StrUtil 而非 StringUtils
- 统一 JSON: 使用 JSONUtil
