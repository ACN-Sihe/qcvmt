# 船舶加油舱位行配置详情 (Manage Vessel Bay Row)

## 1. 概述 (Overview)

本页面用于管理船舶加油舱位行（Vessel Refuel Bay Row）的配置信息。用户可以新增或修改船舶的舱位行配置，包括船舶访问ID、甲板/货舱标识、舱位号、行范围以及层范围等关键信息。该配置用于定义船舶在码头加油时的具体舱位位置，支持按行和层的范围进行精确配置。

**业务目的**：为港口运营人员提供船舶加油舱位行的配置管理能力，确保加油作业能够准确定位到船舶的具体舱位区域。

**页面类型**：表单编辑页（支持新增和修改两种模式）

## 2. 用户角色 (User Roles)

| 角色 | 权限说明 |
|------|----------|
| 港口运营管理员 | 可以新增、修改船舶加油舱位行配置 |
| 系统管理员 | 拥有全部配置管理权限 |

## 3. 页面布局 (Page Layout)

页面采用居中单栏布局结构：

```text
┌─────────────────────────────────────────────┐
│              MODERN TERMINALS               │  ← 页面标题栏（蓝色背景）
├─────────────────────────────────────────────┤
│                                             │
│   Vessel Visit Id : [____________]          │  ← 表单字段
│   Deck/Hold       : [▼ A/B      ]           │
│   BAY             : [____]                  │
│   Rows            : [____] - [____]         │  ← 起始行 - 结束行
│   Tiers           : [____] - [____]         │  ← 起始层 - 结束层
│                                             │
│         [OK]        [Cancel]                │  ← 操作按钮
│                                             │
│         [错误提示信息]                       │  ← 验证错误或服务器错误
│                                             │
└─────────────────────────────────────────────┘
```

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselColorDetail.jsp → template (lines 206-315)

## 4. 搜索字段 (Search Fields)

本页面为表单编辑页，不包含搜索功能。搜索功能位于列表页（vesselColorManage.jsp）。

## 5. 表格列 (Table Columns)

本页面为表单编辑页，不包含数据表格。表格展示位于列表页（vesselColorManage.jsp），包含以下列：
- Vessel Visit Id
- Deck/Hold
- BAY
- Start Row
- End Row
- Start Tier
- End Tier
- Operation（删除/修改链接）

## 6. 交互组件 (Interaction Components)

### 6.1 主表单

**触发条件**：
- 从列表页点击"Add"链接进入新增模式（URL: `addVesselCol.html`）
- 从列表页点击"Modify"链接进入修改模式（URL: `modifyVesselCol.html?id={id}`）

**表单字段**：

| 字段名 | 标签 | 类型 | 必填 | 最大长度 | 默认值/选项 | 说明 |
|--------|------|------|------|----------|-------------|------|
| vesselid | Vessel Visit Id | 文本输入 | 是 | 30 | 修改模式下回填已有值 | 船舶访问ID |
| deck_hold | Deck/Hold | 下拉选择 | 是 | - | A 或 B | 甲板(A)或货舱(B)标识 |
| bay | BAY | 文本输入 | 是 | 3（前端限制10，后端数据库10） | 修改模式下回填已有值 | 舱位号，必须为数字 |
| rowStart | Start Row | 文本输入 | 是 | 2 | 修改模式下回填已有值 | 起始行号，必须为数字 |
| rowEnd | End Row | 文本输入 | 是 | 3（前端限制2） | 修改模式下回填已有值 | 结束行号，必须为数字 |
| tierStart | Start Tier | 文本输入 | 否* | 2 | 修改模式下回填已有值 | 起始层号，必须为偶数数字 |
| tierEnd | End Tier | 文本输入 | 否* | 3（前端限制2） | 修改模式下回填已有值 | 结束层号，必须为偶数数字 |
| id | ID | 隐藏字段 | - | - | 修改模式下有值 | 记录主键，用于区分新增/修改 |

*注：tierStart 和 tierEnd 要么同时为空，要么同时填写

**提交逻辑**：
- 点击"OK"按钮触发表单提交
- 提交前执行 `checkValue()` 客户端验证函数
- 验证通过后提交至 `/user/saveVesselCol.html`（POST方法）
- 成功则重定向至列表页 `allVesselCol.html`
- 失败则停留在当前页并显示错误信息 "The operation failed"

**关闭/取消逻辑**：
- 点击"Cancel"按钮调用 `back()` 函数
- 直接跳转至列表页 `allVesselCol.html`

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselColorDetail.jsp → checkValue() (lines 40-191); src/main/java/com/springMVC/control/CellControl.java → saveOrUpdateVesselCol() (lines 440-496)

### 6.2 退出登录

**触发条件**：点击右上角退出图标

**交互逻辑**：
- 弹出确认对话框（国际化消息：confirm_logout）
- 确认后跳转至 `logout.html`

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselColorDetail.jsp → sh() (lines 194-198)

## 7. 用户流程 (User Flows)

### 流程1：新增船舶舱位行配置

1. 用户在列表页点击"Add"链接
2. 系统跳转至空白表单页面（新增模式）
3. 用户填写所有必填字段：
   - 输入 Vessel Visit Id
   - 选择 Deck/Hold（A或B）
   - 输入 BAY（数字）
   - 输入 Start Row 和 End Row（数字，且奇偶性相同）
   - 可选：输入 Start Tier 和 End Tier（必须同时填写，且都为偶数，Start ≤ End）
4. 用户点击"OK"按钮
5. 系统执行客户端验证
6. 验证通过后提交表单至后端
7. 后端保存数据并记录操作日志
8. 成功后跳转回列表页

### 流程2：修改船舶舱位行配置

1. 用户在列表页点击某条记录的"Modify"链接
2. 系统根据ID查询现有数据并回填至表单（修改模式）
3. 用户修改需要更新的字段
4. 用户点击"OK"按钮
5. 系统执行客户端验证
6. 验证通过后提交表单至后端
7. 后端更新数据并记录操作日志（包含旧值和新值）
8. 成功后跳转回列表页

### 流程3：取消操作

1. 用户在表单页面点击"Cancel"按钮
2. 系统直接跳转至列表页 `allVesselCol.html`
3. 不保存任何更改

### 流程4：验证失败处理

1. 用户点击"OK"按钮
2. 客户端验证发现错误（如字段为空、格式不正确、范围不合理等）
3. 系统在红色提示区域显示具体错误信息
4. 表单保持当前状态，用户可修正后重新提交

## 8. 业务规则 (Business Rules)

### 8.1 校验规则 (Validation)

#### 客户端验证（checkValue函数）

| 字段 | 规则 | 错误提示 |
|------|------|----------|
| vesselid | 不能为空 | "Vessel Visit Id cannot be empty!" |
| vesselid | 长度≤30 | "Vessel Visit Id is too long!" |
| deck_hold | 不能为空 | "Deck Hold cannot be empty!" |
| deck_hold | 长度=1 | "Deck Hold is too long!" |
| deck_hold | 值必须为"A"或"B" | "Deck Hold should be 'A' or 'B'!" |
| bay | 不能为空 | "The bay cannot be empty!" |
| bay | 长度≤10 | "The bay is too long!" |
| bay | 必须为纯数字 | "The bay should be number!" |
| rowStart | 不能为空 | "Start Row cannot be empty!" |
| rowStart | 长度≤2 | "Start Row is too long!" |
| rowStart | 必须为纯数字 | "Start Row should be number!" |
| rowEnd | 不能为空 | "End Row cannot be empty!" |
| rowEnd | 长度≤3 | "End Row is too long!" |
| rowEnd | 必须为纯数字 | "End Row should be number!" |
| rowStart vs rowEnd | rowStart ≤ rowEnd | "Start Row can't be larger than End Row!" |
| rowStart vs rowEnd | 奇偶性必须相同（同为奇数或同为偶数） | "Start Row, End Row should be both odd or even number!" |
| tierStart | 如果填写，长度≤2 | "The tierStart is too long!" |
| tierStart | 如果填写，必须为纯数字 | "The tierStart should be number !" |
| tierEnd | 如果填写，长度≤3 | "The tierEnd is too long!" |
| tierEnd | 如果填写，必须为纯数字 | "The tierEnd should be number !" |
| tierStart/tierEnd | 必须同时为空或同时填写 | "Please input Start Tier and End Tier together or both are blank." |
| tierStart/tierEnd | 如果都填写，必须都为偶数 | "Start Tier, End Tier should be even number!" |
| tierStart vs tierEnd | 如果都填写，tierStart ≤ tierEnd | "Start Tier can't be larger than End Tier!" |

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselColorDetail.jsp → checkValue() (lines 40-191)

#### 服务端验证

后端接收参数后进行数据持久化操作，未实现额外的业务验证逻辑。保存失败时返回错误信息 "The operation failed"。

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → saveOrUpdateVesselCol() (lines 440-496)

### 8.2 条件显示规则 (Conditional Display)

| 条件 | 显示内容 |
|------|----------|
| `${!empty vesselCol}` 为真（修改模式） | 表单字段回填已有数据，deck_hold下拉框根据当前值设置selected属性 |
| `${!empty vesselCol}` 为假（新增模式） | 表单字段为空，deck_hold默认选中"A" |
| `${!empty result}` 为真 | 显示服务器返回的错误信息（红色字体） |
| 客户端验证失败 | 显示验证错误信息（红色字体，id="message"区域） |

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselColorDetail.jsp → c:choose/c:when (lines 210-313)

### 8.3 数据转换规则 (Data Transformation)

| 字段 | 转换规则 |
|------|----------|
| deck_hold | 前端显示"A"或"B"，后端存储为字符串 |
| bay, rowStart, rowEnd, tierStart, tierEnd | 前端以字符串形式输入和验证（通过checkNumber函数），后端存储为字符串类型（数据库字段类型为VARCHAR(10)） |
| id | 隐藏字段，修改模式下从后端获取，用于区分新增/修改操作 |

> 📎 Source: src/main/java/com/springMVC/entity/VesselCol.java → 字段定义 (lines 21-40)

### 8.4 权限控制规则 (Permission Control)

- 页面访问依赖于用户登录状态（通过Session中的USER_LOGIN属性验证）
- 操作日志记录包含当前登录用户信息
- 未实现细粒度的按钮级权限控制

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → saveOrUpdateVesselCol() (line 456)
