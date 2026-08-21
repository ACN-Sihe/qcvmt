# QCVMT 现代化迁移变更提案

## 1. 背景与问题

### 1.1 项目背景

QCVMT（Quay Crane Visual Monitoring Tool）是码头岸桥可视化监控系统，用于实时展示岸桥作业序列、船舶箱位、装卸进度等关键信息。当前系统基于 **Spring MVC 3.0.1 + HibernateTemplate + Java 1.7** 构建，已运行超过 15 年。

### 1.2 核心问题

#### 1.2.1 技术债务严重

| 问题 | 影响 | 严重程度 |
|------|------|----------|
| **Spring MVC 3.0.1 已过时** | 无官方安全更新，存在已知 CVE 漏洞 | 高 |
| **Java 1.7 不受支持** | 无法使用现代 JVM 性能优化（G1 GC、ZGC）和安全特性 | 高 |
| **HibernateTemplate 已废弃** | API 不稳定，与 Spring Data JPA 不兼容 | 高 |
| **C3P0 连接池老旧** | 性能差，无监控，无连接泄漏检测 | 中 |
| **无自动化测试** | 任何改动都可能引入回归问题 | 高 |

#### 1.2.2 数据源配置混乱

当前 `springMVC-servlet.xml` 中，`jdbcTemplate` 和 `hibernateTemplate` 共用同一个 C3P0 数据源（指向 Oracle），导致：
- MySQL 本地数据和 N4 Oracle 数据无法区分
- 无法独立配置连接池参数
- 无法实现 Oracle 只读隔离

#### 1.2.3 认证机制过时

- 使用手写 `SecurityInterceptor` 拦截 Session
- 密码明文存储于数据库
- 无 OIDC/OAuth2 支持，无法与企业 SSO 集成
- 无 CSRF 防护

#### 1.2.4 响应格式不适合现代前端

`Busihandler.returnResponse()` 返回 XML 包裹的 HTML 字符串：
```xml
<type>
  <table_info>
    <table width="100%" height="70%" align="center">
      <tr><td>...</td></tr>
    </table>
  </table_info>
</type>
```

前端无法直接解析，无法支持前后端分离架构。

#### 1.2.5 业务逻辑耦合严重

- `CellDaoImpl.java` (1100+ 行) 同时处理 N4 Oracle SQL 和 MySQL 操作
- `UserControl.java` (550 行) 同时处理用户管理、登录登出、导入导出
- `CellControl.java` (497 行) 同时处理船舶、颜色集、Bay 配置

### 1.3 现状分析总结

根据 `analysis.md` 的详细代码分析，当前系统存在以下关键问题：

1. **12 个 N4 Oracle SQL 查询**混入 MySQL DAO 类中
2. **所有 N4 SQL 使用字符串拼接**，存在 SQL 注入风险
3. **CellMatrix N4 回退逻辑**复杂，易丢失数据
4. **Session 管理脆弱**，无法支持分布式部署
5. **无 API 文档**，前端调用依赖猜测

---

## 2. 变更目标

### 2.1 主要目标

| 目标 | 度量标准 | 验收方式 |
|------|----------|----------|
| **框架现代化** | 升级到 Spring Boot 3.5.16 + Java 17 | `gradlew build` 成功，`gradlew test` 通过 |
| **安全认证升级** | 集成 Keycloak OIDC，移除明文密码 | JWT Token 验证通过，Keycloak Realm 角色正确映射 |
| **双数据源隔离** | MySQL (JPA) + Oracle (JdbcTemplate 只读) | HikariCP 连接池独立配置，Oracle read-only=true |
| **REST API 重构** | 所有端点返回 JSON (ApiResponse<T>) | Swagger UI 可正常访问，所有 API 可调用 |
| **N4 SQL 迁移** | 12 个复杂 SQL 完整迁移到 N4Service 层 | N4WorkQueueService/N4ContainerQueryService 单元测试通过 |
| **前后端分离** | 移除所有 JSP、Busihandler HTML 响应 | 前端 SPA 可独立部署，后端仅提供 REST API |

### 2.2 次要目标

| 目标 | 度量标准 | 验收方式 |
|------|----------|----------|
| **代码可维护性** | 单一职责原则，Controller < 100 行 | SonarQube 代码质量评级 A |
| **API 文档化** | SpringDoc OpenAPI 自动生成 | /swagger-ui.html 可访问 |
| **配置外部化** | 所有敏感信息通过环境变量注入 | application.yml 无硬编码密码 |
| **Docker 化部署** | 构建 JAR 镜像，替代 WAR 部署 | `docker build` 成功，`docker run` 启动正常 |

---

## 3. 变更范围

### 3.1 在范围内

#### 3.1.1 技术栈升级

| 组件 | 当前版本 | 目标版本 | 说明 |
|------|----------|----------|------|
| Java | 1.7 | 17 | LTS 版本，支持 Record、Sealed Class、Pattern Matching |
| Spring Framework | 3.0.1 | 6.2.x | Spring Boot 3.5.16 依赖 |
| Spring Boot | 无 | 3.5.16 | 自动配置、Actuator、Starter 依赖 |
| Spring Data JPA | 无 | 3.2.x | Repository 模式，替代 HibernateTemplate |
| Hibernate | 3.2.6 | 6.4.x | JPA 3.1 实现 |
| Spring Security | 无 | 6.2.x | OAuth2 Resource Server |
| Keycloak Admin Client | 无 | 26.0.0 | 用户管理 API |
| MySQL Connector | 5.0.5 | 8.4.0 | Java 17 兼容 |
| Oracle OJDBC | 11.2.0.2.0 | 23.6.0.24.10 | Java 17 兼容 |
| HikariCP | 无 (C3P0 0.9.2.1) | 5.1.x (Spring Boot 默认) | 高性能连接池 |
| Lombok | 无 | 最新 | 减少样板代码 |
| MapStruct | 无 | 1.6.3 | DTO 映射 |
| SpringDoc OpenAPI | 无 | 2.7.0 | Swagger UI |
| POI | 3.9 | 5.3.0 | Excel 导入导出 |

#### 3.1.2 代码重构

| 重构项 | 当前实现 | 目标实现 | 文件数 |
|--------|----------|----------|--------|
| **Controller 拆分** | 2 个巨型 Controller | 9 个单一职责 Controller | 9 |
| **Service 分层** | DAO 直接操作数据库 | local/keycloak/n4 三层 Service | 13 |
| **Repository 层** | 无 | Spring Data JPA Repository | 8 |
| **N4 Service 层** | CellDaoImpl 中的 12 个 SQL | N4WorkQueue/N4Container/N4Vessel/N4Facility Service | 4 |
| **Entity 迁移** | Oracle SEQUENCE | MySQL IDENTITY + @Version 乐观锁 | 10 |
| **DTO 层** | 无 (直接返回 Entity) | Request/Response DTO + ApiResponse | 17 |
| **异常处理** | e.printStackTrace() | GlobalExceptionHandler + BusinessException | 3 |
| **安全配置** | SecurityInterceptor | SecurityConfig + JwtAuthConverter | 2 |
| **数据源配置** | springMVC-servlet.xml | MysqlDataSourceConfig + N4OracleDataSourceConfig | 2 |

#### 3.1.3 API 端点迁移

根据 `analysis.md` 的路由映射，完整迁移所有 24 个 API 端点：

| 模块 | 端点数 | 示例 |
|------|--------|------|
| 用户管理 | 5 | GET /api/users, POST /api/users |
| 用户日志 | 2 | GET /api/users/{id}/logs |
| 船舶管理 | 6 | GET /api/vessels, POST /api/vessels |
| 颜色集管理 | 5 | GET /api/color-sets, POST /api/color-sets |
| 船舶颜色配置 | 4 | GET /api/vessel-colors |
| 船舶加油配置 | 4 | GET /api/vessel-refuels |
| Bay 配置 | 2 | GET /api/bay-config, PUT /api/bay-config |
| 业务查询 | 1 | GET /api/terminal/query |
| 导入导出 | 3 | POST /api/import/vessel, GET /api/export/logs |
| 操作日志 | 2 | GET /api/operation-logs |

#### 3.1.4 N4 SQL 迁移

完整迁移 `CellDaoImpl.java` 中的 12 个 N4 Oracle SQL：

| 方法 | 涉及 N4 表 | 目标 Service | 复杂度 |
|------|-----------|--------------|--------|
| getLoadOrder | 10 表 JOIN | N4WorkQueueService | 高 |
| getDischargeOrder | 10 表 JOIN | N4WorkQueueService | 高 |
| getSequenceList | 10 表 JOIN + 多子查询 | N4WorkQueueService | 最高 |
| checkSequenceList | 10 表 JOIN (动态 SQL) | N4WorkQueueService | 高 |
| checkLoadSequenceList | 10 表 JOIN | N4WorkQueueService | 高 |
| checkDischargeSequenceList | 10 表 JOIN | N4WorkQueueService | 高 |
| getROBList | 3 表 JOIN | N4ContainerQueryService | 中 |
| getROBListByBay | 3 表 JOIN | N4ContainerQueryService | 中 |
| getROBListByBayNew | 3 表 JOIN (2 个重载) | N4ContainerQueryService | 中 |
| getCellMatrixFromnN4 | 4 表 JOIN + MySQL 回退 | N4VesselQueryService | 高 |
| getTwentyUnitList | 3 表 JOIN | N4ContainerQueryService | 中 |
| getHazardList | 2 表 JOIN | N4ContainerQueryService | 中 |
| queryQcId | 3 表子查询 | N4FacilityQueryService | 中 |
| queryFacilityByQcId | 3 表 JOIN | N4FacilityQueryService | 中 |
| getN4VesselNameById | 单表查询 | N4VesselQueryService | 低 |

#### 3.1.5 配置迁移

| 配置文件 | 当前实现 | 目标实现 |
|----------|----------|----------|
| 数据源 | springMVC-servlet.xml (C3P0) | application.yml (HikariCP) |
| 国际化 | messages_en/zh_CN/zh_TW.properties | 保留，Spring MessageSource 自动加载 |
| Hibernate | springMVC-servlet.xml | application.yml (hibernate.dialect) |
| 日志 | log4j.properties | application.yml (logging.level) |
| 系统属性 | system.properties | application.yml (qcvmt.company) |

---

## 4. 明确非目标

### 4.1 不在范围内

| 非目标项 | 原因 |
|----------|------|
| **前端 SPA 开发** | 本次仅迁移后端，前端由独立团队开发 |
| **N4 TOS 系统升级** | N4 Oracle Schema 保持不变，仅调整查询方式 |
| **数据库 Schema 变更** | MySQL 表结构保持不变，仅调整列名大小写和主键策略 |
| **业务流程重构** | 保留所有现有业务规则，仅重构技术实现 |
| **性能优化** | 本次聚焦功能迁移，性能优化作为后续任务 |
| **Redis 缓存集成** | 本次不引入缓存，后续根据性能测试结果决定 |
| **GraphQL API** | 本次仅提供 REST API，后续根据前端需求决定 |
| **WebSocket 实时推送** | 本次保持前端轮询机制，后续升级 |
| **Elasticsearch 日志** | 本次保留 MySQL 日志表，后续根据数据量决定 |
| **Kubernetes 部署** | 本次提供 Docker 镜像，部署方式由运维团队决定 |

### 4.2 明确排除

| 排除项 | 原因 |
|--------|------|
| **旧代码保留** | 不保留任何 Spring MVC 3.0.1 代码，完全重写 |
| **向后兼容** | 不保留旧 API 端点（如 /user/BusiQuery），前端需配合调整 |
| **数据迁移** | 不执行 MySQL 数据迁移，仅调整表结构（如主键策略） |
| **历史数据兼容** | 不保证旧时间字段（String）与新 LocalDateTime 完全兼容 |

---

## 5. 交付结果

### 5.1 代码交付

| 交付物 | 数量 | 说明 |
|--------|------|------|
| **build.gradle.kts** | 1 | Gradle 构建配置，包含所有依赖 |
| **settings.gradle.kts** | 1 | 项目名称配置 |
| **QcvmtApplication.java** | 1 | Spring Boot 启动类 |
| **config/** | 7 | 数据源、Security、Keycloak、CORS、Web、I18n 配置 |
| **entity/** | 10 | User, Vessel, VesselCol, VesselRefuel, ColSet, CellMatrix, ShowLog, OperationLog, BaySize, SequenceVO |
| **repository/** | 8 | UserRepository, VesselRepository, VesselColRepository, VesselRefuelRepository, ColSetRepository, CellMatrixRepository, ShowLogRepository, OperationLogRepository |
| **n4/** | 2 | N4QueryRepository, N4TableConstants |
| **security/** | 2 | JwtAuthConverter, SecurityContextHelper |
| **service/local/** | 7 | UserService, VesselService, VesselColorService, VesselRefuelService, ColorSetService, CellMatrixService, OperationLogService |
| **service/keycloak/** | 2 | KeycloakUserSyncService, KeycloakRoleMappingService |
| **service/n4/** | 4 | N4VesselQueryService, N4WorkQueueService, N4ContainerQueryService, N4FacilityQueryService |
| **dto/request/** | 9 | CreateUserRequest, UpdateUserRequest, CreateVesselRequest, UpdateVesselRequest, CreateColSetRequest, UpdateColSetRequest, SaveVesselColRequest, SaveVesselRefuelRequest, UpdateBaySizeRequest |
| **dto/response/** | 8 | UserResponse, VesselResponse, ColSetResponse, VesselColResponse, VesselRefuelResponse, TerminalView, WorkQueueResult, PageResponse |
| **dto/** | 1 | ApiResponse |
| **controller/** | 9 | UserController, VesselController, ColorSetController, VesselColorController, VesselRefuelController, BayConfigController, TerminalController, OperationLogController, ImportExportController |
| **exception/** | 3 | GlobalExceptionHandler, BusinessException, N4ConnectionException |
| **util/** | 3 | WebUtil, ImportHandler, ExportHandler |
| **resources/** | 8 | application.yml, application-dev/sit/uat/prod.yml, messages_en/zh_CN/zh_TW.properties |
| **SQL** | 1 | schema.sql (MySQL 建表脚本) |
| **测试** | ~10 | 核心 Service 单元测试 |
| **Docker** | 2 | Dockerfile, docker-compose.yml |
| **.gitignore** | 1 | Gradle + IDE 忽略规则 |

**总计：约 65 个文件**

### 5.2 文档交付

| 文档 | 说明 |
|------|------|
| **README.md** | 项目说明、启动方式、环境变量配置 |
| **API 文档** | Swagger UI 自动生成 |
| **数据库设计** | schema.sql + ER 图 |
| **部署指南** | Docker 构建、Kubernetes 部署示例 |
| **Keycloak 配置指南** | Realm 创建、Client 配置、角色映射 |

### 5.3 验收交付

| 交付物 | 说明 |
|--------|------|
| **测试报告** | 单元测试覆盖率 > 60% |
| **性能测试报告** | 关键 API 响应时间 < 500ms |
| **安全扫描报告** | 无高危漏洞 |
| **代码质量报告** | SonarQube 评级 A |

---

## 6. 验收标准

### 6.1 功能验收

#### 6.1.1 用户管理

| 验收项 | 通过标准 | 验证方式 |
|--------|----------|----------|
| 用户列表 | GET /api/users 返回分页数据 | Swagger UI 调用 |
| 创建用户 | POST /api/users 同时在 Keycloak 和 MySQL 创建 | Keycloak Admin Console + MySQL 查询验证 |
| 更新用户 | PUT /api/users/{id} 更新 qcid | MySQL 查询验证 |
| 删除用户 | DELETE /api/users/{id} 同时删除 ShowLog | MySQL 查询验证 |
| 用户日志 | GET /api/users/{id}/logs 返回登录日志 | Swagger UI 调用 |

#### 6.1.2 业务查询（核心功能）

| 验收项 | 通过标准 | 验证方式 |
|--------|----------|----------|
| 终端查询 | GET /api/terminal/query?qcNum=QC83 返回 TerminalView | Swagger UI 调用 |
| N4 SQL 正确性 | 12 个 N4 SQL 返回结果与旧系统一致 | 对比测试（同一 qcNum 查询结果） |
| CellMatrix 回退 | N4 查询失败时回退到 MySQL | 模拟 N4 Oracle 连接断开 |
| 跨 Bay 校验 | 跨 3 个 Bay 以上抛出 error_more_than_3bay | 构造测试数据验证 |
| ROB 查询 | LOAD/DISCH 类型分别调用 getROBListByBay/New | 对比测试 |

#### 6.1.3 船舶管理

| 验收项 | 通过标准 | 验证方式 |
|--------|----------|----------|
| 船舶列表 | GET /api/vessels 返回分页数据 | Swagger UI 调用 |
| 船舶唯一性 | POST /api/vessels 拒绝重复 (vesselid, deck_hold, bay) | 提交重复数据验证 |
| 船舶导入 | POST /api/import/vessel 从 Excel 导入 | 上传 Excel 文件验证 |

#### 6.1.4 认证与安全

| 验收项 | 通过标准 | 验证方式 |
|--------|----------|----------|
| JWT 验证 | 无 Token 访问 API 返回 401 | curl 调用验证 |
| 角色权限 | qcvmt-admin 可访问所有 API | curl + 不同 Token 验证 |
| 角色权限 | qcvmt-user 仅可访问 /api/terminal | curl + 不同 Token 验证 |
| 首次登录同步 | 新 Keycloak 用户首次访问时自动创建本地用户 | 删除本地用户后访问 API 验证 |

### 6.2 技术验收

#### 6.2.1 构建与部署

| 验收项 | 通过标准 | 验证方式 |
|--------|----------|----------|
| 构建成功 | `gradlew build` 无错误 | CI/CD 流水线验证 |
| 单元测试 | `gradlew test` 全部通过 | CI/CD 流水线验证 |
| 代码质量 | SonarQube 评级 A，无 Critical/Bug | SonarQube 扫描报告 |
| 测试覆盖率 | 核心 Service > 60% | JaCoCo 覆盖率报告 |
| Docker 构建 | `docker build` 成功 | 本地构建验证 |
| 健康检查 | GET /actuator/health 返回 UP | curl 调用验证 |

#### 6.2.2 性能验收

| 验收项 | 通过标准 | 验证方式 |
|--------|----------|----------|
| API 响应时间 | 本地业务 API < 200ms | JMeter 压测 |
| N4 查询时间 | Oracle 复杂查询 < 2s | JMeter 压测 |
| 连接池 | HikariCP 无连接泄漏（1 小时压测后 active=0） | HikariCP JMX 监控 |
| 并发用户 | 支持 50 并发用户 | JMeter 压测 |

#### 6.2.3 安全验收

| 验收项 | 通过标准 | 验证方式 |
|--------|----------|----------|
| SQL 注入 | 所有 N4 SQL 参数化 | 代码审查 + SQL 注入测试 |
| XSS 防护 | 输入参数转义 | OWASP ZAP 扫描 |
| CORS 配置 | 仅允许指定 Origin | curl 跨域请求验证 |
| 敏感信息 | application.yml 无硬编码密码 | 代码审查 |
| JWT 校验 | Token 签名验证失败返回 401 | 伪造 Token 测试 |

### 6.3 业务验收

#### 6.3.1 数据一致性

| 验收项 | 通过标准 | 验证方式 |
|--------|----------|----------|
| 用户数据 | 新旧系统用户数据一致 | 数据对比脚本 |
| 船舶数据 | 新旧系统船舶配置一致 | 数据对比脚本 |
| 颜色集数据 | 新旧系统颜色集一致 | 数据对比脚本 |
| 日志数据 | 新旧系统日志格式兼容 | 抽样对比 |

#### 6.3.2 业务流程

| 验收项 | 通过标准 | 验证方式 |
|--------|----------|----------|
| 装卸查询 | LOAD/DISCH 作业序列正确展示 | 操作员 UAT 测试 |
| 跨 Bay 作业 | Twin/Tandem/Quad 多箱型正确识别 | 构造测试数据验证 |
| 危险品标记 | DG 集装箱正确标记 | 对比测试 |
| 冷藏箱标记 | Powered 冷藏箱正确标记 | 对比测试 |
| 加油状态 | Refuel 状态正确显示 | 操作员 UAT 测试 |

---

## 7. 时间线与里程碑

### 7.1 阶段划分

| 阶段 | 内容 | 预计时间 | 交付物 |
|------|------|----------|--------|
| **阶段 1: 基础设施** | Gradle 构建、双数据源配置、Security 配置 | 2 天 | 可启动的空项目 |
| **阶段 2: Entity + Repository** | 实体迁移、Repository 定义、schema.sql | 2 天 | 数据库可访问 |
| **阶段 3: 本地 Service** | User/Vessel/ColorSet 等 MySQL Service | 3 天 | 本地业务可操作 |
| **阶段 4: N4 Service** | 12 个 N4 SQL 迁移 | 5 天 | 业务查询可执行 |
| **阶段 5: Keycloak 集成** | JwtAuthConverter + KeycloakUserSyncService | 2 天 | 认证可用 |
| **阶段 6: Controller** | 9 个 Controller + DTO 映射 | 3 天 | API 可调用 |
| **阶段 7: 测试与修复** | 单元测试、集成测试、Bug 修复 | 5 天 | 测试通过 |
| **阶段 8: 文档与部署** | README、Swagger UI、Dockerfile | 2 天 | 可部署 |

**总计：约 24 天**

### 7.2 关键里程碑

| 里程碑 | 时间节点 | 验收标准 |
|--------|----------|----------|
| M1: 基础框架就绪 | 第 4 天 | `gradlew build` 成功，双数据源连接正常 |
| M2: 本地业务就绪 | 第 7 天 | 用户管理、船舶管理 API 可调用 |
| M3: N4 查询就绪 | 第 12 天 | TerminalController 返回正确数据 |
| M4: 认证就绪 | 第 14 天 | Keycloak Token 验证通过 |
| M5: API 全量就绪 | 第 17 天 | 所有 API 端点可调用 |
| M6: 测试完成 | 第 22 天 | 单元测试通过率 100% |
| M7: 可部署 | 第 24 天 | Docker 镜像构建成功 |

---

## 8. 风险与缓解

### 8.1 技术风险

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|----------|
| **N4 SQL 语义丢失** | 中 | 高 | 逐一对照原始 SQL，编写详细注释，添加单元测试 |
| **双数据源事务冲突** | 低 | 高 | 明确拆分 local/n4 Service，Oracle 仅只读 |
| **Keycloak 角色映射错误** | 中 | 中 | JwtAuthConverter 仅过滤 `qcvmt-` 前缀角色 |
| **CellMatrix 回退失败** | 中 | 高 | N4VesselQueryService 实现相同回退逻辑，添加异常处理 |
| **SQL 注入漏洞** | 高 | 高 | 所有 N4 SQL 使用 PreparedStatement |

### 8.2 进度风险

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|----------|
| **N4 SQL 调试时间超预期** | 高 | 高 | 预留 2 天缓冲时间，优先迁移核心 3 个 SQL |
| **Keycloak 配置复杂** | 中 | 中 | 参照 O00017 文档，提前准备 Keycloak Realm |
| **前端联调延迟** | 中 | 中 | 后端独立交付，前端后续联调 |

### 8.3 业务风险

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|----------|
| **业务流程不一致** | 中 | 高 | UAT 测试阶段，操作员逐一对比新旧系统 |
| **数据格式不兼容** | 低 | 中 | 时间字段统一转换为 LocalDateTime，前端自行格式化 |
| **用户习惯变化** | 中 | 低 | 前端 UI 设计尽量贴近旧系统 |

---

## 9. 依赖项

### 9.1 外部依赖

| 依赖项 | 提供方 | 要求 |
|--------|--------|------|
| **Keycloak Server** | IT 基础设施团队 | 部署 Keycloak 26.0+，创建 qcvmt Realm |
| **MySQL 8.x** | DBA 团队 | 创建 qcvmt 数据库，执行 schema.sql |
| **N4 Oracle** | N4 TOS 运维团队 | 提供只读账号，允许访问 MN4O_QC_* 视图 |
| **Nginx** | 运维团队 | 配置 /api/* 反向代理 |
| **前端 SPA** | 前端开发团队 | Vue 3 / React，对接新 REST API |

### 9.2 内部依赖

| 依赖项 | 提供方 | 要求 |
|--------|--------|------|
| **N4 Schema 文档** | N4 TOS 运维团队 | 提供 MN4O_QC_* 表结构和字段说明 |
| **业务流程文档** | 业务分析师 | 提供装卸作业流程、跨 Bay 规则等 |
| **测试数据** | QA 团队 | 提供 N4 Oracle 测试数据（多 Bay、多箱型） |

---

## 10. 总结

本变更提案旨在将 QCVMT 系统从过时的 Spring MVC 3.0.1 架构迁移到现代化的 Spring Boot 3.5.16 技术栈，同时实现：

1. **安全性提升**：Keycloak OIDC 认证，移除明文密码
2. **可维护性提升**：代码拆分、单一职责、Spring Data JPA
3. **可扩展性提升**：REST API、前后端分离、Docker 化部署
4. **业务连续性**：完整保留所有业务规则和 N4 SQL 语义

预计总工期 **24 天**，交付 **65 个文件**，覆盖 **24 个 API 端点**和 **12 个 N4 Oracle SQL**。

通过本变更，QCVMT 系统将具备以下能力：

- 支持分布式部署（无 Session 依赖）
- 支持企业 SSO 集成（Keycloak）
- 支持现代前端框架（REST JSON API）
- 支持容器化部署（Docker + Kubernetes）
- 支持自动化测试（JUnit 5 + Mockito）
- 支持 API 文档自动生成（Swagger UI）
