# Hutool StrUtil 源码分析

## 概述

`StrUtil` 是 Hutool 工具库中的字符串处理工具类，位于 `cn.hutool.core.util` 包下。它继承自 `CharSequenceUtil`，提供了丰富的字符串操作方法，包括空值判断、字符串填充、截断、格式化等功能。

## 核心设计

### 1. 继承结构

```java
public class StrUtil extends CharSequenceUtil implements StrPool
```

- 继承自 `CharSequenceUtil`，复用字符序列相关工具方法
- 实现 `StrPool` 接口，提供字符串常量

### 2. 空值判断

`StrUtil` 提供了精细的空值判断方法：

```java
// 判断是否为空白 (null、空串、空格、制表符、换行符等)
public static boolean isBlankIfStr(Object obj) {
    if (null == obj) {
        return true;
    } else if (obj instanceof CharSequence) {
        return isBlank((CharSequence) obj);
    }
    return false;
}

// 判断是否为空串 (只判断 null 和 "")
public static boolean isEmptyIfStr(Object obj) {
    if (null == obj) {
        return true;
    } else if (obj instanceof CharSequence) {
        return 0 == ((CharSequence) obj).length();
    }
    return false;
}
```

**设计亮点**：
- `isBlankIfStr` vs `isEmptyIfStr`：前者会检查空白字符，后者只检查长度
- 参数是 `Object`，可以安全处理任意对象

### 3. 字符串填充

```java
// 填充字符串到指定长度
public static String fillBefore(String str, char filledChar, int len) {
    return fill(str, filledChar, len, true);  // 填充在前
}

public static String fillAfter(String str, char filledChar, int len) {
    return fill(str, filledChar, len, false); // 填充在后
}

private static String fill(String str, char filledChar, int len, boolean isPre) {
    if (null == str) {
        str = "";
    }
    final int strLen = str.length();
    if (strLen > len) {
        return str;  // 超过长度直接返回
    }
    
    String filledStr = StrUtil.repeat(filledChar, len - strLen);
    return isPre ? filledStr.concat(str) : str.concat(filledStr);
}
```

### 4. 字符串截断

```java
// 按 UTF-8 字节长度截断 (用于数据库 VARCHAR)
public static String truncateUtf8(String str, int maxBytes) {
    Charset charset = StandardCharsets.UTF_8;
    return truncateByByteLength(str, charset, maxBytes, 4, true);
}

// 按指定编码字节长度截断
public static String truncateByByteLength(String str, Charset charset, 
        int maxBytesLength, int factor, boolean appendDots) {
    // 使用 CharBuffer 避免截断到半个字符
    final ByteBuffer bb = ByteBuffer.wrap(sba, 0, limitBytes);
    final CharBuffer cb = CharBuffer.allocate(limitBytes);
    final CharsetDecoder decoder = charset.newDecoder();
    decoder.onMalformedInput(CodingErrorAction.IGNORE);
    decoder.decode(bb, cb, true);
    decoder.flush(cb);
}
```

**技术亮点**：
- 使用 `CharsetDecoder` 确保不截断到多字节字符的中间位置
- 可选添加省略号 `...`

### 5. 字符串反转

```java
// 常规反转
public static String reverse(final String str) {
    if (isBlank(str)) {
        return str;
    }
    return new String(ArrayUtil.reverse(str.toCharArray()));
}

// 按 CodePoint 反转 (支持 Emoji 等 Unicode 字符)
public static String reverseByCodePoint(String str) {
    if (null == str) {
        return null;
    }
    
    StringBuilder result = new StringBuilder();
    for (int i = str.length(); i > 0; ) {
        int codePoint = str.codePointBefore(i);
        i -= Character.charCount(codePoint);
        result.appendCodePoint(codePoint);
    }
    return result.toString();
}
```

### 6. 字符串格式化

```java
// 使用 {key} 占位符格式化
public static String format(CharSequence template, Map<?, ?> map) {
    return format(template, map, true);
}

public static String format(CharSequence template, Map<?, ?> map, boolean ignoreNull) {
    return StrFormatter.format(template, map, ignoreNull);
}

// 示例
// template: "{name} is {age} years old"
// map: {"name": "Tom", "age": 20}
// result: "Tom is 20 years old"
```

### 7. 字符串相似度计算

```java
// 计算两个字符串的相似度
public static double similar(String str1, String str2) {
    return TextSimilarity.similar(str1, str2);
}

// 计算相似度百分比
public static String similar(String str1, String str2, int scale) {
    return TextSimilarity.similar(str1, str2, scale);
}
```

### 8. 字符串构建

```java
// 创建 StringBuilder
public static StringBuilder builder() {
    return new StringBuilder();
}

public static StringBuilder builder(int capacity) {
    return new StringBuilder(capacity);
}

// 创建 StrBuilder (Hutool 自定义)
public static StrBuilder strBuilder() {
    return StrBuilder.create();
}
```

## 常用 API

### 空值判断
```java
// 判断空白 (null, "", 空格, \t, \n 等)
boolean isBlank = StrUtil.isBlank(str);

// 判断空串 (null, "")
boolean isEmpty = StrUtil.isEmpty(str);

// 对象空判断
boolean isBlankObj = StrUtil.isBlankIfStr(obj);
boolean isEmptyObj = StrUtil.isEmptyIfStr(obj);
```

### 字符串操作
```java
// 去除首尾空格
String[] trimmed = StrUtil.trim(strArray);

// 字符串填充
String padded = StrUtil.fillBefore(str, '0', 10); // 左侧填充
String padded = StrUtil.fillAfter(str, ' ', 20);   // 右侧填充

// 字符串截断
String truncated = StrUtil.truncateUtf8(str, 100); // 按字节截断

// 字符串反转
String reversed = StrUtil.reverse(str);
String reversed = StrUtil.reverseByCodePoint(str); // 支持 Emoji
```

### 字符串转换
```java
// Object 转字符串
String str = StrUtil.str(obj);           // UTF-8
String str = StrUtil.str(obj, charset);   // 指定编码

// byte[] 转字符串
String str = StrUtil.str(bytes, "UTF-8");

// ByteBuffer 转字符串
String str = StrUtil.str(byteBuffer, charset);
```

### 字符串格式化
```java
// 占位符格式化
String result = StrUtil.format("Hello {name}", Map.of("name", "World"));

// 生成 UUID
String uuid = StrUtil.uuid();
```

### 字符串相似度
```java
// 相似度计算
double similarity = StrUtil.similar("hello", "hallo");
String percent = StrUtil.similar("hello", "hallo", 2); // "50.00%"
```

## Hutool vs JDK vs Guava 对比

| 特性 | JDK | Hutool | Guava |
|------|-----|--------|-------|
| **空判断** | 手动判断 | `isBlank()`, `isEmpty()` | `Strings.isNullOrEmpty()` |
| **字符串填充** | 需要手动实现 | `fillBefore()`, `fillAfter()` | `Strings.padStart()` |
| **字符串截断** | 无 | `truncateUtf8()` | `Splitter.on("").trimResults()` |
| **字符串反转** | 手动实现 | `reverse()`, `reverseByCodePoint()` | 无直接支持 |
| **格式化** | `MessageFormat` | `{key}` 占位符 | `replace()` + `Map` |
| **工具链** | 无 | `CharSequenceUtil` | `CharMatcher`, `CaseFormat` |
| **Null 安全** | 否 | 是 | 部分是 |

### 详细对比

**1. 空值判断**

```java
// JDK
if (str == null || str.trim().isEmpty()) { }

// Hutool
if (StrUtil.isBlank(str)) { }

// Guava
if (Strings.isNullOrEmpty(str)) { }
```

**2. 字符串填充**

```java
// JDK
String.format("%10s", str);

// Hutool
StrUtil.fillBefore(str, ' ', 10);

// Guava
Strings.padStart(str, 10, ' ');
```

**3. 字符串截断**

```java
// JDK - 无直接支持

// Hutool - 按字节截断，适合数据库 VARCHAR
StrUtil.truncateUtf8(str, 100);

// Guava - 使用 Splitter
Splitter.fixedLength(10).splitToList(str);
```

## 代码示例

```java
import cn.hutool.core.util.StrUtil;

// 1. 空值判断
String str = null;
StrUtil.isBlank(str);      // true
StrUtil.isEmpty(str);      // true
StrUtil.isBlankIfStr(obj); // 安全判断任意对象

// 2. 字符串截断 (数据库 VARCHAR)
String longStr = "这是一段很长的文本...";
String truncated = StrUtil.truncateUtf8(longStr, 50); // 按字节截断

// 3. 字符串填充
String orderNo = StrUtil.fillBefore("123", '0', 6); // "000123"
String phone = StrUtil.fillAfter("1380000", '0', 11); // "13800000000"

// 4. 字符串反转
String reversed = StrUtil.reverse("abc");       // "cba"
String reversedEmoji = StrUtil.reverseByCodePoint("👨‍👩‍👧‍👦"); // 正确处理家庭表情

// 5. 格式化
Map<String, Object> params = new HashMap<>();
params.put("name", "张三");
params.put("age", 25);
String result = StrUtil.format("{name}今年{age}岁", params); // "张三今年25岁"

// 6. 相似度
double sim = StrUtil.similar("hello", "hallo"); // 0.8

// 7. 字符串构建
StringBuilder sb = StrUtil.builder();
sb.append("Hello").append(" World");

StrBuilder sb2 = StrUtil.strBuilder(); // 可变字符串构建器
sb2.append("Hello").append(" World");

// 8. UUID 生成
String uuid = StrUtil.uuid();

// 9. Reader/Writer
StringReader reader = StrUtil.getReader("text");
StringWriter writer = StrUtil.getWriter();
```

## 总结

**Hutool StrUtil 优势**：
1. **API 丰富** - 200+ 方法覆盖各种字符串操作
2. **Null 安全** - 大部分方法处理了 NullPointerException
3. **Unicode 支持** - `reverseByCodePoint` 正确处理 Emoji
4. **数据库友好** - `truncateUtf8` 解决 VARCHAR 截断问题
5. **链式调用** - 支持构建器模式

**适用场景**：
- Web 表单数据处理
- 数据库字符串字段操作
- 日志文本处理
- 用户输入验证
- 国际化字符串处理
