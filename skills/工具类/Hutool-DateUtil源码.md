# Hutool DateUtil 源码分析

## 概述

`DateUtil` 是 Hutool 工具库中的日期时间处理工具类，位于 `cn.hutool.core.date` 包下。它对 JDK 原生的 `Date`、`Calendar` 和 Java 8 的 `java.time` API 进行了封装，提供了更加便捷和强大的日期时间处理能力。

## 核心设计

### 1. 继承结构

```java
public class DateUtil extends CalendarUtil
```

- 继承自 `CalendarUtil`，复用日历相关的工具方法
- 核心数据结构是 `DateTime`（继承自 `Date`）

### 2. 核心类 DateTime

`DateTime` 是 Hutool 日期处理的核心类，继承自 `java.util.Date`：

```java
public class DateTime extends Date {
    private boolean mutable = true;           // 是否可变对象
    private Week firstDayOfWeek = Week.MONDAY; // 一周第一天
    private TimeZone timeZone;               // 时区
    private int minimalDaysInFirstWeek;      // 第一周最少天数
}
```

**设计亮点**：
- 继承 `Date` 保持与 JDK API 的兼容性
- 扩展了时区、可变性等企业级特性
- 重写 `toString()` 返回 `yyyy-MM-dd HH:mm:ss` 格式

## 实现原理

### 1. 日期解析 (parse)

`DateUtil` 的解析功能非常强大，支持多种日期格式自动识别：

```java
public static DateTime parse(CharSequence dateCharSequence) {
    // 1. 纯数字形式 (时间戳)
    if (NumberUtil.isNumber(dateStr)) {
        if (length == 13) return date(NumberUtil.parseLong(dateStr));
    }
    
    // 2. 时间格式 HH:mm:ss
    if (ReUtil.isMatch(PatternPool.TIME, dateStr)) {
        return parseTimeToday(dateStr);
    }
    
    // 3. JDK 默认 toString 格式
    if (StrUtil.containsAnyIgnoreCase(dateStr, wtb)) {
        return parseRFC2822(dateStr);
    }
    
    // 4. ISO8601 格式
    if (StrUtil.contains(dateStr, 'T')) {
        return parseISO8601(dateStr);
    }
    
    // 5. 标准日期格式
    dateStr = normalize(dateStr);
    // 解析 yyyy-MM-dd, yyyy-MM-dd HH:mm 等
}
```

**关键特性**：
- `normalize()` 方法将各种中文格式标准化
- 自动检测格式并选择合适的解析器
- 支持 ISO8601、RFC2822 等国际标准

### 2. 日期格式化 (format)

```java
public static String format(Date date, String format) {
    // 1. 检查自定义格式
    if (GlobalCustomFormat.isCustomFormat(format)) {
        return GlobalCustomFormat.format(date, format);
    }
    
    // 2. 获取时区
    TimeZone timeZone = null;
    if (date instanceof DateTime) {
        timeZone = ((DateTime) date).getTimeZone();
    }
    
    // 3. 使用 FastDateFormat 格式化
    return format(date, FastDateFormat.getInstance(format, timeZone));
}
```

**性能优化**：使用 `FastDateFormat`（线程安全的日期格式化器）

### 3. 日期偏移 (offset)

```java
public static DateTime offset(Date date, DateField dateField, int offset) {
    return dateNew(date).offset(dateField, offset);
}

// 使用示例
DateTime nextDay = DateUtil.offsetDay(new Date(), 1);
DateTime lastMonth = DateUtil.offsetMonth(new Date(), -1);
```

### 4. 日期计算 (between)

```java
public static long between(Date beginDate, Date endDate, DateUnit unit, boolean isAbs) {
    return new DateBetween(beginDate, endDate, isAbs).between(unit);
}

// 使用示例
long days = DateUtil.between(start, end, DateUnit.DAY);
String format = DateUtil.formatBetween(start, end); // "XX天XX小时XX分XX秒"
```

## 常用 API

### 获取日期组件
```java
int year = DateUtil.year(date);           // 年
int month = DateUtil.month(date);         // 月 (0-11)
int day = DateUtil.dayOfMonth(date);      // 日
int hour = DateUtil.hour(date, true);     // 小时
Week week = DateUtil.dayOfWeekEnum(date); // 星期枚举
int quarter = DateUtil.quarter(date);     // 季度 (1-4)
```

### 日期解析
```java
// 自动识别格式
DateTime dt = DateUtil.parse("2024-01-15");
DateTime dt = DateUtil.parse("2024-01-15 10:30:00");
DateTime dt = DateUtil.parse("2024/01/15");

// 指定格式解析
DateTime dt = DateUtil.parse("2024-01-15", "yyyy-MM-dd");

// ISO8601 解析
DateTime dt = DateUtil.parseISO8601("2024-01-15T10:30:00Z");
```

### 日期格式化
```java
String str = DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
String now = DateUtil.now();      // "2024-01-15 10:30:00"
String today = DateUtil.today();  // "2024-01-15"
```

### 日期偏移
```java
DateTime tomorrow = DateUtil.offsetDay(date, 1);
DateTime nextWeek = DateUtil.offsetWeek(date, 1);
DateTime nextMonth = DateUtil.offsetMonth(date, 1);
```

### 日期区间
```java
// 获取开始/结束时间
DateTime beginOfDay = DateUtil.beginOfDay(date);
DateTime endOfDay = DateUtil.endOfDay(date);

// 昨天/明天
DateTime yesterday = DateUtil.yesterday();
DateTime tomorrow = DateUtil.tomorrow();

// 日期区间遍历
DateUtil.range(start, end, DateField.DAY).forEach(System.out::println);
```

### 日期计算
```java
// 相差天数
long days = DateUtil.betweenDay(start, end, true);

// 格式化间隔
String format = DateUtil.formatBetween(start, end); // "1天2小时30分"

// 是否在区间内
boolean isIn = DateUtil.isIn(date, begin, end);
```

### 计时器
```java
StopWatch stopWatch = DateUtil.createStopWatch();
stopWatch.start("任务1");
Thread.sleep(1000);
stopWatch.stop();
System.out.println(stopWatch.prettyPrint());
```

## Hutool vs JDK vs Guava 对比

| 特性 | JDK | Hutool | Guava |
|------|-----|--------|-------|
| **时间戳获取** | `System.currentTimeMillis()` | `DateUtil.current()` | `Instant.now().toEpochMilli()` |
| **字符串解析** | `SimpleDateFormat` (线程不安全) | `DateUtil.parse()` 自动识别 | `DateTimeFormatter` (线程安全) |
| **日期计算** | `Calendar` API 繁琐 | `offsetDay()`, `betweenDay()` 简洁 | `Years`, `Months`, `Days` 不可变对象 |
| **格式化** | `SimpleDateFormat` | `format(date, "yyyy-MM-dd")` | `DateTimeFormatter.ofPattern()` |
| **时间戳** | `java.util.Date` (可变) | `DateTime` 扩展 | `Instant` (不可变) |
| **线程安全** | 否 | 是 | 是 |
| **中文支持** | 一般 | 优秀 | 一般 |

## 代码示例

```java
import cn.hutool.core.date.*;

// 1. 获取当前时间
DateTime now = DateUtil.now();
System.out.println(now); // 2024-01-15 10:30:45

// 2. 字符串解析 (自动识别格式)
DateTime dt1 = DateUtil.parse("2024-01-15");
DateTime dt2 = DateUtil.parse("2024/01/15 10:30:45");
DateTime dt3 = DateUtil.parse("2024-01-15T10:30:00Z"); // ISO8601

// 3. 格式化
String format1 = DateUtil.format(new Date(), "yyyy年MM月dd日");
String format2 = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");

// 4. 日期偏移
DateTime tomorrow = DateUtil.offsetDay(new Date(), 1);
DateTime lastMonth = DateUtil.offsetMonth(new Date(), -1);

// 5. 计算相差天数
long days = DateUtil.betweenDay(startDate, endDate, true);
String between = DateUtil.formatBetween(startDate, endDate);

// 6. 日期区间
DateTime start = DateUtil.beginOfDay(date);
DateTime end = DateUtil.endOfDay(date);

// 7. 判断
boolean isWeekend = DateUtil.isWeekend(date);
boolean isSameDay = DateUtil.isSameDay(date1, date2);

// 8. 计时器
StopWatch stopWatch = DateUtil.createStopWatch();
stopWatch.start();
// ... 执行代码
stopWatch.stop();
System.out.println(stopWatch.getTotalTimeMillis());
```

## 总结

**Hutool DateUtil 优势**：
1. **API 简洁** - 链式调用，代码量少
2. **功能丰富** - 支持多种格式自动识别
3. **兼容性好** - 兼容 JDK Date 和 Java 8 Time API
4. **线程安全** - 内部使用线程安全实现
5. **中文优化** - 完善的本地化支持

**适用场景**：
- 日常日期处理
- 日志时间戳转换
- 定时任务时间计算
- 用户界面日期展示
