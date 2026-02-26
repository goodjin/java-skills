# MyBatis-Plus 分页插件深入分析

## 概述

MyBatis-Plus 提供了强大的分页查询能力，通过 `PaginationInnerInterceptor` 拦截器实现自动分页。无需编写分页 SQL，框架自动完成分页查询和总数统计。

## 核心类图

```
┌─────────────────────────────────────────────────────────────────────┐
│                      分页插件架构图                                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌─────────────────┐       ┌─────────────────┐                   │
│  │   IPage<T>      │       │    Page<T>       │                   │
│  │   (分页接口)     │◀──────│   (分页实现)     │                   │
│  └─────────────────┘       └─────────────────┘                   │
│           │                           │                            │
│           │                           │                            │
│           │           ┌───────────────┴───────────────┐            │
│           │           │                               │            │
│           ▼           ▼                               ▼            │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────────┐  │
│  │ OrderItem       │ │   IRowBounds    │ │  PaginationInner   │  │
│  │ (排序项)        │ │   (行边界)      │ │  Interceptor       │  │
│  └─────────────────┘ └─────────────────┘ │  (分页拦截器)      │  │
│                                          └─────────────────────┘  │
│                                                     │              │
│                                                     ▼              │
│  ┌─────────────────────────────────────────────────────────────────┐│
│  │                    InnerInterceptor 链                          ││
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐  ││
│  │  │ Tenant.. │ │ Block..  │ │ DataPerm.│ │ PaginationInner  │  ││
│  │  │ (多租户) │ │ (防攻击) │ │ (数据权限)│ │ (分页)          │  ││
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────────────┘  ││
│  └─────────────────────────────────────────────────────────────────┘│
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## 一、核心接口与类

### 1.1 IPage 分页接口

```java
public interface IPage<T> extends Serializable {
    // 获取当前页
    long getCurrent();
    
    // 获取每页大小
    long getSize();
    
    // 获取总页数
    long getPages();
    
    // 获取总记录数
    long getTotal();
    
    // 获取结果列表
    List<T> getRecords();
    
    // 设置结果列表
    IPage<T> setRecords(List<T> records);
    
    // 获取排序项列表
    List<OrderItem> getOrders();
    
    // 设置排序项
    IPage<T> setOrders(List<OrderItem> orders);
    
    // 是否查找 count
    boolean searchCount();
    
    // 设置是否查找 count
    IPage<T> searchCount(Boolean searchCount);
}
```

### 1.2 Page 分页实现类

```java
public class Page<T> implements IPage<T> {
    
    // 当前页
    private long current = 1;
    
    // 每页大小
    private long size = 10;
    
    // 总记录数
    private long total = 0;
    
    // 结果列表
    private List<T> records;
    
    // 排序项
    private List<OrderItem> orders;
    
    // 是否查询总数
    private boolean searchCount = true;
    
    // count ID
    private String countId;
    
    // 最大每页大小
    private Long maxLimit;
    
    // 构造方法
    public Page() {}
    
    public Page(long current, long size) {
        this(current, size, 0);
    }
    
    public Page(long current, long size, long total) {
        this.current = current;
        this.size = size;
        this.total = total;
    }
}
```

## 二、分页拦截器

### 2.1 PaginationInnerInterceptor

```java
@Data
@NoArgsConstructor
@SuppressWarnings({"rawtypes"})
public class PaginationInnerInterceptor implements InnerInterceptor {
    
    // 溢出总页数后是否进行处理
    protected boolean overflow = false;
    
    // 单页分页条数限制
    protected Long maxLimit;
    
    // 数据库类型
    private DbType dbType;
    
    // 方言实现类
    private IDialect dialect;
    
    // 是否优化 count SQL（去掉 join）
    protected boolean optimizeJoin = true;
    
    public PaginationInnerInterceptor(DbType dbType) { ... }
    public PaginationInnerInterceptor(IDialect dialect) { ... }
}
```

### 2.2 核心方法

```java
public class PaginationInnerInterceptor implements InnerInterceptor {
    
    /**
     * 执行查询前处理 - 判断是否需要分页
     */
    @Override
    public boolean willDoQuery(Executor executor, MappedStatement ms, 
                              Object parameter, RowBounds rowBounds, 
                              ResultHandler resultHandler, BoundSql boundSql) {
        // 1. 从参数中获取 IPage
        IPage<?> page = ParameterUtils.findPage(parameter).orElse(null);
        
        // 2. 如果没有分页参数，直接返回
        if (page == null || page.getSize() < 0 || !page.searchCount()) {
            return true;
        }
        
        // 3. 构建 count SQL 并执行
        BoundSql countSql = buildCountSql(ms, page, boundSql);
        
        // 4. 执行 count 查询
        CacheKey cacheKey = executor.createCacheKey(countMs, parameter, rowBounds, countSql);
        List<Object> result = executor.query(countMs, parameter, rowBounds, resultHandler, cacheKey, countSql);
        
        // 5. 设置总数
        long total = 0;
        if (CollectionUtils.isNotEmpty(result)) {
            total = Long.parseLong(result.get(0).toString());
        }
        page.setTotal(total);
        
        // 6. 判断是否继续分页
        return continuePage(page);
    }
    
    /**
     * 查询前 - 拦截并修改 SQL
     */
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, 
                           Object parameter, RowBounds rowBounds, 
                           ResultHandler resultHandler, BoundSql boundSql) {
        // 1. 获取分页参数
        IPage<?> page = ParameterUtils.findPage(parameter).orElse(null);
        if (null == page) {
            return;
        }
        
        // 2. 处理排序
        if (CollectionUtils.isNotEmpty(page.orders())) {
            buildSql = this.concatOrderBy(buildSql, page.orders());
        }
        
        // 3. 限制每页大小
        handlerLimit(page, maxLimit);
        
        // 4. 获取方言并构建分页 SQL
        IDialect dialect = findIDialect(executor);
        DialectModel model = dialect.buildPaginationSql(buildSql, page.offset(), page.getSize());
        
        // 5. 替换原始 SQL
        mpBoundSql.sql(model.getDialectSql());
    }
}
```

## 三、分页执行流程

```
┌─────────────────────────────────────────────────────────────────────┐
│                      分页执行流程                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  用户调用: userMapper.selectPage(page, wrapper)                     │
│       │                                                             │
│       ▼                                                             │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  1. MyBatis 执行器开始查询                                    │    │
│  └─────────────────────────────────────────────────────────────┘    │
│       │                                                             │
│       ▼                                                             │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  2. PaginationInnerInterceptor.willDoQuery()                │    │
│  │                                                               │    │
│  │  ├─ 获取 IPage 参数                                           │    │
│  │  ├─ 构建 count SQL                                            │    │
│  │  │   SELECT COUNT(*) AS total FROM user ...                  │    │
│  │  ├─ 执行 count 查询                                           │    │
│  │  └─ 设置总记录数                                               │    │
│  └─────────────────────────────────────────────────────────────┘    │
│       │                                                             │
│       ▼                                                             │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  3. PaginationInnerInterceptor.beforeQuery()                │    │
│  │                                                               │    │
│  │  ├─ 处理排序 ORDER BY                                         │    │
│  │  ├─ 获取方言实现 (MysqlDialect/H2Dialect/...)              │    │
│  │  ├─ 构建分页 SQL                                              │    │
│  │  │   SELECT * FROM user ... LIMIT 10 OFFSET 0               │    │
│  │  └─ 替换原始 SQL                                              │    │
│  └─────────────────────────────────────────────────────────────┘    │
│       │                                                             │
│       ▼                                                             │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  4. Executor 执行分页查询                                      │    │
│  └─────────────────────────────────────────────────────────────┘    │
│       │                                                             │
│       ▼                                                             │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  5. 返回 IPage 结果                                           │    │
│  │     page.setRecords(results)                                 │    │
│  │     page.getTotal() / getPages()                             │    │
│  └─────────────────────────────────────────────────────────────┘    │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## 四、Count SQL 优化

### 4.1 优化原理

```java
// 原始 SQL（有 JOIN）
SELECT u.*, r.* FROM user u 
LEFT JOIN role r ON u.role_id = r.id 
WHERE u.status = 1

// 优化后 count SQL（去掉 JOIN）
SELECT COUNT(*) FROM user u WHERE u.status = 1
```

### 4.2 实现逻辑

```java
protected String autoCountSql(IPage<?> page, String originalSql) {
    // 1. 使用 JSqlParser 解析 SQL
    Select select = (Select) JsqlParserUtil.parse(originalSql);
    SelectBody selectBody = select.getSelectBody();
    
    // 2. 去除 JOIN（默认只优化 LEFT JOIN）
    if (optimizeJoin) {
        // 递归处理 SELECT BODY
        removeJoin(selectBody);
    }
    
    // 3. 替换为 COUNT(*)
    // SELECT u.*, r.* -> SELECT COUNT(*)
    replaceToCountSelect(selectBody);
    
    // 4. 去除 ORDER BY（count 不需要排序）
    // （但保留子查询中的 ORDER BY）
    
    // 5. 返回优化后的 SQL
    return select.toString();
}

// 移除 JOIN
private void removeJoin(SelectBody selectBody) {
    if (selectBody instanceof PlainSelect) {
        PlainSelect plainSelect = (PlainSelect) selectBody;
        // 移除 JOIN
        plainSelect.getJoins().clear();
    }
}
```

## 五、方言实现

### 5.1 IDialect 接口

```java
public interface IDialect {
    /**
     * 构建分页 SQL
     *
     * @param originalSql 原始 SQL
     * @param offset      偏移量
     * @param limit      每页大小
     * @return 分页 SQL
     */
    DialectModel buildPaginationSql(String originalSql, long offset, long limit);
}
```

### 5.2 常用方言实现

| 数据库 | 方言类 | 分页语法 |
|--------|--------|----------|
| MySQL | MysqlDialect | `LIMIT offset, limit` |
| PostgreSQL | PostgreDialect | `LIMIT limit OFFSET offset` |
| Oracle | OracleDialect | `OFFSET offset ROWS FETCH NEXT limit ROWS ONLY` |
| SQL Server | H2Dialect / SqlServerDialect | `OFFSET offset ROWS FETCH NEXT limit ROWS ONLY` |
| H2 | H2Dialect | `LIMIT limit OFFSET offset` |
| DB2 | Db2Dialect | `FETCH FIRST limit ROWS ONLY` |

### 5.3 Mysql 方言示例

```java
public class MysqlDialect implements IDialect {
    
    @Override
    public DialectModel buildPaginationSql(String originalSql, long offset, long limit) {
        String sql = originalSql;
        
        if (offset > 0) {
            sql = String.format("%s LIMIT %d, %d", originalSql, offset, limit);
        } else {
            sql = String.format("%s LIMIT %d", originalSql, limit);
        }
        
        return new DialectModel(sql, new ArrayList<>());
    }
}
```

## 六、配置使用

### 6.1 Spring Boot 配置

```java
@Configuration
public class MybatisPlusConfig {
    
    /**
     * 分页插件配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 添加分页拦截器
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();
        
        // 设置最大每页大小（可选，默认 500）
        paginationInterceptor.setMaxLimit(500L);
        
        // 设置数据库类型（可选，自动检测）
        paginationInterceptor.setDbType(DbType.MYSQL);
        
        // 溢出总页数后是否进行处理（可选，默认 false）
        paginationInterceptor.setOverflow(false);
        
        // 是否优化 count SQL（可选，默认 true）
        paginationInterceptor.setOptimizeJoin(true);
        
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        // 可以添加其他拦截器
        // interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(...));
        
        return interceptor;
    }
}
```

### 6.2 Spring 配置

```xml
<bean class="org.mybatis.spring.annotation.MapperScan">
    <property name="basePackage" value="com.example.mapper"/>
</bean>

<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
    <property name="dataSource" ref="dataSource"/>
    <property name="mapperLocations" value="classpath*:mapper/**/*.xml"/>
    <property name="typeAliasesPackage" value="com.example.entity"/>
    <property name="plugins">
        <array>
            <bean class="com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor">
                <property name="interceptors">
                    <list>
                        <bean class="com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor">
                            <constructor-arg name="dbType" value="MYSQL"/>
                            <property name="maxLimit" value="500"/>
                        </bean>
                    </list>
                </property>
            </bean>
        </array>
    </property>
</bean>
```

## 七、使用示例

### 7.1 基本分页查询

```java
// 1. 创建分页对象
IPage<User> page = new Page<>(1, 10);  // 第1页，每页10条

// 2. 创建条件构造器
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.eq("status", 1);
wrapper.orderByDesc("create_time");

// 3. 执行分页查询
IPage<User> result = userMapper.selectPage(page, wrapper);

// 4. 获取结果
List<User> records = result.getRecords();   // 数据列表
long total = result.getTotal();              // 总记录数
long pages = result.getPages();              // 总页数
long current = result.getCurrent();           // 当前页
long size = result.getSize();                // 每页大小

System.out.println("总记录数: " + total);
System.out.println("总页数: " + pages);
System.out.println("当前页: " + current);
System.out.println("每页大小: " + size);
```

### 7.2 返回 Map 分页

```java
IPage<Map<String, Object>> page = new Page<>(1, 10);
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.select("id", "name", "email");

IPage<Map<String, Object>> result = userMapper.selectMapsPage(page, wrapper);
```

### 7.3 自定义 SQL 分页

```java
// 使用 XML 自定义 SQL
IPage<User> page = new Page<>(1, 10);
userMapper.selectMyPage(page);

// Mapper
IPage<User> selectMyPage(IPage<User> page);

// XML
<select id="selectMyPage" resultType="User">
    SELECT * FROM user WHERE status = 1
</select>
```

### 7.4 多表分页查询

```java
IPage<UserVO> page = new Page<>(1, 10);
QueryWrapper<UserVO> wrapper = new QueryWrapper<>();
wrapper.eq("u.status", 1);

IPage<UserVO> result = userMapper.selectUserPage(page, wrapper);

// Mapper
IPage<UserVO> selectUserPage(IPage<UserVO> page, Wrapper<UserVO> wrapper);

// XML
<select id="selectUserPage" resultType="UserVO">
    SELECT u.*, r.name as role_name 
    FROM user u 
    LEFT JOIN role r ON u.role_id = r.id 
    ${ew.customSqlSegment}
</select>
```

### 7.5 不查询总数

```java
// 不需要查询总数，提升性能
IPage<User> page = new Page<>(1, 10, false);  // 第三个参数为 false
page.setSearchCount(false);

IPage<User> result = userMapper.selectPage(page, wrapper);
// result.getTotal() = 0
// result.getPages() = 0
```

### 7.6 溢出处理

```java
// 配置溢出处理
PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();
paginationInterceptor.setOverflow(true);  // 允许溢出

// 当请求页码超过总页数时，自动调整为最后一页
IPage<User> page = new Page<>(999, 10);  // 假设只有 5 页
IPage<User> result = userMapper.selectPage(page, wrapper);
// 实际查询的是第 5 页
```

### 7.7 排序

```java
IPage<User> page = new Page<>(1, 10);

// 方式1：使用 OrderItem
List<OrderItem> orders = new ArrayList<>();
orders.add(new OrderItem("create_time", false));  // 倒序
orders.add(new OrderItem("id", true));            // 正序
page.setOrders(orders);

// 方式2：使用 QueryWrapper
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.orderByDesc("create_time").orderByAsc("id");

IPage<User> result = userMapper.selectPage(page, wrapper);
```

### 7.8 最大每页限制

```java
// 设置最大每页大小
IPage<User> page = new Page<>(1, 1000);  // 请求 1000 条
// 但最大限制为 100 条
PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();
paginationInterceptor.setMaxLimit(100L);

// 实际只查询 100 条
```

## 八、批量分页

### 8.1 IService 分页

```java
// IService 中的分页方法
IPage<User> page = new Page<>(1, 10);
IPage<User> result = userService.page(page);
IPage<User> result = userService.page(page, new QueryWrapper<>());

// IService 中的自定义分页
IPage<User> result = userService.selectPage(page, wrapper);
```

### 8.2 返回 IPage

```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Override
    public IPage<User> myPage(IPage<User> page, QueryWrapper<User> wrapper) {
        return baseMapper.selectMyPage(page, wrapper);
    }
}
```

## 九、性能优化

### 9.1 优化建议

```java
// 1. 合理设置每页大小
// 推荐每页 10-100 条，不要超过 500
IPage<User> page = new Page<>(1, 20);  // 推荐

// 2. 使用索引
// 确保 WHERE 条件字段有索引
wrapper.eq("status", 1);  // status 字段应有索引

// 3. 不需要总数时禁用
IPage<User> page = new Page<>(1, 10);
page.setSearchCount(false);  // 不查询总数

// 4. 优化 count SQL
PaginationInnerInterceptor interceptor = new PaginationInnerInterceptor();
interceptor.setOptimizeJoin(true);  // 去掉 JOIN

// 5. 避免深度分页
// 深度分页性能差，改为游标分页或 ID 范围分页
```

### 9.2 深度分页优化

```java
// 深度分页（性能差）
IPage<User> page = new Page<>(10000, 10);  // 第 10000 页

// 优化方式1：ID 游标
// 记录上一页最后一条的 ID
Long lastId = 1000;
IPage<User> page = new Page<>(1, 10);
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.gt("id", lastId).orderByAsc("id");
userMapper.selectPage(page, wrapper);

// 优化方式2：范围查询
wrapper.between("id", startId, endId);

// 优化方式3：子查询（不推荐，数据量大时性能仍然差）
wrapper.apply("id >= (SELECT id FROM user ORDER BY id LIMIT {0} 1)", (pageNum - 1) * pageSize);
```

## 十、常见问题

### 10.1 分页不生效

```java
// 检查1：是否配置了分页插件
@Bean
public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
    return interceptor;
}

// 检查2：参数类型是否正确
// IPage 必须是第一个参数
IPage<User> page = new Page<>(1, 10);
userMapper.selectPage(page, wrapper);

// 检查3：是否被缓存影响
// 检查是否有二级缓存干扰
```

### 10.2 Count 为 0

```java
// 检查1：searchCount 设置
page.setSearchCount(false);  // 设为 false 不会查询总数

// 检查2：数据库连接
// 确认数据库连接正常

// 检查3：SQL 错误
// 查看日志中的 SQL 是否正确
```

### 10.3 分页参数获取

```java
// PaginationInnerInterceptor 从参数中查找 IPage
public static Optional<IPage<?>> findPage(Object parameter) {
    if (parameter instanceof IPage) {
        return Optional.of((IPage<?>) parameter);
    }
    // 查找 @Param 注解的参数
    if (parameter instanceof ParamMap) {
        // ...
    }
    return Optional.empty();
}
```

## 十一、总结

MyBatis-Plus 分页插件核心特点：

1. **自动拦截** - 通过 `InnerInterceptor` 自动拦截分页查询
2. **多方言支持** - 支持 MySQL、PostgreSQL、Oracle 等主流数据库
3. **Count 优化** - 自动优化 count SQL，去除不必要的 JOIN
4. **链式 API** - 与 QueryWrapper 无缝集成
5. **配置灵活** - 支持最大每页、溢出处理等配置
6. **性能优化** - 提供多种性能优化策略
