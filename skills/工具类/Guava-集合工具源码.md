# Guava 集合工具源码分析

## 概述

Guava 提供了丰富的不可变集合和 multimap 实现，是对 JDK 集合框架的重要补充。本文档主要分析：
- **ImmutableList**：不可变列表
- **Multimap**：一键多值映射

## 一、ImmutableList 不可变列表

### 1.1 源码结构

```
com.google.common.collect/
├── ImmutableList.java           # 抽象基类
├── RegularImmutableList.java    # 常规实现（数组存储）
├── SingletonImmutableList.java  # 单元素实现
├── EmptyImmutableList.java      # 空列表实现
├── ImmutableAsList.java         # 视图实现
└── ...
```

### 1.2 实现原理

#### 1.2.1 设计模式 - 工厂方法

`ImmutableList` 使用静态工厂方法创建实例：

```java
// 空列表 - 单例模式
public static <E> ImmutableList<E> of() {
    return (ImmutableList<E>) EMPTY;
}

// 单元素列表
public static <E> ImmutableList<E> of(E e1) {
    return new SingletonImmutableList<>(e1);
}

// 2-10 个元素 - 使用 construct 方法
public static <E> ImmutableList<E> of(E e1, E e2) {
    return construct(e1, e2);
}

// 超过 10 个元素 - 使用可变参数
public static <E> ImmutableList<E> of(E... elements) {
    return construct(elements);
}
```

#### 1.2.2 内部实现类

```
ImmutableList
    │
    ├── RegularImmutableList     # 常规实现
    │     └── 使用 Object[] array 存储元素
    │
    ├── SingletonImmutableList   # 单元素
    │     └── 存储单个元素，无数组
    │
    ├── EmptyImmutableList       # 空列表
    │     └── 特殊处理，共享实例
    │
    └── ImmutableSortedList      # 有序列表（继承自 RegularImmutableList）
```

#### 1.2.3 核心实现 - RegularImmutableList

```java
// 核心存储结构
class RegularImmutableList<E> extends ImmutableList<E> {
    private final Object[] array;
    
    RegularImmutableList(Object[] array) {
        this.array = array;
    }
    
    @Override
    public int size() {
        return array.length;
    }
    
    @Override
    public E get(int index) {
        return (E) array[index];
    }
    
    // 复用数组，无需拷贝 - 这是性能关键
    static <E> ImmutableList<E> construct(Object... elements) {
        return new RegularImmutableList<>(elements.clone());
    }
}
```

#### 1.2.4 不变性保证

```java
// ImmutableList 重写了所有修改方法，抛出 UnsupportedOperationException
@Override
public final void add(int index, E element) {
    throw new UnsupportedOperationException();
}

@Override
public final E remove(int index) {
    throw new UnsupportedOperationException();
}

@Override
public final void clear() {
    throw new UnsupportedOperationException();
}
```

### 1.3 代码示例

```java
// 创建不可变列表
ImmutableList<String> list = ImmutableList.of("a", "b", "c");

// 常用操作
list.size();                           // 3
list.get(0);                           // "a"
list.contains("a");                    // true
list.indexOf("b");                     // 1

// 不可变特性 - 以下操作会抛出异常
list.add("d");                         // UnsupportedOperationException
list.remove(0);                        // UnsupportedOperationException
list.clear();                          // UnsupportedOperationException

// 视图操作（不复制数据）
ImmutableList<String> subList = list.subList(0, 2);

// 复制为可变列表
ArrayList<String> mutableList = Lists.newArrayList(list);

// Stream 支持
list.stream().filter(s -> s.startsWith("a")).collect(Collectors.toList());

// 使用 Builder
ImmutableList<String> builder = ImmutableList.<String>builder()
    .add("a", "b", "c")
    .addAll(anotherList)
    .build();
```

### 1.4 与 JDK 对比

```java
// JDK - Arrays.asList 返回的是可变的
List<String> jdkList = Arrays.asList("a", "b", "c");
jdkList.set(0, "x");  // 允许修改！

// Guava - 真正的不可变
ImmutableList<String> guavaList = ImmutableList.of("a", "b", "c");
guavaList.set(0, "x");  // 抛出异常！
```

| 特性 | ImmutableList | Arrays.asList | Collections.unmodifiableList |
|------|---------------|---------------|-------------------------------|
| 真正不可变 | ✅ | ❌ | ✅（但基于原列表） |
| 内存优化 | ✅（内部类优化） | ❌ | ❌ |
| 丰富 API | ✅ | ❌ | ❌ |
| 线程安全 | ✅（无需同步） | ❌ | ✅ |

---

## 二、Multimap 一键多值映射

### 2.1 概述

`Multimap` 是允许一个键对应多个值的集合结构：

```
传统 Map:                        Multimap:
┌───────┬───────┐               ┌───────┬───────────┐
│  Key  │ Value │               │  Key  │  Values   │
├───────┼───────┤               ├───────┼───────────┤
│   A   │   1   │               │   A   │ [1, 2, 3] │
│   B   │   2   │               │   B   │ [4, 5]    │
│   A   │   3   │  ← 重复键      │   C   │ [6]       │
│   C   │   4   │               └───────┴───────────┘
│   B   │   5   │               (自动处理重复键)
└───────┴───────┘
```

### 2.2 源码结构

```
com.google.common.collect/
├── Multimap.java               # 主接口
├── ListMultimap.java           # 列表类型子接口
├── SetMultimap.java            # 集合类型子接口
├── SortedSetMultimap.java      # 有序集合子接口
│
├── ArrayListMultimap.java      # 基于 ArrayList
├── HashMultimap.java           # 基于 HashMap
├── LinkedListMultimap.java     # 基于 LinkedList
├── LinkedHashMultimap.java     # 基于 LinkedHashMap
├── TreeMultimap.java           # 基于 TreeMap
│
├── ImmutableListMultimap.java  # 不可变实现
├── ImmutableSetMultimap.java   # 不可变实现
│
└── Multimaps.java              # 工具类
```

### 2.3 接口设计

```java
public interface Multimap<K, V> {
    // 键值对数量（不是键的数量！）
    int size();
    
    // 获取键对应的所有值（永不为 null）
    Collection<V> get(K key);
    
    // 添加键值对
    boolean put(K key, V value);
    
    // 批量添加
    boolean putAll(K key, Iterable<? extends V> values);
    
    // 移除
    boolean remove(K key, V value);
    Collection<V> removeAll(K key);
    
    // 替换
    Collection<V> replaceValues(K key, Iterable<? extends V> values);
    
    // 视图
    Map<K, Collection<V>> asMap();
    Set<K> keySet();
    Collection<V> values();
    Collection<Entry<K, V>> entries();
}
```

### 2.4 实现原理

#### 2.4.1 AbstractMapBasedMultimap

大部分可变实现继承自 `AbstractMapBasedMultimap`：

```java
abstract class AbstractMapBasedMultimap<K, V> extends AbstractMultimap<K, V> {
    // 底层使用 Map<K, Collection<V>>
    private Map<K, Collection<V>> map;
    
    // 子类提供具体的 Collection 类型
    abstract Collection<V> createCollection();
    
    @Override
    public Collection<V> get(K key) {
        // 获取或创建 Collection
        Collection<V> collection = map.get(key);
        if (collection == null) {
            collection = createCollection();
            map.put(key, collection);
        }
        return new WrappedCollection(key, collection);
    }
}
```

#### 2.4.2 ArrayListMultimap 实现

```java
public class ArrayListMultimap<K, V> extends AbstractListMultimap<K, V> {
    // 底层 Map: HashMap
    // 值的集合: ArrayList
    private final Map<K, Collection<V>> map;
    
    @Override
    List<V> createCollection() {
        return new ArrayList<>(initialCapacity);
    }
}

// 使用
ListMultimap<String, Integer> multimap = ArrayListMultimap.create();
multimap.put("a", 1);
multimap.put("a", 2);
multimap.put("a", 3);
System.out.println(multimap.get("a"));  // [1, 2, 3]
```

#### 2.4.3 视图层 - WrappedCollection

`get()` 返回的不是原始 Collection，而是包装后的视图：

```java
class WrappedCollection<K, V> extends Collection<V> {
    private final K key;
    private final Collection<V> delegate;
    
    @Override
    public boolean add(V value) {
        // 自动将新值添加到 map 中
        delegate.add(value);
        map.put(key, delegate);  // 保持 map 同步
        return true;
    }
    
    @Override
    public boolean remove(Object value) {
        boolean removed = delegate.remove(value);
        if (delegate.isEmpty()) {
            map.remove(key);  // 空时自动移除键
        }
        return removed;
    }
}
```

### 2.5 代码示例

#### 基本用法

```java
// 创建 multimap
ListMultimap<String, String> multimap = ArrayListMultimap.create();

// 添加
multimap.put("John", "Doe");
multimap.put("John", "Smith");
multimap.put("Jane", "Doe");

// 获取 - 永不为 null
System.out.println(multimap.get("John"));     // [Doe, Smith]
System.out.println(multimap.get("Unknown"));   // [] (空列表)

// 操作
multimap.containsKey("John");   // true
multimap.containsValue("Doe");  // true
multimap.size();                // 3

// 移除
multimap.remove("John", "Doe");     // 移除单个
multimap.removeAll("John");         // 移除整个键
```

#### 各种实现

```java
// ListMultimap - 保持插入顺序，允许重复
ListMultimap<String, Integer> listMultimap = ArrayListMultimap.create();
listMultimap.put("a", 1);
listMultimap.put("a", 1);  // 允许重复
System.out.println(listMultimap.get("a"));  // [1, 1]

// SetMultimap - 去重，无序
SetMultimap<String, Integer> setMultimap = HashMultimap.create();
setMultimap.put("a", 1);
setMultimap.put("a", 1);  // 重复被忽略
System.out.println(setMultimap.get("a"));  // [1]

// SortedSetMultimap - 有序
SortedSetMultimap<String, Integer> sortedMultimap = TreeMultimap.create();
sortedMultimap.put("a", 3);
sortedMultimap.put("a", 1);
sortedMultimap.put("a", 2);
System.out.println(sortedMultimap.get("a"));  // [1, 2, 3]
```

#### 不可变 Multimap

```java
ImmutableListMultimap<String, Integer> immutable = 
    ImmutableListMultimap.of(
        "a", 1,
        "a", 2,
        "b", 3
    );

// 编译时检查
// immutable.put("c", 4);  // 编译错误！
```

#### 使用 Builder

```java
ListMultimap<String, Integer> multimap = Multimaps.newListMultimap(
    new HashMap<>(),
    () -> new ArrayList<>()
);

// 或者使用 MultimapBuilder
ListMultimap<String, Integer> builder = 
    ArrayListMultimap.<String, Integer>create()
        .put("a", 1)
        .putAll("b", Arrays.asList(2, 3, 4))
        .putAll("c", 5, 6);
```

#### 转换为 Map

```java
ListMultimap<String, Integer> multimap = ArrayListMultimap.create();
multimap.put("a", 1);
multimap.put("a", 2);

// 转换为 Map
Map<String, Collection<Integer>> mapView = multimap.asMap();
System.out.println(mapView);  // {a=[1, 2]}

// 遍历
for (Map.Entry<String, Collection<Integer>> entry : mapView.entrySet()) {
    System.out.println(entry.getKey() + ": " + entry.getValue());
}

// 获取所有值
System.out.println(multimap.values());  // [1, 2]
```

### 2.6 与 JDK 对比

```java
// JDK 方式 - 需要手动处理重复键
Map<String, List<Integer>> jdkMap = new HashMap<>();
jdkMap.computeIfAbsent("a", k -> new ArrayList<>()).add(1);
jdkMap.computeIfAbsent("a", k -> new ArrayList<>()).add(2);

// Guava 方式 - 更简洁
ListMultimap<String, Integer> multimap = ArrayListMultimap.create();
multimap.put("a", 1);
multimap.put("a", 2);
```

| 特性 | Multimap | JDK Map<K, List<V>> |
|------|----------|---------------------|
| API 简洁度 | ✅ 高 | ❌ 需手动处理 |
| get() 返回 null | ❌ 返回空集合 | ❌ 需要判空 |
| 自动清理空键 | ✅ | ❌ 需手动 |
| 多种视图 | ✅ List/Set/SortedSet | ❌ 需自行实现 |
| 不可变支持 | ✅ | ❌ |

### 2.7 性能特点

| 实现 | 适用场景 | 特点 |
|------|----------|------|
| ArrayListMultimap | 最常用，允许重复 | 内存效率高 |
| HashMultimap | 去重，无序 | 查找快 |
| LinkedListMultimap | 保持插入顺序 | 迭代效率高 |
| LinkedHashMultimap | 保持插入顺序+去重 | 兼顾两者 |
| TreeMultimap | 需要排序 | 查找 O(log n) |

### 2.8 最佳实践

```java
// 1. 优先使用子接口
ListMultimap<String, User> usersByDepartment = ArrayListMultimap.create();

// 2. 使用 Multimaps 工具类进行转换
Map<String, List<Integer>> map = ...;
ListMultimap<String, Integer> multimap = Multimaps.invertFrom(
    Multimaps.forMap(map), 
    ArrayListMultimap.create()
);

// 3. 使用 Immutable* 避免意外修改
ImmutableSetMultimap<String, Integer> immutable = ImmutableSetMultimap.of(...);
```

## 总结

Guava 集合工具提供了：

1. **ImmutableList**：真正的不可变列表，内存优化，丰富的 API
2. **Multimap**：一键多值映射，简化多值场景的处理

相比 JDK，Guava 提供了更安全、更简洁、更高效的集合操作能力。
