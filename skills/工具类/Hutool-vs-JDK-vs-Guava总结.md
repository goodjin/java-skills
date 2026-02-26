# Hutool vs JDK vs Guava 全面对比总结

## 概述

本文档对 Hutool、JDK 原生 API 和 Google Guava 三个工具库进行全面的对比分析，从功能完整性、API 设计、性能、使用场景等多个维度进行评估。

---

## 一、模块功能对比

### 1.1 日期时间处理

| 特性 | JDK | Hutool (DateUtil) | Guava |
|------|-----|-------------------|-------|
| **核心类** | `Date`, `Calendar`, `LocalDateTime` | `DateTime`, `DateUtil` | `DateTime`, `Instant` |
| **字符串解析** | `SimpleDateFormat` (线程不安全) | 自动识别多种格式 | `DateTimeFormatter` (线程安全) |
| **日期计算** | `Calendar` 方法繁琐 | `offsetDay()`, `betweenDay()` | `Years`, `Months`, `Days` |
| **格式化** | `SimpleDateFormat` | `format(date, pattern)` | `DateTimeFormatter` |
| **时区支持** | 支持 | 支持 | 支持 |
| **线程安全** | 否 | 是 | 是 |
| **中文格式** | 一般 | 优秀 | 一般 |
| **智能解析** | 无 | 自动格式检测 | 无 |

**代码对比**：

```java
// JDK - 获取当前日期时间字符串
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
String now = sdf.format(new Date());

// Hutool - 简洁直观
String now = DateUtil.now();

// Guava
String now = DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
```

### 1.2 字符串处理

| 特性 | JDK | Hutool (StrUtil) | Guava |
|------|-----|------------------|-------|
| **空值判断** | 手动判断 | `isBlank()`, `isEmpty()` | `Strings.isNullOrEmpty()` |
| **字符串填充** | 手动实现 | `fillBefore()`, `fillAfter()` | `Strings.padStart()` |
| **字符串截断** | 无 | `truncateUtf8()` | `Splitter.fixedLength()` |
| **字符串反转** | 手动 | `reverse()`, `reverseByCodePoint()` | 无 |
| **格式化** | `MessageFormat` | `{key}` 占位符 | `replace()` + `Map` |
| **Unicode** | 基础支持 | Emoji 正确处理 | 基础支持 |

### 1.3 集合处理

| 特性 | JDK | Hutool (CollUtil) | Guava |
|------|-----|-------------------|-------|
| **空集合判断** | 手动 | `isEmpty()` | `isEmpty()` |
| **集合工具** | `Collections` | `CollUtil` | `Lists`, `Maps`, `Sets` |
| **过滤** | Stream API | `filter()` | `Collections2.filter()` |
| **转换** | Stream API | `toList()` | `ImmutableList.copyOf()` |
| **分组** | Stream API | `groupBy()` | `Maps.groupBy()` |

### 1.4 JSON 处理

| 特性 | JDK | Hutool (JSONUtil) | Guava |
|------|-----|-------------------|-------|
| **解析** | `JSONObject` (Java EE) | 自动识别类型 | 需配合 Jackson |
| **创建** | 构造函数 | 链式构建 | 需配合 Jackson |
| **Bean 转换** | 手动反射 | 自动反射 | 需配合 Jackson |
| **Null 处理** | 抛异常 | 可配置忽略 | 需配合 Jackson |
| **XML 互转** | 无 | 支持 | 无 |

### 1.5 HTTP 请求

| 特性 | JDK | Hutool (HttpUtil) | Guava |
|------|-----|-------------------|-------|
| **API 风格** | `HttpURLConnection` | 链式构建 | 链式构建 |
| **同步请求** | 支持 | 支持 | 支持 |
| **异步请求** | 需手动 | 需配合线程池 | `executeAsync()` |
| **文件上传** | 手动拼接 | `multiPart()` | 支持 |
| **文件下载** | 手动流处理 | 一行代码 | 支持 |
| **连接池** | 无 | 无 | 无 |
| **HTTP/2** | 不支持 | 不支持 | 支持 |

---

## 二、API 设计对比

### 2.1 设计理念

| 库 | 设计理念 | 特点 |
|----|----------|------|
| **JDK** | 基础功能 | 稳定、向后兼容、但 API 较为底层 |
| **Hutool** | 简化开发 | 中文文档、链式调用、零学习成本 |
| **Guava** | 工程化实践 | 函数式风格、不可变集合、Preconditions |

### 2.2 代码风格

```java
// Hutool - 链式调用，简洁直观
String result = HttpUtil.get(url)
    .form(params)
    .execute()
    .body();

User user = JSONUtil.toBean(json, User.class);

DateTime tomorrow = DateUtil.offsetDay(date, 1);

// Guava - 函数式风格
List<String> filtered = Lists.newArrayList(
    Iterables.filter(list, Predicates.notNull())
);

ImmutableList<String> immutable = ImmutableList.copyOf(list);

// JDK - 传统风格
List<String> list = new ArrayList<>();
list.add("a");
```

### 2.3 空值安全

```java
// JDK - 容易 NPE
String name = user.getName();
int len = name.length(); // NPE!

// Hutool - 完善的空值处理
String name = StrUtil.blankToDefault(user.getName(), "");
int len = StrUtil.length(name); // 安全

// Guava - 使用 Optional
String name = Optional.ofNullable(user)
    .map(User::getName)
    .orElse("");
```

---

## 三、性能对比

### 3.1 性能特点

| 库 | 性能特点 | 适用场景 |
|----|----------|----------|
| **JDK** | 最稳定，无额外开销 | 所有场景 |
| **Hutool** | 中等性能，有一定反射开销 | 日常开发，简单场景 |
| **Guava** | 优化良好 | 大规模数据处理，高并发 |

### 3.2 性能优化建议

```java
// Hutool - 避免频繁创建对象
// 不推荐
for (int i = 0; i < 1000; i++) {
    DateUtil.parse(dateStr); // 每次创建解析器
}

// 推荐 - 复用
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
for (int i = 0; i < 1000; i++) {
    LocalDate.parse(dateStr, formatter); // 复用
}
```

---

## 四、依赖与体积

### 4.1 依赖对比

| 库 | 依赖情况 | 额外依赖 |
|----|----------|----------|
| **JDK** | 无 | 无 |
| **Hutool** | 零依赖或轻量依赖 | 可选 |
| **Guava** | 单一依赖 | 无 |

### 4.2 jar 包大小

| 库 | 核心模块大小 |
|----|--------------|
| **Hutool-core** | ~700KB (可按需引入) |
| **Guava** | ~2.5MB |
| **JDK** | 内置 |

### 4.3 模块化

```xml
<!-- Hutool 模块化引入 -->
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-core</artifactId>    <!-- 核心 -->
    <artifactId>hutool-json</artifactId>     <!-- JSON -->
    <artifactId>hutool-http</artifactId>      <!-- HTTP -->
    <artifactId>hutool-crypto</artifactId>   <!-- 加密 -->
    <artifactId>hutool-db</artifactId>       <!-- 数据库 -->
</dependency>

<!-- Guava 单一引入 -->
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>32.1.2-jre</version>
</dependency>
```

---

## 五、学习成本与文档

### 5.1 文档对比

| 库 | 文档语言 | 文档质量 | 示例丰富度 |
|----|----------|----------|------------|
| **JDK** | 英文 | 官方文档 | 中等 |
| **Hutool** | 中文 | 完善 (作者维护) | 丰富 |
| **Guava** | 英文 | 完善 | 丰富 |

### 5.2 学习曲线

```
JDK        ████████████░░░░░░░░░░░ 较陡 (需要了解很多类)
Hutool     ████░░░░░░░░░░░░░░░░░░░ 较平缓 (文档详细)
Guava      ████████░░░░░░░░░░░░░░░ 中等 (函数式风格)
```

---

## 六、使用建议

### 6.1 场景选择

| 场景 | 推荐选择 |
|------|----------|
| **企业级项目** | Hutool + Guava 组合 |
| **Android 开发** | Hutool (轻量) |
| **底层库开发** | JDK 原生 |
| **大数据处理** | Guava |
| **快速原型开发** | Hutool |
| **学习理解原理** | JDK 原生 |

### 6.2 组合策略

```java
// 推荐组合：JDK + Hutool
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.http.HttpUtil;

// 使用 JDK 基础类型和 API
List<String> list = new ArrayList<>();
Map<String, Integer> map = new HashMap<>();

// 使用 Hutool 处理边界情况
if (StrUtil.isNotBlank(name)) { ... }
DateTime dt = DateUtil.parse(dateStr);
```

### 6.3 注意事项

1. **Hutool**
   - 不适合高性能场景 (有反射开销)
   - 某些功能重复 (如集合工具)
   - 版本兼容性需注意

2. **Guava**
   - 体积较大
   - 学习曲线较陡
   - 部分 API 与 JDK 重叠

3. **JDK**
   - 需要自行处理边界情况
   - 代码量较大
   - 线程安全需自行保证

---

## 七、总结对比表

| 维度 | JDK | Hutool | Guava |
|------|-----|--------|-------|
| **API 简洁性** | ★★☆☆☆ | ★★★★★ | ★★★★☆ |
| **功能完整性** | ★★★☆☆ | ★★★★★ | ★★★★☆ |
| **性能** | ★★★★★ | ★★★☆☆ | ★★★★☆ |
| **文档** | ★★★☆☆ | ★★★★★ | ★★★★☆ |
| **体积** | ★★★★★ | ★★★★☆ | ★★☆☆☆ |
| **线程安全** | ★★☆☆☆ | ★★★★★ | ★★★★★ |
| **学习成本** | ★★★☆☆ | ★★★★★ | ★★★☆☆ |
| **维护活跃度** | ★★★★★ | ★★★★★ | ★★★★☆ |
| **中文支持** | ★★☆☆☆ | ★★★★★ | ★★☆☆☆ |
| **适用场景** | 底层/性能敏感 | 日常开发 | 工程化项目 |

---

## 八、快速入门代码

```java
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.http.HttpUtil;

/**
 * Hutool 快速入门
 */
public class HutoolDemo {
    public static void main(String[] args) {
        // 1. 日期时间 - DateUtil
        String now = DateUtil.now();           // "2024-01-15 10:30:45"
        String today = DateUtil.today();       // "2024-01-15"
        
        DateTime dt = DateUtil.parse("2024-01-15 10:30:45");
        DateTime tomorrow = DateUtil.offsetDay(dt, 1);
        
        long days = DateUtil.betweenDay(start, end, true);
        String between = DateUtil.formatBetween(start, end);
        
        // 2. 字符串 - StrUtil
        if (StrUtil.isBlank(str)) { /* 处理空白 */ }
        if (StrUtil.isEmpty(str)) { /* 处理空串 */ }
        
        String padded = StrUtil.fillBefore("123", '0', 6); // "000123"
        String truncated = StrUtil.truncateUtf8(str, 100); // 按字节截断
        
        // 3. 集合 - CollUtil
        if (CollUtil.isEmpty(list)) { /* 空集合 */ }
        
        List<String> filtered = CollUtil.filter(list, s -> s.startsWith("a"));
        Map<String, List<?>> grouped = CollUtil.groupBy(list, Object::toString);
        
        // 4. 数组 - ArrayUtil
        String[] arr = ArrayUtil.newArray(String.class, 10);
        boolean hasNull = ArrayUtil.hasNull(array);
        
        // 5. JSON - JSONUtil
        JSONObject obj = JSONUtil.parseObj("{\"name\":\"张三\"}");
        String json = JSONUtil.toJsonStr(user);
        User u = JSONUtil.toBean(json, User.class);
        
        // 6. HTTP - HttpUtil
        String html = HttpUtil.get("https://example.com");
        String result = HttpUtil.post(url, params);
        HttpUtil.downloadFile(url, "D:/file.pdf");
    }
}
```

---

## 结论

1. **日常开发首选 Hutool** - API 简洁、中文文档、零学习成本
2. **性能敏感场景使用 JDK 原生** - 无额外开销
3. **工程化项目结合 Guava** - 不可变集合、函数式编程
4. **推荐组合**: JDK + Hutool (简单项目) 或 JDK + Hutool + Guava (企业级项目)
