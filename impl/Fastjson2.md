# Fastjson2 项目分析

## 项目简介
Fastjson2 是阿里巴巴开发的高性能 JSON 解析和序列化库，是 Fastjson 的升级版。

## 核心类结构

### 1. JSON（主入口）
- **位置**: `com.alibaba.fastjson2.JSON`
- **职责**: 提供 JSON 解析和序列化的静态方法
- **方法**: `parseObject()`, `toJSONString()`, `parseArray()` 等

### 2. JSONReader（读取器）
- **位置**: `com.alibaba.fastjson2.JSONReader`
- **职责**: 低级 JSON 解析
- **功能**: 支持多种数据源（String, byte[], InputStream）

### 3. JSONWriter（写入器）
- **位置**: `com.alibaba.fastjson2.JSONWriter`
- **职责**: 低级 JSON 序列化

### 4. ObjectReader / ObjectWriter
- **职责**: 对象和 JSON 之间的转换
- **支持**: 注解配置、自定义解析

### 5. TypeUtils（类型工具）
- **职责**: 类型转换和反射操作

### 6. JSONObject / JSONArray
- **职责**: JSON 对象和数组的 Map/List 实现

## 设计模式

### 1. 工厂模式
- `JSONReaderFactory`, `JSONWriterFactory`
- `ObjectReaderProvider`, `ObjectWriterProvider`

### 2. 策略模式
- 多种 `JSONReader.Feature`, `JSONWriter.Feature`
- 多种 `Filter` 实现

### 3. 装饰器模式
- `Filter` 链
- 各种 `Feature` 装饰

### 4. 适配器模式
- 兼容 Fastjson 1.x API

### 5. 享元模式
- 字符串常量池
- 类型信息缓存

## 代码技巧

### 1. 读取优化
```java
// 基于位置的随机访问
public final int readInt() {
    // 高效的整数解析
}
```

### 2. 写入优化
```java
// 字符串转义优化
private static void writeEscapedString(JSONWriter writer, String value) {
    // 快速转义
}
```

### 3. 类型推断
```java
// 使用 TypeReference 保留泛型信息
TypeReference<Map<String, List<User>>> typeRef = new TypeReference<>() {};
```

### 4. 注解驱动
```java
@JSONField(name = "user_id")
private Long userId;
```

### 5.ASM 字节码生成
- 动态生成序列化/反序列化代码
- 编译期和运行期生成

## 代码规范

### 1. 包结构
- `fastjson2` - 核心模块
- `fastjson2-extension` - 扩展模块

### 2. 命名规范
- 清晰的方法命名
- 常量统一命名

### 3. 静态方法为主
- JSON 类提供大量静态方法
- 无状态设计

## 值得学习的地方

1. **高性能解析**: 理解 JSON 解析的优化技巧
2. **字节码生成**: ASM/ByteBuddy 使用
3. **类型系统**: 泛型类型信息保留
4. **Unicode 处理**: 特殊字符转义
5. **日期处理**: 多种日期格式支持
6. **循环引用**: 处理对象循环引用
7. **注解处理**: 自定义注解的使用
