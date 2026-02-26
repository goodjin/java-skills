# Hutool JSONUtil 源码分析

## 概述

`JSONUtil` 是 Hutool 工具库中的 JSON 处理工具类，位于 `cn.hutool.json` 包下。它提供了 JSON 字符串解析、对象转换、格式化等功能，内部核心是 `JSONObject` 和 `JSONArray` 类。

## 核心设计

### 1. 核心类结构

```
JSON (接口)
├── JSONObject (继承 MapWrapper, 实现 JSON, JSONGetter)
└── JSONArray (继承 ArrayList, 实现 JSON, JSONGetter)
```

### 2. JSONObject 内部结构

```java
public class JSONObject extends MapWrapper<String, Object> implements JSON, JSONGetter<String> {
    private JSONConfig config;
    
    // 内部存储
    public JSONObject(int capacity, JSONConfig config) {
        super(InternalJSONUtil.createRawMap(capacity, config));
        this.config = config;
    }
}
```

**设计亮点**：
- 继承 `MapWrapper` 实现 `Map` 接口
- 支持配置化（是否忽略 null、大小写敏感等）
- 支持有序/无序

### 3. JSONConfig 配置

```java
public class JSONConfig {
    private boolean ignoreNullValue = false;   // 忽略 null 值
    private boolean ignoreError = false;       // 忽略转换错误
    private boolean ignoreCase = false;        // KEY 忽略大小写
    private String dateFormat;                 // 日期格式
    // ... 更多配置
}
```

## 实现原理

### 1. 解析流程 (parse)

```java
public static JSON parse(Object obj, JSONConfig config) {
    if (null == obj) {
        return null;
    }
    
    JSON json;
    if (obj instanceof JSON) {
        // 已经是 JSON 对象，直接返回
        json = (JSON) obj;
    } else if (obj instanceof CharSequence) {
        // 字符串类型，自动识别是对象还是数组
        final String jsonStr = StrUtil.trim((CharSequence) obj);
        json = isTypeJSONArray(jsonStr) ? parseArray(jsonStr, config) : parseObj(jsonStr, config);
    } else if (obj instanceof MapWrapper) {
        // 修正 MapWrapper 被误判为 JSONArray
        json = parseObj(obj, config);
    } else if (obj instanceof Iterable || obj instanceof Iterator || ArrayUtil.isArray(obj)) {
        // 集合或数组 -> JSONArray
        json = parseArray(obj, config);
    } else {
        // 普通对象 -> JSONObject
        json = parseObj(obj, config);
    }
    
    return json;
}
```

### 2. 对象转 JSON (toJsonStr)

```java
public static String toJsonStr(Object obj, JSONConfig jsonConfig) {
    if (null == obj) {
        return null;
    }
    
    if (obj instanceof CharSequence) {
        return StrUtil.str((CharSequence) obj);
    } else if (obj instanceof Boolean || obj instanceof Number) {
        return obj.toString();
    }
    
    // 先 parse 再 toJSONString
    return toJsonStr(parse(obj, jsonConfig));
}
```

### 3. JSON 转 Bean (toBean)

```java
public static <T> T toBean(String jsonString, Class<T> beanClass) {
    return toBean(parseObj(jsonString), beanClass);
}

public static <T> T toBean(JSONObject json, Class<T> beanClass) {
    return null == json ? null : json.toBean(beanClass);
}

// JSONObject 内部使用反射
public <T> T toBean(Class<T> beanClass) {
    T bean = ReflectUtil.newInstance(beanClass);
    // 遍历 JSON 字段，调用 setter
    for (Map.Entry<String, Object> entry : this.entrySet()) {
        // 找到对应的 setter 方法并调用
        Method setter = ReflectUtil.getMethodByName(beanClass, "set" + StrUtil.upperFirst(entry.getKey()));
        if (setter != null) {
            ReflectUtil.invoke(bean, setter, entry.getValue());
        }
    }
    return bean;
}
```

### 4. 类型包装 (wrap)

```java
public static Object wrap(Object object, JSONConfig jsonConfig) {
    if (object == null) {
        return jsonConfig.isIgnoreNullValue() ? null : JSONNull.NULL;
    }
    
    // 已经是 JSON 类型或基础类型
    if (object instanceof JSON || object instanceof CharSequence || 
        object instanceof Number || ObjectUtil.isBasicType(object)) {
        return object;
    }
    
    // 集合或数组
    if (object instanceof Iterable || ArrayUtil.isArray(object)) {
        return new JSONArray(object, jsonConfig);
    }
    
    // Map
    if (object instanceof Map || object instanceof Map.Entry) {
        return new JSONObject(object, jsonConfig);
    }
    
    // 日期类型原样保存
    if (object instanceof Date || object instanceof Calendar || 
        object instanceof TemporalAccessor) {
        return object;
    }
    
    // 枚举转字符串
    if (object instanceof Enum) {
        return object.toString();
    }
    
    // Class 类型保存类名
    if (object instanceof Class<?>) {
        return ((Class<?>) object).getName();
    }
    
    // 默认当作 JSONObject 处理
    return new JSONObject(object, jsonConfig);
}
```

### 5. JSON 序列化

```java
// 内部使用 JSONWriter
public String toJSONString(int indentFactor) {
    JSONWriter writer = JSONWriter.of(indentFactor);
    this.write(writer);
    return writer.toString();
}

public void write(Writer writer) {
    writer.write('{');
    // 遍历 key-value
    for (Map.Entry<String, Object> entry : entrySet()) {
        writer.write('"');
        writer.write(escape(entry.getKey()));
        writer.write('":');
        // 递归写入 value
        writer.write(JSONUtil.toJsonStr(entry.getValue()));
    }
    writer.write('}');
}
```

### 6. XML 转 JSON

```java
public static JSONObject xmlToJson(String xml) {
    return XML.toJSONObject(xml);
}
```

## 常用 API

### 创建 JSON 对象
```java
// 创建空 JSONObject
JSONObject obj = JSONUtil.createObj();
JSONObject obj = JSONUtil.createObj(config);

// 创建空 JSONArray
JSONArray arr = JSONUtil.createArray();
JSONArray arr = JSONUtil.createArray(config);
```

### 解析 JSON
```java
// 从字符串解析
JSONObject obj = JSONUtil.parseObj("{\"name\":\"张三\",\"age\":20}");
JSONArray arr = JSONUtil.parseArray("[1,2,3]");

// 从对象解析 (Bean, Map)
JSONObject obj = JSONUtil.parseObj(user);
JSONArray arr = JSONUtil.parseArray(list);

// 自动识别类型
JSON json = JSONUtil.parse("{\"name\":\"张三\"}"); // JSONObject
JSON json = JSONUtil.parse("[1,2,3]");            // JSONArray
```

### 转换为字符串
```java
// 转换为 JSON 字符串
String jsonStr = JSONUtil.toJsonStr(obj);
String prettyJson = JSONUtil.toJsonPrettyStr(obj); // 格式化
String jsonStr = JSONUtil.toJsonStr(obj, 2);        // 指定缩进

// 转换为 XML
String xml = JSONUtil.toXmlStr(json);
```

### 转换为 Bean
```java
// 转换为 Bean
User user = JSONUtil.toBean(jsonStr, User.class);

// 转换为 List
List<User> list = JSONUtil.toList(jsonArray, User.class);

// 复杂类型转换
Type type = new TypeReference<Map<String, List<User>>>(){}.getType();
Map<String, List<User>> map = JSONUtil.toBean(jsonStr, type, false);
```

### JSONPath 操作
```java
// 获取嵌套对象
Object value = JSONUtil.getByPath(json, "person.name");
Object value = JSONUtil.getByPath(json, "items[0].price");

// 设置值
JSONUtil.putByPath(json, "person.name", "李四");
```

### JSON 验证
```java
// 判断是否为 JSON
boolean isJson = JSONUtil.isTypeJSON(str);
boolean isObj = JSONUtil.isTypeJSONObject(str);
boolean isArr = JSONUtil.isTypeJSONArray(str);

// 判断是否为 null
boolean isNull = JSONUtil.isNull(jsonNullObj);
```

### 格式化
```java
// 格式化 JSON 字符串 (不严格检查)
String formatted = JSONUtil.formatJsonStr(jsonStr);
```

## Hutool vs JDK vs Guava 对比

| 特性 | JDK | Hutool | Guava |
|------|-----|--------|-------|
| **解析** | `JSONObject` (Java EE) | 自动识别类型 | 无 (使用 Jackson) |
| **创建** | `new JSONObject()` | `JSONUtil.parseObj()` | 无 |
| **Bean 转换** | 手动映射 | 自动反射 | 无 |
| **日期处理** | 默认时间戳 | 自定义格式 | 无 |
| **Null 处理** | 抛出异常 | 可配置忽略 | 无 |
| **XML 互转** | 无 | 支持 | 无 |

### 详细对比

**1. 解析 JSON**

```java
// JDK (Java EE)
JSONObject obj = new JSONObject(jsonString);

// Hutool
JSONObject obj = JSONUtil.parseObj(jsonString);
JSONObject obj = JSONUtil.parseObj(map);

// Guava (需配合 Jackson)
ObjectMapper mapper = new ObjectMapper();
JsonNode node = mapper.readTree(jsonString);
```

**2. 对象转 JSON**

```java
// JDK
String json = new JSONObject(user).toString();

// Hutool
String json = JSONUtil.toJsonStr(user);
String pretty = JSONUtil.toJsonPrettyStr(user); // 格式化

// Guava (需配合 Jackson)
String json = mapper.writeValueAsString(user);
```

**3. JSON 转 Bean**

```java
// JDK
User user = new JSONObject(jsonString).getObject("user", User.class);

// Hutool
User user = JSONUtil.toBean(jsonString, User.class);

// Guava (需配合 Jackson)
User user = mapper.readValue(jsonString, User.class);
```

**4. 特性对比**

| 特性 | Hutool JSONUtil | FastJSON | Jackson |
|------|-----------------|----------|---------|
| **体积** | 小 (~500KB) | 小 (~600KB) | 大 (~250KB) |
| **性能** | 中等 | 最快 | 快 |
| **功能** | 完整 | 完整 | 完整 |
| **Android** | 友好 | 友好 | 友好 |
| **API 简洁** | ★★★★★ | ★★★★☆ | ★★★☆☆ |

## 代码示例

```java
import cn.hutool.json.*;

// 1. 创建 JSON 对象
JSONObject obj = new JSONObject();
obj.set("name", "张三");
obj.set("age", 20);

// 或者使用工具类
JSONObject obj2 = JSONUtil.createObj()
    .set("name", "张三")
    .set("age", 20);

// 2. 解析 JSON
String jsonStr = "{\"name\":\"张三\",\"age\":20,\"city\":\"北京\"}";
JSONObject obj = JSONUtil.parseObj(jsonStr);

// 3. 获取值
String name = obj.getStr("name");
int age = obj.getInt("age");
JSONObject deep = obj.getJSONObject("address");
JSONArray list = obj.getJSONArray("hobbies");

// 4. 转换为 Bean
User user = JSONUtil.toBean(jsonStr, User.class);

// 5. 格式化输出
String pretty = JSONUtil.toJsonPrettyStr(obj);
System.out.println(pretty);

/*
{
    "name": "张三",
    "age": 20,
    "city": "北京"
}
*/

// 6. 处理日期
JSONConfig config = JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss");
String json = JSONUtil.toJsonStr(user, config);

// 7. 处理 null 值
JSONConfig ignoreNull = JSONConfig.create().setIgnoreNullValue(true);
JSONObject nullObj = JSONUtil.parseObj(map, ignoreNull);

// 8. JSONPath 访问
JSONObject data = JSONUtil.parseObj(jsonStr);
String city = JSONUtil.getByPath(data, "address.city").toString();
int price = (int) JSONUtil.getByPath(data, "items[0].price");

// 9. JSONArray 操作
JSONArray arr = JSONUtil.parseArray("[1,2,3,4,5]");
arr.add(6);
List<Integer> list = arr.toList(Integer.class);

// 10. XML 转 JSON
String xml = "<root><name>张三</name></root>";
JSONObject xmlJson = JSONUtil.xmlToJson(xml);

// 11. 自定义序列化
JSONUtil.putSerializer(MyClass.class, new MySerializer());
```

## 总结

**Hutool JSONUtil 优势**：
1. **API 简洁** - 链式调用，代码优雅
2. **功能完整** - 解析、转换、格式化一手抓
3. **配置灵活** - JSONConfig 可定制化处理
4. **类型安全** - 泛型支持类型推断
5. **无依赖** - 不依赖第三方 JSON 库

**注意事项**：
- 性能不如 FastJSON，对性能敏感场景考虑其他方案
- Bean 转换使用反射，有一定性能开销
- 大型 JSON 建议使用流式处理

**适用场景**：
- 前后端数据交互
- 配置文件读取
- HTTP API 响应处理
- 简单 JSON 操作
