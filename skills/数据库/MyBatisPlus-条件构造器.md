# MyBatis-Plus 条件构造器深入分析

## 概述

条件构造器（Wrapper）是 MyBatis-Plus 的核心组件之一，用于构建 WHERE 条件、SET 语句等 SQL 片段。它提供了丰富的 API 来构建复杂的查询和更新条件。

## 核心类图

```
┌─────────────────────────────────────────────────────────────────────┐
│                      条件构造器类图                                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│                         ┌──────────────┐                            │
│                         │   Wrapper<T>  │  (顶层接口)                │
│                         └──────┬───────┘                            │
│                                │                                    │
│                    ┌───────────┴───────────┐                        │
│                    │                       │                        │
│          ┌─────────▼─────────┐    ┌────────▼────────┐                │
│          │ AbstractWrapper  │    │  UpdateWrapper │                │
│          │   (抽象类)        │    │   (更新)        │                │
│          └─────────┬─────────┘    └─────────────────┘                │
│                    │                                               │
│         ┌─────────┴─────────┐                                       │
│         │                  │                                       │
│ ┌───────▼────────┐  ┌───────▼─────────┐                            │
│ │  QueryWrapper  │  │ LambdaQueryWrap │                            │
│ │  (查询)        │  │  (Lambda查询)    │                            │
│ └───────────────┘  └─────────────────┘                            │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │                    条件构建接口                                  │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐               │ │
│  │  │  Compare    │ │    Join     │ │   Nested    │               │ │
│  │  │ (比较条件)  │ │  (连接条件)  │ │  (嵌套条件)  │               │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘               │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐               │ │
│  │  │   Func     │ │   Where     │ │   SubQuery  │               │ │
│  │  │  (函数)    │ │  (where)    │ │ (子查询)    │               │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘               │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## 一、Wrapper 接口

```java
public interface Wrapper<T> {
    // 获取 SQL 语句
    String getSqlSegment();
    
    // 获取 SQL 片段（不含 WHERE）
    String getSqlExpression();
    
    // 获取实体
    T getEntity();
    
    // 清除条件
    void clear();
}
```

## 二、AbstractWrapper 抽象类

### 2.1 核心属性

```java
public abstract class AbstractWrapper<T, R, Children extends AbstractWrapper<T, R, Children>>
    extends Wrapper<T> implements Compare<Children, R>, Nested<Children, Children>, Join<Children>, Func<Children, R> {
    
    // 实体对象
    protected T entity;
    protected Class<T> entityClass;
    
    // 参数序列
    protected AtomicInteger paramNameSeq;
    protected Map<String, Object> paramNameValuePairs;
    
    // SQL 片段容器
    protected MergeSegments expression;
    
    // 其他 SQL 片段
    protected SharedString paramAlias;     // 参数别名
    protected SharedString lastSql;        // 最后的 SQL
    protected SharedString sqlComment;     // SQL 注释
    protected SharedString sqlFirst;        // SQL 起始
    
    // 查询字段
    protected SharedString sqlSelect;
}
```

### 2.2 继承接口

```java
// 比较条件：eq, ne, gt, lt, ge, le, like, between...
public interface Compare<Children, R> {
    Children eq(R column, Object val);
    Children ne(R column, Object val);
    Children gt(R column, Object val);
    Children ge(R column, Object val);
    Children lt(R column, Object val);
    Children le(R column, Object val);
    Children like(R column, Object val);
    Children between(R column, Object val1, Object val2);
    // ... 更多
}

// 连接条件：and, or, nested
public interface Join<Children> {
    Children and(boolean condition, Consumer<Children> consumer);
    Children or(boolean condition, Consumer<Children> consumer);
    Children nested(boolean condition, Consumer<Children> consumer);
}

// 嵌套条件
public interface Nested<Children, Children1> {
    Children1 and(boolean condition, Consumer<Children> consumer);
    Children1 or(boolean condition, Consumer<Children> consumer);
    Children1 not(boolean condition, Consumer<Children> consumer);
    Children1 nested(boolean condition, Consumer<Children> consumer);
}

// 函数条件：in, exists, apply...
public interface Func<Children, R> {
    Children in(R column, Collection<?> values);
    Children notIn(R column, Collection<?> values);
    Children exists(Wrapper<?> wrapper);
    Children notExists(Wrapper<?> wrapper);
    Children apply(String applySql, Object... values);
    Children exists(Supplier<SELECT> selectSupplier);
}
```

## 三、QueryWrapper 查询条件构造器

### 3.1 基本使用

```java
// 1. 基本查询
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.eq("name", "张三").gt("age", 18);
List<User> list = userMapper.selectList(wrapper);
// SELECT * FROM user WHERE name = '张三' AND age > 18

// 2. 模糊查询
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.like("name", "张").likeLeft("email", "@qq.com");
List<User> list = userMapper.selectList(wrapper);
// SELECT * FROM user WHERE name LIKE '%张%' AND email LIKE '%@qq.com'

// 3. 排序
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.orderByDesc("age").orderByAsc("id");
List<User> list = userMapper.selectList(wrapper);
// SELECT * FROM user ORDER BY age DESC, id ASC

// 4. 分页
IPage<User> page = new Page<>(1, 10);
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.eq("status", 1);
IPage<User> result = userMapper.selectPage(page, wrapper);
```

### 3.2 链式调用

```java
// 链式调用风格
List<User> users = userMapper.selectList(
    new QueryWrapper<User>()
        .eq("status", 1)
        .like("name", "张")
        .between("age", 18, 30)
        .orderByDesc("create_time")
        .last("LIMIT 10")
);
```

### 3.3 QueryWrapper 核心方法

```java
public class QueryWrapper<T> extends AbstractWrapper<T, String, QueryWrapper<T>> 
    implements Query<QueryWrapper<T>, T, String> {
    
    // 查询字段
    protected final SharedString sqlSelect = new SharedString();
    
    // 指定查询字段
    public QueryWrapper<T> select(String... columns) {
        this.sqlSelect.setStringValue(String.join(",", columns));
        return this;
    }
    
    // 排除查询字段
    public QueryWrapper<T> select(Class<T> entityClass, Predicate<TableFieldInfo> predicate) {
        // 动态选择字段
        return this;
    }
    
    // 条件判断
    public QueryWrapper<T> eq(String column, Object val) { ... }
    public QueryWrapper<T> ne(String column, Object val) { ... }
    public QueryWrapper<T> gt(String column, Object val) { ... }
    public QueryWrapper<T> ge(String column, Object val) { ... }
    public QueryWrapper<T> lt(String column, Object val) { ... }
    public QueryWrapper<T> le(String column, Object val) { ... }
    
    // 模糊匹配
    public QueryWrapper<T> like(String column, Object val) { ... }
    public QueryWrapper<T> notLike(String column, Object val) { ... }
    public QueryWrapper<T> likeLeft(String column, Object val) { ... }  // %xxx
    public QueryWrapper<T> likeRight(String column, Object val) { ... } // xxx%
    
    // 范围查询
    public QueryWrapper<T> between(String column, Object val1, Object val2) { ... }
    public QueryWrapper<T> notBetween(String column, Object val1, Object val2) { ... }
    
    // IN 查询
    public QueryWrapper<T> in(String column, Collection<?> values) { ... }
    public QueryWrapper<T> notIn(String column, Collection<?> values) { ... }
    public QueryWrapper<T> inSql(String column, String inSql) { ... }  // 子查询
    
    // NULL 判断
    public QueryWrapper<T> isNull(String column) { ... }
    public QueryWrapper<T> isNotNull(String column) { ... }
    
    // 聚合函数
    public QueryWrapper<T> groupBy(String... columns) { ... }
    public QueryWrapper<T> having(String sql, Object... params) { ... }
    
    // 排序
    public QueryWrapper<T> orderByAsc(String... columns) { ... }
    public QueryWrapper<T> orderByDesc(String... columns) { ... }
    public QueryWrapper<T> orderBy(boolean condition, boolean asc, String... columns) { ... }
    
    // 逻辑连接
    public QueryWrapper<T> and(Consumer<QueryWrapper<T>> consumer) { ... }
    public QueryWrapper<T> or(Consumer<QueryWrapper<T>> consumer) { ... }
    public QueryWrapper<T> nested(Consumer<QueryWrapper<T>> consumer) { ... }
    
    // 拼接 SQL
    public QueryWrapper<T> apply(String applySql, Object... values) { ... }
    public QueryWrapper<T> last(String lastSql) { ... }
}
```

### 3.4 查询示例

```java
// 示例1：复杂多条件查询
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.select("id", "name", "age", "email")
       .eq("status", 1)
       .like("name", "张")
       .notLike("email", "test")
       .between("age", 20, 30)
       .in("department_id", Arrays.asList(1, 2, 3))
       .isNotNull("email")
       .groupBy("department_id")
       .having("COUNT(*) > 5")
       .orderByDesc("create_time")
       .last("LIMIT 100");

// 生成的 SQL:
// SELECT id, name, age, email FROM user 
// WHERE status = 1 
// AND name LIKE '%张%' 
// AND email NOT LIKE '%test%' 
// AND age BETWEEN 20 AND 30 
// AND department_id IN (1,2,3) 
// AND email IS NOT NULL 
// GROUP BY department_id 
// HAVING COUNT(*) > 5 
// ORDER BY create_time DESC 
// LIMIT 100


// 示例2：动态条件
String name = "张三";
Integer age = null;
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.eq(StringUtils.isNotBlank(name), "name", name)
       .eq(age != null, "age", age);

// 当 name 不为空时添加 name 条件
// 当 age 不为空时添加 age 条件


// 示例3：AND/OR 嵌套
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.eq("status", 1)
       .and(w -> w.eq("type", 1).or().eq("type", 2))
       .or(w -> w.like("name", "VIP").orLike("email", "vip"));

// WHERE status = 1 AND (type = 1 OR type = 2) OR (name LIKE '%VIP%' OR email LIKE '%VIP%')
```

## 四、UpdateWrapper 更新条件构造器

### 4.1 基本使用

```java
// 使用 UpdateWrapper 更新
UpdateWrapper<User> wrapper = new UpdateWrapper<>();
wrapper.eq("id", 1).set("name", "新名字").set("age", 25);
userMapper.update(null, wrapper);
// UPDATE user SET name='新名字', age=25 WHERE id = 1


// 结合实体更新
User user = new User();
user.setName("新名字");
UpdateWrapper<User> wrapper = new UpdateWrapper<>();
wrapper.eq("id", 1);
userMapper.update(user, wrapper);
// UPDATE user SET name='新名字' WHERE id = 1


// 使用 lambda 表达式
UpdateWrapper<User> wrapper = new UpdateWrapper<>();
wrapper.set(User::getName, "新名字")
       .eq(User::getId, 1);
userMapper.update(null, wrapper);
```

### 4.2 核心方法

```java
public class UpdateWrapper<T> extends AbstractWrapper<T, String, UpdateWrapper<T>> 
    implements Update<UpdateWrapper<T>, String> {
    
    // 设置字段值
    public UpdateWrapper<T> set(String column, Object value) { ... }
    public UpdateWrapper<T> set(boolean condition, String column, Object value) { ... }
    
    // SET 片段
    public UpdateWrapper<T> setSql(String setSql) { ... }
    
    // 使用 lambda
    public UpdateWrapper<T> set(SFunction<T, ?> column, Object value) { ... }
}
```

### 4.3 更新示例

```java
// 示例1：普通更新
UpdateWrapper<User> wrapper = new UpdateWrapper<>();
wrapper.set("name", "张三")
       .set("age", 30)
       .eq("id", 1);
userMapper.update(null, wrapper);
// UPDATE user SET name='张三', age=30 WHERE id=1


// 示例2：使用 lambda 表达式
UpdateWrapper<User> wrapper = new UpdateWrapper<>();
wrapper.set(User::getName, "张三")
       .set(User::getAge, 30)
       .set(User::getUpdateTime, new Date())
       .eq(User::getId, 1);
userMapper.update(null, wrapper);


// 示例3：使用实体 + 条件
User user = new User();
user.setName("新名字");
user.setStatus(1);
UpdateWrapper<User> wrapper = new UpdateWrapper<>();
wrapper.eq("department_id", 10)
       .gt("salary", 5000);
userMapper.update(user, wrapper);
// UPDATE user SET name='新名字', status=1 
// WHERE department_id=10 AND salary>5000


// 示例4：使用 setSql 拼接自定义 SQL
UpdateWrapper<User> wrapper = new UpdateWrapper<>();
wrapper.setSql("age = age + 1, status = IFNULL(status, 0) + 1")
       .eq("id", 1);
userMapper.update(null, wrapper);
// UPDATE user SET age = age + 1, status = IFNULL(status, 0) + 1 WHERE id=1
```

## 五、Lambda 条件构造器

### 5.1 LambdaQueryWrapper

使用 Lambda 表达式，避免硬编码列名：

```java
// 普通写法
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.eq("name", "张三");

// Lambda 写法（编译时安全）
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getName, "张三");

// 链式调用
List<User> users = userMapper.selectList(
    new LambdaQueryWrapper<User>()
        .eq(User::getStatus, 1)
        .like(User::getName, "张")
        .between(User::getAge, 18, 30)
        .orderByDesc(User::getCreateTime)
);
```

### 5.2 LambdaUpdateWrapper

```java
// Lambda 更新
LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
wrapper.set(User::getName, "新名字")
       .set(User::getStatus, 1)
       .eq(User::getId, 1)
       .or()
       .eq(User::getEmail, "test@qq.com");
userMapper.update(null, wrapper);
```

### 5.3 Lambda 优势

```java
// 1. 编译时检查
// 正确：列名写错会编译报错
wrapper.eq(User::getName, "张三");  // User 类没有 getName 字段？编译错误！

// 2. 避免硬编码
// 错误：列名写错不会报错，运行时报错
wrapper.eq("nmae", "张三拼写错误，但");  // 不会报错


// 3. IDE 自动补全
// Lambda 方式可以利用 IDE 的代码补全功能
```

## 六、条件构造器原理

### 6.1 条件组装流程

```
┌─────────────────────────────────────────────────────────────────────┐
│                      条件组装流程                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  用户调用: wrapper.eq("name", "张三").gt("age", 18)                 │
│       │                                                             │
│       ▼                                                             │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  AbstractWrapper                                            │    │
│  │                                                               │    │
│  │  1. eq("name", "张三")                                       │    │
│  │     ├── columnToString("name") -> "name"                    │    │
│  │     ├── formatParam("name", "张三") -> "#{__MPGENVAL1__}"    │    │
│  │     └── appendSqlSegments("name", "=", "#{__MPGENVAL1__}")  │    │
│  │                                                               │    │
│  │  2. gt("age", 18)                                            │    │
│  │     ├── columnToString("age") -> "age"                      │    │
│  │     ├── formatParam("age", 18) -> "#{__MPGENVAL2__}"        │    │
│  │     └── appendSqlSegments("age", ">", "#{__MPGENVAL2__}")   │    │
│  │                                                               │    │
│  └─────────────────────────────────────────────────────────────┘    │
│       │                                                             │
│       ▼                                                             │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  MergeSegments 组装                                          │    │
│  │                                                               │    │
│  │  AND (name = #{__MPGENVAL1__})                                │    │
│  │  AND (age > #{__MPGENVAL2__})                                 │    │
│  │                                                               │    │
│  └─────────────────────────────────────────────────────────────┘    │
│       │                                                             │
│       ▼                                                             │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  生成最终 SQL                                                  │    │
│  │                                                               │    │
│  │  WHERE name = ? AND age > ?                                  │    │
│  │                                                               │    │
│  │  参数: [张三, 18]                                             │    │
│  │                                                               │    │
│  └─────────────────────────────────────────────────────────────┘    │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 6.2 SQL 片段管理

```java
// MergeSegments 管理所有 SQL 片段
public class MergeSegments {
    // 1. WHERE 条件片段
    // 2. JOIN 片段
    // 3. 普通片段 (GROUP BY, HAVING, ORDER BY)
}

// 每个片段类型
public enum SegmentType {
    WHERE,       // WHERE 条件
    AND,         // AND
    OR,          // OR
    AND_NESTED,  // AND (嵌套)
    OR_NESTED,   // OR (嵌套)
    SET,         // SET (更新用)
    SELECT,      // SELECT 字段
    FROM,        // FROM 表
    JOIN,        // JOIN
    GROUP_BY,    // GROUP BY
    HAVING,      // HAVING
    ORDER_BY,    // ORDER BY
    LAST,        // 最后的 SQL
    APPLY        // 动态 SQL
}
```

### 6.3 参数处理

```java
// 参数命名和缓存
protected String formatParam(String name, Object value) {
    // 生成参数名: paramNameSeq 递增
    String paramName = "MPGENVAL" + paramNameSeq.incrementAndGet();
    
    // 存入参数映射
    paramNameValuePairs.put(paramName, value);
    
    // 返回占位符
    return "#{" + paramName + "}";
}

// 条件判断 - 避免无谓的条件添加
public Children eq(boolean condition, R column, Object val) {
    if (condition) {
        // 只有 condition 为 true 时才添加条件
        return addCondition(true, column, EQ, val);
    }
    return typedThis;
}

// 示例
wrapper.eq(true, "name", "张三");  // 添加条件
wrapper.eq(false, "name", "张三"); // 不添加条件
```

## 七、常用示例

### 7.1 查询示例

```java
// 1. 基础查询
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.eq("age", 20);                    // age = 20
wrapper.ne("status", 0);                  // status != 0
wrapper.gt("score", 60);                  // score > 60
wrapper.ge("score", 60);                  // score >= 60
wrapper.lt("age", 30);                    // age < 30
wrapper.le("age", 30);                    // age <= 30


// 2. 模糊查询
wrapper.like("name", "张");               // name LIKE '%张%'
wrapper.notLike("email", "test");         // email NOT LIKE '%test%'
wrapper.likeLeft("code", "01");           // code LIKE '%01'
wrapper.likeRight("prefix", "VIP");       // code LIKE 'VIP%'


// 3. 范围查询
wrapper.between("age", 18, 30);           // age BETWEEN 18 AND 30
wrapper.notBetween("age", 18, 30);       // age NOT BETWEEN 18 AND 30


// 4. IN 查询
wrapper.in("id", Arrays.asList(1, 2, 3)); // id IN (1,2,3)
wrapper.notIn("id", Arrays.asList(4, 5)); // id NOT IN (4,5)
wrapper.inSql("id", "SELECT id FROM user WHERE status = 1"); // 子查询


// 5. NULL 判断
wrapper.isNull("email");                  // email IS NULL
wrapper.isNotNull("phone");               // phone IS NOT NULL


// 6. 逻辑组合
wrapper.and(w -> w.eq("a", 1).or().eq("b", 2));   // AND (a = 1 OR b = 2)
wrapper.or(w -> w.eq("a", 1).and().eq("b", 2));   // OR (a = 1 AND b = 2)
wrapper.nested(w -> w.eq("a", 1).eq("b", 2));     // (a = 1 AND b = 2)


// 7. 排序
wrapper.orderByAsc("age", "name");        // ORDER BY age, name ASC
wrapper.orderByDesc("create_time");       // ORDER BY create_time DESC
wrapper.orderBy(true, false, "age");     // 动态排序


// 8. 分组聚合
wrapper.groupBy("department_id");         // GROUP BY department_id
wrapper.having("COUNT(*) > 5");           // HAVING COUNT(*) > 5


// 9. 限定结果
wrapper.last("LIMIT 10 OFFSET 20");       // LIMIT 10 OFFSET 20
wrapper.apply("DATE_FORMAT(create_time,'%Y') = 2024"); // 动态SQL


// 10. 实体查询（根据 entity 生成 WHERE）
User user = new User();
user.setName("张三");
user.setAge(20);
QueryWrapper<User> wrapper = new QueryWrapper<>(user);
List<User> list = userMapper.selectList(wrapper);
// WHERE name='张三' AND age=20
```

### 7.2 更新示例

```java
// 1. 基础更新
UpdateWrapper<User> wrapper = new UpdateWrapper<>();
wrapper.eq("id", 1).set("name", "新名字");
userMapper.update(null, wrapper);
// UPDATE user SET name='新名字' WHERE id = 1


// 2. 批量设置
UpdateWrapper<User> wrapper = new UpdateWrapper<>();
wrapper.eq("id", 1)
       .set("name", "新名字")
       .set("age", 25)
       .set("status", 1);
userMapper.update(null, wrapper);
// UPDATE user SET name='新名字', age=25, status=1 WHERE id = 1


// 3. 条件更新
UpdateWrapper<User> wrapper = new UpdateWrapper<>();
wrapper.eq("department_id", 10)
       .gt("salary", 5000)
       .set("status", 1);
User user = new User();
user.setBonus(1000);
userMapper.update(user, wrapper);
// UPDATE user SET bonus=1000, status=1 WHERE department_id=10 AND salary>5000


// 4. 运算更新
UpdateWrapper<User> wrapper = new UpdateWrapper<>();
wrapper.setSql("age = age + 1, visit_count = visit_count + 1")
       .eq("id", 1);
userMapper.update(null, wrapper);
// UPDATE user SET age = age + 1, visit_count = visit_count + 1 WHERE id = 1
```

## 八、Wrappers 工具类

```java
// 快速创建 Wrapper
QueryWrapper<User> queryWrapper = Wrappers.query();
QueryWrapper<User> queryWrapper = Wrappers.query(user);
QueryWrapper<User> queryWrapper = Wrappers.query(User.class);

UpdateWrapper<User> updateWrapper = Wrappers.update();
UpdateWrapper<User> updateWrapper = Wrappers.update(User.class);

LambdaQueryWrapper<User> lambdaQuery = Wrappers.lambdaQuery();
LambdaUpdateWrapper<User> lambdaUpdate = Wrappers.lambdaUpdate();
```

## 九、总结

MyBatis-Plus 条件构造器的核心特点：

1. **链式 API** - 支持链式调用，代码简洁
2. **类型安全** - Lambda 方式提供编译时类型检查
3. **条件判断** - 支持动态条件，避免无谓的 SQL 拼接
4. **嵌套条件** - 支持复杂的 AND/OR 嵌套
5. **实体结合** - 支持结合实体对象生成条件
6. **SQL 注入防护** - 自动处理参数，防止 SQL 注入
