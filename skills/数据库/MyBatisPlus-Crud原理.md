# MyBatis-Plus CRUD 原理深入分析

## 概述

MyBatis-Plus (简称 MP) 是一个 MyBatis 的增强工具，在 MyBatis 的基础上只做增强不做改变，为简化开发、提高效率而生。本文深入分析 BaseMapper 的 CRUD 原理。

## 核心类图

```
┌─────────────────────────────────────────────────────────────────────┐
│                         MyBatis-Plus CRUD 架构                       │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────┐       ┌──────────────┐       ┌────────────────┐  │
│  │  BaseMapper  │──────▶│ ISqlInjector │──────▶│ AbstractMethod │  │
│  │   (接口)     │       │  (SQL注入器)  │       │  (抽象方法)    │  │
│  └──────────────┘       └──────────────┘       └────────────────┘  │
│         │                                                │           │
│         │                                                ▼           │
│  ┌──────────────┐                               ┌────────────────┐  │
│  │  Mapper<T>    │                               │  TableInfo     │  │
│  │  (父接口)     │                               │  (表信息)      │  │
│  └──────────────┘                               └────────────────┘  │
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐   │
│  │                    SQL 方法注入器                              │   │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ │   │
│  │  │ Insert  │ │ Update  │ │ Delete  │ │ Select  │ │Select..│ │   │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘ │   │
│  └───────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## 一、BaseMapper 接口

### 1.1 BaseMapper 定义

`BaseMapper<T>` 是 MyBatis-Plus 的核心接口，定义了 CRUD 的基本方法：

```java
public interface BaseMapper<T> extends Mapper<T> {
    // 插入
    int insert(T entity);
    
    // 删除
    int deleteById(Serializable id);
    int deleteById(T entity);
    int deleteByMap(Map<String, Object> columnMap);
    int delete(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);
    int deleteByIds(Collection<?> collections);
    
    // 更新
    int updateById(@Param(Constants.ENTITY) T entity);
    int update(@Param(Constants.ENTITY) T entity, @Param(Constants.WRAPPER) Wrapper<T> updateWrapper);
    
    // 查询
    T selectById(Serializable id);
    List<T> selectByIds(Collection<? extends Serializable> idList);
    List<T> selectByMap(Map<String, Object> columnMap);
    T selectOne(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);
    Long selectCount(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);
    List<T> selectList(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);
    List<Map<String, Object>> selectMaps(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);
    <E> List<E> selectObjs(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);
    
    // 分页查询
    default <P extends IPage<T>> P selectPage(P page, Wrapper<T> queryWrapper)
    default <P extends IPage<Map<String, Object>>> P selectMapsPage(P page, Wrapper<T> queryWrapper)
    
    // 批量操作
    default List<BatchResult> insert(Collection<T> entityList)
    default List<BatchResult> updateById(Collection<T> entityList)
    default boolean insertOrUpdate(T entity)
}
```

### 1.2 CRUD 方法分类

| 分类 | 方法 | 说明 |
|------|------|------|
| **插入** | `insert()` | 插入一条记录 |
| **删除** | `deleteById()`, `deleteByIds()`, `deleteByMap()`, `delete()` | 根据不同条件删除 |
| **更新** | `updateById()`, `update()` | 根据条件更新 |
| **查询** | `selectById()`, `selectByIds()`, `selectOne()`, `selectList()`, `selectCount()` | 单条/批量/条件查询 |
| **分页** | `selectPage()`, `selectMapsPage()` | 分页查询 |
| **批量** | `insert()`, `updateById()`, `insertOrUpdate()` | 批量操作 |

## 二、SQL 注入原理

### 2.1 整体流程

```
┌─────────────────────────────────────────────────────────────────────┐
│                      SQL 注入流程                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  1. Spring 启动                                                      │
│       │                                                             │
│       ▼                                                             │
│  2. MyBatis 扫描 Mapper 接口                                         │
│       │                                                             │
│       ▼                                                             │
│  3. AbstractSqlInjector.inspectInject()                           │
│       │                                                             │
│       ├── 获取泛型实体类 (modelClass)                                 │
│       │                                                             │
│       ├── TableInfoHelper.initTableInfo() 初始化表信息               │
│       │    ├── 解析 @TableName 注解                                   │
│       │    ├── 解析 @TableId 注解                                     │
│       │    ├── 解析 @TableField 注解                                  │
│       │    └── 缓存 TableInfo                                        │
│       │                                                             │
│       └── 遍历注入方法列表                                             │
│            ├── Insert()                                             │
│            ├── Delete() / DeleteById() / DeleteByIds()             │
│            ├── Update() / UpdateById()                              │
│            ├── SelectCount() / SelectList() / SelectMaps() ...     │
│            │                                                         │
│            └── 每个方法执行 injectMappedStatement()                  │
│                 └── 创建 MappedStatement 注册到 MyBatis              │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2 SQL 注入器

#### AbstractSqlInjector

```java
public abstract class AbstractSqlInjector implements ISqlInjector {
    @Override
    public void inspectInject(MapperBuilderAssistant builderAssistant, Class<?> mapperClass) {
        // 1. 获取泛型实体类
        Class<?> modelClass = ReflectionKit.getSuperClassGenericType(mapperClass, Mapper.class, 0);
        
        // 2. 初始化表信息
        TableInfo tableInfo = TableInfoHelper.initTableInfo(builderAssistant, modelClass);
        
        // 3. 获取要注入的方法列表
        List<AbstractMethod> methodList = this.getMethodList(mapperClass, tableInfo);
        
        // 4. 循环注入方法
        methodList.forEach(m -> m.inject(builderAssistant, mapperClass, modelClass, tableInfo));
    }
}
```

#### DefaultSqlInjector

```java
public class DefaultSqlInjector extends AbstractSqlInjector {
    @Override
    public List<AbstractMethod> getMethodList(Configuration configuration, 
                                               Class<?> mapperClass, TableInfo tableInfo) {
        return Stream.of(
            new Insert(dbConfig.isInsertIgnoreAutoIncrementColumn()),
            new Delete(),
            new Update(),
            new SelectCount(),
            new SelectMaps(),
            new SelectObjs(),
            new SelectList(),
            // 以下方法需要主键
            new DeleteById(),
            new DeleteByIds(),
            new UpdateById(),
            new SelectById(),
            new SelectByIds()
        ).collect(toList());
    }
}
```

### 2.3 AbstractMethod 抽象方法

每个具体的 SQL 方法都继承自 `AbstractMethod`，核心方法是 `injectMappedStatement()`：

```java
public abstract class AbstractMethod {
    // 注入 MappedStatement
    public abstract MappedStatement injectMappedStatement(
        Class<?> mapperClass, 
        Class<?> modelClass, 
        TableInfo tableInfo
    );
    
    // 创建 SQL 源码
    protected SqlSource createSqlSource(Configuration configuration, String sql, Class<?> parameterType) {
        return languageDriver.createSqlSource(configuration, sql, parameterType);
    }
}
```

## 三、SQL 方法详解

### 3.1 Insert 插入

```java
public class Insert extends AbstractMethod {
    @Override
    public MappedStatement injectMappedStatement(...) {
        // 1. 处理主键生成策略
        KeyGenerator keyGenerator = NoKeyGenerator.INSTANCE;
        if (tableInfo.getIdType() == IdType.AUTO) {
            keyGenerator = Jdbc3KeyGenerator.INSTANCE;  // 自增主键
        } else if (tableInfo.getKeySequence() != null) {
            // 序列主键（如 Oracle）
            keyGenerator = TableInfoHelper.genKeyGenerator(...);
        }
        
        // 2. 生成 SQL
        // INSERT INTO user (id, name, age) VALUES (?, ?, ?)
        String columnScript = SqlScriptUtils.convertTrim(
            tableInfo.getAllInsertSqlColumnMaybeIf(...), 
            LEFT_BRACKET, RIGHT_BRACKET, null, COMMA
        );
        String valuesScript = ...;
        
        String sql = SqlMethod.INSERT_ONE.format(
            tableInfo.getTableName(), 
            columnScript, 
            valuesScript
        );
        
        // 3. 创建 MappedStatement
        return this.addInsertMappedStatement(
            mapperClass, modelClass, methodName, 
            sqlSource, keyGenerator, keyProperty, keyColumn
        );
    }
}
```

**生成的 SQL 示例：**
```sql
INSERT INTO user (id, name, age, create_time, update_time) 
VALUES (?, ?, ?, ?, ?)
```

### 3.2 Delete 删除

```java
// DeleteById 方法
public class DeleteById extends AbstractMethod {
    @Override
    public MappedStatement injectMappedStatement(...) {
        SqlMethod sqlMethod = tableInfo.isWithLogicDelete() 
            ? SqlMethod.LOGIC_DELETE_BY_ID 
            : SqlMethod.DELETE_BY_ID;
        
        String sql = sqlMethod.format(
            tableInfo.getTableName(),
            tableInfo.getKeyColumn(),
            tableInfo.getKeyProperty()
        );
        // 逻辑删除: UPDATE user SET deleted=1 WHERE id=? AND deleted=0
        // 物理删除: DELETE FROM user WHERE id=?
    }
}
```

### 3.3 Update 更新

```java
// UpdateById 方法
public class UpdateById extends AbstractMethod {
    @Override
    public MappedStatement injectMappedStatement(...) {
        // 生成 SET 片段，包含自动填充字段
        String sqlSet = sqlSet(logic, ew, tableInfo, ...);
        
        String sql = SqlMethod.UPDATE_BY_ID.format(
            tableInfo.getTableName(),
            sqlSet,
            tableInfo.getKeyColumn(),
            tableInfo.getKeyProperty(),
            sqlComment()
        );
        // UPDATE user SET name=?, age=? WHERE id=?
    }
}
```

### 3.4 Select 查询

```java
// SelectList 方法
public class SelectList extends AbstractMethod {
    @Override
    public MappedStatement injectMappedStatement(...) {
        // 1. 处理查询条件
        String whereSegment = getWhereSegment(tableInfo);
        
        // 2. 处理排序
        String orderBySegment = getOrderBySegment(tableInfo);
        
        // 3. 生成 SQL
        String sql = SqlMethod.SELECT_LIST.format(
            sqlComment(),           // SQL 注释
            sqlSelect,              // SELECT 字段
            tableInfo.getTableName(),
            whereSegment,           // WHERE 条件
            orderBySegment,
            sqlFirst                // 额外 SQL（如 limit）
        );
    }
}
```

## 四、TableInfo 表信息

`TableInfo` 是 MyBatis-Plus 的核心元数据类，封装了实体类与数据库表的映射关系：

```java
@Data
public class TableInfo {
    private Class<?> entityType;        // 实体类型
    private IdType idType;             // 主键类型
    private String tableName;          // 表名
    private String keyColumn;          // 主键列名
    private String keyProperty;        // 主键属性名
    private Class<?> keyType;          // 主键类型
    private List<TableFieldInfo> fieldList;  // 字段列表
    
    // 特性标志
    private boolean withInsertFill;    // 启用插入填充
    private boolean withUpdateFill;    // 启用更新填充
    private boolean withLogicDelete;  // 启用逻辑删除
    private boolean withVersion;       // 启用乐观锁
}
```

### 4.1 TableInfo 初始化

```java
public static TableInfo initTableInfo(MapperBuilderAssistant assistant, Class<?> entityClass) {
    // 1. 查找缓存
    TableInfo tableInfo = TABLE_INFO_CACHE.get(entityClass);
    if (tableInfo != null) {
        return tableInfo;
    }
    
    // 2. 创建 TableInfo
    tableInfo = new TableInfo(assistant.getConfiguration(), entityClass);
    
    // 3. 解析 @TableName 注解
    TableName tableName = (TableName) entityClass.getDeclaredAnnotation(TableName.class);
    if (tableName != null) {
        tableInfo.setTableName(tableName.value());
    }
    
    // 4. 解析字段（遍历所有属性）
    for (Field field : entityClass.getDeclaredFields()) {
        // 解析 @TableId
        // 解析 @TableField
        // 处理自动填充
        // 处理逻辑删除
        // 处理乐观锁
    }
    
    // 5. 缓存
    TABLE_INFO_CACHE.put(entityClass, tableInfo);
    return tableInfo;
}
```

## 五、SqlMethod 枚举

定义了所有 SQL 方法的模板：

```java
public enum SqlMethod {
    // 插入
    INSERT_ONE("insert", "插入一条数据", 
        "INSERT INTO %s %s VALUES %s"),
    
    // 删除
    DELETE_BY_ID("deleteById", "根据ID删除", 
        "DELETE FROM %s WHERE %s=#{%s}"),
    DELETE_BY_IDS("deleteByIds", "批量删除", 
        "DELETE FROM %s WHERE %s IN (%s)"),
    
    // 逻辑删除
    LOGIC_DELETE_BY_ID("deleteById", "逻辑删除", 
        "UPDATE %s %s WHERE %s=#{%s} %s"),
    
    // 更新
    UPDATE_BY_ID("updateById", "根据ID更新", 
        "UPDATE %s %s WHERE %s=#{%s} %s"),
    
    // 查询
    SELECT_BY_ID("selectById", "根据ID查询", 
        "SELECT %s FROM %s WHERE %s=#{%s} %s"),
    SELECT_LIST("selectList", "列表查询", 
        "SELECT %s FROM %s %s %s %s %s"),
    SELECT_COUNT("selectCount", "统计数量", 
        "SELECT COUNT(%s) FROM %s %s %s");
    
    public String format(Object... args) {
        return String.format(sql, args);
    }
}
```

## 六、完整执行流程

```
┌─────────────────────────────────────────────────────────────────────┐
│                      完整执行流程                                     │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  用户调用: userMapper.insert(user)                                  │
│       │                                                             │
│       ▼                                                             │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  MyBatis-Plus 动态代理                                        │    │
│  │  (MapperProxy)                                               │    │
│  └─────────────────────────────────────────────────────────────┘    │
│       │                                                             │
│       ▼                                                             │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  查找已注入的 MappedStatement                                  │    │
│  │  statementId = "com.xxx.UserMapper.insert"                   │    │
│  └─────────────────────────────────────────────────────────────┘    │
│       │                                                             │
│       ▼                                                             │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  Executor 执行 SQL                                            │    │
│  │  ├── ParameterHandler 处理参数                                │    │
│  │  ├── StatementHandler 执行 SQL                                │    │
│  │  └── ResultHandler 处理结果                                    │    │
│  └─────────────────────────────────────────────────────────────┘    │
│       │                                                             │
│       ▼                                                             │
│  返回结果                                                            │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## 七、关键注解

| 注解 | 说明 | 示例 |
|------|------|------|
| `@TableName` | 映射表名 | `@TableName("user")` |
| `@TableId` | 主键字段 | `@TableId(value = "id", type = IdType.AUTO)` |
| `@TableField` | 普通字段 | `@TableField("user_name")` |
| `@TableLogic` | 逻辑删除 | `@TableLogic` |
| `@Version` | 乐观锁 | `@Version` |
| `@TableField(fill = FieldFill.INSERT)` | 插入填充 | 自动填充字段 |

## 八、总结

MyBatis-Plus 的 CRUD 原理核心在于：

1. **接口定义** - `BaseMapper<T>` 定义了统一的 CRUD 接口
2. **SQL 注入** - 通过 `ISqlInjector` 在启动时将 SQL 方法注入到 Mapper 接口
3. **元数据管理** - `TableInfo` 封装了实体类与表的映射关系
4. **动态代理** - 使用 MyBatis 的 `MapperProxy` 执行注入的 SQL
5. **方法复用** - 所有方法基于 `AbstractMethod` 抽象，通过模板方法模式生成 SQL

这种设计使得开发者无需编写 XML 文件，只需继承 `BaseMapper` 即可获得完整的 CRUD 功能。
