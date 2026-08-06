# Set Bay Size - 产品需求文档 (PRD)

## 1. 概述 (Overview)

**页面名称**: Set Bay Size（设置贝位尺寸）

**业务目的**: 该页面用于配置集装箱船舶的甲板（DECK）和货舱（HOLD）的行列尺寸参数。系统根据这些参数激活或停用 t_cellmatrix 表中的单元格记录，从而定义船舶的可用装载空间矩阵。

**业务范围**: 
- 管理甲板区域的行数（Rows）和层数（Tiers）
- 管理货舱区域的行数（Rows）和层数（Tiers）
- 更新后影响船舶装载计划的可用空间计算

**数据来源**: 
> 📎 Source: src/main/java/com/springMVC/dao/CellDaoImpl.java → getBaySize()

## 2. 用户角色 (User Roles)

| 角色 | 权限说明 |
|------|----------|
| 系统管理员 / 码头操作人员 | 可查看当前贝位尺寸配置，可修改并提交新的尺寸参数 |

## 3. 页面布局 (Page Layout)

页面采用居中卡片式布局，整体结构如下：

```text
┌─────────────────────────────────────────────┐
│              MODERN TERMINALS               │  ← 标题栏（蓝色背景）
├─────────────────────────────────────────────┤
│                                             │
│   DECK :  Rows [____]  Tiers [____]        │  ← 甲板配置行
│   HOLD :  Rows [____]  Tiers [____]        │  ← 货舱配置行
│                                             │
│         [OK]          [Cancel]              │  ← 操作按钮
│                                             │
│         （错误消息显示区域）                  │  ← 条件显示
│                                             │
└─────────────────────────────────────────────┘
```

> 📎 Source: src/main/webapp/WEB-INF/jsp/setbaysize.jsp → template

## 4. 搜索字段 (Search Fields)

本页面为表单编辑页，无搜索功能。以下为表单输入字段：

| 字段名 | 标签 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| deckRows | DECK Rows | 文本输入 | 是 | 甲板区域的行数 |
| deckTiers | DECK Tiers | 文本输入 | 是 | 甲板区域的层数 |
| holdRows | HOLD Rows | 文本输入 | 是 | 货舱区域的行数 |
| holdTiers | HOLD Tiers | 文本输入 | 是 | 货舱区域的层数 |

> 📎 Source: src/main/webapp/WEB-INF/jsp/setbaysize.jsp → form inputs; src/main/java/com/springMVC/entity/BaySize.java

## 5. 表格列 (Table Columns)

本页面不包含数据表格，为纯表单编辑页面。

## 6. 交互组件 (Interaction Components)

### 6.1 主表单

**触发条件**: 页面加载时自动显示，从后端获取当前配置值并填充到表单中。

**表单字段**:
- deckRows: 甲板行数
- deckTiers: 甲板层数
- holdRows: 货舱行数
- holdTiers: 货舱层数

**提交逻辑**: 
- 点击 "OK" 按钮触发表单提交
- 提交前执行客户端校验函数 `checkv()`
- 校验通过后 POST 请求发送至 `/user/updateBay.html`
- 成功后重定向至 `/user/all.html`（管理主页）

**关闭条件**: 
- 点击 "Cancel" 按钮导航至 `/user/all.html`
- 提交成功后自动重定向

> 📎 Source: src/main/webapp/WEB-INF/jsp/setbaysize.jsp → form:form, checkv(), back()

### 6.2 登出按钮

**位置**: 页面右上角

**触发条件**: 点击登出图标

**交互逻辑**: 
- 弹出确认对话框（显示国际化消息 `confirm_logout`）
- 用户确认后跳转至 `logout.html`

> 📎 Source: src/main/webapp/WEB-INF/jsp/setbaysize.jsp → sh()

## 7. 用户流程 (User Flows)

### 流程 1: 查看并修改贝位尺寸

1. 用户访问 `/user/setbay.html`
2. 系统从数据库查询当前甲板/货舱的最大行列值（cmtype='A' 为甲板，cmtype='B' 为货舱）
3. 页面展示当前配置值（deckRows, deckTiers, holdRows, holdTiers）
4. 用户修改任意字段的数值
5. 用户点击 "OK" 按钮
6. 系统执行客户端校验（见业务规则）
7. 校验通过后提交 POST 请求至 `/user/updateBay.html`
8. 后端更新 t_cellmatrix 表：
   - 将 cmrow < 新Rows 且 cmtype='A'/'B' 的记录设为 active='1'
   - 将 cmrow >= 新Rows 的记录设为 active='0'
   - 同时更新 cmtier 字段为 Tiers-1
9. 更新成功后重定向至 `/user/all.html`
10. 若更新失败，页面保留并显示错误消息

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → setBaySize(), updateBay(); src/main/java/com/springMVC/dao/CellDaoImpl.java → getBaySize(), updateCellMatrix()

### 流程 2: 取消操作

1. 用户在页面上点击 "Cancel" 按钮
2. 系统直接导航至 `/user/all.html`（管理主页）

> 📎 Source: src/main/webapp/WEB-INF/jsp/setbaysize.jsp → back()

## 8. 业务规则 (Business Rules)

### 8.1 校验规则 (Validation)

所有四个字段（deckRows, deckTiers, holdRows, holdTiers）遵循相同的校验规则：

| 规则编号 | 字段 | 校验条件 | 错误提示 |
|----------|------|----------|----------|
| V1 | 所有字段 | 不能为空 | "The [Field] can't be empty!" |
| V2 | 所有字段 | 必须为纯数字（正则 `^[0-9]*$`） | "The [Field] must be a nubmer!" |
| V3 | 所有字段 | 最大值不超过 100 | "The [Field] must not more than 100!" |
| V4 | 所有字段 | 最小值不低于 5 | "The [Field] must not less than 5!" |

**格式化规则**: 校验通过后，若数值 < 10，则在前面补零（如 5 → "05"）

> 📎 Source: src/main/webapp/WEB-INF/jsp/setbaysize.jsp → checkv(), formatNum()

### 8.2 条件显示规则 (Conditional Display)

| 条件 | 显示内容 |
|------|----------|
| `${!empty baymsg }` 为 true | 显示红色错误消息（`${result }`） |

> 📎 Source: src/main/webapp/WEB-INF/jsp/setbaysize.jsp → c:if test="${!empty baymsg }"

### 8.3 数据转换规则 (Data Transformation)

**读取转换**: 
- 数据库存储的值为最大行列号（max(cmrow), max(cmtier)）
- 前端显示时需 +1（getRealSize 方法：`Integer.parseInt(size) + 1`）
- 例如：数据库存储 49，前端显示 50

**写入转换**: 
- 用户输入的 Tiers 值在存入数据库时需 -1
- 例如：用户输入 50，数据库存储 49

> 📎 Source: src/main/java/com/springMVC/dao/CellDaoImpl.java → getRealSize(), updateCellMatrix()

### 8.4 权限控制规则 (Permission Control)

- 页面访问需要用户已登录（通过 Spring Security 或会话管理控制）
- 右上角提供登出功能
- 无细粒度的字段级权限控制

> 📎 Source: src/main/webapp/WEB-INF/jsp/setbaysize.jsp → logout icon
