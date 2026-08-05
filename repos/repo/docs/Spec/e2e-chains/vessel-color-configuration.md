# Vessel Color and Bay/Row Configuration - Spec

[← Back to Overview](../overview.md)

## Architecture

### Service Layer

```
CellControl (Controller)
    ├── VesselDao (Shared Service)
    │   └── t_vessel_col (Database Table)
    └── OperationLog (Cross-module Service)
        └── t_operation_log (Database Table)
```

**依赖关系**：
- CellControl 依赖 VesselDao 进行数据持久化操作
- CellControl 依赖 OperationLog 模块记录操作日志
- VesselDao 被多个链共享（vessel-configuration, vessel-refuel-configuration）

### 共享服务边界

VesselDao 作为共享服务，其接口变更会影响以下链：
- vessel-color-configuration（本链）
- vessel-configuration
- vessel-refuel-configuration

⚠️ [PERF:bottleneck] VesselDao 作为共享服务可能成为性能瓶颈，需关注并发访问时的数据库连接池配置和查询效率。

> 📎 Source: TBD — VesselDao interface definition not accessible via current tools

## API Contracts

### CellControl Endpoints

#### GET /user/allVesselCol

**描述**：获取全部船舶颜色配置列表

**请求参数**：无

**响应格式**：
```json
{
  "code": 200,
  "data": [
    {
      "id": "number",
      "vesselid": "string",
      "deck_hold": "string",
      "bay": "number",
      "rowStart": "number",
      "rowEnd": "number",
      "tierStart": "number",
      "tierEnd": "number"
    }
  ]
}
```

**状态码**：
- 200: 成功
- 500: 服务器内部错误

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → getVesselCol()

---

#### GET /user/searchVesselColor

**描述**：按关键字搜索船舶颜色配置

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| key | string | 是 | 搜索关键字 |

**响应格式**：同 allVesselCol

**状态码**：
- 200: 成功
- 400: 缺少搜索关键字
- 500: 服务器内部错误

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → searchVesselCol()

---

#### GET /user/addVesselCol

**描述**：进入新增颜色配置页面

**请求参数**：无

**响应**：返回 vesselColorDetail.jsp 视图（空表单）

**状态码**：
- 200: 成功返回页面
- 500: 服务器内部错误

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → addVesselBayColor()

---

#### GET /user/modifyVesselCol

**描述**：进入修改颜色配置页面（加载已有数据）

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | number/string | 是 | 颜色配置ID |

**响应**：返回 vesselColorDetail.jsp 视图（带数据的表单）

**状态码**：
- 200: 成功返回页面
- 404: 配置不存在
- 500: 服务器内部错误

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → modVesselCol()

---

#### POST /user/saveVesselCol

**描述**：保存或更新颜色配置

**请求参数**（Form Data）：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| vesselid | string | 是 | 船舶ID |
| deck_hold | string | 是 | 甲板/货舱标识 |
| bay | number | 是 | Bay编号 |
| rowStart | number | 是 | Row起始位置 |
| rowEnd | number | 是 | Row结束位置 |
| tierStart | number | 是 | Tier起始位置 |
| tierEnd | number | 是 | Tier结束位置 |
| id | number | 否 | 配置ID（存在则为更新，不存在则为新增） |

**响应格式**：
```json
{
  "code": 200,
  "message": "保存成功"
}
```

**状态码**：
- 200: 保存成功
- 400: 参数校验失败（如 rowStart > rowEnd）
- 500: 服务器内部错误

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → saveOrUpdateVesselCol()

---

#### GET /user/delVesselCol

**描述**：删除指定颜色配置

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | number/string | 是 | 颜色配置ID |

**响应格式**：
```json
{
  "code": 200,
  "message": "删除成功"
}
```

**状态码**：
- 200: 删除成功
- 404: 配置不存在
- 500: 服务器内部错误

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → delVesselCol()

## Data Model

### 实体关系

```erDiagram
  VESSEL_COL ||--o{ OPERATION_LOG : "triggers"
```

> 实体字段定义详见：[cell](../../services/cell.md)、[operation-log](../../services/operation-log.md)

### 表结构参考

**t_vessel_col**（船舶颜色配置表）
- 主键：id
- 外键关联：vesselid 关联船舶主表
- 索引建议：(vesselid, bay, rowStart, rowEnd) 组合索引用于快速查询

**t_operation_log**（操作日志表）
- 记录所有 SAVE/UPDATE/DELETE 操作
- 包含操作类型、操作人、操作时间、操作对象ID等字段

> 详细表结构见模块级文档：[cell](../../services/cell.md)、[operation-log](../../services/operation-log.md)

## Integration Specs

### 跨模块集成

**OperationLog 模块集成**：
- 触发时机：saveOrUpdateVesselCol() 执行成功后、delVesselCol() 执行成功后
- 传递数据：操作类型（SAVE/UPDATE/DELETE）、操作对象ID、操作人信息
- ⚠️ [ERR:no-rollback] 操作日志记录失败不应回滚主业务操作，但需记录警告日志

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → saveOrUpdateVesselCol(), delVesselCol()

### 共享服务集成

**VesselDao 共享服务**：
- 本链通过 VesselDao 访问 t_vessel_col 表
- VesselDao 同时被 vessel-configuration 和 vessel-refuel-configuration 链使用
- ⚠️ [PERF:no-circuit-breaker] 共享服务无熔断机制，高并发时可能影响所有依赖链
- ⚠️ [ERR:cascade-failure] VesselDao 故障会导致本链及其他两个链同时不可用

> 📎 Source: TBD — VesselDao implementation details not accessible

## Error Handling

### 错误场景

| 错误场景 | 错误码 | 处理方式 |
|----------|--------|----------|
| 配置不存在（修改/删除时） | 404 | 返回友好提示，引导用户返回列表页 |
| 参数校验失败（范围无效） | 400 | 前端校验 + 后端二次校验，返回具体错误字段 |
| 数据库操作失败 | 500 | 记录异常日志，返回通用错误提示 |
| 操作日志记录失败 | 200（主业务成功） | 记录警告日志，不影响主业务流程 |

### 异常处理策略

⚠️ [ERR:no-timeout] CellControl 调用 VesselDao 时未明确配置超时时间，可能导致长时间等待

⚠️ [ERR:no-conflict-resolution] 多人同时修改同一配置时无冲突检测机制，后提交者覆盖先提交者

> 📎 Source: TBD — Exception handling code in CellControl not accessible

## Security

### 认证与授权

- 所有 API 端点需要用户登录认证
- ⚠️ [OWASP:A01] 未明确看到基于角色的权限控制，可能存在越权访问风险（如普通用户删除他人配置）

> 📎 Source: TBD — Authentication/authorization mechanism not visible in current scope

### 数据访问范围

- 用户只能查看和操作有权限的船舶配置
- ⚠️ [OWASP:A01] 未在API层看到明确的租户/组织隔离逻辑，需确认数据过滤是否在VesselDao层实现

> 📎 Source: TBD — Data access control logic not accessible

## Performance

### 端到端延迟关注点

1. **列表查询**：allVesselCol 可能返回大量数据，建议分页或限制返回数量
   - ⚠️ [PERF:no-cache] 列表数据未使用缓存，每次请求都查询数据库

2. **搜索功能**：searchVesselColor 使用关键字模糊匹配，大数据量下性能较差
   - 建议在 t_vessel_col 表上建立全文索引或使用 Elasticsearch

3. **共享服务瓶颈**：VesselDao 被三个链共享
   - ⚠️ [PERF:bottleneck] 高并发场景下 VesselDao 可能成为瓶颈
   - 建议监控数据库连接池使用情况

4. **操作日志写入**：每次保存/删除都同步写入操作日志
   - ⚠️ [PERF:cascade-call] 同步写入日志增加响应时间，可考虑异步记录

> 📎 Source: TBD — Performance optimization details not accessible in current code view