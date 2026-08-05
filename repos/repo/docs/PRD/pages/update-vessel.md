# 更新船舶配置 (Update Vessel)

## 1. 概述 (Overview)

本页面用于修改现有船舶的舱位配置信息。用户可以从船舶管理列表进入此页面，编辑船舶的基本标识（船名、甲板/舱位）以及集装箱位置范围（Bay、Row、Tier），然后提交保存。

**业务目的**：维护船舶的舱位结构定义，确保系统能够正确追踪和管理集装箱在船舶上的物理位置。

**作用域**：仅支持修改已存在的船舶记录，不支持新增或删除操作。

> 📎 Source: src/main/webapp/WEB-INF/jsp/updateVessel.jsp → form; src/main/java/com/springMVC/control/CellControl.java → updateVessel()

## 2. 用户角色 (User Roles)

- **系统管理员 / 操作员**：负责维护船舶基础数据，包括修改船舶名称、甲板/舱位标识以及集装箱位置范围参数。

## 3. 页面布局 (Page Layout)

页面采用居中单栏表单布局，整体结构如下：

```text
┌─────────────────────────────────────────────┐
│              MODERN TERMINALS                │  ← 标题栏（蓝色背景）
├─────────────────────────────────────────────┤
│                                             │
│   Vessel Name : [____________]              │
│   Deck/Hold   : [A ▼]                       │
│   BAY         : [____]                      │
│   Rows        : [__] - [___]                │
│   Tiers       : [__] - [__]                 │
│                                             │
│          [OK]      [Cancel]                 │
│                                             │
│   [错误提示信息（红色）]                     │
│                                             │
└─────────────────────────────────────────────┘
```

> 📎 Source: src/main/webapp/WEB-INF/jsp/updateVessel.jsp → template

## 4. 搜索字段 (Search Fields)

本页面为表单编辑页，不包含搜索功能。

## 5. 表格列 (Table Columns)

本页面为表单编辑页，不包含数据表格。

## 6. 交互组件 (Interaction Components)

### 6.1 主表单 (Main Form)

**触发条件**：从船舶管理列表点击"Modify"链接进入（URL: `modifyVessel.html?id={vesselId}`）。

**表单字段**：

| 字段名 | 标签 | 类型 | 必填 | 最大长度 | 说明 |
|--------|------|------|------|----------|------|
| vesselid | Vessel Name | 文本输入 | 是 | 30 | 船舶标识名称 |
| deck_hold | Deck/Hold | 下拉选择 | 是 | 1 | 可选值：A（甲板）、B（舱位） |
| bay | BAY | 文本输入 | 是 | 3 | Bay编号，必须为数字 |
| rowStart | Row Start | 文本输入 | 是 | 2 | Row起始值，必须为数字 |
| rowEnd | Row End | 文本输入 | 是 | 3 | Row结束值，必须为数字 |
| tierStart | Tier Start | 文本输入 | 是 | 2 | Tier起始值，必须为数字 |
| tierEnd | Tier End | 文本输入 | 是 | 2 | Tier结束值，必须为数字 |
| id | (隐藏字段) | 隐藏输入 | - | - | 船舶记录ID，用于后端定位待更新记录 |

**提交逻辑**：
1. 点击"OK"按钮触发表单提交前验证（`checkValue()`函数）
2. 验证通过后POST到 `/user/updateVessel.html`
3. 后端检查 vesselid + deck_hold + bay 组合是否已存在（排除当前记录）
4. 若存在重复则返回错误提示；否则更新记录并重定向到船舶管理列表

**关闭条件**：
- 点击"Cancel"按钮：直接跳转回 `allVessel.html`（船舶管理列表）
- 提交成功后：自动重定向到 `allVessel.html`
- 提交失败：停留在当前页面并显示错误信息

> 📎 Source: src/main/webapp/WEB-INF/jsp/updateVessel.jsp → checkValue(); src/main/java/com/springMVC/control/CellControl.java → updateVessel()

### 6.2 退出登录 (Logout)

**触发条件**：点击右上角登出图标

**交互逻辑**：弹出确认对话框（国际化消息 `confirm_logout`），确认后跳转到 `logout.html`

> 📎 Source: src/main/webapp/WEB-INF/jsp/updateVessel.jsp → sh()

## 7. 用户流程 (User Flows)

### 流程1：修改船舶配置

1. 用户在船舶管理列表页面点击某条记录的"Modify"链接
2. 系统加载 `updateVessel.jsp`，预填充该船舶的现有数据
3. 用户修改表单字段（如更改船名、调整Row/Tier范围等）
4. 用户点击"OK"按钮
5. 前端执行 `checkValue()` 验证所有字段
   - 若验证失败：显示红色错误提示，阻止提交
   - 若验证通过：提交表单到后端
6. 后端检查 vesselid + deck_hold + bay 组合的唯一性
   - 若存在重复：返回当前页面并显示 "the vesselid,deck_hold,bay already exists!"
   - 若无重复：更新数据库记录
7. 更新成功：自动跳转回船舶管理列表页面
8. 更新失败：停留在当前页面并显示 "The operation failed"

### 流程2：取消编辑

1. 用户在编辑页面点击"Cancel"按钮
2. 系统直接跳转到 `allVessel.html`（船舶管理列表）

> 📎 Source: src/main/webapp/WEB-INF/jsp/updateVessel.jsp → back(); src/main/java/com/springMVC/control/CellControl.java → updateVessel()

## 8. 业务规则 (Business Rules)

### 8.1 校验规则 (Validation)

| 字段 | 规则 | 错误提示 |
|------|------|----------|
| vesselid | 不能为空 | "The vesselid cannot be empty!" |
| vesselid | 长度 ≤ 30 | "The vesselid is too long!" |
| deck_hold | 不能为空 | "The deck_hold cannot be empty!" |
| deck_hold | 长度 = 1 | "The deck_hold is too long!" |
| deck_hold | 值必须为 'A' 或 'B' | "The deck_hold should be 'A' or 'B' !" |
| bay | 不能为空 | "The bay cannot be empty!" |
| bay | 长度 ≤ 10 | "The bay is too long!" |
| bay | 必须为数字 | "The bay should be number !" |
| rowStart | 不能为空 | "The rowStart cannot be empty!" |
| rowStart | 长度 ≤ 2 | "The rowStart is too long!" |
| rowStart | 必须为数字 | "The rowStart should be number !" |
| rowEnd | 不能为空 | "The rowEnd cannot be empty!" |
| rowEnd | 长度 ≤ 3 | "The rowEnd is too long!" |
| rowEnd | 必须为数字 | "The rowEnd should be number !" |
| tierStart | 不能为空 | "The tierStart cannot be empty!" |
| tierStart | 长度 ≤ 2 | "The tierStart is too long!" |
| tierStart | 必须为数字 | "The tierStart should be number !" |
| tierEnd | 不能为空 | "The tierEnd cannot be empty!" |
| tierEnd | 长度 ≤ 3 | "The tierEnd is too long!" |
| tierEnd | 必须为数字 | "The tierEnd should be number !" |

> 📎 Source: src/main/webapp/WEB-INF/jsp/updateVessel.jsp → checkValue()

### 8.2 条件显示规则 (Conditional Display)

- **错误消息区域**：
  - `ess` 行：当后端返回 `result` 属性时显示（服务器端验证错误）
  - `message` 行：当前端验证失败时动态显示（客户端验证错误）
  - 两者均使用红色字体、15px字号、居中对齐

- **deck_hold 下拉选项**：根据当前值预选对应选项
  - 若当前值为 'A'：A 选项设为 selected
  - 若当前值为 'B'：B 选项设为 selected

> 📎 Source: src/main/webapp/WEB-INF/jsp/updateVessel.jsp → c:if test="${!empty result}"; c:choose test="${vessel.deck_hold =='A'}"

### 8.3 数据转换规则 (Data Transformation)

- **deck_hold 枚举映射**：
  - 'A' → 甲板（Deck）
  - 'B' → 舱位（Hold）

- **数值字段**：bay、rowStart、rowEnd、tierStart、tierEnd 均以字符串形式存储和传输，但前端验证要求必须为纯数字

> 📎 Source: src/main/java/com/springMVC/entity/Vessel.java → deck_hold, bay, rowStart, rowEnd, tierStart, tierEnd

### 8.4 权限控制规则 (Permission Control)

- 页面访问：需要用户已登录（通过会话管理）
- 操作权限：无细粒度按钮级权限控制，所有登录用户均可修改船舶配置
- 登出功能：所有页面右上角均提供登出入口

> 📎 Source: src/main/webapp/WEB-INF/jsp/updateVessel.jsp → sh()

### 8.5 唯一性约束 (Uniqueness Constraint)

- **复合唯一键**：vesselid + deck_hold + bay 三者组合必须在系统中唯一
- 后端在更新前会查询是否存在相同组合的其他记录
- 若存在重复，拒绝更新并返回错误提示："the vesselid,deck_hold,bay already exists!"

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → updateVessel(); src/main/java/com/springMVC/dao/VesselDaoImpl.java → getVesselByCondition()
