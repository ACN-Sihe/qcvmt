# Box Case Color List - 产品需求文档 (PRD)

## 1. 概述 (Overview)

本页面用于管理集装箱箱型（Box Case）与颜色的对应关系配置。管理员可以查看已配置的箱型颜色列表，修改现有配置的颜色值，或添加新的箱型颜色配置。该功能属于系统管理模块的一部分，主要用于终端操作界面的视觉标识配置。

**业务目的**：为不同类型的集装箱箱型分配特定的显示颜色，便于在终端操作界面中通过颜色快速识别箱型类别。

**页面路径**：`/user/allColSet.html`

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → getAllColSet()

## 2. 用户角色 (User Roles)

| 角色 | 权限说明 |
|------|----------|
| 系统管理员 | 可查看箱型颜色列表、修改现有配置的颜色、添加新箱型颜色配置、删除配置（通过导航到详情页后操作） |

## 3. 页面布局 (Page Layout)

页面采用居中容器布局，整体结构如下：

```text
┌─────────────────────────────────────────────┐
│              MODERN TERMINALS                │
│                                    [Logout]  │
├─────────────────────────────────────────────┤
│  Box Case    │    Color    │   Operation    │
├──────────────┼─────────────┼────────────────┤
│  [箱型名称]   │  [颜色预览]  │    [Modify]    │
│  [箱型名称]   │  [颜色预览]  │    [Modify]    │
│     ...      │     ...     │      ...       │
├─────────────────────────────────────────────┤
│         [Home] [Prev] [1] [2] [Next] [End]  │
├─────────────────────────────────────────────┤
│                  [Back]                      │
└─────────────────────────────────────────────┘
```

**主要区域**：
- **标题栏**：显示 "MODERN TERMINALS"，右上角有登出按钮
- **数据表格**：三列结构 - 箱型名称、颜色预览、操作链接
- **分页控件**：位于表格下方右侧，支持首页、上一页、页码、下一页、末页导航
- **返回按钮**：页面底部居中，点击返回管理员主页

> 📎 Source: src/main/webapp/WEB-INF/jsp/colorManage.jsp → template

## 4. 搜索字段 (Search Fields)

本页面**无搜索/筛选功能**。数据以分页形式展示所有已配置的箱型颜色记录。

## 5. 表格列 (Table Columns)

| 列名 | 字段说明 | 数据来源 | 显示方式 |
|------|----------|----------|----------|
| Box Case | 箱型名称 | `col.boxcase` | 文本左对齐显示 |
| Color | 颜色 | `col.color` | 只读输入框，背景色设置为颜色值，用于可视化预览 |
| Operation | 操作 | - | 包含 "Modify" 链接，点击跳转到修改页面 |

**空数据提示**：当无配置数据时，显示 "no_color_setting_data" 消息。

> 📎 Source: src/main/webapp/WEB-INF/jsp/colorManage.jsp → forEach loop

## 6. 交互组件 (Interaction Components)

### 6.1 Modify Link（修改链接）

- **触发条件**：点击表格中某行的 "Modify" 链接
- **跳转目标**：`/user/modifyColSet.html?id={col.id}`
- **目标页面**：`updateColSet.jsp` - 箱型颜色修改表单
- **传递参数**：箱型颜色记录的 ID

> 📎 Source: src/main/webapp/WEB-INF/jsp/colorManage.jsp → modify link

### 6.2 Pagination（分页控件）

- **触发条件**：点击页码、首页、上一页、下一页、末页链接
- **请求地址**：`allColSet.html?pager.offset={offset}`
- **行为**：重新加载当前页面，展示对应页的数据

> 📎 Source: src/main/webapp/WEB-INF/jsp/colorManage.jsp → pg:pager

### 6.3 Back Button（返回按钮）

- **触发条件**：点击页面底部的 "Back" 按钮
- **跳转目标**：`all.html`（管理员主页）
- **行为**：直接导航到管理员主页

> 📎 Source: src/main/webapp/WEB-INF/jsp/colorManage.jsp → back()

### 6.4 Logout（登出）

- **触发条件**：点击右上角登出图标
- **行为**：弹出确认对话框，确认后跳转到 `logout.html`

> 📎 Source: src/main/webapp/WEB-INF/jsp/colorManage.jsp → sh()

## 7. 用户流程 (User Flows)

### 流程 1：查看箱型颜色列表

1. 管理员从管理员主页点击 "Color" 链接进入本页面
2. 系统调用 `/user/allColSet.html` 接口获取分页数据
3. 页面展示箱型颜色列表，每行显示箱型名称、颜色预览和修改链接
4. 用户可通过分页控件浏览不同页的数据

### 流程 2：修改箱型颜色

1. 用户在列表中点击某行的 "Modify" 链接
2. 系统跳转到 `/user/modifyColSet.html?id={id}`
3. 进入 `updateColSet.jsp` 修改页面，显示当前箱型名称（只读）和颜色选择器
4. 用户通过颜色选择器选择新颜色
5. 点击 "OK" 提交，系统保存修改并返回列表页
6. 点击 "Cancel" 取消修改，返回列表页

### 流程 3：添加新箱型颜色配置

1. 管理员从管理员主页点击相关入口（隐含导航）
2. 进入 `colSetDetail.jsp` 新增页面
3. 填写箱型名称和选择颜色
4. 点击 "OK" 提交，系统保存并返回列表页
5. 点击 "Cancel" 取消，返回列表页

### 流程 4：返回管理员主页

1. 用户点击页面底部 "Back" 按钮
2. 跳转到 `all.html` 管理员主页

## 8. 业务规则 (Business Rules)

### 8.1 校验规则 (Validation)

**新增箱型颜色配置（colSetDetail.jsp）**：
- 箱型名称不能为空
- 箱型名称长度不能超过 20 个字符
- 颜色值不能为空
- 颜色值长度不能超过 12 个字符
- 箱型名称不能重复（后端校验）

> 📎 Source: src/main/webapp/WEB-INF/jsp/colSetDetail.jsp → checkValue(); src/main/java/com/springMVC/control/CellControl.java → saveColSet()

**修改箱型颜色（updateColSet.jsp）**：
- 颜色值不能为空
- 颜色值长度不能超过 12 个字符

> 📎 Source: src/main/webapp/WEB-INF/jsp/updateColSet.jsp → checkValue()

### 8.2 条件显示规则 (Conditional Display)

- 当数据列表为空时，显示 "no_color_setting_data" 提示信息，替代表格内容
- 分页控件始终显示，即使只有一页数据

> 📎 Source: src/main/webapp/WEB-INF/jsp/colorManage.jsp → c:choose/c:when

### 8.3 数据转换规则 (Data Transformation)

- 颜色值以 CSS 颜色格式存储（如十六进制或颜色名称），直接在输入框的 `background-color` 样式中应用，实现可视化预览
- 分页参数 `pager.offset` 从请求中解析，默认值为 0

> 📎 Source: src/main/webapp/WEB-INF/jsp/colorManage.jsp → background-color:${col.color}; src/main/java/com/springMVC/control/CellControl.java → getAllColSet()

### 8.4 权限控制规则 (Permission Control)

- 本页面仅对已登录的管理员开放（通过会话验证）
- 所有操作（查看、修改、新增）均需管理员权限
- 登出功能对所有登录用户可用

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → session-based auth
