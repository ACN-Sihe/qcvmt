# 船舶加油配置列表 (Vessel Refuel List)

## 1. 概述 (Overview)

本页面用于管理船舶加油状态配置。管理员可以查看、搜索、添加、修改和删除船舶的加油状态记录。每条记录包含船舶访问ID（Vessel Visit Id）和是否加油（Is Refuel）两个字段。

**业务目的**：为码头运营提供船舶加油状态的配置管理功能，支持对特定船舶进行加油标识的设置和维护。

**页面路径**：`/user/allVesselRefuel.html`

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → getVesselRefuel()

## 2. 用户角色 (User Roles)

| 角色 | 权限说明 |
|------|----------|
| 管理员 (ADMIN) | 可以查看所有船舶加油配置记录，执行搜索、添加、修改、删除操作 |

## 3. 页面布局 (Page Layout)

页面采用经典的头部-主体布局结构：

```text
+--------------------------------------------------+
|                  MODERN TERMINALS                 |
|                                      [Logout]     |
+--------------------------------------------------+
|  [搜索框] [Search按钮]              [Add链接]     |
+--------------------------------------------------+
| Vessel Visit Id | Is Refuel | Operation          |
+-----------------+-----------+--------------------+
| 记录1           | Yes/No    | [Delete] [Modify]  |
| 记录2           | Yes/No    | [Delete] [Modify]  |
| ...             | ...       | ...                |
+-----------------+-----------+--------------------+
|                          [Home] [Pre] [1][2]...  |
+--------------------------------------------------+
|                    [Back按钮]                     |
+--------------------------------------------------+
```

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselRefuelManage.jsp → template

## 4. 搜索字段 (Search Fields)

| 字段名 | 类型 | 说明 | 搜索逻辑 |
|--------|------|------|----------|
| searchcontent | 文本输入框 | 搜索关键字 | 模糊匹配 vesselid 或 is_refuel 字段 |

**搜索行为**：点击 Search 按钮后，跳转到 `/user/searchVesselRefuel.html?key={encodedKey}`，对 vesselid 和 is_refuel 两个字段进行模糊匹配查询。

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselRefuelManage.jsp → search(); src/main/java/com/springMVC/dao/VesselDaoImpl.java → searchVesselRefuel()

## 5. 表格列 (Table Columns)

| 列名 | 数据来源 | 说明 |
|------|----------|------|
| Vessel Visit Id | vesselRefuel.vesselid | 船舶访问ID，最大长度30字符 |
| Is Refuel | vesselRefuel.is_refuel | 是否加油，值为 "Yes" 或 "No" |
| Operation | - | 操作列，包含 Delete 和 Modify 链接 |

**操作列内容**：
- **Delete**：点击后弹出确认对话框，确认后调用 `/user/delVesselRefuel.html?id={id}` 删除记录
- **Modify**：点击后跳转到 `/user/modifyVesselRefuel.html?id={id}` 进入编辑页面

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselRefuelManage.jsp → table; src/main/java/com/springMVC/entity/VesselRefuel.java

## 6. 交互组件 (Interaction Components)

### 6.1 添加/修改表单页面 (vesselRefuelDetail.jsp)

**触发条件**：
- 点击列表页的 "Add" 链接 → 跳转到 `/user/addVesselRefuel.html`
- 点击列表页某行的 "Modify" 链接 → 跳转到 `/user/modifyVesselRefuel.html?id={id}`

**表单字段**：

| 字段名 | 类型 | 必填 | 校验规则 |
|--------|------|------|----------|
| vesselid | 文本输入框 | 是 | 不能为空；最大长度30字符 |
| is_refuel | 下拉选择框 | 是 | 只能选择 "Yes" 或 "No"；最大长度3字符 |

**提交逻辑**：
- 点击 OK 按钮触发表单提交，POST 到 `/user/updateVesselRefuelStatus.html`
- 提交前执行客户端校验函数 `checkValue()`
- 如果存在 id 参数则为更新操作，否则为新增操作
- 成功后重定向回列表页 `/user/allVesselRefuel.html`
- 失败时显示错误信息 "The operation failed"

**关闭条件**：
- 点击 Cancel 按钮返回到 `/user/allVesselRefuel.html`

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselRefuelDetail.jsp → checkValue(); src/main/java/com/springMVC/control/CellControl.java → updateVesselRefuelStatus()

### 6.2 删除确认对话框

**触发条件**：点击列表中的 Delete 链接

**确认文案**：使用国际化消息 `confirm_delete`

**提交逻辑**：确认后 GET 请求 `/user/delVesselRefuel.html?id={id}`，成功后重定向回列表页

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselRefuelManage.jsp → del link onclick

### 6.3 退出登录

**触发条件**：点击右上角 Logout 图标

**确认文案**：使用国际化消息 `confirm_logout`

**提交逻辑**：确认后跳转到 `logout.html`

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselRefuelManage.jsp → sh()

## 7. 用户流程 (User Flows)

### 7.1 查看列表流程
1. 用户访问 `/user/allVesselRefuel.html`
2. 系统加载所有船舶加油配置记录（每页10条）
3. 用户可查看分页导航进行翻页

### 7.2 搜索流程
1. 用户在搜索框输入关键字
2. 点击 Search 按钮
3. 系统跳转到搜索页面，显示匹配 vesselid 或 is_refuel 的记录
4. 用户可继续翻页浏览搜索结果

### 7.3 添加记录流程
1. 用户点击 Add 链接
2. 系统跳转到添加表单页面
3. 用户填写 Vessel Id 和 Is Refuel 字段
4. 点击 OK 按钮提交
5. 系统进行客户端校验
6. 校验通过后提交到后端
7. 成功后重定向回列表页，新记录显示在列表中

### 7.4 修改记录流程
1. 用户点击某行的 Modify 链接
2. 系统跳转到编辑表单页面，预填充当前记录数据
3. 用户修改 Vessel Id 或 Is Refuel 字段
4. 点击 OK 按钮提交
5. 系统进行客户端校验
6. 校验通过后提交到后端更新
7. 成功后重定向回列表页

### 7.5 删除记录流程
1. 用户点击某行的 Delete 链接
2. 系统弹出确认对话框
3. 用户确认删除
4. 系统调用删除接口
5. 成功后重定向回列表页，该记录不再显示

### 7.6 返回列表流程
1. 用户在列表页点击 Back 按钮
2. 系统跳转到 `/user/all.html`（管理员主页）

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselRefuelManage.jsp → back(); src/main/java/com/springMVC/control/CellControl.java

## 8. 业务规则 (Business Rules)

### 8.1 校验规则 (Validation)

| 字段 | 规则 | 错误提示 |
|------|------|----------|
| vesselid | 不能为空 | "Vessel Visit Id cannot be empty!" |
| vesselid | 最大长度30字符 | "Vessel Visit Id is too long!" |
| is_refuel | 不能为空 | "Is Refuel cannot be empty!" |
| is_refuel | 最大长度3字符 | "Is Refuel is too long!" |
| is_refuel | 值必须为 "Yes" 或 "No" | "Is Refuel should be 'Yes' or 'No'!" |

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselRefuelDetail.jsp → checkValue()

### 8.2 条件显示规则 (Conditional Display)

| 条件 | 显示内容 |
|------|----------|
| 列表有数据 (`pm.datas` 非空) | 显示数据行 |
| 列表无数据 | 不显示任何数据行 |
| 存在搜索关键字 (`searchKey` 非空) | 分页链接指向 `searchVesselRefuel.html` |
| 无搜索关键字 | 分页链接指向 `allVesselRefuel.html` |
| 编辑模式 (`vesselRefuel` 对象非空) | 表单预填充现有数据，is_refuel 下拉框根据当前值设置 selected |
| 新增模式 (`vesselRefuel` 对象为空) | 表单为空，is_refuel 默认选中第一个选项 "Yes" |

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselRefuelManage.jsp → c:forEach, c:if; src/main/webapp/WEB-INF/jsp/vesselRefuelDetail.jsp → c:choose

### 8.3 数据转换规则 (Data Transformation)

| 字段 | 转换规则 |
|------|----------|
| is_refuel | 枚举值映射：数据库存储 "Yes"/"No"，前端下拉框直接显示相同值 |
| 分页页码 | 当前页码用红色字体高亮显示 |

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselRefuelDetail.jsp → select options; src/main/webapp/WEB-INF/jsp/vesselRefuelManage.jsp → pg:pages

### 8.4 权限控制规则 (Permission Control)

| 资源 | 权限要求 |
|------|----------|
| 访问列表页 | 需要 ADMIN 角色登录 |
| 添加记录 | 需要 ADMIN 角色登录 |
| 修改记录 | 需要 ADMIN 角色登录 |
| 删除记录 | 需要 ADMIN 角色登录 |

**注意**：权限控制在后端通过 Session 中的用户信息进行验证，前端无显式权限检查。

> 📎 Source: src/main/java/com/springMVC/filter/SecurityInterceptor.java; src/main/java/com/springMVC/control/CellControl.java
