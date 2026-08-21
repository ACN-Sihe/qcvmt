# 用户列表页面 (User List) - PRD

## 1. 概述 (Overview)

本页面是系统管理员的用户管理主界面，用于查看、创建、修改和删除系统用户账户。页面以表格形式展示所有用户的基本信息（用户名、QC编号、角色），并提供分页浏览功能。管理员可以通过此页面对用户进行增删改查操作，并查看用户的操作日志。

**业务目的**：提供集中化的用户账户管理能力，确保系统访问权限的可控性和可追溯性。

**适用范围**：仅限具有 ADMIN 角色的管理员用户访问。

> 📎 Source: src/main/webapp/WEB-INF/jsp/admin.jsp → template; src/main/java/com/springMVC/control/UserControl.java → listAllUser()

## 2. 用户角色 (User Roles)

| 角色 | 权限说明 |
|------|----------|
| ADMIN（管理员） | 可查看用户列表、创建新用户、修改用户信息、删除用户、查看用户操作日志、导出日志 |
| USER（受限管理员） | 仅可查看部分配置链接（Vessel Refuel Configure、Vessel Refuel Bay Row Configure），无法访问用户管理功能 |

> 📎 Source: src/main/webapp/WEB-INF/jsp/admin.jsp → c:choose test="${ limit == 'Yes' }"; src/main/java/com/springMVC/control/UserControl.java → listAllUser()

## 3. 页面布局 (Page Layout)

页面采用居中容器布局，整体结构如下：

```text
┌─────────────────────────────────────────────────────┐
│                  MODERN TERMINALS                    │
│                                      [Logout Icon]   │
├─────────────────────────────────────────────────────┤
│  [导航链接区域 - 右对齐]                              │
│  Vessel Refuel Configure | Vessel Refuel Bay Row    │
│  Configure | Vessel Configure | Export Logs |       │
│  Set Bay Size | Color | Create User                 │
├─────────────────────────────────────────────────────┤
│  [用户列表表格]                                      │
│  ┌──────────┬──────────┬──────────┬──────────────┐  │
│  │ USERNAME │ QCNAME   │ ROLE     │ OPERATION    │  │
│  ├──────────┼──────────┼──────────┼──────────────┤  │
│  │ user1    │ QC01     │ ADMIN    │ DEL|MODIFY|LOG│ │
│  │ user2    │ HC02     │ USER     │ DEL|MODIFY|LOG│ │
│  └──────────┴──────────┴──────────┴──────────────┘  │
├─────────────────────────────────────────────────────┤
│  [分页控件 - 右对齐]                                 │
│  HOME | PRE | 1 | 2 | 3 | NEXT | END               │
└─────────────────────────────────────────────────────┘
```

**布局说明**：
- 顶部标题栏：显示 "MODERN TERMINALS"，右上角有登出图标
- 导航链接区：根据用户权限动态显示不同的配置链接
- 数据表格区：展示用户列表，包含4列（USERNAME、QCNAME、ROLE、OPERATION）
- 分页控件：位于表格下方右侧，支持首页、上一页、页码跳转、下一页、末页

> 📎 Source: src/main/webapp/WEB-INF/jsp/admin.jsp → div#d1, div#d1_head, table structure

## 4. 搜索字段 (Search Fields)

本页面**无搜索/筛选功能**。用户列表直接展示所有用户数据，通过分页控件进行浏览。

## 5. 表格列 (Table Columns)

| 列名 | 字段来源 | 说明 |
|------|----------|------|
| USERNAME | `${user.username}` | 用户登录名 |
| QCNAME | `${user.qcid}` | QC设备编号（如 QC01、HC02、C03） |
| ROLE | `${user.role}` | 用户角色（ADMIN 或 USER） |
| OPERATION | 操作链接 | 包含三个操作链接：DELETE、MODIFY、LOG |

**操作列详细说明**：
- **DELETE**：点击后弹出确认对话框，确认后调用 `/user/del.html?id={userId}` 删除用户
- **MODIFY**：跳转到用户修改页面 `/user/modify.html?id={userId}`
- **LOG**：跳转到用户操作日志页面 `/user/log.html?id={userId}`

> 📎 Source: src/main/webapp/WEB-INF/jsp/admin.jsp → c:forEach var="user", td columns; src/main/java/com/springMVC/control/UserControl.java → delUser(), modUser(), showLog()

## 6. 交互组件 (Interaction Components)

### 6.1 登出确认对话框

- **触发条件**：点击右上角登出图标
- **交互逻辑**：弹出浏览器原生 confirm 对话框，提示 "Are you sure to logout?"
- **确认操作**：跳转到 `logout.html`
- **取消操作**：关闭对话框，停留在当前页面

> 📎 Source: src/main/webapp/WEB-INF/jsp/admin.jsp → sh() function

### 6.2 删除确认对话框

- **触发条件**：点击表格中某行的 DELETE 链接
- **交互逻辑**：弹出浏览器原生 confirm 对话框，提示 "Are you sure to delete it?"
- **确认操作**：执行删除请求 `del.html?id={userId}`，删除成功后重定向回用户列表页
- **取消操作**：取消删除，停留在当前页面

> 📎 Source: src/main/webapp/WEB-INF/jsp/admin.jsp → a#del onclick; src/main/java/com/springMVC/control/UserControl.java → delUser()

### 6.3 创建用户页面（独立页面）

- **触发条件**：点击导航区的 "CREATE USER" 链接
- **目标页面**：`userDetail.jsp`
- **表单字段**：
  - USERNAME（文本输入，必填，最大长度10）
  - QCNAME（文本输入，最大长度6）
  - ROLE（单选按钮：USER / ADMIN，默认选中 USER）
  - PASSWORD（密码输入，必填，最大长度6）
  - CONFIRM PASSWORD（密码输入，必填，最大长度6）
- **提交逻辑**：POST 到 `/user/save.html`，验证通过后重定向回用户列表
- **关闭/返回**：点击 CANCEL 按钮返回用户列表页

> 📎 Source: src/main/webapp/WEB-INF/jsp/userDetail.jsp → form:form; src/main/java/com/springMVC/control/UserControl.java → addUser(), add()

### 6.4 修改用户页面（独立页面）

- **触发条件**：点击表格中某行的 MODIFY 链接
- **目标页面**：`update.jsp`
- **表单字段**：
  - USERNAME（只读文本，显示当前用户名）
  - QCNAME（文本输入，最大长度6，预填当前值）
  - ROLE（单选按钮：USER / ADMIN，根据当前角色预选中）
  - PASSWORD（密码输入，最大长度6，预填当前密码）
  - CONFIRM PASSWORD（密码输入，最大长度6，预填当前密码）
  - u_id（隐藏字段，存储用户ID）
- **提交逻辑**：POST 到 `/user/update.html`，验证通过后重定向回用户列表
- **关闭/返回**：点击 CANCEL 按钮返回用户列表页

> 📎 Source: src/main/webapp/WEB-INF/jsp/update.jsp → form:form; src/main/java/com/springMVC/control/UserControl.java → modUser(), updateUser()

### 6.5 用户日志页面（独立页面）

- **触发条件**：点击表格中某行的 LOG 链接
- **目标页面**：`log.jsp`
- **展示内容**：该用户的操作日志列表，包含列：USERNAME、QC NUMBER、TIME、OPERATION
- **时间格式**：原始数据为 `yyyyMMddHHmmss` 格式，前端转换为 `yyyy-MM-dd HH:mm:ss` 显示
- **分页**：支持分页浏览日志记录
- **返回**：页面底部有 BACK 按钮，点击后返回用户列表页

> 📎 Source: src/main/webapp/WEB-INF/jsp/log.jsp → table structure; src/main/java/com/springMVC/control/UserControl.java → showLog()

### 6.6 导出日志页面（独立页面）

- **触发条件**：点击导航区的 "Export Logs" 链接
- **目标页面**：`exportPage.jsp`
- **表单字段**：
  - fromTime（文本输入，起始时间，格式 yyyy-mm-dd hh:mi:ss，自动初始化为当天 00:00:00）
  - toTime（文本输入，结束时间，格式 yyyy-mm-dd hh:mi:ss，自动初始化为当前时间）
- **提交逻辑**：点击 Export 按钮后，验证时间格式和时间范围，然后跳转到 `exportLogs.html?fromTime={fromTime}&toTime={toTime}` 触发文件下载
- **返回**：点击 BACK 按钮返回用户列表页

> 📎 Source: src/main/webapp/WEB-INF/jsp/exportPage.jsp → exportLogs(); src/main/java/com/springMVC/control/UserControl.java → exportLogs()

## 7. 用户流程 (User Flows)

### 流程1：查看用户列表
1. 管理员登录后自动跳转到用户列表页（`/user/all.html`）
2. 页面加载时调用后端接口获取用户列表数据
3. 表格展示所有用户，每页显示固定数量（由后端分页参数控制）
4. 用户可通过分页控件切换页面

### 流程2：创建新用户
1. 在用户列表页点击 "CREATE USER" 链接
2. 跳转到用户创建页面（`userDetail.jsp`）
3. 填写表单：输入用户名、QC编号、选择角色、输入密码和确认密码
4. 点击 OK 按钮提交
5. 前端校验：检查必填项、长度限制、密码一致性
6. 后端校验：检查用户名是否已存在
7. 创建成功后重定向回用户列表页，新用户在列表中可见
8. 若失败（如用户名已存在），在页面显示错误提示

### 流程3：修改用户信息
1. 在用户列表页找到目标用户，点击其 OPERATION 列的 MODIFY 链接
2. 跳转到用户修改页面（`update.jsp`），表单预填当前用户信息
3. 修改需要更新的字段（用户名不可修改）
4. 点击 OK 按钮提交
5. 前端校验：检查密码非空、长度限制、密码一致性
6. 后端更新用户信息
7. 更新成功后重定向回用户列表页
8. 若失败，在修改页面显示错误提示

### 流程4：删除用户
1. 在用户列表页找到目标用户，点击其 OPERATION 列的 DELETE 链接
2. 弹出确认对话框："Are you sure to delete it?"
3. 点击确认后，发送删除请求 `del.html?id={userId}`
4. 后端删除用户记录
5. 删除成功后重定向回用户列表页，该用户不再显示

### 流程5：查看用户操作日志
1. 在用户列表页找到目标用户，点击其 OPERATION 列的 LOG 链接
2. 跳转到用户日志页面（`log.jsp`），展示该用户的所有操作记录
3. 日志按时间倒序排列，支持分页浏览
4. 点击页面底部的 BACK 按钮返回用户列表页

### 流程6：导出用户日志
1. 在用户列表页点击导航区的 "Export Logs" 链接
2. 跳转到导出日志页面（`exportPage.jsp`）
3. 页面自动初始化起始时间为当天 00:00:00，结束时间为当前时间
4. 用户可手动修改时间范围
5. 点击 Export 按钮
6. 前端校验：检查时间格式（yyyy-mm-dd hh:mi:ss）、时间有效性、结束时间必须大于起始时间
7. 校验通过后跳转到 `exportLogs.html?fromTime={fromTime}&toTime={toTime}`
8. 后端生成日志文件并触发浏览器下载

### 流程7：登出系统
1. 点击右上角的登出图标
2. 弹出确认对话框："Are you sure to logout?"
3. 点击确认后跳转到 `logout.html`
4. 后端清除会话，重定向到登录页

## 8. 业务规则 (Business Rules)

### 8.1 校验规则 (Validation)

**创建用户表单校验**：
- USERNAME：不能为空，最大长度10个字符
- PASSWORD：不能为空，最大长度6个字符
- CONFIRM PASSWORD：不能为空，最大长度6个字符，必须与 PASSWORD 一致
- QCNAME：最大长度6个字符

> 📎 Source: src/main/webapp/WEB-INF/jsp/userDetail.jsp → check() function

**修改用户表单校验**：
- PASSWORD：不能为空，最大长度6个字符
- CONFIRM PASSWORD：不能为空，最大长度6个字符，必须与 PASSWORD 一致
- QCNAME：最大长度6个字符

> 📎 Source: src/main/webapp/WEB-INF/jsp/update.jsp → check() function

**导出日志时间校验**：
- fromTime 和 toTime：不能为空，长度必须为19个字符（yyyy-mm-dd hh:mi:ss）
- 时间格式必须符合正则表达式：`/^(\d{1,4})(-|\/)(\d{1,2})\2(\d{1,2}) (\d{1,2}):(\d{1,2}):(\d{1,2})$/`
- 结束时间必须大于或等于起始时间

> 📎 Source: src/main/webapp/WEB-INF/jsp/exportPage.jsp → exportLogs(), strDateTime(), comptime()

**后端业务校验**：
- 创建用户时检查用户名是否已存在，若存在则返回错误提示 "The username already exists!"
- 登录时检查用户名和密码是否正确
- 登录时检查用户角色是否与选择的角色一致

> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → add(), login()

### 8.2 条件显示规则 (Conditional Display)

**导航链接权限控制**：
- 当后端返回 `limit == 'Yes'` 时（即当前用户为受限管理员），仅显示以下链接：
  - Vessel Refuel Configure
  - Vessel Refuel Bay Row Configure
- 否则（普通管理员），显示全部链接：
  - Vessel Refuel Configure
  - Vessel Refuel Bay Row Configure
  - Vessel Configure
  - Export Logs
  - Set Bay Size
  - Color
  - Create User

> 📎 Source: src/main/webapp/WEB-INF/jsp/admin.jsp → c:choose test="${ limit == 'Yes' }"; src/main/java/com/springMVC/control/UserControl.java → listAllUser()

**受限管理员判定逻辑**：
- 从配置文件读取 `limitAccount` 属性（逗号分隔的用户名列表）
- 若当前登录用户的用户名在该列表中，则设置 `limit = 'Yes'`

> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → listAllUser() lines 379-385

### 8.3 数据转换规则 (Data Transformation)

**日志时间格式化**：
- 后端存储格式：`yyyyMMddHHmmss`（如 20240115143022）
- 前端显示格式：`yyyy-MM-dd HH:mm:ss`（如 2024-01-15 14:30:22）
- 使用 JSTL fmt 标签库进行转换：`<fmt:parseDate>` 解析后 `<fmt:formatDate>` 格式化

> 📎 Source: src/main/webapp/WEB-INF/jsp/log.jsp → fmt:parseDate, fmt:formatDate

**QC编号拼接规则**：
- 用户登录时，根据输入的 QC、HC、C 编号拼接完整的 qcid：
  - 若输入 QC 编号：`qcid = "QC" + idQc`
  - 若输入 HC 编号：`qcid = "HC" + idHc`
  - 若输入 C 编号：`qcid = "C" + idC`

> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → login() line 117

### 8.4 权限控制规则 (Permission Control)

**页面访问权限**：
- 用户列表页（`admin.jsp`）：仅 ADMIN 角色可访问
- 创建用户页（`userDetail.jsp`）：仅 ADMIN 角色可访问
- 修改用户页（`update.jsp`）：仅 ADMIN 角色可访问
- 用户日志页（`log.jsp`）：仅 ADMIN 角色可访问
- 导出日志页（`exportPage.jsp`）：仅 ADMIN 角色可访问

**操作权限**：
- DELETE、MODIFY、LOG 操作：仅对 ADMIN 角色开放
- 受限管理员（limitAccount 列表中的用户）无法看到 Create User、Export Logs 等敏感操作链接

**会话管理**：
- 用户登录后，用户对象存储在 session 中（`Constants.USER_LOGIN`）
- 登出时清除 session，并重定向到登录页

> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → listAllUser(), login(), logout()
