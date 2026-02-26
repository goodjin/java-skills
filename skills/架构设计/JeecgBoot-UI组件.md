# JeecgBoot UI 组件库分析

## 概述

JeecgBoot UI 组件库是基于 **Ant Design Vue 4** + **Vue 3** 构建的企业级组件库，提供了丰富的业务组件和低代码组件。核心特点包括：

- 基于 VxeTable 的高级表格组件 (JVxeTable)
- 完善的表单组件
- 业务封装组件
- 低代码在线表单组件

## 组件架构

### 目录结构

```
src/components/
├── Basic/                    # 基础组件
├── Button/                  # 按钮组件
├── CardList/                # 卡片列表
├── ClickOutSide/            # 点击外部检测
├── CodeEditor/              # 代码编辑器
├── Container/               # 容器组件
├── CountDown/               # 倒计时
├── Cropper/                 # 图片裁剪
├── Description/             # 描述列表
├── Drawer/                  # 抽屉组件
├── Dropdown/                # 下拉菜单
├── Form/                    # 表单组件
├── Icon/                    # 图标组件
├── InFilter/                # 条件过滤
├── Loading/                 # 加载组件
├── Markdown/                # Markdown渲染
├── Menu/                    # 菜单组件
├── Modal/                   # 模态框
├── Page/                    # 页面组件
├── Preview/                 # 预览组件
├── Scrollbar/               # 滚动条
├── Table/                   # 表格组件
├── Tinymce/                 # 富文本编辑器
├── Tree/                    # 树形组件
├── Upload/                  # 上传组件
├── Verify/                  # 验证码
├── chart/                   # 图表组件
├── jeecg/                   # Jeecg 业务组件
│   ├── JPrompt/             # 输入提示
│   ├── JVxeTable/           # 高级表格 ★
│   ├── OnLine/              # Online表单组件
│   └── ...
└── registerGlobComp.ts      # 全局组件注册
```

## 核心组件详解

### 1. JVxeTable 高级表格 ★★★

JVxeTable 是 JeecgBoot 最核心的组件，基于 VxeTable 封装的企业级表格组件。

#### 特点

- **行编辑** - 支持单元格在线编辑
- **列配置** - 列拖拽排序、显示/隐藏
- **大数据渲染** - 虚拟滚动，支持万级数据
- **灵活扩展** - 支持自定义组件
- **多数据源** - 支持多种数据展示方式

#### 目录结构

```
JVxeTable/
├── src/
│   ├── components/           # 子组件
│   ├── hooks/               # 组合式函数
│   ├── types/               # 类型定义
│   ├── utils/               # 工具函数
│   ├── JVxeTable.ts        # 主组件
│   ├── componentMap.ts     # 组件映射
│   ├── install.ts          # 安装配置
│   └── vxe.data.ts         # Vxe配置
├── utils.ts                 # 导出工具
├── hooks.ts                 # 导出钩子
└── types.ts                 # 类型导出
```

#### 核心 Hooks

| Hook | 说明 |
|------|------|
| `useData` | 数据管理 |
| `useColumns` | 列配置管理 |
| `useMethods` | 方法封装 |
| `useDataSource` | 数据源处理 |
| `useDragSort` | 拖拽排序 |
| `useValidateRules` | 校验规则 |
| `useLinkage` | 联动处理 |
| `useWebSocket` | WebSocket 支持 |

#### 使用示例

```vue
<template>
  <JVxeTable
    ref="jVxeTableRef"
    :height="500"
    :loading="loading"
    :data="tableData"
    :columns="tableColumns"
    @save="handleSave"
  >
    <!-- 自定义列 -->
    <template #action="{ row }">
      <a-button type="link" @click="handleEdit(row)">编辑</a-button>
    </template>
  </JVxeTable>
</template>

<script setup>
import { ref } from 'vue'
import { JVxeTable } from '@/components/jeecg/JVxeTable'

const jVxeTableRef = ref()
const loading = ref(false)
const tableData = ref([])
const tableColumns = [
  { key: 'name', title: '名称', editRender: { name: 'AInput' } },
  { key: 'status', title: '状态', editRender: { name: 'ASwitch' } },
  { key: 'action', title: '操作', slots: { default: 'action' } }
]

const handleSave = async ({ row, column, values }) => {
  // 保存逻辑
}
</script>
```

### 2. JPrompt 输入提示

提供输入时的实时提示和自动补全功能。

```vue
<template>
  <JPrompt
    v-model:value="inputValue"
    :items="promptItems"
    @select="handleSelect"
  />
</template>
```

### 3. OnLine 在线表单组件

用于渲染 Online 配置的动态表单。

```vue
<template>
  <JOnLine
    :code="formCode"
    :data="formData"
    :mode="mode"
    @change="handleChange"
  />
</template>
```

### 4. 业务通用组件

#### ExcelButton 导出按钮

```vue
<JEexcelButton
  ref="excelButtonRef"
  @click="handleExport"
  export-url="/api/demo/export"
>
  导出Excel
</JEexcelButton>
```

#### UserAvatar 用户头像

```vue
<UserAvatar :user-id="userId" :size="40" />
```

## 组件注册机制

### 全局注册

`registerGlobComp.ts` 实现全局组件自动注册：

```typescript
// 自动加载并注册 components 目录下的所有组件
import { globInstall } from 'vite-plugin-glob'
import JVxeTable from './jeecg/JVxeTable'
import JPrompt from './jeecg/JPrompt'

// 组件列表
const components = {
  JVxeTable,
  JPrompt,
  // ... 其他组件
}

// 全局注册
export function registerGlobComp(app: App) {
  Object.keys(components).forEach(key => {
    app.component(key, components[key])
  })
}
```

## 组件通信模式

```
┌─────────────────────────────────────────────────────────────────┐
│                    组件通信架构                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   ┌─────────────┐              ┌─────────────┐                 │
│   │   父组件    │ ───── props ─────> │   子组件    │                 │
│   │  (Provider) │              │  (Consumer) │                 │
│   └──────┬──────┘              └─────────────┘                 │
│          │                                                       │
│          │ emit                                                 │
│          └──────────────────────────────────> 事件处理           │
│                                                                 │
│   ┌─────────────┐              ┌─────────────┐                 │
│   │  Provide   │ <── inject ────│   Inject   │                 │
│   └─────────────┘              └─────────────┘                 │
│                                                                 │
│   ┌─────────────────────────────────────────────┐             │
│   │              EventBus /mitt                   │             │
│   │              全局事件总线                       │             │
│   └─────────────────────────────────────────────┘             │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 表单组件

### Form 组件矩阵

| 组件 | 说明 | 支持类型 |
|------|------|----------|
| `JInput` | 文本输入 | text, password |
| `JInputNumber` | 数字输入 | number |
| `JSelect` | 下拉选择 | select |
| `JTreeSelect` | 树形选择 | treeSelect |
| `JDatePicker` | 日期选择 | date, datetime, time |
| `JSwitch` | 开关 | switch |
| `JRadio` | 单选 | radio |
| `JCheckbox` | 复选 | checkbox |
| `JUpload` | 上传 | upload |
| `JEditor` | 富文本 | editor |
| `JCascader` | 级联选择 | cascader |

## 表格组件

### Table 特性

- **固定表头** - 滚动时保持表头可见
- **列拖拽** - 调整列顺序和宽度
- **行选择** - 多选/单选
- **行展开** - 支持展开行详情
- **虚拟滚动** - 大数据量优化
- **列排序** - 服务端/前端排序
- **列筛选** - 过滤条件

### 列配置类型

```typescript
interface Column {
  key: string           // 列标识
  title: string         // 列标题
  width?: number        // 列宽度
  minWidth?: number    // 最小宽度
  fixed?: 'left'|'right' // 固定列
  align?: 'left'|'center'|'right' // 对齐方式
  sortable?: boolean    // 是否可排序
  ellipsis?: boolean    // 超出省略
  editRender?: {        // 行编辑配置
    name: string        // 编辑组件名
    props?: object      // 组件属性
  }
}
```

## 权限控制

### 按钮级权限

```vue
<template>
  <!-- 根据权限显示/隐藏 -->
  <a-button v-if="hasPermission('demo:add')">新增</a-button>
  <a-button v-if="hasPermission('demo:edit')">编辑</a-button>
  <a-button v-if="hasPermission('demo:delete')">删除</a-button>
</template>

<script setup>
const { hasPermission } = usePermission()
</script>
```

### 数据级权限

```typescript
// 查询时自动注入数据权限条件
const queryParams = ref({
  ...QueryGenerator.getQueryObject(props),
})
```

## 低代码集成

### Online 表单渲染

```vue
<template>
  <OnLineForm
    :code="onlineFormCode"
    :data-id="dataId"
    :is-readonly="readonly"
    @success="handleSuccess"
  />
</template>
```

### 动态组件

```vue
<template>
  <component
    :is="componentMap[column.type]"
    v-model:value="value"
    v-bind="column.props"
  />
</template>

<script setup>
const componentMap = {
  input: JInput,
  select: JSelect,
  date: JDatePicker,
  // ...
}
</script>
```

## 主题定制

### CSS 变量

```css
:root {
  --primary-color: #1890ff;
  --success-color: #52c41a;
  --warning-color: #faad14;
  --error-color: #f5222d;
  --border-radius: 4px;
}
```

## 总结

JeecgBoot UI 组件库特点：

1. **企业级** - 面向业务场景的完整解决方案
2. **低代码** - Online 表单动态渲染
3. **高性能** - VxeTable 虚拟滚动
4. **可扩展** - 组件按需加载
5. **强类型** - 完整的 TypeScript 支持
6. **主题化** - 灵活的样式定制
