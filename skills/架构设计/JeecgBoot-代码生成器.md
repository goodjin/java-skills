# JeecgBoot 代码生成器分析

## 概述

JeecgBoot 代码生成器是基于 BPM 思想实现的可视化代码生成平台，采用 **Online Coding + 代码生成器** 的双轨模式。代码生成器作为外部依赖引入 (org.jeecgframework.boot3:jeecg-codegenerate)，核心逻辑封装在第三方库中，项目通过配置调用实现功能。

## 源码分析

### 1. 架构模式

JeecgBoot 提供两种代码生成模式：

#### 模式一：Online 在线配置（低代码）
- 通过 Online 表单设计器在线配置数据模型
- 支持单表、树形表、一对多表的数据模型
- 无需编写代码，通过配置实现 CRUD 功能
- 提供在线代码编辑器，支持高级定制

#### 模式二：代码生成器（半自动化）
- 基于 Velocity 模板引擎生成代码
- 支持单表、树模型、一对多等模板
- 生成后需手工合并到项目中
- 适合复杂业务场景

### 2. 核心代码结构

```
jeecg-module-system/
├── jeecg-system-start/
│   └── src/main/java/org/jeecg/codegenerate/
│       ├── JeecgOneGUI.java              # GUI模式入口
│       └── JeecgOneToMainUtil.java       # 一对多生成器入口
```

### 3. 代码生成器入口类

#### JeecgOneToMainUtil.java
```java
// 一对多(父子表)数据模型，生成方法
public static void main(String[] args) {
    // 第一步：设置主表配置
    MainTableVo mainTable = new MainTableVo();
    mainTable.setTableName("jeecg_order_main");
    mainTable.setEntityName("GuiTestOrderMain");
    mainTable.setEntityPackage("gui");
    mainTable.setFtlDescription("GUI订单管理");
    
    // 第二步：设置子表集合配置
    List<SubTableVo> subTables = new ArrayList<SubTableVo>();
    SubTableVo po = new SubTableVo();
    po.setTableName("jeecg_order_customer");
    po.setEntityName("GuiTestOrderCustom");
    po.setForeignKeys(new String[]{"order_id"});
    subTables.add(po);
    
    // 第三步：生成代码
    new CodeGenerateOneToMany(mainTable, subTables).generateCodeFile(null);
}
```

### 4. 代码生成流程

```
┌─────────────────────────────────────────────────────────────────┐
│                      代码生成器工作流程                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐   │
│  │  配置数据模型  │ -> │  读取数据库   │ -> │  解析字段    │   │
│  │ (MainTableVo) │    │   (表结构)    │    │   (列信息)   │   │
│  └──────────────┘    └──────────────┘    └──────────────┘   │
│                                                      │         │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐   │
│  │  生成实体类   │ -> │  生成Controller│ -> │  生成Service │   │
│  │   (Entity)   │    │    (*.java)   │    │   (*.java)   │   │
│  └──────────────┘    └──────────────┘    └──────────────┘   │
│                                                                 │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐   │
│  │  生成Mapper  │ -> │  生成Vue页面  │ -> │  生成API接口 │   │
│  │   (*.xml)    │    │   (*.vue)     │    │   (*.ts)     │   │
│  └──────────────┘    └──────────────┘    └──────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 5. 核心依赖

```xml
<!-- pom.xml -->
<codegenerate.version>1.5.5</codegenerate.version>

<dependency>
    <groupId>org.jeecgframework.boot3</groupId>
    <artifactId>jeecg-codegenerate</artifactId>
    <version>${codegenerate.version}</version>
</dependency>
```

## 核心类说明

| 类名 | 说明 |
|------|------|
| `JeecgOneGUI.java` | GUI模式代码生成入口 |
| `JeecgOneToMainUtil.java` | 一对多模型代码生成入口 |
| `MainTableVo.java` | 主表配置实体 |
| `SubTableVo.java` | 子表配置实体 |
| `CodeGenerateOneToMany.java` | 一对多代码生成器实现 |

## 模板类型

JeecgBoot 代码生成器提供多套模板：

1. **单表模板** - 基础 CRUD 功能
2. **树形模板** - 支持树形结构展示
3. **一对多模板** - 父子表关系
4. **Online模板** - 在线配置表单

## 生成文件清单

### 后端 (Java)
| 文件 | 说明 |
|------|------|
| `Entity.java` | 实体类 |
| `Controller.java` | 控制器 |
| `Service.java` | 服务接口 |
| `ServiceImpl.java` | 服务实现 |
| `Mapper.java` | Mapper接口 |
| `Mapper.xml` | MyBatis映射 |

### 前端 (Vue3)
| 文件 | 说明 |
|------|------|
| `index.vue` | 列表页 |
| `form.vue` | 表单页 |
| `api.ts` | API接口 |
| `types.ts` | 类型定义 |

## 使用流程

```
┌────────────────────────────────────────────────────────────┐
│                    Online代码生成流程                        │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  1. Online建表                                             │
│     └── 配置数据库表结构、字段类型、索引                     │
│                                                            │
│  2. Online表单配置                                         │
│     └── 可视化配置表单字段、布局、校验规则                   │
│                                                            │
│  3. 代码生成                                               │
│     └── 选择模板 -> 预览代码 -> 下载/合并到项目             │
│                                                            │
│  4. 菜单配置                                               │
│     └── 将生成的菜单配置到系统菜单树                        │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

## 总结

JeecgBoot 代码生成器的核心特点是：
1. **配置化** - 通过 Online 配置实现零代码建表
2. **模板化** - 多套模板满足不同业务场景
3. **半自动化** - 生成代码需手工合并，保留定制灵活性
4. **一体化** - 前后端代码一键生成
