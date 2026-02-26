# Hutool 工具库分析

## 核心模块

| 模块 | 功能 |
|------|------|
| hutool-core | 核心 (DateUtil, StrUtil, JSONUtil) |
| hutool-crypto | 加密解密 |
| hutool-http | HTTP 请求 |
| hutool-cron | 定时任务 |
| hutool-db | 数据库 |

## 常用工具

### DateUtil 日期
```java
DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss")
DateUtil.parse("2024-01-01")
DateUtil.offsetDay(date, 1)
```

### StrUtil 字符串
```java
StrUtil.isBlank(str)
StrUtil.toUnderlineCase("UserName")  // user_name
StrUtil.format("Hello, {}", name)
```

### JSONUtil JSON
```java
JSONUtil.toJsonStr(obj)
JSONUtil.toBean(json, User.class)
JSONUtil.createObj().set("key", "value")
```

### HttpUtil HTTP
```java
HttpUtil.get(url)
HttpUtil.post(url, params)
```

## 对比 Guava

| 特性 | Hutool | Guava |
|------|--------|-------|
| 适用场景 | 国内项目 | 国际项目 |
| API 风格 | 简洁 | 完整 |
| 更新频率 | 高 | 低 |

## 最佳实践

```java
// 链式调用
JSONObject json = JSONUtil.createObj()
    .set("name", "张三")
    .set("age", 18)
    .set("tags", JSONUtil.createArray().add("Java").add("Python"));

// 日期区间
DateRange range = DateUtil.range(start, end, DateField.DAY);
for (DateTime date : range) {
    System.out.println(date);
}
```
