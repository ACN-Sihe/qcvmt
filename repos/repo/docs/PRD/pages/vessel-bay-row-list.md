# 船舶舱位行管理 (Vessel Bay Row List)

## 1. 概述 (Overview)

本页面用于管理船舶加油舱位行配置数据。用户可以查看、搜索、添加、修改和删除船舶的舱位行信息，包括船舶访问ID、甲板/货舱标识、舱位号、起始行、结束行、起始层、结束层等字段。该功能属于码头管理系统中的船舶加油配置模块。

**业务目的**: 为船舶加油作业提供舱位行的空间配置数据，确保加油操作能够准确定位到具体的舱位位置。

**页面路径**: `src/main/webapp/WEB-INF/jsp/vesselColorManage.jsp`

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselColorManage.jsp → template

## 2. 用户角色 (User Roles)

| 角色 | 权限说明 |
|------|----------|
| 系统管理员 | 可以查看所有舱位行配置、搜索、添加新配置、修改现有配置、删除配置 |
| 普通操作员 | 仅可查看舱位行配置列表（根据admin.jsp中的limit参数判断） |

## 3. 页面布局 (Page Layout)

页面采用经典的JSP表格布局结构：

```text
+--------------------------------------------------+
| MODERN TERMINALS (蓝色标题栏)              [退出] |
+--------------------------------------------------+
| [搜索框] [Search按钮]                    [添加]   |
+--------------------------------------------------+
| Vessel Visit Id | Deck/Hold | BAY | Start Row |  |
| End Row | Start Tier | End Tier | Operation     |
+--------------------------------------------------+
| 数据行1: 各字段值                          [删除][修改]|
| 数据行2: 各字段值                          [删除][修改]|
| ...                                            |
+--------------------------------------------------+
| 分页导航: [首页] [上一页] [页码] [下一页] [末页]   |
+--------------------------------------------------+
|                      [返回]                       |
+--------------------------------------------------+
```

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselColorManage.jsp → table structure (lines 60-98)

## 4. 搜索字段 (Search Fields)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| searchcontent | 文本输入框 | 通用搜索关键字，通过GET参数`key`传递给后端API |

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselColorManage.jsp → input#searchcontent (line 63)

## 5. 表格列 (Table Columns)

| 列名 | 数据来源 | 说明 |
|------|----------|------|
| Vessel Visit Id | vesselCol.vesselid | 船舶访问ID |
| Deck/Hold | vesselCol.deck_hold | 甲板/货舱标识（A或B） |
| BAY | vesselCol.bay | 舱位号 |
| Start Row | vesselCol.rowStart | 起始行号 |
| End Row | vesselCol.rowEnd | 结束行号 |
| Start Tier | vesselCol.tierStart | 起始层号 |
| End Tier | vesselCol.tierEnd | 结束层号 |
| Operation | - | 操作列，包含"删除"和"修改"链接 |

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselColorManage.jsp → table headers (lines 70-77), data rows (lines 83-89)

## 6. 交互组件 (Interaction Components)

### 6.1 搜索功能
- **触发条件**: 点击"Search"按钮
- **逻辑**: 获取搜索框内容，URL编码后跳转到`searchVesselColor.html?key={encodedKey}`
- **关闭条件**: 页面跳转

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselColorManage.jsp → search() function (lines 41-44)

### 6.2 添加舱位行
- **触发条件**: 点击"Add"链接
- **目标页面**: `vesselColorDetail.jsp` (通过`addVesselCol.html`)
- **表单字段**: 见下方表单详情

### 6.3 修改舱位行
- **触发条件**: 点击某行的"Modify"链接
- **目标页面**: `vesselColorDetail.jsp` (通过`modifyVesselCol.html?id={id}`)
- **传递参数**: 记录ID

### 6.4 删除舱位行
- **触发条件**: 点击某行的"Delete"链接
- **确认对话框**: 显示国际化消息`confirm_delete`
- **目标API**: `delVesselCol.html?id={id}`
- **关闭条件**: 确认后页面跳转

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselColorManage.jsp → delete link (line 91)

### 6.5 返回按钮
- **触发条件**: 点击底部"Back"按钮
- **目标页面**: `admin.jsp` (通过`all.html`)

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselColorManage.jsp → back() function (lines 51-53)

### 6.6 退出登录
- **触发条件**: 点击右上角退出图标
- **确认对话框**: 显示国际化消息`confirm_logout`
- **目标**: `logout.html`

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselColorManage.jsp → sh() function (lines 45-49)

### 6.7 添加/修改表单 (vesselColorDetail.jsp)

**表单字段**:

| 字段名 | 类型 | 必填 | 验证规则 |
|--------|------|------|----------|
| vesselid | 文本输入 | 是 | 非空，最大长度30 |
| deck_hold | 下拉选择 | 是 | 只能选A或B |
| bay | 文本输入 | 是 | 非空，最大长度10，必须为数字 |
| rowStart | 文本输入 | 是 | 非空，最大长度2，必须为数字 |
| rowEnd | 文本输入 | 是 | 非空，最大长度3，必须为数字 |
| tierStart | 文本输入 | 否* | 可选，但若填写则最大长度2，必须为数字 |
| tierEnd | 文本输入 | 否* | 可选，但若填写则最大长度3，必须为数字 |
| id | 隐藏字段 | - | 修改时携带记录ID |

*\*tierStart和tierEnd必须同时填写或同时为空*

**验证规则详情**:
1. vesselid: 非空，长度≤30
2. deck_hold: 非空，长度=1，值必须为'A'或'B'
3. bay: 非空，长度≤10，必须为纯数字
4. rowStart: 非空，长度≤2，必须为纯数字
5. rowEnd: 非空，长度≤3，必须为纯数字
6. rowStart ≤ rowEnd
7. rowStart和rowEnd必须同为奇数或同为偶数
8. tierStart: 若填写，长度≤2，必须为纯数字
9. tierEnd: 若填写，长度≤3，必须为纯数字
10. tierStart和tierEnd必须同时填写或同时为空
11. 若tierStart和tierEnd都填写，两者都必须为偶数
12. tierStart ≤ tierEnd

**提交逻辑**: 
- 表单提交前执行`checkValue()`验证函数
- 验证失败时显示红色错误消息在`#message`区域
- 验证通过后POST提交到`/user/saveVesselCol.html`
- 服务器返回结果可能显示在`#ess`区域

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselColorDetail.jsp → checkValue() function (lines 40-191), form fields (lines 212-265, 269-311)

## 7. 用户流程 (User Flows)

### 流程1: 查看舱位行列表
1. 用户访问`allVesselCol.html`
2. 系统加载所有舱位行配置数据
3. 以表格形式展示，支持分页浏览

### 流程2: 搜索舱位行
1. 用户在搜索框输入关键字
2. 点击"Search"按钮
3. 系统跳转到`searchVesselColor.html?key={keyword}`
4. 显示匹配的舱位行记录

### 流程3: 添加新舱位行
1. 用户点击"Add"链接
2. 跳转到`vesselColorDetail.jsp`（空白表单）
3. 用户填写所有必填字段
4. 点击"OK"提交
5. 前端执行验证，若有错误显示提示
6. 验证通过后提交到`/user/saveVesselCol.html`
7. 保存成功后返回列表页

### 流程4: 修改舱位行
1. 用户在列表中点击某行的"Modify"链接
2. 跳转到`vesselColorDetail.jsp?id={id}`
3. 表单预填充该记录的现有数据
4. 用户修改字段值
5. 点击"OK"提交
6. 前端执行验证
7. 验证通过后提交到`/user/saveVesselCol.html`
8. 更新成功后返回列表页

### 流程5: 删除舱位行
1. 用户在列表中点击某行的"Delete"链接
2. 弹出确认对话框
3. 用户确认后跳转到`delVesselCol.html?id={id}`
4. 删除成功后返回列表页

### 流程6: 返回列表/管理页
1. 用户点击底部"Back"按钮
2. 跳转到`admin.jsp`（通过`all.html`）

## 8. 业务规则 (Business Rules)

### 8.1 校验规则 (Validation)

| 字段 | 规则 | 错误提示 |
|------|------|----------|
| vesselid | 非空，长度≤30 | "Vessel Visit Id cannot be empty!" / "Vessel Visit Id is too long!" |
| deck_hold | 非空，长度=1，值为A或B | "Deck Hold cannot be empty!" / "Deck Hold is too long!" / "Deck Hold should be 'A' or 'B'!" |
| bay | 非空，长度≤10，纯数字 | "The bay cannot be empty!" / "The bay is too long!" / "The bay should be number!" |
| rowStart | 非空，长度≤2，纯数字 | "Start Row cannot be empty!" / "Start Row is too long!" / "Start Row should be number!" |
| rowEnd | 非空，长度≤3，纯数字 | "End Row cannot be empty!" / "End Row is too long!" / "End Row should be number!" |
| rowStart vs rowEnd | rowStart ≤ rowEnd | "Start Row can't be larger than End Row!" |
| rowStart vs rowEnd | 同奇偶性 | "Start Row, End Row should be both odd or even number!" |
| tierStart | 若填写，长度≤2，纯数字 | "The tierStart is too long!" / "The tierStart should be number !" |
| tierEnd | 若填写，长度≤3，纯数字 | "The tierEnd is too long!" / "The tierEnd should be number !" |
| tierStart/tierEnd | 必须同时填写或同时为空 | "Please input Start Tier and End Tier together or both are blank." |
| tierStart/tierEnd | 若都填写，必须都为偶数 | "Start Tier, End Tier should be even number!" |
| tierStart vs tierEnd | 若都填写，tierStart ≤ tierEnd | "Start Tier can't be larger than End Tier!" |

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselColorDetail.jsp → checkValue() function (lines 40-191)

### 8.2 条件显示规则 (Conditional Display)

| 条件 | 显示内容 |
|------|----------|
| `${!empty pm.datas}` | 显示数据表格行 |
| `${! empty searchKey}` | 分页URL使用`searchVesselCol.html` |
| `${ empty searchKey}` | 分页URL使用`allVesselCol.html` |
| `${!empty result}` | 显示服务器返回的结果消息（红色） |
| `${limit == 'Yes'}` (admin.jsp) | 限制显示部分管理功能 |

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselColorManage.jsp → c:choose/c:if blocks (lines 79-105); src/main/webapp/WEB-INF/jsp/admin.jsp → limit condition (lines 49-54)

### 8.3 数据转换规则 (Data Transformation)

- 搜索关键字通过`encodeURIComponent()`进行URL编码
- 表格数据通过JSTL `<c:out>` 标签输出，自动进行HTML转义防止XSS

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselColorManage.jsp → search() function (line 43), c:out tags (lines 83-89)

### 8.4 权限控制规则 (Permission Control)

- 添加、修改、删除操作对所有已登录用户开放（无额外权限检查）
- admin.jsp中根据`limit`参数控制显示的管理功能范围
  - `limit == 'Yes'`: 仅显示"Vessel Refuel Configure"和"Vessel Refuel Bay Row Configure"
  - 否则: 显示全部管理功能（包括用户管理、导出日志、设置舱位大小、颜色配置、创建用户等）

> 📎 Source: src/main/webapp/WEB-INF/jsp/admin.jsp → limit condition (lines 49-64)
