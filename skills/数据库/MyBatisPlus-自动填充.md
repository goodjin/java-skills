# MyBatis-Plus 自动填充深入分析

## 概述

MyBatis-Plus 提供了自动填充功能，用于在插入和更新操作时自动为指定字段填充值，如创建时间、更新时间、创建人、修改人等公共字段。

## 核心类图

```
┌─────────────────────────────────────────────────────────────────────┐
│                      自动填充架构图                                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌─────────────────┐       ┌─────────────────┐                   │
│  │ MetaObjectHandler│◀──────│ DefaultMetaObj  │                   │
│  │  (填充接口)     │       │   (处理器)       │                   │
│  └─────────────────┘       └─────────────────┘                   │
│           │                                                         │
│           │           ┌─────────────────────────────────────────┐  │
│           │           │              注解定义                    │  │
│           │           │  ┌───────────┐ ┌───────────┐           │  │
│           │           │  │@TableField│ │@TableLogic│           │  │
│           │           │  │ (字段注解) │ │(逻辑删除)  │           │  │
│           │           │  └───────────┘ └───────────┘           │  │
│           │           └─────────────────────────────────────────┘  │
│           │                                                         │
│           ▼                                                         │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                    自动填充流程                                │    │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │    │
│  │  │ Insert   │  │ Update   │  │ UpdateById│  │DeleteById │  │    │
│  │  │ (插入)   │  │ (更新)   │  │(按ID更新) │  │(按ID删除) │  │    │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │    │
│  │       │              │              │              │       │    │
│  │       ▼              ▼              ▼              ▼       │    │
│  │  ┌─────────────────────────────────────────────────────┐    │    │
│  │  │            MetaObjectHandler                         │    │    │
│  │  │  insertFill()  /  updateFill()                      │    │    │
│  │  └─────────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────────┘    │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## 一、核心接口

### 1.1 MetaObjectHandler

```java
public interface MetaObjectHandler {
    
    /**
     * 是否开启插入填充
     * @param mappedStatement MappedStatement
     * @return 是否开启
     */
    default boolean openInsertFill(MappedStatement mappedStatement) {
        return true;
    }
    
    /**
     * 是否开启更新填充
     * @param mappedStatement MappedStatement
     * @return 是否开启
     */
    default boolean openUpdateFill(MappedStatement mappedStatement) {
        return true;
    }
    
    /**
     * 插入元对象字段填充
     * @param metaObject 元对象
     */
    void insertFill(MetaObject metaObject);
    
    /**
     * 更新元对象字段填充
     * @param metaObject 元对象
     */
    void updateFill(MetaObject metaObject);
}
```

### 1.2 注解定义

#### @TableField 注解

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableField {
    
    // 字段策略
    FieldStrategy fill() default FieldStrategy.DEFAULT;
    
    // 是否为插入填充字段
    boolean exist() default true;
    
    // 更多属性...
}
```

#### FieldFill 枚举

```java
public enum FieldFill {
    DEFAULT,          // 默认不填充
    INSERT,          // 插入时填充
    INSERT_UPDATE,   // 插入和更新时填充
    UPDATE           // 更新时填充
}
```

## 二、实体类配置

### 2.1 基本配置

```java
@Data
@TableName("user")
public class User {
    
    /**
     * 主键 - 自动填充
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 创建时间 - 插入时填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间 - 插入和更新时填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 创建人 - 插入时填充
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;
    
    /**
     * 更新人 - 插入和更新时填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
    
    /**
     * 版本号 - 乐观锁
     */
    @Version
    private Integer version;
    
    /**
     * 逻辑删除标志
     */
    @TableLogic
    private Integer deleted;
}
```

### 2.2 常用公共字段

| 字段 | 说明 | 填充时机 | 注解 |
|------|------|----------|------|
| `create_time` | 创建时间 | INSERT | `@TableField(fill = FieldFill.INSERT)` |
| `update_time` | 更新时间 | INSERT_UPDATE | `@TableField(fill = FieldFill.INSERT_UPDATE)` |
| `create_by` | 创建人 | INSERT | `@TableField(fill = FieldFill.INSERT)` |
| `update_by` | 更新人 | INSERT_UPDATE | `@TableField(fill = FieldFill.INSERT_UPDATE)` |
| `version` | 乐观锁 | INSERT_UPDATE | `@Version` |
| `deleted` | 逻辑删除 | 自动 | `@TableLogic` |

## 三、处理器实现

### 3.1 基本实现

```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    
    /**
     * 插入时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 1. 填充创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        
        // 2. 填充更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        // 3. 填充创建人（需要从上下文获取当前用户）
        this.strictInsertFill(metaObject, "createBy", String.class, getCurrentUser());
        
        // 4. 填充更新人
        this.strictInsertFill(metaObject, "updateBy", String.class, getCurrentUser());
        
        // 5. 填充版本号
        this.strictInsertFill(metaObject, "version", Integer.class, 1);
    }
    
    /**
     * 更新时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 1. 填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        // 2. 填充更新人
        this.strictUpdateFill(metaObject, "updateBy", String.class, getCurrentUser());
    }
    
    /**
     * 获取当前用户
     * 根据实际场景从 SecurityContext 或其他地方获取
     */
    private String getCurrentUser() {
        // 示例：Spring Security
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // return auth != null ? auth.getName() : "system";
        
        // 示例：从 ThreadLocal 获取
        return UserContext.getUsername();
    }
}
```

### 3.2 严格填充方法

```java
// 严格填充 - 只填充存在的字段
this.strictInsertFill(metaObject, "fieldName", String.class, "value");

// 使用 Supplier 延迟加载
this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime::now);

// 批量填充
this.strictInsertFill(metaObject, Arrays.asList(
    StrictFill.of("createTime", LocalDateTime.class, LocalDateTime.now()),
    StrictFill.of("createBy", String.class, "admin")
));
```

### 3.3 非严格填充

```java
// 直接设置值（不检查字段是否存在）
this.setFieldValByName("createTime", LocalDateTime.now(), metaObject);
this.setFieldValByName("createBy", "admin", metaObject);
```

## 四、自动填充原理

### 4.1 源码分析

#### Insert 方法中的填充

```java
// Insert.java
public class Insert extends AbstractMethod {
    @Override
    public MappedStatement injectMappedStatement(...) {
        // 生成插入 SQL
        String sql = SqlMethod.INSERT_ONE.format(...);
        
        // 关键：创建带有自动填充的 MappedStatement
        return this.addInsertMappedStatement(
            mapperClass, modelClass, methodName, 
            sqlSource, keyGenerator, keyProperty, keyColumn
        );
    }
}
```

#### AutoFillInterceptor

实际的自动填充是通过 `AutoFillInterceptor` 拦截器完成的：

```java
public class AutoFillInterceptor implements Interceptor {
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 获取方法
        Method method = invocation.getMethod();
        
        // 判断是插入还是更新
        if (isInsertMethod(method)) {
            // 获取参数中的实体对象
            Object parameter = invocation.getArgs();
            MetaObject metaObject = SystemMetaObject.forObject(parameter);
            
            // 执行插入填充
            if (handler.openInsertFill(ms)) {
                handler.insertFill(metaObject);
            }
        } else if (isUpdateMethod(method)) {
            // 获取参数中的实体对象
            MetaObject metaObject = SystemMetaObject.forObject(parameter);
            
            // 执行更新填充
            if (handler.openUpdateFill(ms)) {
                handler.updateFill(metaObject);
            }
        }
        
        return invocation.proceed();
    }
}
```

### 4.2 执行流程

```
┌─────────────────────────────────────────────────────────────────────┐
│                      自动填充执行流程                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  用户调用: userMapper.insert(user)                                   │
│       │                                                             │
│       ▼                                                             │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  1. MyBatis 拦截器链                                          │    │
│  │                                                               │    │
│  │  ┌─────────────────────────────────────────────────────────┐│    │
│  │  │ AutoFillInterceptor                                     ││    │
│  │  │  - 检测是否为 INSERT 或 UPDATE 方法                      ││    │
│  │  │  - 获取实体对象的 MetaObject                             ││    │
│  │  └─────────────────────────────────────────────────────────┘│    │
│  └─────────────────────────────────────────────────────────────┘    │
│       │                                                             │
│       ▼                                                             │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  2. 获取 MetaObjectHandler                                    │    │
│  │                                                               │    │
│  │  GlobalConfigUtils.getMetaObjectHandler(configuration)       │    │
│  └─────────────────────────────────────────────────────────────┘    │
│       │                                                             │
│       ▼                                                             │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  3. 执行填充方法                                              │    │
│  │                                                               │    │
│  │  if (是 INSERT 方法) {                                        │    │
│  │      handler.insertFill(metaObject);                         │    │
│  │  } else if (是 UPDATE 方法) {                                │    │
│  │      handler.updateFill(metaObject);                         │    │
│  │  }                                                            │    │
│  │                                                               │    │
│  │  // 内部执行 strictInsertFill / strictUpdateFill             │    │
│  │  // 检查字段是否存在、是否需要填充                              │    │
│  │  // 设置字段值                                                 │    │
│  └─────────────────────────────────────────────────────────────┘    │
│       │                                                             │
│       ▼                                                             │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  4. 继续执行 SQL                                              │    │
│  └─────────────────────────────────────────────────────────────┘    │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 4.3 填充判断逻辑

```java
// strictFill 核心逻辑
default MetaObjectHandler strictFill(boolean insertFill, TableInfo tableInfo, 
                                     MetaObject metaObject, List<StrictFill<?, ?>> strictFills) {
    
    // 1. 检查是否开启了填充
    if ((insertFill && tableInfo.isWithInsertFill()) || 
        (!insertFill && tableInfo.isWithUpdateFill())) {
        
        // 2. 遍历填充项
        strictFills.forEach(fill -> {
            String fieldName = fill.getFieldName();
            Class<?> fieldType = fill.getFieldType();
            Object fieldVal = fill.getFieldValue();
            
            // 3. 检查字段是否存在
            // 4. 检查是否需要填充（根据注解配置）
            // 5. 检查当前值是否为空（严格模式下）
            // 6. 设置值
            if (metaObject.hasSetter(fieldName)) {
                // 设置值
                metaObject.setValue(fieldName, fieldVal);
            }
        });
    }
    
    return this;
}
```

## 五、配置使用

### 5.1 Spring Boot 配置

```java
@Configuration
public class MybatisPlusConfig {
    
    /**
     * 自动填充处理器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MyMetaObjectHandler();
    }
}
```

### 5.2 Spring 配置

```xml
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
    <property name="dataSource" ref="dataSource"/>
    <property name="mapperLocations" value="classpath*:mapper/**/*.xml"/>
    <property name="typeAliasesPackage" value="com.example.entity"/>
    <property name="globalConfig">
        <bean class="com.baomidou.mybatisplus.core.config.GlobalConfig">
            <property name="metaObjectHandler">
                <bean class="com.example.handler.MyMetaObjectHandler"/>
            </property>
        </bean>
    </property>
</bean>
```

## 六、使用示例

### 6.1 基本使用

```java
// 1. 定义实体
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

// 2. 实现处理器
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}

// 3. 使用
User user = new User();
user.setName("张三");
userMapper.insert(user);
// 自动填充 createTime 和 updateTime
```

### 6.2 获取当前用户

```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        // 从 SecurityContext 获取当前用户
        String username = getCurrentUsername();
        
        this.strictInsertFill(metaObject, "createBy", String.class, username);
        this.strictInsertFill(metaObject, "updateBy", String.class, username);
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        String username = getCurrentUsername();
        
        this.strictUpdateFill(metaObject, "updateBy", String.class, username);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
    
    private String getCurrentUsername() {
        // 方式1：Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        
        // 方式2：自定义上下文
        return UserContextHolder.getUsername();
    }
}
```

### 6.3 使用 ThreadLocal 传递用户

```java
// 1. 定义 UserContextHolder
public class UserContextHolder {
    private static final ThreadLocal<String> USER = new ThreadLocal<>();
    
    public static void setUsername(String username) {
        USER.set(username);
    }
    
    public static String getUsername() {
        return USER.get();
    }
    
    public static void clear() {
        USER.remove();
    }
}

// 2. 在拦截器中设置用户
@Component
public class LoginInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                            Object handler) {
        String username = request.getHeader("X-User-Name");
        UserContextHolder.setUsername(username);
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        UserContextHolder.clear();
    }
}

// 3. 在 MetaObjectHandler 中使用
@Override
public void insertFill(MetaObject metaObject) {
    this.strictInsertFill(metaObject, "createBy", String.class, UserContextHolder.getUsername());
}
```

### 6.4 条件填充

```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        // 方式1：只填充 null 值
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 如果 createTime 已有值，则不会覆盖
        
        // 方式2：强制填充（无论是否有值）
        this.setFieldValByName("createTime", LocalDateTime.now(), metaObject);
    }
}
```

## 七、与其他功能结合

### 7.1 逻辑删除 + 自动填充

```java
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(fill = FieldFill.INSERT)
    private String createBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
    
    // 逻辑删除字段
    @TableLogic
    private Integer deleted;
    
    // 乐观锁
    @Version
    private Integer version;
}
```

### 7.2 多租户 + 自动填充

```java
@Component
public class TenantMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        // 填充租户ID
        String tenantId = TenantContextHolder.getTenantId();
        this.strictInsertFill(metaObject, "tenantId", String.class, tenantId);
        
        // 填充创建人
        this.strictInsertFill(metaObject, "createBy", String.class, getCurrentUser());
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        // 填充更新人
        this.strictUpdateFill(metaObject, "updateBy", String.class, getCurrentUser());
    }
}
```

## 八、常见问题

### 8.1 填充不生效

```java
// 检查1：是否注册了 MetaObjectHandler
@Bean
public MetaObjectHandler metaObjectHandler() {
    return new MyMetaObjectHandler();
}

// 检查2：注解是否添加
@TableField(fill = FieldFill.INSERT)
private LocalDateTime createTime;

// 检查3：字段名是否匹配
// 严格模式下，实体字段名必须与数据库列名对应
// 或者使用 @TableField("create_time") 显式指定

// 检查4：是否手动设置过值
// strictInsertFill 不会覆盖已存在的值
```

### 8.2 填充时机

```java
// 自动填充在以下时机触发：

// 1. INSERT 方法
userMapper.insert(user);                    // 触发 insertFill

// 2. UPDATE BY ID 方法
userMapper.updateById(user);                // 触发 updateFill

// 3. UPDATE 方法（带条件）
userMapper.update(user, wrapper);           // 触发 updateFill

// 4. IService 的批量方法
userService.save(user);                      // 触发 insertFill
userService.updateById(user);                // 触发 updateFill

// 注意：使用 Wrapper 的更新不会触发自动填充！
UpdateWrapper<User> wrapper = new UpdateWrapper<>();
wrapper.eq("id", 1).set("name", "新名字");
userMapper.update(null, wrapper);            // 不触发 fill！

// 解决方法：手动设置值
User user = new User();
user.setName("新名字");
user.setUpdateTime(LocalDateTime.now());
userMapper.update(user, wrapper);
```

### 8.3 类型问题

```java
// 时间类型
@TableField(fill = FieldFill.INSERT)
private Date createTime;          // 使用 Date

@TableField(fill = FieldFill.INSERT)
private LocalDateTime createTime; // 使用 LocalDateTime

@TableField(fill = FieldFill.INSERT)
private Long createTime;          // 使用时间戳

// 在 Handler 中需要对应
this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
```

## 九、高级用法

### 9.1 多种数据类型支持

```java
@Override
public void insertFill(MetaObject metaObject) {
    // String
    this.strictInsertFill(metaObject, "createBy", String.class, "admin");
    
    // Date
    this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
    
    // LocalDateTime
    this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
    
    // Integer
    this.strictInsertFill(metaObject, "status", Integer.class, 0);
    
    // Long
    this.strictInsertFill(metaObject, "tenantId", Long.class, 1L);
}
```

### 9.2 自定义填充策略

```java
@Component
public class CustomMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        // 使用 Supplier 延迟执行
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime::now);
        
        // 根据实体类型判断
        Object entity = metaObject.getOriginalObject();
        if (entity instanceof User) {
            User user = (User) entity;
            // 自定义逻辑
        }
        
        // 获取 TableInfo 进行判断
        TableInfo tableInfo = this.findTableInfo(metaObject);
        if ("user".equals(tableInfo.getTableName())) {
            // 针对 user 表的特殊处理
        }
    }
}
```

### 9.3 条件判断填充

```java
@Override
public void insertFill(MetaObject metaObject) {
    // 只在特定条件下填充
    String createBy = getCurrentUser();
    if (createBy != null) {
        this.strictInsertFill(metaObject, "createBy", String.class, createBy);
    }
    
    // 使用 exists 检查字段是否存在
    if (metaObject.hasSetter("tenantId")) {
        this.strictInsertFill(metaObject, "tenantId", String.class, getTenantId());
    }
}
```

## 十、总结

MyBatis-Plus 自动填充核心特点：

1. **声明式配置** - 通过 `@TableField(fill = FieldFill.INSERT)` 注解声明填充时机
2. **统一处理** - 在 `MetaObjectHandler` 中统一处理插入和更新填充
3. **多种填充策略** - 支持 INSERT、INSERT_UPDATE、UPDATE 等填充策略
4. **严格模式** - `strictInsertFill` 不会覆盖已有值，`setFieldValByName` 强制覆盖
5. **与逻辑删除/乐观锁配合** - 可与 `@TableLogic`、`@Version` 等注解配合使用
6. **灵活获取上下文** - 可从 SecurityContext、ThreadLocal 等获取当前用户信息
