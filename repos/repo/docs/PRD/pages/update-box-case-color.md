# 更新箱案颜色 (Update Box Case Color)

## 1. 概述 (Overview)

本页面用于修改系统中已配置的箱案（Box Case）的颜色设置。用户可以从颜色选择器中选择新的颜色值，并提交保存。该功能是箱案颜色管理模块的一部分，允许管理员自定义不同箱案的显示颜色，以便在系统中进行视觉区分。

**业务目的**：为已存在的箱案记录更新其关联的颜色值，支持可视化颜色选择。

**范围**：仅支持修改已有箱案的颜色，不支持新增箱案或删除操作。

## 2. 用户角色 (User Roles)

| 角色 | 权限说明 |
|------|----------|
| 系统管理员 | 可以访问颜色管理页面，查看箱案列表，点击修改链接进入本页面更新箱案颜色 |

## 3. 页面布局 (Page Layout)

页面采用居中卡片式布局，整体结构如下：

```text
┌─────────────────────────────────────────────┐
│              MODERN TERMINALS                │
│                                      [Logout]│
├─────────────────────────────────────────────┤
│                                             │
│   Box Case :  [_______________] (readonly)  │
│                                             │
│   Color    :  [■] (color preview + picker)  │
│                                             │
│         [OK]          [Cancel]              │
│                                             │
│         [Error Message Area]                │
│                                             │
└─────────────────────────────────────────────┘
```

- **顶部标题栏**：显示 "MODERN TERMINALS"，右上角有登出按钮
- **表单区域**：包含两个字段（Box Case、Color）和操作按钮
- **消息区域**：显示验证错误或操作结果

> 📎 Source: src/main/webapp/WEB-INF/jsp/updateColSet.jsp → div#d1, div#d1_head, div#d1_body

## 4. 搜索字段 (Search Fields)

本页面不包含搜索功能，为单记录编辑页面。

## 5. 表格列 (Table Columns)

本页面不包含数据表格，为表单编辑页面。

## 6. 交互组件 (Interaction Components)

### 6.1 颜色选择器 (Color Picker)

- **触发条件**：点击颜色预览输入框（`#colorShow`）
- **组件类型**：jQuery 插件 `soColorPacker` 弹出的颜色选择面板
- **交互逻辑**：
  - 点击颜色单元格后，调用回调函数 `process(colorSelected)`
  - 将选中的颜色值（如 `#FF0000`）写入隐藏字段 `#color`
  - 颜色预览框的背景色会实时更新为选中颜色
- **关闭条件**：点击颜色选择器面板上的 "close" 按钮或再次点击触发元素

> 📎 Source: src/main/webapp/WEB-INF/jsp/updateColSet.jsp → jQuery('#colorShow').soColorPacker(); src/main/webapp/js/jquery.soColorPicker-1.0.js

### 6.2 确认对话框 (Confirmation Dialog)

- **触发条件**：点击右上角登出图标
- **内容**：显示国际化消息 `confirm_logout`
- **确认行为**：跳转到 `logout.html`
- **取消行为**：保持当前页面

> 📎 Source: src/main/webapp/WEB-INF/jsp/updateColSet.jsp → function sh()

### 6.3 表单提交

- **触发条件**：点击 "OK" 按钮
- **验证逻辑**：
  - 颜色值不能为空
  - 颜色值长度不能超过 12 个字符
- **提交目标**：POST `/user/updateColSet.html`
- **成功行为**：重定向到 `/user/allColSet.html`（箱案颜色列表页）
- **失败行为**：停留在当前页面，显示错误消息 "The operation failed"

> 📎 Source: src/main/webapp/WEB-INF/jsp/updateColSet.jsp → function checkValue(), form:form; src/main/java/com/springMVC/control/CellControl.java → updateUser()

### 6.4 取消按钮

- **触发条件**：点击 "Cancel" 按钮
- **行为**：跳转到 `allColSet.html`（箱案颜色列表页）

> 📎 Source: src/main/webapp/WEB-INF/jsp/updateColSet.jsp → function back()

## 7. 用户流程 (User Flows)

### 流程 1：修改箱案颜色

1. 用户在箱案颜色列表页（`colorManage.jsp`）点击某条记录的 "Modify" 链接
2. 系统加载 `updateColSet.jsp` 页面，显示该箱案的当前信息
3. 用户点击颜色预览框，弹出颜色选择器
4. 用户在颜色选择器中选择新颜色
5. 颜色预览框背景色更新，隐藏字段存储颜色值
6. 用户点击 "OK" 按钮
7. 前端执行验证（非空、长度≤12）
8. 验证通过后，提交 POST 请求到 `/user/updateColSet.html`
9. 后端更新数据库中的颜色值
10. 更新成功后，重定向到箱案颜色列表页
11. 更新失败时，显示错误消息 "The operation failed"

### 流程 2：取消修改

1. 用户在修改页面点击 "Cancel" 按钮
2. 系统跳转到箱案颜色列表页（`allColSet.html`）
3. 不保存任何更改

### 流程 3：登出系统

1. 用户点击右上角登出图标
2. 系统弹出确认对话框
3. 用户确认后，跳转到 `logout.html`

## 8. 业务规则 (Business Rules)

### 8.1 校验规则 (Validation)

| 字段 | 规则 | 错误提示 |
|------|------|----------|
| Color | 不能为空 | "The color cannot be empty!" |
| Color | 长度不超过 12 个字符 | "The color is too long!" |

> 📎 Source: src/main/webapp/WEB-INF/jsp/updateColSet.jsp → function checkValue()

### 8.2 条件显示规则 (Conditional Display)

| 条件 | 显示内容 |
|------|----------|
| `${!empty result}` 为真 | 显示操作结果消息（红色字体） |
| 验证失败 | 显示 `#message` 区域的错误提示 |

> 📎 Source: src/main/webapp/WEB-INF/jsp/updateColSet.jsp → tr#ess, tr#message

### 8.3 数据转换规则 (Data Transformation)

| 字段 | 转换规则 |
|------|----------|
| Color | 颜色选择器返回的格式为 `#RRGGBB`（如 `#FF0000`），直接存储到数据库 |
| Box Case | 只读字段，从后端传入的 `col.boxcase` 值直接显示 |

> 📎 Source: src/main/webapp/WEB-INF/jsp/updateColSet.jsp → input#color, input#boxcase; src/main/java/com/springMVC/entity/ColSet.java

### 8.4 权限控制规则 (Permission Control)

- 本页面通过 Spring MVC 控制器访问，依赖会话认证
- 登出功能需要用户确认，防止误操作

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → modUser(), updateUser()