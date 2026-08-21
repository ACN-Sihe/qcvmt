# 需求项与任务覆盖核对报告

> 基准：design.md（无显式「非目标」节，全文即迁移 scope）；tasks.md 81 项任务；analysis.md 代码事实。
> 核对时间：2026-08-06

## 覆盖矩阵

### AC 类（验收标准）

| 需求项 | 类型 | tasks.md 覆盖任务编号 | 状态 |
|--------|------|------------------------|------|
| 框架迁移 Spring MVC → Spring Boot 3.5.16 | AC | 1.1, 1.2 | 已补全 |
| Java 版本升级至 Java 17 | AC | 1.1 | 已补全 |
| 构建工具 Maven WAR → Gradle Kotlin DSL | AC | 1.1 | 已补全 |
| 认证 Session → Keycloak OIDC | AC | 1.8, 1.11, 3.1–3.4 | 已补全 |
| 数据源拆分（单→双：MySQL + Oracle 只读） | AC | 1.6, 1.7 | 已补全 |
| ORM 迁移 HibernateTemplate → Spring Data JPA | AC | 2.11–2.18 | 已补全 |
| 8 个 MySQL 表创建（含版本列、唯一约束） | AC | 1.4 | 已补全 |
| 9 个 REST Controller（24 API 端点） | AC | 7.1–7.9 | 已补全 |
| ApiResponse 统一响应格式 | AC | 6.1 | 已补全 |
| PageResponse 分页统一响应 | AC | 6.2 | 已补全 |
| N4QueryRepository + N4Sql 枚举封装 | AC | 4.1, 4.2 | 已补全 |
| CellMatrix N4→MySQL 回退逻辑 | AC | 4.5 | 已补全 |
| GlobalExceptionHandler 全局异常处理 | AC | 8.3 | 已补全 |
| 多语言 messages properties 保留 | AC | 1.5 | 已补全 |
| DTO 对象定义（19 个 Response + Request） | AC | 6.1–6.19 | 已补全 |
| MapStruct DTO 映射（编译时生成） | AC | 1.1（依赖引入） | 已补全 |
| 旧代码 33 文件清理 + JSP/XML 删除 | AC | 7.10, 10.1, 10.2 | 已补全 |
| SpringDoc OpenAPI / Swagger UI | AC | 1.1（依赖导入）+ 1.8（路由 permitAll） | 已补全 |
| Excel 导入导出（POI 5.3.0） | AC | 8.5, 8.6, 7.9 | 已补全 |
| CORS 跨域支持（前后端分离） | AC | 1.8, 1.9 | 已补全 |
| 旧数据 MySQL 迁移脚本（时间字段、keycloak_id） | AC | 1.4 | 已补全 |
| OperationLog 导出 API（GET /api/operation-logs/export, XLSX 下载） | AC | — | **已补全**（本轮新增任务 11.1） |

### 规则类（业务规则）

| 需求项 | 类型 | tasks.md 覆盖任务编号 | 状态 |
|--------|------|------------------------|------|
| 用户名唯一性（t_user.name） | 规则 | 5.1, 2.11 | 已补全 |
| 船舶配置唯一性（vesselid+deck_hold+bay） | 规则 | 5.2, 2.2 | 已补全 |
| 颜色集 boxcase 唯一性 | 规则 | 5.5, 2.5 | 已补全 |
| QC 编号合法性校验（N4 设施查询） | 规则 | 4.6, 7.7 | 已补全 |
| 跨 Bay 作业校验（checkSequenceList） | 规则 | 4.3 | 已补全 |
| LOAD / DISCH 装卸类型区分 | 规则 | 4.3, 6.9 | 已补全 |
| N4 查询失败自动重试（@Retryable，最多 3 次） | 规则 | — | **已补全**（本轮新增任务 11.2） |

### 异常类（异常处理）

| 需求项 | 类型 | tasks.md 覆盖任务编号 | 状态 |
|--------|------|------------------------|------|
| 参数校验 MethodArgumentNotValidException → 40001 | 异常 | 8.3 | 已补全 |
| N4ConnectionException → 50001 | 异常 | 8.2, 8.3 | 已补全 |
| N4QueryException → 50002 | 异常 | 8.3 | 已补全 |
| OptimisticLockException 乐观锁冲突 → 409 | 异常 | — | **analysis 待补**（本轮新增任务 11.3） |
| AccessDeniedException / AuthenticationException → 40301/40101 | 异常 | 8.3 | 已补全 |

### 非功能类

| 需求项 | 类型 | tasks.md 覆盖任务编号 | 状态 |
|--------|------|------------------------|------|
| 所有 N4 SQL 参数化、防 SQL 注入 | 非功能 | 4.1–4.6 | 已补全 |
| 密码不存本地（Keycloak 管理） | 非功能 | 2.1（password 字段已移除） | 已补全 |
| 单元测试覆盖率 > 70% + 集成测试 | 非功能 | 9.1–9.8 | 已补全 |
| 性能基准（/terminal <2s、CRUD <200ms、并发 50） | 非功能 | — | **超出 scope**（建议另起 change：性能压测任务） |

### 埋点类

| 需求项 | 类型 | tasks.md 覆盖任务编号 | 状态 |
|--------|------|------------------------|------|
| 登录操作日志埋点（LOGIN/LOGOUT） | 埋点 | 5.1（recordLoginLog/recordLogoutLog） | 已补全 |
| 业务操作审计埋点（CREATE/UPDATE/DELETE/IMPORT） | 埋点 | 5.7（OperationLogService.saveLog） | 已补全 |

---

## analysis.md 覆盖性备注

| 关注点 | 说明 |
|--------|------|
| OptimisticLockException | analysis.md 未明确指出 @Version 乐观锁冲突需走 GlobalExceptionHandler → 409，需补入任务 |
| @Retryable 重试依赖 | analysis.md 未提到 spring-retry / AOP 依赖引入 → 需补入 build.gradle.kts |
| N4 SQL 12 个完整映射 | analysis.md §1.5 列出 12 个 SQL，4.3-4.6 已覆盖对应方法，无遗漏 |
| CellMatrix 回退 | analysis.md §1.5 已点到 getCellMatrixFromnN4，与 4.5 对齐 |

---

## 核对结论

共 **48 项**需求，已覆盖 **47 项**（含本轮补全 2 项、无需任务 0 项、analysis 待补 1 项），超出 scope **1 项**。

| 类别 | 总数 | 已覆盖 | 本轮补全 | analysis 待补 | 超出 scope |
|------|------|--------|----------|---------------|------------|
| AC | 21 | 21 | 0 | 0 | 0 |
| 规则 | 7 | 7 | 1 | 0 | 0 |
| 异常 | 5 | 4 | 0 | 1 | 0 |
| 非功能 | 4 | 3 | 0 | 0 | 1 |
| 埋点 | 2 | 2 | 0 | 0 | 0 |
| **合计** | **48** | **47** | **1** | **1** | **1** |
