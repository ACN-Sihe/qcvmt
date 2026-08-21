# 用户操作日志页面 PRD

## 1. 概述 (Overview)

用户操作日志页面用于展示指定用户的登录和操作历史记录。该页面从管理员用户列表页（admin.jsp）通过点击"LOG"链接进入，显示该用户在过去一个月内的所有登录和操作记录，支持分页浏览。

**业务目的**：为系统管理员提供审计功能，追踪特定用户的登录行为和操作历史，便于安全审计和问题排查。

**数据范围**：仅显示当前选中用户（通过 userid 参数传递）的日志记录，时间范围为最近一个月。

> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → showLog(); src/main/java/com/springMVC/dao/UserDaoImpl.java → getUserLog()

## 2. 用户角色 (User Roles)

| 角色 | 权限说明 |
|------|----------|
| 管理员 (ADMIN) | 可从用户管理页面查看任意用户的操作日志 |

> 📎 Source: src/main/webapp/WEB-INF/jsp/admin.jsp → log.html?id=${user.id}

## 3. 页面布局 (Page Layout)

页面采用居中容器布局，整体结构如下：

```text
┌─────────────────────────────────────────────┐
│              MODERN TERMINALS                │  ← 标题栏（蓝色背景，白色文字）
├─────────────────────────────────────────────┤
│  [Logout Icon]                              │  ← 右上角登出按钮
├─────────────────────────────────────────────┤
│                                             │
│  USERNAME    QC NUMBER    TIME    OPERATION │  ← 表头
│  ─────────────────────────────────────────  │
│  admin       QC001        ...      LOGIN    │  ← 日志数据行
│  admin       QC001        ...      LOGIN    │
│  ...                                        │
│                                             │
├─────────────────────────────────────────────┤
│  HOME  PRE  1  2  3  NEXT  END             │  ← 分页控件（右对齐）
│                                             │
│              [BACK]                         │  ← 返回按钮（居中）
└─────────────────────────────────────────────┘
```

> 📎 Source: src/main/webapp/WEB-INF/jsp/log.jsp → template structure

## 4. 搜索字段 (Search Fields)

本页面**无搜索/筛选功能**。日志数据由后端根据 userid 参数自动过滤，仅显示指定用户的记录。

- **过滤条件**：userid（从 URL 参数或隐藏字段获取）
- **时间范围**：自动限定为最近一个月（后端计算）

> 📎 Source: src/main/java/com/springMVC/dao/UserDaoImpl.java → getUserLog()

## 5. 表格列 (Table Columns)

| 列名 | 字段来源 | 业务含义 | 数据格式 |
|------|----------|----------|----------|
| USERNAME | log.username | 用户名 | 字符串，最大长度20 |
| QC NUMBER | log.qcid | QC编号（设备标识） | 字符串，格式如 QC001、HC001、C001 |
| TIME | log.loginTime | 操作时间 | yyyy-MM-dd HH:mm:ss（原始存储格式为 yyyyMMddHHmmss） |
| OPERATION | log.operation | 操作类型 | 字符串，如 "LOGIN" |

> 📎 Source: src/main/webapp/WEB-INF/jsp/log.jsp → table columns; src/main/java/com/springMVC/entity/ShowLog.java

## 6. 交互组件 (Interaction Components)

### 6.1 登出确认对话框

- **触发条件**：点击右上角登出图标
- **交互逻辑**：弹出浏览器原生 confirm 对话框，提示"Are you sure to logout?"
- **确认后行为**：跳转到 logout.html，执行登出操作
- **取消后行为**：无操作，停留在当前页面

> 📎 Source: src/main/webapp/WEB-INF/jsp/log.jsp → sh() function

### 6.2 分页控件

- **触发条件**：点击分页链接（HOME/PRE/页码/NEXT/END）
- **交互逻辑**：通过 URL 参数 pager.offset 传递偏移量，重新加载页面
- **分页参数**：每页显示 10 条记录
- **当前页高亮**：当前页码以红色字体显示

> 📎 Source: src/main/webapp/WEB-INF/jsp/log.jsp → pg:pager taglib

### 6.3 返回按钮

- **触发条件**：点击页面底部的 BACK 按钮
- **交互逻辑**：提交表单到 /user/all.html，返回管理员用户列表页
- **表单方法**：GET

> 📎 Source: src/main/webapp/WEB-INF/jsp/log.jsp → form:form action="${showlog}"

## 7. 用户流程 (User Flows)

### 流程 1：查看用户操作日志

1. 管理员在用户管理页面（admin.jsp）查看用户列表
2. 点击某用户行的"LOG"链接（URL: log.html?id={userid}）
3. 系统加载该用户的操作日志页面
4. 页面显示该用户最近一个月的登录和操作记录（每页10条）
5. 管理员可通过分页控件浏览更多记录
6. 点击 BACK 按钮返回用户管理页面

### 流程 2：登出系统

1. 用户在日志页面点击右上角登出图标
2. 系统弹出确认对话框："Are you sure to logout?"
3. 用户点击"确定"
4. 系统跳转到 logout.html，清除会话并重定向到登录页

## 8. 业务规则 (Business Rules)

### 8.1 校验规则 (Validation)

- **userid 参数校验**：后端尝试从请求参数或 ModelAttribute 中解析 userid，若解析失败则默认为 0
- **分页偏移量校验**：pager.offset 参数必须为整数，解析失败时默认为 0

> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → showLog() lines 444-475

### 8.2 条件显示规则 (Conditional Display)

- **空数据处理**：当 pm.datas 为空时，不渲染任何数据行，仅显示表头和分页控件
- **当前页高亮**：分页控件中当前页码以红色字体显示，其他页码为普通链接

> 📎 Source: src/main/webapp/WEB-INF/jsp/log.jsp → c:if test="${! empty pm.datas}"

### 8.3 数据转换规则 (Data Transformation)

- **时间格式转换**：
  - 数据库存储格式：yyyyMMddHHmmss（如 20240101120000）
  - 页面显示格式：yyyy-MM-dd HH:mm:ss（如 2024-01-01 12:00:00）
  - 转换方式：使用 JSTL fmt:parseDate 和 fmt:formatDate 标签

- **国际化消息**：所有界面文本通过 spring:message 标签从资源文件读取，支持多语言（英文、简体中文、繁体中文）

> 📎 Source: src/main/webapp/WEB-INF/jsp/log.jsp → fmt:parseDate/fmt:formatDate; src/main/resources/messages_en.properties

### 8.4 权限控制规则 (Permission Control)

- **访问控制**：页面受 SecurityInterceptor 保护，未登录用户将被重定向到登录页
- **数据来源限制**：日志数据仅显示指定 userid 的记录，无法跨用户查看
- **时间范围限制**：后端自动限定查询范围为最近一个月，防止数据量过大

> 📎 Source: src/main/java/com/springMVC/filter/SecurityInterceptor.java → preHandle(); src/main/java/com/springMVC/dao/UserDaoImpl.java → getUserLog()
