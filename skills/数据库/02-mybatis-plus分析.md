# MyBatis-Plus 分析

## 核心特性

### CRUD 接口
```java
// 插入
int insert(T entity);

// 根据 ID 删除
int deleteById(T entity);

// 根据 ID 更新
int updateById(T entity);

// 根据 ID 查询
T selectById(Serializable id);

// 条件查询
List<T> selectList(Wrapper<T> wrapper);
```

### 条件构造器
```java
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.lambda()
    .select(User::getName, User::getAge)
    .eq(User::getStatus, 1)
    .like(User::getName, "张")
    .orderByDesc(User::getCreateTime);

List<User> list = userMapper.selectList(wrapper);
```

### 自动填充
```java
@TableField(fill = FieldFill.INSERT)
private LocalDateTime createTime;

@TableField(fill = FieldFill.INSERT_UPDATE)
private LocalDateTime updateTime;
```

### 逻辑删除
```java
@TableLogic
private Integer deleted;
```

## 对比 MyBatis

| 特性 | MyBatis-Plus | MyBatis |
|------|-------------|---------|
| CRUD | 自动生成 | 需手写 |
| 条件构造 | 强大 | 无 |
| 分页 | 内置插件 | 需插件 |
| 自动填充 | 支持 | 无 |
| 逻辑删除 | 支持 | 无 |

## 最佳实践

```java
// 配置分页插件
@Bean
public MybatisPlusInterceptor paginationInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
    return interceptor;
}
```
