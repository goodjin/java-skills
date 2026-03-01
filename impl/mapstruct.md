# MapStruct 项目分析

## 项目简介
MapStruct 是一个 Java 注解处理器，用于在编译时生成类型安全的 bean 映射代码。避免运行时反射开销，提供高效的对象转换能力。

## 核心类结构

### 1. @Mapper 注解
- **位置**: `org.mapstruct.Mapper`
- **职责**: 标记接口/抽象类为映射器
- **属性**: `componentModel`, `unmappedTargetPolicy`, `uses` 等

### 2. @Mapping 注解
- **位置**: `org.mapstruct.Mapping`
- **职责**: 配置属性映射规则
- **属性**: `source`, `target`, `dateFormat`, `qualifiedBy` 等

### 3. @MappingTarget
- **位置**: `org.mapstruct.MappingTarget`
- **职责**: 指定目标对象进行更新

### 4. @Context
- **位置**: `org.mapstruct.Context`
- **职责**: 注入共享参数到映射方法

### 5. Mapper 接口
- **职责**: 生成的映射器实现
- **方法**: `sourceToTarget()`, `targetToSource()`

## 设计模式

### 1. 注解驱动配置
- 使用注解定义映射规则
- 编译时生成实现类

### 2. 策略模式（Strategy）
- `Converter` 接口自定义转换逻辑
- `Formatter` 接口格式化数据

### 3. 模板方法模式
- 生成的映射器继承公共基类

### 4. 工厂模式
- 使用 `ObjectFactory` 创建目标对象

## 代码技巧

### 1. 基本映射
```java
@Mapper
public interface CarMapper {
    CarMapper INSTANCE = Mappers.getMapper(CarMapper.class);

    CarDto carToCarDto(Car car);
}
```

### 2. 属性映射
```java
@Mapper
public interface OrderMapper {
    @Mapping(source = "orderId", target = "id")
    @Mapping(source = "createTime", target = "createDate", dateFormat = "yyyy-MM-dd")
    @Mapping(source = "customer.name", target = "customerName")
    OrderDto orderToOrderDto(Order order);
}
```

### 3. 更新已有对象
```java
@Mapper
public interface CarMapper {
    void updateCarEntity(CarDto dto, @MappingTarget Car entity);
}
```

### 4. 自定义映射方法
```java
@Mapper
public interface CarMapper {
    default CarStatus mapStatus(String status) {
        return CarStatus.valueOf(status.toUpperCase());
    }

    @Mapping(source = "price", target = "price", qualifiedByName = "formatPrice")
    CarDto carToCarDto(Car car);

    @Named("formatPrice")
    default String formatPrice(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP) + " USD";
    }
}
```

### 5. 使用Context共享参数
```java
@Mapper
public interface CarMapper {
    @Mapping(source = "vin", target = "vin", context = "defaultPrefix")
    CarDto carToCarDto(Car car, @Context String prefix);

    default String resolveVin(String vin, @Context String prefix) {
        return prefix + vin;
    }
}
```

### 6. 注入Spring Bean
```java
@Mapper(componentModel = "spring")
public interface CarMapper {
    @Mapping(source = "ownerId", target = "owner", qualifiedBy = Lookup.class)
    CarDto carToCarDto(Car car);

    @Qualifier
    @Named("Lookup")
    default Owner lookupOwner(String ownerId) {
        return ownerService.findById(ownerId);
    }
}
```

## 性能优化要点

1. **编译时生成**: 无运行时反射开销
2. **正确使用componentModel**: 选择合适的生命周期管理
3. **复用映射器**: 使用`uses`属性复用已有转换逻辑
4. **避免嵌套方法**: 复杂的嵌套映射拆分为多个简单映射器
5. **批量映射**: 大量对象使用`mapAll`或流式处理
