# 导出用户日志页面 (Export User Logs)

## 1. 概述 (Overview)

本页面用于导出指定时间段内的用户操作日志。管理员可以通过设置起始时间和结束时间，查询并下载该时间段内所有用户的操作记录，包括用户名、QC号码、操作类型和操作时间。导出的数据以Excel文件格式提供。

**业务目的**：为系统管理员提供用户行为审计功能，支持按时间范围筛选和导出用户操作日志，便于追溯和分析用户活动。

**页面路径**：`/user/export.html`（通过 `/user/export` 路由访问）

> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → export()

## 2. 用户角色 (User Roles)

| 角色 | 权限说明 |
|------|----------|
| 系统管理员 | 可以访问此页面，设置时间范围并导出用户日志 |

## 3. 页面布局 (Page Layout)

页面采用居中卡片式布局，整体结构如下：

```text
┌─────────────────────────────────────────────┐
│              MODERN TERMINALS                │
│                                      [Logout]│
├─────────────────────────────────────────────┤
│                                             │
│    [起始时间输入框] To [结束时间输入框] [Export按钮] │
│                                             │
│                    [Back按钮]                │
│                                             │
└─────────────────────────────────────────────┘
```

**布局说明**：
- 顶部标题栏：显示"MODERN TERMINALS"，右上角有登出图标
- 主体区域：包含时间范围选择表单和返回按钮
- 时间输入框：两个文本输入框，分别用于输入起始时间和结束时间，格式为 `yyyy-mm-dd hh:mi:ss`
- Export按钮：点击后触发日志导出
- Back按钮：点击后返回管理主页

> 📎 Source: src/main/webapp/WEB-INF/jsp/exportPage.jsp → template

## 4. 搜索字段 (Search Fields)

| 字段名 | 类型 | 必填 | 格式要求 | 说明 |
|--------|------|------|----------|------|
| fromTime | 文本输入 | 是 | yyyy-mm-dd hh:mi:ss (19字符) | 起始时间，页面加载时自动填充当天00:00:00 |
| toTime | 文本输入 | 是 | yyyy-mm-dd hh:mi:ss (19字符) | 结束时间，页面加载时自动填充当前时间 |

**字段验证规则**：
- 不能为空
- 长度必须为19个字符
- 必须符合日期时间格式 `yyyy-mm-dd hh:mi:ss`
- 结束时间必须大于或等于起始时间

> 📎 Source: src/main/webapp/WEB-INF/jsp/exportPage.jsp → exportLogs()

## 5. 表格列 (Table Columns)

本页面不直接展示表格数据，而是将查询结果导出为Excel文件。导出的Excel文件包含以下列：

| 列名 | 字段说明 | 数据来源 |
|------|----------|----------|
| 用户名 | 执行操作的用户名称 | ShowLog.username |
| QC号码 | 质检编号 | ShowLog.qcid |
| 操作 | 操作类型描述 | ShowLog.operation |
| 时间 | 操作发生的时间 | ShowLog.loginTime |

**导出条件**：仅导出 `qcid` 不为空的日志记录，并按登录时间降序排列。

> 📎 Source: src/main/java/com/springMVC/util/ExportHandler.java → exportQCLog(); src/main/java/com/springMVC/dao/UserDaoImpl.java → getUserLogByPeriod()

## 6. 交互组件 (Interaction Components)

### 6.1 时间范围表单

**触发条件**：页面加载时自动初始化时间字段

**表单字段**：
- fromTime：起始时间输入框
- toTime：结束时间输入框

**提交逻辑**：
1. 点击"Export"按钮触发 `exportLogs()` 函数
2. 验证起始时间和结束时间的格式和有效性
3. 验证结束时间是否大于起始时间
4. 验证通过后，通过URL跳转方式请求后端接口：`exportLogs.html?fromTime={fromTime}&toTime={toTime}`
5. 后端返回Excel文件，浏览器触发下载

**关闭条件**：不适用（页面级操作）

> 📎 Source: src/main/webapp/WEB-INF/jsp/exportPage.jsp → exportLogs()

### 6.2 返回按钮

**触发条件**：点击"Back"按钮

**交互逻辑**：跳转到管理主页 `all.html`

> 📎 Source: src/main/webapp/WEB-INF/jsp/exportPage.jsp → bback()

### 6.3 登出功能

**触发条件**：点击右上角登出图标

**交互逻辑**：调用 `sh()` 函数，弹出确认对话框，确认后跳转到 `logout.html`

> 📎 Source: src/main/webapp/WEB-INF/jsp/exportPage.jsp → sh()

## 7. 用户流程 (User Flows)

### 流程1：导出用户日志

1. 用户访问导出日志页面（`/user/export.html`）
2. 页面加载时自动初始化时间字段：
   - fromTime 设置为当天 00:00:00
   - toTime 设置为当前时间
3. 用户可修改起始时间和结束时间（可选）
4. 用户点击"Export"按钮
5. 系统验证时间格式和有效性：
   - 检查是否为空
   - 检查长度是否为19字符
   - 检查格式是否符合 `yyyy-mm-dd hh:mi:ss`
   - 检查结束时间是否大于起始时间
6. 验证失败时，弹出相应错误提示并终止操作
7. 验证成功后，浏览器跳转到 `exportLogs.html?fromTime=xxx&toTime=xxx`
8. 后端查询数据库，生成Excel文件并返回
9. 浏览器触发文件下载，文件名格式为 `{yyyyMMddHHmmss}.xls`

### 流程2：返回管理主页

1. 用户点击"Back"按钮
2. 页面跳转到 `all.html`（管理主页）

### 流程3：登出系统

1. 用户点击右上角登出图标
2. 弹出确认对话框
3. 用户确认后，跳转到 `logout.html`

## 8. 业务规则 (Business Rules)

### 8.1 校验规则 (Validation)

| 规则ID | 规则描述 | 错误提示 |
|--------|----------|----------|
| V001 | 起始时间不能为空 | "Please enter the start Time with correct format 'yyyy-mm-dd hh:mi:ss'." |
| V002 | 结束时间不能为空 | "Please enter the End Time with correct format 'yyyy-mm-dd hh:mi:ss'." |
| V003 | 起始时间长度必须为19字符 | 同V001 |
| V004 | 结束时间长度必须为19字符 | 同V002 |
| V005 | 起始时间格式必须为 yyyy-mm-dd hh:mi:ss | "Invail start time." |
| V006 | 结束时间格式必须为 yyyy-mm-dd hh:mi:ss | "Invail end time." |
| V007 | 结束时间必须大于或等于起始时间 | "The end time must be larger than the start time." |

> 📎 Source: src/main/webapp/WEB-INF/jsp/exportPage.jsp → exportLogs(), strDateTime(), comptime()

### 8.2 条件显示规则 (Conditional Display)

无特殊条件显示规则。

### 8.3 数据转换规则 (Data Transformation)

| 转换项 | 转换规则 | 说明 |
|--------|----------|------|
| 时间格式化（前端初始化） | 年-月(补零)-日(补零) 时(补零):分(补零):秒(补零) | initTime() 函数确保所有时间单位均为两位数 |
| 时间格式化（后端导出） | yyyyMMddHHmmss → yyyy-MM-dd HH:mm:ss | 数据库中存储格式为 yyyyMMddHHmmss，导出时转换为可读格式 |
| 文件名生成 | 当前时间戳 yyyyMMddHHmmss + ".xls" | 使用 WebUtil.getDateTimeNow() 生成 |

> 📎 Source: src/main/webapp/WEB-INF/jsp/exportPage.jsp → initTime(); src/main/java/com/springMVC/util/ExportHandler.java → exportQCLog()

### 8.4 权限控制规则 (Permission Control)

- 此页面属于用户管理模块（user-management），需要管理员权限才能访问
- 登出功能对所有已登录用户可用

> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → export()
