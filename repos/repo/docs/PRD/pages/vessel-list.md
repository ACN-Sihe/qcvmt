# 船舶列表页面 (Vessel List) - PRD

## 1. 概述 (Overview)

本页面是船舶配置管理模块的核心列表页面，用于展示和管理港口终端系统中的船舶基础配置信息。用户可以查看已配置的船舶列表、搜索特定船舶、添加新船舶、修改现有船舶配置、删除船舶记录，以及通过Excel文件批量导入船舶数据。

**业务目的**：为码头操作系统提供船舶基础数据结构（舱位/甲板、BAY、ROW、TIER范围）的配置管理功能，支撑后续的集装箱装卸作业调度。

**页面路径**：`src/main/webapp/WEB-INF/jsp/vesselManage.jsp`

**所属模块**：vessel-configuration（船舶配置）

## 2. 用户角色 (User Roles)

| 角色 | 权限说明 |
|------|----------|
| 系统管理员 | 完全访问权限：可查看、搜索、添加、修改、删除船舶配置，可导入船舶数据 |
| 普通操作员 | 仅可查看和搜索船舶配置（根据系统权限控制，当前代码未显式实现角色限制） |

## 3. 页面布局 (Page Layout)

页面采用经典的顶部标题栏 + 主体内容区布局结构：

```text
+--------------------------------------------------+
|                  MODERN TERMINALS                 |
|                                      [Logout]     |
+--------------------------------------------------+
| [搜索框] [Search按钮]    [Import链接] [Add链接]   |
+--------------------------------------------------+
| Vessel Name | Deck/Hold | BAY | Row Start | ...  |
|-------------|-----------|-----|-----------|------|
| [数据行1]                                        |
| [数据行2]                                        |
| ...                                              |
+--------------------------------------------------+
|                    [分页控件]                     |
+--------------------------------------------------+
|                   [Back 按钮]                     |
+--------------------------------------------------+
```

**主要区域**：
- **顶部标题区**：显示"MODERN TERMINALS"系统标识，右上角有登出图标
- **操作工具栏**：包含搜索输入框、搜索按钮、导入链接、添加链接
- **数据表格区**：展示船舶配置列表，每行包含船舶ID、Deck/Hold、BAY、Row范围、Tier范围及操作列
- **分页控件区**：位于表格右下方，支持首页、上一页、页码跳转、下一页、末页
- **底部操作区**：居中放置的Back按钮，返回管理主页

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselManage.jsp → template (lines 57-146)

## 4. 搜索字段 (Search Fields)

| 字段名 | 类型 | 说明 | 搜索逻辑 |
|--------|------|------|----------|
| searchcontent | 文本输入框 | 通用搜索关键字 | 模糊匹配 vesselid、deck_hold、bay 三个字段（任一字段包含关键字即匹配） |

**搜索行为**：
- 用户在搜索框输入关键字后点击"Search"按钮
- 前端通过 `encodeURIComponent()` 对关键字进行URL编码
- 跳转到 `/user/searchVessel.html?key={encodedKey}`
- 后端执行 LIKE '%keyword%' 模糊查询
- 搜索结果保留在同一个页面模板（vesselManage.jsp），但分页URL切换为 `searchVessel.html`

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselManage.jsp → search() function (lines 41-44); src/main/java/com/springMVC/dao/VesselDaoImpl.java → searchVessel() (lines 147-172)

## 5. 表格列 (Table Columns)

| 列名 | 数据来源字段 | 说明 | 示例值 |
|------|-------------|------|--------|
| Vessel name | vessel.vesselid | 船舶标识ID | "MSC001" |
| Deck/Hold | vessel.deck_hold | 甲板/舱位标识，值为"A"或"B" | "A" |
| BAY | vessel.bay | BAY编号 | "01" |
| Row Start | vessel.rowStart | 起始ROW号 | "01" |
| Row End | vessel.rowEnd | 结束ROW号 | "10" |
| Tier Start | vessel.tierStart | 起始TIER号 | "01" |
| Tier End | vessel.tierEnd | 结束TIER号 | "08" |
| Operation | - | 操作列，包含Delete和Modify两个链接 | - |

**操作列功能**：
- **Delete链接**：点击后弹出确认对话框（"confirm_delete"国际化消息），确认后跳转到 `/user/delVessel.html?id={vessel.id}` 执行删除，删除成功后重定向回列表页
- **Modify链接**：点击后跳转到 `/user/modifyVessel.html?id={vessel.id}`，进入船舶编辑页面

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselManage.jsp → table rows (lines 72-100)

## 6. 交互组件 (Interaction Components)

### 6.1 搜索功能
- **触发条件**：用户在搜索框输入内容后点击"Search"按钮
- **交互流程**：
  1. 获取搜索框值 `document.getElementById("searchcontent").value`
  2. URL编码后拼接参数：`searchVessel.html?key={encodedKey}`
  3. 页面跳转至搜索结果页（同一模板，不同数据源）
- **关闭条件**：无（页面级导航）

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselManage.jsp → search() (lines 41-44)

### 6.2 添加船舶 (Add Vessel)
- **触发条件**：点击页面右上角的"Add"链接（国际化消息key: "add"）
- **目标页面**：`vesselDetail.jsp`（船舶详情/新增表单页）
- **表单字段**：
  - Vessel name（必填，最大长度30）
  - Deck/Hold（下拉选择：A或B，必填）
  - BAY（必填，数字，最大长度3）
  - Row Start（必填，数字，最大长度2）
  - Row End（必填，数字，最大长度3）
  - Tier Start（必填，数字，最大长度2）
  - Tier End（必填，数字，最大长度2）
- **提交逻辑**：POST到 `/user/saveVessel.html`，后端校验唯一性（vesselid+deck_hold+bay组合不能重复）
- **成功行为**：重定向回船舶列表页 `/user/allVessel.html`
- **失败行为**：停留在表单页，显示错误消息"the vesselid,deck_hold,bay already exists!"

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselManage.jsp → Add link (line 69); src/main/webapp/WEB-INF/jsp/vesselDetail.jsp → form (lines 190-232); src/main/java/com/springMVC/control/CellControl.java → saveVessel() (lines 243-271)

### 6.3 修改船舶 (Modify Vessel)
- **触发条件**：点击表格操作列中的"Modify"链接
- **目标页面**：`updateVessel.jsp`（船舶编辑表单页）
- **预填充数据**：从后端加载对应ID的船舶数据，表单字段自动填充现有值
- **表单字段**：同添加船舶，所有字段带初始值
- **隐藏字段**：`id`（船舶主键，用于更新定位）
- **提交逻辑**：POST到 `/user/updateVessel.html`，后端校验唯一性（排除当前记录）
- **成功行为**：重定向回船舶列表页
- **失败行为**：停留在编辑页，显示错误消息

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselManage.jsp → Modify link (line 95); src/main/webapp/WEB-INF/jsp/updateVessel.jsp → form (lines 185-240); src/main/java/com/springMVC/control/CellControl.java → updateVessel() (lines 210-241)

### 6.4 删除船舶 (Delete Vessel)
- **触发条件**：点击表格操作列中的"Delete"链接
- **确认机制**：浏览器原生 `confirm()` 对话框，显示国际化消息"confirm_delete"
- **取消行为**：用户点击"Cancel"时，`onclick` 返回false，阻止跳转
- **确认行为**：跳转到 `/user/delVessel.html?id={vessel.id}`，后端执行物理删除，完成后重定向回列表页

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselManage.jsp → Delete link (line 94); src/main/java/com/springMVC/control/CellControl.java → delVessel() (lines 191-195)

### 6.5 导入船舶数据 (Import Vessel Info)
- **触发条件**：点击页面右上角的"Import vessel info"链接（国际化消息key: "import_vessel_info"）
- **目标页面**：`importPage.jsp`（文件上传页）
- **表单字段**：
  - filename：文件选择器（`type="file"`），接受Excel文件
- **提交逻辑**：POST到 `/user/importVessel.html`，`enctype="multipart/form-data"`
- **前端校验**：检查文件名是否为空，为空则alert提示"filename can't be empty."
- **后端处理**：解析Excel文件，批量插入船舶数据；若文件中船舶在N4系统中不存在，抛出异常并显示错误消息
- **成功行为**：停留在导入页，不显示明确成功提示（当前实现缺陷）
- **失败行为**：停留在导入页，显示错误消息（"error_no_vessel_found_in_n4"或"import_vessel_file_empty"）

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselManage.jsp → Import link (line 66); src/main/webapp/WEB-INF/jsp/importPage.jsp → form (lines 70-105); src/main/java/com/springMVC/control/UserControl.java → importVessel() (lines 524-541)

### 6.6 分页控件
- **触发条件**：点击分页链接（首页、上一页、页码、下一页、末页）
- **分页参数**：通过 `pager.offset` 参数传递偏移量
- **每页条数**：固定10条
- **URL动态切换**：
  - 正常列表：`allVessel.html`
  - 搜索结果：`searchVessel.html`（保留搜索关键字参数 `key`）
- **当前页高亮**：当前页码以红色字体显示

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselManage.jsp → pager (lines 103-141); src/main/java/com/springMVC/dao/VesselDaoImpl.java → getAllVessel() (lines 54-74)

### 6.7 登出功能
- **触发条件**：点击右上角登出图标
- **确认机制**：浏览器原生 `confirm()` 对话框，显示国际化消息"confirm_logout"
- **确认行为**：跳转到 `logout.html`

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselManage.jsp → sh() function (lines 45-49)

### 6.8 返回管理主页
- **触发条件**：点击页面底部的"Back"按钮
- **目标页面**：`admin.jsp`（管理主页，通过 `all.html` 路由）
- **行为**：直接跳转，无确认对话框

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselManage.jsp → back() function (lines 51-53)

## 7. 用户流程 (User Flows)

### 流程1：查看船舶列表
1. 用户访问 `/user/allVessel.html`
2. 后端查询所有船舶数据（按 vesselid, deck_hold, id 排序），每页10条
3. 页面渲染表格，显示第一页数据
4. 用户可通过分页控件浏览其他页

### 流程2：搜索船舶
1. 用户在搜索框输入关键字（如船舶ID部分字符）
2. 点击"Search"按钮
3. 页面跳转到 `/user/searchVessel.html?key={keyword}`
4. 后端执行模糊查询（匹配 vesselid、deck_hold、bay）
5. 页面重新渲染，显示匹配的船舶列表
6. 分页控件URL切换为 `searchVessel.html`，保留搜索关键字

### 流程3：添加新船舶
1. 用户点击"Add"链接
2. 跳转到 `vesselDetail.jsp` 表单页
3. 用户填写所有必填字段（Vessel name、Deck/Hold、BAY、Row Start/End、Tier Start/End）
4. 前端JavaScript校验：非空、长度限制、数字格式、Deck/Hold只能为A或B
5. 用户点击"OK"提交
6. 后端校验唯一性（vesselid+deck_hold+bay组合）
7. 若重复，返回表单页显示错误；若成功，保存并重定向回列表页

### 流程4：修改船舶
1. 用户在列表中点击某行的"Modify"链接
2. 跳转到 `updateVessel.jsp`，后端加载该船舶数据并预填充表单
3. 用户修改需要更新的字段
4. 前端JavaScript校验（同添加流程）
5. 用户点击"OK"提交
6. 后端校验唯一性（排除当前记录ID）
7. 若重复，返回表单页显示错误；若成功，更新并重定向回列表页

### 流程5：删除船舶
1. 用户在列表中点击某行的"Delete"链接
2. 浏览器弹出确认对话框
3. 用户点击"OK"确认
4. 页面跳转到 `/user/delVessel.html?id={id}`
5. 后端执行物理删除
6. 重定向回列表页，刷新数据

### 流程6：批量导入船舶
1. 用户点击"Import vessel info"链接
2. 跳转到 `importPage.jsp`
3. 用户点击文件选择器，选择Excel文件
4. 点击"Import"按钮
5. 前端校验文件名非空
6. 文件上传至后端，后端解析Excel并批量插入
7. 若文件中船舶在N4系统不存在，显示错误；否则静默成功（当前实现无成功提示）
8. 用户点击"Back"返回列表页验证导入结果

## 8. 业务规则 (Business Rules)

### 8.1 校验规则 (Validation)

#### 前端校验（vesselDetail.jsp / updateVessel.jsp）
| 字段 | 规则 | 错误提示 |
|------|------|----------|
| vesselid | 必填；最大长度30 | "The vesselid cannot be empty!" / "The vesselid is too long!" |
| deck_hold | 必填；长度≤1；值必须为"A"或"B" | "The deck_hold cannot be empty!" / "The deck_hold is too long!" / "The deck_hold should be 'A' or 'B' !" |
| bay | 必填；最大长度10；必须为纯数字 | "The bay cannot be empty!" / "The bay is too long!" / "The bay should be number !" |
| rowStart | 必填；最大长度2；必须为纯数字 | "The rowStart cannot be empty!" / "The rowStart is too long!" / "The rowStart should be number !" |
| rowEnd | 必填；最大长度3；必须为纯数字 | "The rowEnd cannot be empty!" / "The rowEnd is too long!" / "The rowEnd should be number !" |
| tierStart | 必填；最大长度2；必须为纯数字 | "The tierStart cannot be empty!" / "The tierStart is too long!" / "The tierStart should be number !" |
| tierEnd | 必填；最大长度3；必须为纯数字 | "The tierEnd cannot be empty!" / "The tierEnd is too long!" / "The tierEnd should be number !" |

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselDetail.jsp → checkValue() (lines 38-164); src/main/webapp/js/vmt.js → checkNumber() (lines 351-366)

#### 后端校验（CellControl.java）
| 规则 | 说明 | 错误提示 |
|------|------|----------|
| 唯一性约束 | vesselid + deck_hold + bay 组合必须唯一（新增和修改时校验） | "the vesselid,deck_hold,bay already exists!" |
| 数据库持久化 | 调用 `vesselDao.saveOrUpdateVessel()`，失败时捕获异常 | "The operation failed" |

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → saveVessel() (lines 250-254), updateVessel() (lines 218-222)

### 8.2 条件显示规则 (Conditional Display)

| 条件 | 显示内容 | 代码位置 |
|------|----------|----------|
| `${!empty pm.datas}` | 显示船舶数据表格行 | vesselManage.jsp lines 83-99 |
| `${empty pm.datas}` | 不显示任何数据行（表格为空） | vesselManage.jsp line 83 |
| `${! empty searchKey}` | 分页URL使用 `searchVessel.html` | vesselManage.jsp lines 103-105 |
| `${ empty searchKey}` | 分页URL使用 `allVessel.html` | vesselManage.jsp lines 106-108 |
| `${currentPageNumber eq pageNumber}` | 当前页码以红色字体显示 | vesselManage.jsp lines 123-125 |
| `${!empty result}` | 显示后端返回的错误消息（红色字体） | vesselDetail.jsp lines 226-228; updateVessel.jsp lines 234-236 |

### 8.3 数据转换规则 (Data Transformation)

| 字段 | 转换逻辑 | 说明 |
|------|----------|------|
| 搜索关键字 | `encodeURIComponent(key)` | 前端URL编码，防止特殊字符破坏URL结构 |
| 分页偏移量 | `Integer.parseInt(request.getParameter("pager.offset"))` | 后端解析字符串为整数，解析失败时默认0 |
| deck_hold | 枚举映射：A=甲板(Deck), B=舱位(Hold) | 前端下拉框固定两个选项，后端存储为单字符字符串 |
| 数字字段（bay, rowStart等） | 前端校验纯数字，后端存储为String类型 | 数据库字段定义为VARCHAR(10)，业务上应为数字 |

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselManage.jsp → search() (line 43); src/main/java/com/springMVC/control/CellControl.java → getVessel() (lines 172-177)

### 8.4 权限控制规则 (Permission Control)

**当前实现状态**：代码中未显式实现基于角色的权限控制（RBAC）。所有访问 `/user/*` 路径的用户均可执行增删改查操作。

**潜在扩展点**：
- admin.jsp 中存在 `${ limit == 'Yes' }` 条件判断，用于限制部分管理员的功能可见性
- 可在Controller层添加Spring Security注解或拦截器实现细粒度权限控制

> 📎 Source: src/main/webapp/WEB-INF/jsp/admin.jsp → limit condition (lines 49-64)
