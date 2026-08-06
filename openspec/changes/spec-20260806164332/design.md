# QCVMT 现代化迁移详细设计

## 1. 总体方案与关键决策

### 1.1 架构方案

采用 **Spring Boot 3.5.16 单体 REST API** 架构，不做微服务拆分。理由：

- 当前系统规模适合单体（~65 个文件，24 个 API 端点）
- 前端已独立部署（SPA），后端仅需提供 JSON API
- 微服务会引入不必要的分布式事务和服务注册复杂度

### 1.2 关键设计决策

| 决策项 | 选择方案 | 理由 |
|--------|----------|------|
| **Java 版本** | Java 17 | LTS 版本，支持 Record、Pattern Matching，足够满足需求 |
| **构建工具** | Gradle Kotlin DSL | Spring Boot 官方推荐，依赖管理更灵活 |
| **主数据 ORM** | Spring Data JPA | 标准 CRUD 场景适合 Repository 模式 |
| **N4 查询** | JdbcTemplate | 12 个复杂 SQL 不适合 JPA，直接 SQL 更可控 |
| **认证** | Keycloak OIDC + JWT 公钥验证 | 企业 SSO 集成需求，无需调用 Keycloak API 验证 Token |
| **API 文档** | SpringDoc OpenAPI | Spring Boot 3 原生支持，零配置 |
| **连接池** | HikariCP | Spring Boot 默认，性能优于 C3P0 |
| **DTO 映射** | MapStruct | 编译时生成映射代码，运行时零开销 |
| **Excel 处理** | POI 5.3.0 | 兼容 xlsx 格式，API 稳定 |

### 1.3 分层架构

```
┌─────────────────────────────────────────────────────────┐
│                    Controller 层                          │
│  (9 个 REST Controller，职责：请求校验、响应封装)           │
├─────────────────────────────────────────────────────────┤
│                      Service 层                          │
│  ┌──────────┬───────────┬───────────┐                   │
│  │ local/   │ keycloak/ │   n4/     │                   │
│  │ MySQL    │ Keycloak  │ Oracle    │                   │
│  │ 7 Service│ 2 Service │ 4 Service │                   │
│  └──────────┴───────────┴───────────┘                   │
├─────────────────────────────────────────────────────────┤
│                   Repository 层                          │
│  ┌────────────────┬──────────────────┐                  │
│  │ 8 JPA Repository│ N4QueryRepository│                  │
│  │ (Spring Data)   │ (JdbcTemplate)  │                  │
│  └────────────────┴──────────────────┘                  │
├─────────────────────────────────────────────────────────┤
│                   Entity / DTO 层                        │
│  10 Entity + 17 DTO (Request/Response) + ApiResponse    │
└─────────────────────────────────────────────────────────┘
```

### 1.4 数据流设计

#### 1.4.1 MySQL 写入流

```
Controller → Request DTO → Service → Repository → Entity → MySQL
                                ↓
                          业务校验
                                ↓
                          Response DTO ← Controller ← Service
```

#### 1.4.2 Oracle 只读查询流

```
Controller → Service → N4QueryRepository → JdbcTemplate → Oracle
                  ↓
            业务数据合并
                  ↓
Controller ← Response DTO ← Service
```

#### 1.4.3 Keycloak 认证流

```
请求 → JwtAuthenticationFilter → JwtAuthConverter
                                      ↓
                              提取 realm_access.roles
                                      ↓
                              过滤 qcvmt-* 前缀
                                      ↓
                              映射为 GrantedAuthority
                                      ↓
                              SecurityContext 设置
                                      ↓
                              Controller → Service
```

---

## 2. 组件职责与数据流

### 2.1 Controller 层

| Controller | 职责 | API 前缀 | 依赖 Service |
|------------|------|----------|--------------|
| **UserController** | 用户 CRUD、用户日志查询 | `/api/users` | UserService, KeycloakUserSyncService |
| **VesselController** | 船舶配置 CRUD、搜索 | `/api/vessels` | VesselService |
| **ColorSetController** | 颜色集 CRUD | `/api/color-sets` | ColorSetService |
| **VesselColorController** | 船舶颜色 CRUD | `/api/vessel-colors` | VesselColorService |
| **VesselRefuelController** | 船舶加油配置 CRUD | `/api/vessel-refuels` | VesselRefuelService |
| **BayConfigController** | Bay 尺寸查询与更新 | `/api/bay-config` | CellMatrixService |
| **TerminalController** | 终端实时查询（跨数据源合并） | `/api/terminal` | N4WorkQueueService, N4ContainerQueryService, VesselService, ColorSetService |
| **OperationLogController** | 操作日志查询、导出 | `/api/operation-logs` | OperationLogService |
| **ImportExportController** | Excel 导入导出 | `/api/import`, `/api/export` | UserService, VesselService |

**Controller 规范：**
- 每个 Controller 不超过 100 行
- 仅负责请求参数校验和响应封装
- 不包含业务逻辑
- 所有方法返回 `ApiResponse<T>`

### 2.2 Service 层

#### 2.2.1 Local Service（MySQL 读写）

| Service | 职责 | 依赖 | 事务 |
|---------|------|------|------|
| **UserService** | 用户 CRUD、登录日志记录、用户列表分页 | UserRepository, ShowLogRepository | @Transactional |
| **VesselService** | 船舶配置 CRUD、船舶搜索、唯一性校验 | VesselRepository | @Transactional |
| **ColorSetService** | 颜色集 CRUD、boxcase 唯一性校验 | ColorSetRepository | @Transactional |
| **VesselColorService** | 船舶颜色 CRUD | VesselColorRepository | @Transactional |
| **VesselRefuelService** | 船舶加油配置 CRUD | VesselRefuelRepository | @Transactional |
| **CellMatrixService** | CellMatrix 配置 CRUD、Bay 尺寸查询 | CellMatrixRepository | @Transactional |
| **OperationLogService** | 操作日志记录、查询、导出 | OperationLogRepository | @Transactional(readOnly=true) |

**Local Service 规范：**
- 每个 Service 只操作对应的 MySQL Repository
- 使用 `@Transactional` 管理事务
- 业务校验逻辑放在 Service 层（如唯一性检查）
- 跨 Service 调用在 Controller 层协调

#### 2.2.2 Keycloak Service

| Service | 职责 | 依赖 |
|---------|------|------|
| **KeycloakUserSyncService** | JWT Token 解析、首次登录自动创建本地用户、角色同步 | UserRepository, SecurityContextHelper |
| **KeycloakRoleMappingService** | 创建 Keycloak 用户、分配/移除角色 | Keycloak Admin Client |

**KeycloakUserSyncService 关键方法：**

```java
// 从 JWT Token 提取 subject（Keycloak UUID）
public String extractKeycloakSubject(Jwt jwt)

// 首次登录时自动创建本地用户
public User getOrCreateLocalUser(Jwt jwt) {
    String keycloakId = jwt.getSubject();
    String username = jwt.getClaimAsString("preferred_username");
    
    return userRepository.findByKeycloakId(keycloakId)
        .orElseGet(() -> {
            User newUser = new User();
            newUser.setKeycloakId(keycloakId);
            newUser.setUsername(username);
            newUser.setRole(extractQcvmtRole(jwt)); // qcvmt-admin → ADMIN, qcvmt-user → USER
            newUser.setCreateTime(LocalDateTime.now());
            return userRepository.save(newUser);
        });
}
```

#### 2.2.3 N4 Service（Oracle 只读）

| Service | 职责 | 涉及 N4 表 | SQL 复杂度 |
|---------|------|-----------|------------|
| **N4WorkQueueService** | 装卸订单查询、作业序列查询、跨 Bay 校验 | inv_wq, inv_wi, inv_unit, xps_craneshift, xps_pointofwork, argo_carrier_visit 等 10 表 | 高（10 表 JOIN + 子查询） |
| **N4ContainerQueryService** | ROB 箱位查询（LOAD/DISCH）、危险品/冷藏箱查询 | inv_unit_fcy_visit, inv_unit, argo_carrier_visit, ref_hazardous_material | 中（3-4 表 JOIN） |
| **N4VesselQueryService** | 船舶信息查询、CellMatrix N4 回退逻辑 | argo_carrier_visit, vsl_vsl_visit_details, vsl_vessels | 中（4 表 JOIN + MySQL 回退） |
| **N4FacilityQueryService** | QC ID 验证、岸桥设施查询 | xps_pointofwork, argo_yard, argo_facility | 低（3 表子查询） |

**N4 Service 规范：**
- 所有 SQL 使用 `N4QueryRepository` 执行
- 所有参数使用 `setParameter()` 绑定，禁止字符串拼接
- 每个 Service 方法对应一个 `N4Sql` 枚举常量
- Oracle 数据源配置为 `read-only=true`，无法执行 INSERT/UPDATE/DELETE

### 2.3 Repository 层

#### 2.3.1 JPA Repository（MySQL）

| Repository | 实体 | 继承接口 | 自定义方法 |
|------------|------|----------|------------|
| **UserRepository** | User | JpaRepository<User, Integer> | `findByUsername(String)`, `findByKeycloakId(String)`, `findAllOrderByUsername(Pageable)` |
| **VesselRepository** | Vessel | JpaRepository<Vessel, Integer> | `findByVesselidAndDeckHoldAndBay()`, `findAllOrderByVesselidAscDeckHoldAscBayAsc(Pageable)`, `findByVesselidOrDeckHoldOrBay(String, String, String, Pageable)` |
| **ColorSetRepository** | ColorSet | JpaRepository<ColorSet, Integer> | `findByBoxcase(String)`, `findAllOrderByBoxcaseAsc()` |
| **VesselColorRepository** | VesselColor | JpaRepository<VesselColor, Integer> | `findAllOrderByVesselidAscDeckHoldAscBayAsc(Pageable)`, `findByVesselidOrDeckHoldOrBay(String, String, String, Pageable)` |
| **VesselRefuelRepository** | VesselRefuel | JpaRepository<VesselRefuel, Integer> | `findAllOrderByVesselidAsc(Pageable)` |
| **CellMatrixRepository** | CellMatrix | JpaRepository<CellMatrix, Integer> | `findByTypeAndActiveOrderByIdDesc(String, String)`, `findByTypeAndRowBetweenOrderByRowAsc(String, String, String)` |
| **ShowLogRepository** | ShowLog | JpaRepository<ShowLog, Integer> | `findByQcNumBetween(LocalDateTime, LocalDateTime)`, `findDistinctVesselIds()`, `save(ShowLog)` |
| **OperationLogRepository** | OperationLog | JpaRepository<OperationLog, Integer> | `findAllOrderByCreateTimeDesc(Pageable)` |

**JPA Repository 规范：**
- 优先使用 Spring Data 方法命名规范（findBy、findAllBy、countBy）
- 自定义查询使用 `@Query` 注解
- 所有查询方法支持 `Pageable` 分页参数
- 禁止在 Repository 中编写复杂业务逻辑

#### 2.3.2 N4QueryRepository（Oracle 只读）

```java
@Component
public class N4QueryRepository {
    @Qualifier("n4JdbcTemplate")
    @Autowired
    private JdbcTemplate n4JdbcTemplate;
    
    // 执行参数化查询
    public List<Map<String, Object>> queryForList(N4Sql sql, Map<String, Object> params)
    
    // 执行参数化查询，返回单个结果
    public Map<String, Object> queryForMap(N4Sql sql, Map<String, Object> params)
    
    // 设置参数（防止 SQL 注入）
    private void setParameter(PreparedStatement ps, Object value, int index)
}
```

**N4QueryRepository 规范：**
- 所有 SQL 定义为 `N4Sql` 枚举常量
- 所有参数通过 `Map<String, Object>` 传递
- 内部使用 `JdbcTemplate.query(sql, params, RowMapper)` 执行
- 禁止字符串拼接 SQL

### 2.4 Entity 层

#### 2.4.1 JPA 实体（MySQL）

| 实体 | 表名 | 主键策略 | 关键字段 | 特殊约束 |
|------|------|----------|----------|----------|
| **User** | t_user | @GeneratedValue(IDENTITY) | keycloakId (新增), username, qcid, role, createTime | @UniqueConstraint(columns="keycloak_id") |
| **Vessel** | t_vessel | @GeneratedValue(IDENTITY) | vesselid, deckHold, bay, rowStart, rowEnd, tierStart, tierEnd | @UniqueConstraint(columns={"vesselid", "deck_hold", "bay"}) |
| **ColorSet** | t_col_set | @GeneratedValue(IDENTITY) | boxcase, color | @UniqueConstraint(columns="boxcase") |
| **VesselColor** | t_vessel_col | @GeneratedValue(IDENTITY) | vesselid, deckHold, bay, rowStart, rowEnd, tierStart, tierEnd | 无 |
| **VesselRefuel** | t_vessel_refuel | @GeneratedValue(IDENTITY) | vesselid, isRefuel | 无 |
| **CellMatrix** | t_cell_matrix | @GeneratedValue(IDENTITY) | type, row, tier, tierStart, tierEnd, active | 无 |
| **ShowLog** | t_showlog | @GeneratedValue(IDENTITY) | userId, userName, qcNum, loginTime, operation | 无 |
| **OperationLog** | t_operation_log | @GeneratedValue(IDENTITY) | userId, userName, functionName, actionType, valuesBefore, valuesAfter, createTime | 无 |

**Entity 规范：**
- 使用 Lombok `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`
- 时间字段统一使用 `LocalDateTime`
- 列名使用 `@Column(name="xxx")` 显式指定（避免 ORM 自动转换导致大小写问题）
- 所有实体添加 `@Version` 乐观锁字段（version INT DEFAULT 0）

#### 2.4.2 DTO 定义

**Request DTO（创建/更新请求）：**

| DTO | 用途 | 关键字段 |
|-----|------|----------|
| CreateUserRequest | 创建用户 | username, password, role, qcid |
| UpdateUserRequest | 更新用户 | qcid, role |
| CreateVesselRequest | 创建船舶 | vesselId, deckHold, bay, rowStart, rowEnd, tierStart, tierEnd |
| UpdateVesselRequest | 更新船舶 | rowStart, rowEnd, tierStart, tierEnd |
| CreateColorSetRequest | 创建颜色集 | boxcase, color |
| UpdateColorSetRequest | 更新颜色集 | color |
| CreateVesselColorRequest | 创建船舶颜色 | vesselid, deckHold, bay, rowStart, rowEnd, tierStart, tierEnd |
| UpdateVesselColorRequest | 更新船舶颜色 | rowStart, rowEnd, tierStart, tierEnd |
| CreateVesselRefuelRequest | 创建加油配置 | vesselid, isRefuel |
| UpdateVesselRefuelRequest | 更新加油配置 | isRefuel |
| BaySizeRequest | 更新 Bay 尺寸 | type, row, tier, tierStart, tierEnd |

**Response DTO（响应封装）：**

| DTO | 用途 | 关键字段 |
|-----|------|----------|
| UserResponse | 用户响应 | id, username, role, qcid, createTime |
| VesselResponse | 船舶响应 | id, vesselId, deckHold, bay, rowStart, rowEnd, tierStart, tierEnd |
| ColorSetResponse | 颜色集响应 | id, boxcase, color |
| VesselColorResponse | 船舶颜色响应 | id, vesselid, deckHold, bay, rowStart, rowEnd, tierStart, tierEnd |
| VesselRefuelResponse | 加油配置响应 | id, vesselid, isRefuel |
| OperationLogResponse | 操作日志响应 | id, userId, userName, functionName, actionType, valuesBefore, valuesAfter, createTime |
| ShowLogResponse | 登录日志响应 | id, userId, userName, qcNum, loginTime, operation |
| PageResponse<T> | 分页响应 | content (List<T>), totalPages, totalElements, currentPage, pageSize |

**TerminalView（核心业务响应）：**

```java
public class TerminalView {
    private String vesselId;
    private String bay;
    private String deckHold;
    private String qType; // LOAD 或 DISCH
    private String qOrder;
    private List<String> bayList; // 作业 Bay 列表
    private List<SequenceVO> sequenceList; // 作业序列
    private List<RobContainer> robList; // ROB 箱位
    private List<Vessel> vesselConfig; // 船舶配置
    private List<ColorSet> colorSets; // 颜色集
    private int workCount; // 已作业箱数
    private int robCount; // 剩余箱数
}

public class SequenceVO {
    private String containerId;
    private String slot;
    private String size;
    private String type;
    private int workSequence;
    private String bay;
    private String row;
    private String tier;
    private String status; // LOADED, DISCHARGED, PENDING
    private boolean isImo; // 危险品
    private boolean isReefer; // 冷藏箱
}
```

**ApiResponse（统一响应格式）：**

```java
public class ApiResponse<T> {
    private int code; // 0=成功，非0=错误码
    private String message; // 成功或错误描述
    private T data; // 响应数据
}
```

### 2.5 N4 SQL 定义（N4Sql 枚举）

| 枚举常量 | SQL 描述 | 涉及表 | 参数 |
|----------|----------|--------|------|
| **GET_LOAD_ORDER** | 查询当前装卸订单（LOAD 类型） | inv_wq, inv_wi, inv_unit, xps_craneshift, xps_pointofwork, argo_carrier_visit | qcNum, currentDateTime |
| **GET_DISCHARGE_ORDER** | 查询当前卸货订单（DISCH 类型） | 同上 | qcNum, currentDateTime |
| **GET_SEQUENCE_LIST** | 查询完整作业序列 | 同上 + ref_bay | qOrder |
| **CHECK_SEQUENCE_LIST** | 跨 Bay 作业校验 | 同上 | qOrder, startBay, endBay |
| **GET_CELL_MATRIX** | 查询船舶 CellMatrix 配置 | argo_carrier_visit, vsl_vsl_visit_details, vsl_vessels | vesselId, bay, deckHold |
| **GET_REMAIN_ON_BOARD** | ROB 箱位查询（LOAD） | inv_unit_fcy_visit, inv_unit, argo_carrier_visit | vesselId, bay |
| **GET_REMAIN_ON_BOARD_DISCH** | ROB 箱位查询（DISCH） | 同上 | vesselId, bay |
| **GET_HAZARD_LIST** | 危险品箱查询 | ref_hazardous_material, inv_unit | vesselId |
| **GET_TWENTY_UNIT_LIST** | 20 尺箱查询 | 同上 | vesselId, bay |
| **GET_VESSEL_INFO** | 船舶基本信息查询 | argo_carrier_visit | vesselId |
| **QUERY_QC_ID** | QC ID 验证 | xps_pointofwork, argo_yard, argo_facility | qcNum |
| **VALIDATE_QC_NUM** | QC 编号合法性校验 | 同上 | qcNum |

**N4Sql 枚举结构：**

```java
public enum N4Sql {
    GET_LOAD_ORDER("""
        SELECT wi.wi_id AS wiId, wi.visit_id AS visitId, 
               acv.vessel_id AS vesselId, ...
        FROM MN4O_QC_inv_wq wq
        INNER JOIN MN4O_QC_inv_wi wi ON wq.wq_id = wi.wq_id
        INNER JOIN MN4O_QC_inv_unit unit ON wi.unit_id = unit.unit_id
        INNER JOIN MN4O_QC_xps_craneshift cs ON wq.crane_shift_id = cs.crane_shift_id
        INNER JOIN MN4O_QC_xps_pointofwork pow ON cs.point_of_work_id = pow.point_of_work_id
        INNER JOIN MN4O_QC_argo_carrier_visit acv ON wi.visit_id = acv.visit_id
        WHERE pow.facility_id IN (
            SELECT facility_id FROM MN4O_QC_argo_facility 
            WHERE facility_name = :qcNum
        )
        AND wq.completed IS NULL
        AND wq.start_time <= :currentDateTime
        ORDER BY wq.start_time DESC
        FETCH FIRST 1 ROWS ONLY
    """),
    
    GET_DISCHARGE_ORDER("..."), // 类似结构
    GET_SEQUENCE_LIST("..."),   // 10 表 JOIN + 子查询
    // ... 其他 10 个 SQL
}
```

---

## 3. 数据模型变更

### 3.1 MySQL Schema 变更

| 表名 | 变更类型 | 变更内容 |
|------|----------|----------|
| **t_user** | 新增列 | `keycloak_id VARCHAR(36) UNIQUE NOT NULL` |
| **t_user** | 删除列 | `password` (已废弃，Keycloak 管理密码) |
| **t_user** | 类型变更 | `create_time DATETIME` (原为 VARCHAR) |
| **t_vessel** | 新增列 | `version INT DEFAULT 0` (乐观锁) |
| **t_vessel** | 约束新增 | `UNIQUE KEY uk_vessel_config (vessel_id, deck_hold, bay)` |
| **t_col_set** | 新增列 | `version INT DEFAULT 0` (乐观锁) |
| **t_col_set** | 约束新增 | `UNIQUE KEY uk_color_set (boxcase)` |
| **t_vessel_col** | 新增列 | `version INT DEFAULT 0` (乐观锁) |
| **t_vessel_refuel** | 新增列 | `version INT DEFAULT 0` (乐观锁) |
| **t_cell_matrix** | 新增列 | `version INT DEFAULT 0` (乐观锁) |
| **t_showlog** | 类型变更 | `login_time DATETIME` (原为 VARCHAR) |
| **t_operation_log** | 类型变更 | `create_time DATETIME` (原为 VARCHAR) |

### 3.2 MySQL Schema 定义（DDL）

```sql
CREATE TABLE t_user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    keycloak_id VARCHAR(36) UNIQUE NOT NULL,
    username VARCHAR(20) NOT NULL,
    qcid VARCHAR(20),
    role ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'USER',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INT DEFAULT 0
);

CREATE TABLE t_vessel (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vessel_id VARCHAR(20) NOT NULL,
    deck_hold ENUM('DECK', 'HOLD') NOT NULL,
    bay VARCHAR(10) NOT NULL,
    row_start VARCHAR(10),
    row_end VARCHAR(10),
    tier_start VARCHAR(10),
    tier_end VARCHAR(10),
    version INT DEFAULT 0,
    UNIQUE KEY uk_vessel_config (vessel_id, deck_hold, bay)
);

CREATE TABLE t_col_set (
    id INT AUTO_INCREMENT PRIMARY KEY,
    boxcase VARCHAR(100) UNIQUE NOT NULL,
    color VARCHAR(20) NOT NULL,
    version INT DEFAULT 0
);

CREATE TABLE t_vessel_col (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vessel_id VARCHAR(20) NOT NULL,
    deck_hold ENUM('DECK', 'HOLD') NOT NULL,
    bay VARCHAR(10) NOT NULL,
    row_start VARCHAR(10),
    row_end VARCHAR(10),
    tier_start VARCHAR(10),
    tier_end VARCHAR(10),
    version INT DEFAULT 0
);

CREATE TABLE t_vessel_refuel (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vessel_id VARCHAR(20) NOT NULL,
    is_refuel TINYINT(1) NOT NULL DEFAULT 0,
    version INT DEFAULT 0
);

CREATE TABLE t_cell_matrix (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    row VARCHAR(10),
    tier VARCHAR(10),
    tier_start VARCHAR(10),
    tier_end VARCHAR(10),
    active TINYINT(1) NOT NULL DEFAULT 1,
    version INT DEFAULT 0
);

CREATE TABLE t_showlog (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    user_name VARCHAR(20) NOT NULL,
    qc_num VARCHAR(100),
    login_time DATETIME NOT NULL,
    operation VARCHAR(50),
    INDEX idx_login_time (login_time)
);

CREATE TABLE t_operation_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    user_name VARCHAR(20) NOT NULL,
    function_name VARCHAR(100) NOT NULL,
    action_type ENUM('CREATE', 'UPDATE', 'DELETE', 'IMPORT') NOT NULL,
    values_before TEXT,
    values_after TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_create_time (create_time)
);
```

### 3.3 Oracle Schema（N4 只读，无需变更）

当前 N4 Oracle Schema 为 `MN4O_QC`，包含以下关键视图/表：

| 表/视图名 | 用途 | 访问方式 |
|-----------|------|----------|
| inv_wq | 工作队列 | SELECT ONLY |
| inv_wi | 工作项 | SELECT ONLY |
| inv_unit | 箱单元 | SELECT ONLY |
| inv_unit_fcy_visit | 箱堆场访问 | SELECT ONLY |
| inv_unit_yrd_visit | 箱堆场访问 | SELECT ONLY |
| inv_goods | 货物 | SELECT ONLY |
| xps_pointofwork | 工作点（QC 编号） | SELECT ONLY |
| xps_craneshift | 岸桥班次 | SELECT ONLY |
| ref_equipment | 设备 | SELECT ONLY |
| ref_bay | Bay 定义 | SELECT ONLY |
| ref_hazardous_material | 危险品 | SELECT ONLY |
| argo_carrier_visit | 船舶访问 | SELECT ONLY |
| argo_yard | 堆场 | SELECT ONLY |
| argo_facility | 设施 | SELECT ONLY |
| vsl_vessels | 船舶 | SELECT ONLY |
| vsl_vsl_visit_details | 船舶访问详情 | SELECT ONLY |

**N4 Schema 配置：**
- 连接参数通过环境变量注入（`N4_HOST`, `N4_PORT`, `N4_SERVICE` 等）
- 数据源配置为 `read-only=true`
- HikariCP 最大连接数 20，最小空闲连接数 5

---

## 4. 接口与配置变更

### 4.1 REST API 端点清单

#### 4.1.1 用户管理 API

| HTTP 方法 | 路径 | 功能 | 请求参数 | 响应 | 权限 |
|-----------|------|------|----------|------|------|
| GET | /api/users | 用户列表（分页） | page, size, sortBy | ApiResponse<PageResponse<UserResponse>> | qcvmt-admin |
| GET | /api/users/{id} | 查询单个用户 | 路径参数 id | ApiResponse<UserResponse> | qcvmt-admin |
| POST | /api/users | 创建用户 | CreateUserRequest | ApiResponse<UserResponse> | qcvmt-admin |
| PUT | /api/users/{id} | 更新用户 | 路径参数 id + UpdateUserRequest | ApiResponse<UserResponse> | qcvmt-admin |
| DELETE | /api/users/{id} | 删除用户 | 路径参数 id | ApiResponse<Void> | qcvmt-admin |
| GET | /api/users/{id}/logs | 查询用户登录日志 | 路径参数 id + page, size | ApiResponse<PageResponse<ShowLogResponse>> | qcvmt-admin, qcvmt-user |
| GET | /api/users/me | 获取当前登录用户 | 无（从 JWT 解析） | ApiResponse<UserResponse> | qcvmt-admin, qcvmt-user |

#### 4.1.2 船舶管理 API

| HTTP 方法 | 路径 | 功能 | 请求参数 | 响应 | 权限 |
|-----------|------|------|----------|------|------|
| GET | /api/vessels | 船舶列表（分页） | page, size, vesselId, deckHold, bay | ApiResponse<PageResponse<VesselResponse>> | qcvmt-admin |
| GET | /api/vessels/{id} | 查询单个船舶 | 路径参数 id | ApiResponse<VesselResponse> | qcvmt-admin |
| POST | /api/vessels | 创建船舶 | CreateVesselRequest | ApiResponse<VesselResponse> | qcvmt-admin |
| PUT | /api/vessels/{id} | 更新船舶 | 路径参数 id + UpdateVesselRequest | ApiResponse<VesselResponse> | qcvmt-admin |
| DELETE | /api/vessels/{id} | 删除船舶 | 路径参数 id | ApiResponse<Void> | qcvmt-admin |
| GET | /api/vessels/search | 船舶搜索 | vesselId, deckHold, bay, page, size | ApiResponse<PageResponse<VesselResponse>> | qcvmt-admin |

#### 4.1.3 颜色集管理 API

| HTTP 方法 | 路径 | 功能 | 请求参数 | 响应 | 权限 |
|-----------|------|------|----------|------|------|
| GET | /api/color-sets | 颜色集列表 | 无 | ApiResponse<List<ColorSetResponse>> | qcvmt-admin, qcvmt-user |
| GET | /api/color-sets/{id} | 查询单个颜色集 | 路径参数 id | ApiResponse<ColorSetResponse> | qcvmt-admin |
| POST | /api/color-sets | 创建颜色集 | CreateColorSetRequest | ApiResponse<ColorSetResponse> | qcvmt-admin |
| PUT | /api/color-sets/{id} | 更新颜色集 | 路径参数 id + UpdateColorSetRequest | ApiResponse<ColorSetResponse> | qcvmt-admin |
| DELETE | /api/color-sets/{id} | 删除颜色集 | 路径参数 id | ApiResponse<Void> | qcvmt-admin |

#### 4.1.4 船舶颜色配置 API

| HTTP 方法 | 路径 | 功能 | 请求参数 | 响应 | 权限 |
|-----------|------|------|----------|------|------|
| GET | /api/vessel-colors | 船舶颜色列表（分页） | page, size, vesselId, deckHold, bay | ApiResponse<PageResponse<VesselColorResponse>> | qcvmt-admin |
| GET | /api/vessel-colors/{id} | 查询单个船舶颜色 | 路径参数 id | ApiResponse<VesselColorResponse> | qcvmt-admin |
| POST | /api/vessel-colors | 创建船舶颜色 | CreateVesselColorRequest | ApiResponse<VesselColorResponse> | qcvmt-admin |
| PUT | /api/vessel-colors/{id} | 更新船舶颜色 | 路径参数 id + UpdateVesselColorRequest | ApiResponse<VesselColorResponse> | qcvmt-admin |
| DELETE | /api/vessel-colors/{id} | 删除船舶颜色 | 路径参数 id | ApiResponse<Void> | qcvmt-admin |

#### 4.1.5 船舶加油配置 API

| HTTP 方法 | 路径 | 功能 | 请求参数 | 响应 | 权限 |
|-----------|------|------|----------|------|------|
| GET | /api/vessel-refuels | 加油配置列表（分页） | page, size | ApiResponse<PageResponse<VesselRefuelResponse>> | qcvmt-admin |
| GET | /api/vessel-refuels/{id} | 查询单个加油配置 | 路径参数 id | ApiResponse<VesselRefuelResponse> | qcvmt-admin |
| POST | /api/vessel-refuels | 创建加油配置 | CreateVesselRefuelRequest | ApiResponse<VesselRefuelResponse> | qcvmt-admin |
| PUT | /api/vessel-refuels/{id} | 更新加油配置 | 路径参数 id + UpdateVesselRefuelRequest | ApiResponse<VesselRefuelResponse> | qcvmt-admin |
| DELETE | /api/vessel-refuels/{id} | 删除加油配置 | 路径参数 id | ApiResponse<Void> | qcvmt-admin |

#### 4.1.6 Bay 尺寸配置 API

| HTTP 方法 | 路径 | 功能 | 请求参数 | 响应 | 权限 |
|-----------|------|------|----------|------|------|
| GET | /api/bay-config | 查询 Bay 尺寸配置 | type, active | ApiResponse<List<CellMatrixResponse>> | qcvmt-admin |
| PUT | /api/bay-config | 更新 Bay 尺寸配置 | BaySizeRequest | ApiResponse<CellMatrixResponse> | qcvmt-admin |

#### 4.1.7 终端实时查询 API

| HTTP 方法 | 路径 | 功能 | 请求参数 | 响应 | 权限 |
|-----------|------|------|----------|------|------|
| GET | /api/terminal/query | 终端实时查询 | qcNum, vesselId, bay, deckHold | ApiResponse<TerminalView> | qcvmt-admin, qcvmt-user |

#### 4.1.8 操作日志 API

| HTTP 方法 | 路径 | 功能 | 请求参数 | 响应 | 权限 |
|-----------|------|------|----------|------|------|
| GET | /api/operation-logs | 操作日志列表（分页） | page, size | ApiResponse<PageResponse<OperationLogResponse>> | qcvmt-admin |
| GET | /api/operation-logs/export | 导出操作日志 | startTime, endTime | Excel 文件下载 | qcvmt-admin |

#### 4.1.9 导入导出 API

| HTTP 方法 | 路径 | 功能 | 请求参数 | 响应 | 权限 |
|-----------|------|------|----------|------|------|
| POST | /api/import/excel | 导入船舶数据 | MultipartFile (xlsx) | ApiResponse<List<VesselResponse>> | qcvmt-admin |
| GET | /api/export/vessel | 导出船舶数据 | 无 | Excel 文件下载 | qcvmt-admin |
| GET | /api/export/logs | 导出登录日志 | startTime, endTime | Excel 文件下载 | qcvmt-admin |

### 4.2 统一响应格式

```json
// 成功响应
{
  "code": 0,
  "message": "success",
  "data": {
    // 业务数据
  }
}

// 分页响应
{
  "code": 0,
  "message": "success",
  "data": {
    "content": [...],
    "totalPages": 10,
    "totalElements": 100,
    "currentPage": 1,
    "pageSize": 10
  }
}

// 错误响应
{
  "code": 40001,
  "message": "用户名已存在"
}
```

### 4.3 错误码规范

| 错误码 | 含义 | 使用场景 |
|--------|------|----------|
| 0 | 成功 | 所有成功响应 |
| 40001 | 请求参数错误 | @Valid 校验失败 |
| 40002 | 用户名已存在 | 创建用户时 username 重复 |
| 40003 | 船舶配置已存在 | 创建船舶时 (vesselid, deckHold, bay) 重复 |
| 40004 | 颜色集已存在 | 创建颜色集时 boxcase 重复 |
| 40401 | 用户不存在 | 查询/更新/删除用户时 |
| 40402 | 船舶不存在 | 查询/更新/删除船舶时 |
| 40403 | 颜色集不存在 | 查询/更新/删除颜色集时 |
| 50001 | N4 Oracle 连接失败 | N4 数据源连接超时或断开 |
| 50002 | N4 查询失败 | N4 SQL 执行异常 |
| 50003 | 内部服务器错误 | 未捕获异常 |
| 40101 | 未认证 | JWT Token 缺失或失效 |
| 40301 | 权限不足 | 角色不匹配 |

### 4.4 配置文件变更

#### 4.4.1 application.yml 结构

```yaml
spring:
  application:
    name: qcvmt
  datasource:
    mysql:
      url: ${MYSQL_URL:jdbc:mysql://localhost:3306/qcvmt}
      username: ${MYSQL_USERNAME:root}
      password: ${MYSQL_PASSWORD:}
      driver-class-name: com.mysql.cj.jdbc.Driver
      hikari:
        maximum-pool-size: 20
        minimum-idle: 5
        connection-timeout: 30000
        idle-timeout: 600000
    n4:
      url: ${N4_URL:jdbc:oracle:thin:@localhost:1521:N4DB}
      username: ${N4_USERNAME:}
      password: ${N4_PASSWORD:}
      driver-class-name: oracle.jdbc.OracleDriver
      hikari:
        maximum-pool-size: 20
        minimum-idle: 5
        connection-timeout: 30000
        read-only: true
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_URL:http://localhost:8180}/realms/qcvmt
          jwk-set-uri: ${KEYCLOAK_URL:http://localhost:8180}/realms/qcvmt/protocol/openid-connect/certs

server:
  port: 8080
  servlet:
    context-path: /api

logging:
  level:
    com.mtl.qcvmt: ${LOG_LEVEL:INFO}
    org.springframework.security: ${SECURITY_LOG_LEVEL:INFO}
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/qcvmt.log
    max-size: 10MB
    max-history: 30

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method
```

#### 4.4.2 多环境配置文件

| 文件 | 用途 | 关键配置差异 |
|------|------|--------------|
| application-dev.yml | 开发环境 | 日志级别 DEBUG，MySQL/N4 连接本地 |
| application-sit.yml | 测试环境 | MySQL/N4 连接 SIT 服务器 |
| application-uat.yml | 预生产环境 | MySQL/N4 连接 UAT 服务器 |
| application-prod.yml | 生产环境 | 日志级别 WARN，MySQL/N4 连接生产服务器，密码通过环境变量注入 |

#### 4.4.3 国际化配置文件（保留）

| 文件 | 用途 | 示例键值 |
|------|------|----------|
| messages_en.properties | 英文 | error.user.not.found=User not found |
| messages_zh_CN.properties | 简体中文 | error.user.not.found=用户不存在 |
| messages_zh_TW.properties | 繁体中文 | error.user.not.found=使用者不存在 |

**使用方式：**
```java
@Autowired
private MessageSource messageSource;

String errorMsg = messageSource.getMessage("error.user.not.found", null, LocaleContextHolder.getLocale());
```

---

## 5. 错误处理与容错设计

### 5.1 全局异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @Autowired
    private MessageSource messageSource;
    
    // 业务异常处理
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        return ApiResponse.error(ex.getErrorCode(), ex.getMessage());
    }
    
    // 参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ApiResponse.error(40001, errorMsg);
    }
    
    // N4 Oracle 连接异常
    @ExceptionHandler(N4ConnectionException.class)
    public ApiResponse<Void> handleN4ConnectionException(N4ConnectionException ex) {
        log.error("N4 Oracle connection failed", ex);
        return ApiResponse.error(50001, "N4 数据库连接失败，请稍后重试");
    }
    
    // N4 查询异常
    @ExceptionHandler(N4QueryException.class)
    public ApiResponse<Void> handleN4QueryException(N4QueryException ex) {
        log.error("N4 query failed", ex);
        return ApiResponse.error(50002, "N4 查询失败，请联系管理员");
    }
    
    // 权限异常
    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<Void> handleAccessDeniedException(AccessDeniedException ex) {
        return ApiResponse.error(40301, "权限不足");
    }
    
    // 未认证异常
    @ExceptionHandler(AuthenticationException.class)
    public ApiResponse<Void> handleAuthenticationException(AuthenticationException ex) {
        return ApiResponse.error(40101, "未认证，请先登录");
    }
    
    // 未知异常兜底
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception ex) {
        log.error("Unexpected error", ex);
        return ApiResponse.error(50003, "系统内部错误");
    }
}
```

### 5.2 N4 查询容错策略

#### 5.2.1 CellMatrix N4 回退逻辑

```java
@Service
public class N4VesselQueryService {
    
    public List<CellMatrix> getCellMatrix(String vesselId, String bay, String deckHold) {
        try {
            // 优先从 N4 Oracle 查询
            return queryCellMatrixFromN4(vesselId, bay, deckHold);
        } catch (N4ConnectionException | N4QueryException ex) {
            log.warn("N4 查询失败，回退到 MySQL: vesselId={}, bay={}, deckHold={}", 
                     vesselId, bay, deckHold, ex);
            // N4 查询失败时回退到 MySQL
            return fallbackToMySQL(vesselId, bay, deckHold);
        }
    }
    
    private List<CellMatrix> queryCellMatrixFromN4(String vesselId, String bay, String deckHold) {
        Map<String, Object> params = new HashMap<>();
        params.put("vesselId", vesselId);
        params.put("bay", bay);
        params.put("deckHold", deckHold);
        
        List<Map<String, Object>> rows = n4QueryRepository.queryForList(N4Sql.GET_CELL_MATRIX, params);
        
        return rows.stream()
            .map(row -> convertToCellMatrix(row))
            .collect(Collectors.toList());
    }
    
    private List<CellMatrix> fallbackToMySQL(String vesselId, String bay, String deckHold) {
        // 从 MySQL t_cell_matrix 表查询默认配置
        return cellMatrixRepository.findByTypeAndActiveOrderByIdDesc(vesselId + "_" + bay + "_" + deckHold, "Y");
    }
}
```

#### 5.2.2 N4 连接重试机制

```java
@Component
public class N4QueryRepository {
    
    @Retryable(
        value = {N4ConnectionException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public List<Map<String, Object>> queryForList(N4Sql sql, Map<String, Object> params) {
        try {
            return n4JdbcTemplate.query(sql.getSql(), params, new ColumnMapRowMapper());
        } catch (DataAccessException ex) {
            if (isConnectionException(ex)) {
                throw new N4ConnectionException("N4 Oracle connection failed", ex);
            }
            throw new N4QueryException("N4 query failed", ex);
        }
    }
    
    private boolean isConnectionException(DataAccessException ex) {
        return ex.getCause() instanceof SQLTransientConnectionException
            || ex.getMessage().contains("Connection refused")
            || ex.getMessage().contains("Connection timed out");
    }
}
```

### 5.3 业务校验策略

| 校验项 | 校验位置 | 校验逻辑 |
|--------|----------|----------|
| **用户名唯一性** | UserService.createUser() | 查询 UserRepository.findByUsername()，存在则抛出 BusinessException |
| **船舶配置唯一性** | VesselService.createVessel() | 查询 VesselRepository.findByVesselidAndDeckHoldAndBay()，存在则抛出 BusinessException |
| **颜色集唯一性** | ColorSetService.createColorSet() | 查询 ColorSetRepository.findByBoxcase()，存在则抛出 BusinessException |
| **QC 编号合法性** | TerminalController.query() | 调用 N4FacilityQueryService.validateQcNum()，无效则抛出 BusinessException |
| **Keycloak ID 非空** | KeycloakUserSyncService | JWT Token 必须包含 sub 字段，否则抛出 AuthenticationException |
| **乐观锁冲突** | 所有 update 方法 | JPA @Version 自动处理，抛出 OptimisticLockException 时返回 409 错误 |

---

## 6. 兼容性与迁移策略

### 6.1 数据迁移策略

#### 6.1.1 MySQL 数据迁移

**迁移步骤：**

1. **备份现有数据**
   ```sql
   CREATE TABLE t_user_backup AS SELECT * FROM t_user;
   CREATE TABLE t_vessel_backup AS SELECT * FROM t_vessel;
   -- ... 其他表
   ```

2. **Schema 变更**
   ```sql
   -- t_user 表变更
   ALTER TABLE t_user ADD COLUMN keycloak_id VARCHAR(36) AFTER id;
   ALTER TABLE t_user DROP COLUMN password;
   ALTER TABLE t_user MODIFY create_time DATETIME;
   ALTER TABLE t_user ADD UNIQUE KEY uk_keycloak_id (keycloak_id);
   
   -- t_vessel 表变更
   ALTER TABLE t_vessel ADD COLUMN version INT DEFAULT 0;
   ALTER TABLE t_vessel ADD UNIQUE KEY uk_vessel_config (vessel_id, deck_hold, bay);
   
   -- 其他表类似...
   ```

3. **时间字段转换**
   ```sql
   -- 假设原时间格式为 'yyyy-MM-dd HH:mm:ss' 字符串
   UPDATE t_user SET create_time = STR_TO_DATE(create_time_str, '%Y-%m-%d %H:%i:%s');
   UPDATE t_showlog SET login_time = STR_TO_DATE(login_time_str, '%Y-%m-%d %H:%i:%s');
   UPDATE t_operation_log SET create_time = STR_TO_DATE(create_time_str, '%Y-%m-%d %H:%i:%s');
   ```

4. **Keycloak ID 初始化**
   ```sql
   -- 临时使用 UUID 填充，后续由 KeycloakUserSyncService 自动更新
   UPDATE t_user SET keycloak_id = UUID() WHERE keycloak_id IS NULL;
   ```

#### 6.1.2 Oracle 数据（无需迁移）

N4 Oracle 数据为只读，无需迁移。仅需确保新系统能够正确连接到 N4 Schema `MN4O_QC`。

### 6.2 API 兼容性

#### 6.2.1 旧系统 API 废弃

旧系统 API（`/user/*`, `/cell/*`）将在迁移完成后完全废弃，不提供向后兼容。

**迁移期间双系统并行策略：**

1. 新系统部署在独立端口（如 8081）
2. Nginx 配置路由转发：
   - `/api/*` → 新系统 (8081)
   - `/user/*`, `/cell/*` → 旧系统 (8080)
3. 前端逐步切换到新 API
4. 验证无误后下线旧系统

#### 6.2.2 前端适配指南

**请求头变更：**

```javascript
// 旧系统：Cookie + Session
// 新系统：JWT Bearer Token
headers: {
  'Authorization': `Bearer ${accessToken}`,
  'Content-Type': 'application/json'
}
```

**响应格式变更：**

```javascript
// 旧系统：XML + HTML 字符串
<response>
  <type>LOAD</type>
  <table_info>
    <table>...</table>
  </table_info>
</response>

// 新系统：JSON
{
  "code": 0,
  "message": "success",
  "data": {
    "qType": "LOAD",
    "vesselId": "V001",
    "bay": "10",
    "sequenceList": [...],
    "robList": [...]
  }
}
```

### 6.3 Keycloak 集成配置

#### 6.3.1 Keycloak Realm 配置要求

| 配置项 | 值 | 说明 |
|--------|-----|------|
| Realm Name | qcvmt | 项目专用 Realm |
| Client ID | qcvmt-app | 前端 SPA 客户端 |
| Client Protocol | openid-connect | OIDC 协议 |
| Access Type | public | 前端无需 Client Secret |
| Valid Redirect URIs | http://localhost:3000/* | 前端回调地址 |
| Web Origins | http://localhost:3000 | CORS 允许的前端域名 |

#### 6.3.2 Keycloak Realm Roles

| 角色名 | 用途 | 权限映射 |
|--------|------|----------|
| qcvmt-admin | 管理员 | 可访问所有 qcvmt-admin 标记的 API |
| qcvmt-user | 普通用户 | 可访问 qcvmt-user 标记的 API |

**角色分配流程：**

1. Keycloak 管理员在 Realm 中创建角色 `qcvmt-admin` 和 `qcvmt-user`
2. 为新用户分配对应角色
3. 用户登录后 JWT Token 中包含 `realm_access.roles` 数组
4. Spring Security 自动映射为 `GrantedAuthority("ROLE_qcvmt_admin")`

---

## 7. 回滚策略

### 7.1 回滚触发条件

| 条件 | 触发操作 |
|------|----------|
| N4 Oracle 连接失败率 > 10% | 立即回滚到旧系统 |
| 关键 API（/api/terminal/query）错误率 > 5% | 立即回滚到旧系统 |
| 数据不一致（新旧系统对比失败） | 立即回滚到旧系统 |
| 认证失败率 > 1% | 立即回滚到旧系统 |

### 7.2 回滚步骤

#### 7.2.1 应用层回滚

1. 停止新系统容器
2. 启动旧系统容器（WAR 包）
3. Nginx 路由切换回旧系统（`/user/*`, `/cell/*` → 8080）
4. 验证旧系统可正常访问

#### 7.2.2 数据库回滚

1. 停止新系统写入
2. 从备份恢复 MySQL 数据（如有 Schema 变更）
   ```sql
   DROP TABLE t_user;
   CREATE TABLE t_user AS SELECT * FROM t_user_backup;
   -- ... 其他表
   ```
3. 验证旧系统数据一致性

### 7.3 回滚验证

| 检查项 | 验证方法 |
|--------|----------|
| 旧系统启动成功 | curl http://localhost:8080/user/all |
| 用户可正常登录 | 浏览器访问旧系统登录页 |
| 终端查询正常 | 执行关键路径测试用例 |
| 数据完整性 | 对比新旧系统关键表记录数 |
| N4 查询正常 | 使用同一 qcNum 对比新旧系统输出 |

### 7.4 双系统并行期

| 阶段 | 持续时间 | 说明 |
|------|----------|------|
| 并行期 | 2 周 | 新旧系统同时运行，Nginx 路由 50% 流量到新系统 |
| 观察期 | 1 周 | 监控新系统错误率、响应时间、数据一致性 |
| 切换期 | 1 天 | 100% 流量切换到新系统，旧系统保留但不再接收请求 |
| 下线期 | 1 周后 | 确认无问题后完全下线旧系统 |

---

## 8. 测试策略

### 8.1 单元测试

#### 8.1.1 测试范围与优先级

| 优先级 | 测试对象 | 测试框架 | 覆盖率目标 |
|--------|----------|----------|------------|
| **P0（必须）** | N4WorkQueueService（12 个 SQL 的输入输出） | JUnit 5 + Mockito | 90% |
| **P0（必须）** | N4ContainerQueryService（ROB 查询） | JUnit 5 + Mockito | 90% |
| **P0（必须）** | N4VesselQueryService（CellMatrix 回退逻辑） | JUnit 5 + Mockito | 90% |
| **P0（必须）** | KeycloakUserSyncService（首次登录创建用户） | JUnit 5 + Mockito | 90% |
| **P1（建议）** | 所有 Local Service（CRUD + 唯一性校验） | JUnit 5 + Mockito | 80% |
| **P2（可选）** | Controller 层（请求参数校验） | MockMvc | 60% |
| **P2（可选）** | MapStruct DTO 映射 | JUnit 5 | 70% |

#### 8.1.2 关键测试用例

**N4WorkQueueService 测试：**

```java
@SpringBootTest
class N4WorkQueueServiceTest {

    @MockBean
    private N4QueryRepository n4QueryRepository;

    @Autowired
    private N4WorkQueueService service;

    @Test
    void getDischargeOrder_正常卸货订单() {
        // 准备 Mock 数据
        Map<String, Object> mockResult = Map.of(
            "qOrder", "Q001",
            "qType", "DISCH",
            "vesselId", "V001"
        );
        when(n4QueryRepository.queryForMap(eq(N4Sql.GET_DISCHARGE_ORDER), any()))
            .thenReturn(mockResult);

        // 执行
        WorkQueueResult result = service.getDischargeOrder("QC83");

        // 验证
        assertThat(result.getQOrder()).isEqualTo("Q001");
        assertThat(result.getQType()).isEqualTo("DISCH");
    }

    @Test
    void getSequenceList_完整作业序列() {
        // 准备 Mock 数据（多条序列）
        List<Map<String, Object>> mockResults = List.of(
            Map.of("containerId", "C001", "bay", "10", "row", "02", "tier", "82"),
            Map.of("containerId", "C002", "bay", "10", "row", "04", "tier", "82")
        );
        when(n4QueryRepository.queryForList(eq(N4Sql.GET_SEQUENCE_LIST), any()))
            .thenReturn(mockResults);

        // 执行
        List<SequenceVO> sequences = service.getSequenceList("Q001");

        // 验证
        assertThat(sequences).hasSize(2);
        assertThat(sequences.get(0).getContainerId()).isEqualTo("C001");
    }

    @Test
    void checkSequenceList_跨3个Bay以上抛出异常() {
        // 准备 Mock 数据
        Map<String, Object> mockResult = Map.of("minBay", "01", "maxBay", "05");
        when(n4QueryRepository.queryForMap(eq(N4Sql.CHECK_SEQUENCE_LIST), any()))
            .thenReturn(mockResult);

        // 执行并验证异常
        BusinessException ex = assertThrows(BusinessException.class, () -> {
            service.checkSequenceList("Q001");
        });
        assertThat(ex.getErrorCode()).isEqualTo(40005);
    }
}
```

**N4VesselQueryService 回退测试：**

```java
@Test
void getCellMatrix_N4查询失败回退到MySQL() {
    // N4 查询抛出异常
    when(n4QueryRepository.queryForList(eq(N4Sql.GET_CELL_MATRIX), any()))
        .thenThrow(new N4ConnectionException("Connection refused"));

    // MySQL 回退数据
    List<CellMatrix> mysqlData = List.of(new CellMatrix("V001", "10", "H", "82", "88", "1"));
    when(cellMatrixRepository.findByTypeAndActiveOrderByIdDesc(anyString(), eq("Y")))
        .thenReturn(mysqlData);

    // 执行
    List<CellMatrix> result = service.getCellMatrix("V001", "10", "H");

    // 验证回退到 MySQL 数据
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getVesselId()).isEqualTo("V001");
}
```

**KeycloakUserSyncService 测试：**

```java
@Test
void getOrCreateLocalUser_首次登录自动创建用户() {
    // JWT Mock
    Jwt jwt = Jwt.withTokenValue("token")
        .subject("kc-uuid-123")
        .claim("preferred_username", "testuser")
        .claim("realm_access", Map.of("roles", List.of("qcvmt-user")))
        .build();

    // UserRepository 返回空
    when(userRepository.findByKeycloakId("kc-uuid-123")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    // 执行
    User user = service.getOrCreateLocalUser(jwt);

    // 验证
    assertThat(user.getKeycloakId()).isEqualTo("kc-uuid-123");
    assertThat(user.getUsername()).isEqualTo("testuser");
    assertThat(user.getRole()).isEqualTo("USER");
    verify(userRepository).save(any(User.class));
}
```

### 8.2 集成测试

#### 8.2.1 数据库集成测试

```java
@SpringBootTest
@AutoConfigureTestDatabase
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByKeycloakId_正确查找() {
        User user = new User();
        user.setKeycloakId("test-keycloak-id");
        user.setUsername("testuser");
        user.setRole("ADMIN");
        user.setCreateTime(LocalDateTime.now());
        userRepository.save(user);

        Optional<User> found = userRepository.findByKeycloakId("test-keycloak-id");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @Sql(scripts = "/test-data/vessel-data.sql")
    void findAllOrderByVesselidAsc_正确排序() {
        Page<Vessel> page = vesselRepository.findAllOrderByVesselidAscDeckHoldAscBayAsc(Pageable.ofSize(10));
        assertThat(page.getContent()).isNotEmpty();
        // 验证排序正确性
    }
}
```

#### 8.2.2 API 端点集成测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class TerminalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "qcvmt-admin")
    void query_正常返回TerminalView() throws Exception {
        mockMvc.perform(get("/api/terminal/query")
                .param("qcNum", "QC83")
                .param("vesselId", "V001")
                .param("bay", "10")
                .param("deckHold", "D"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.vesselId").value("V001"));
    }

    @Test
    void query_无Token返回401() throws Exception {
        mockMvc.perform(get("/api/terminal/query")
                .param("qcNum", "QC83"))
            .andExpect(status().isUnauthorized());
    }
}
```

### 8.3 对比测试（新旧系统）

| 测试场景 | 输入 | 验证方法 |
|----------|------|----------|
| 终端查询一致性 | 同一 qcNum + vesselId + bay | 对比新旧系统 TerminalView 字段值 |
| N4 SQL 输出一致性 | 同一 qcNum | 对比 12 个 N4 SQL 返回结果集 |
| CRUD 数据一致性 | 同一请求参数 | 对比新旧系统数据库记录 |
| 错误处理一致性 | 无效参数 | 对比新旧系统错误码和错误信息 |

### 8.4 性能测试基准

| API 端点 | 目标响应时间 | 并发用户数 | 测试工具 |
|----------|-------------|------------|----------|
| GET /api/terminal/query | < 2s（含 N4 Oracle 查询） | 50 | JMeter |
| GET /api/users | < 200ms | 50 | JMeter |
| POST /api/vessels | < 200ms | 50 | JMeter |
| GET /api/vessels | < 200ms | 50 | JMeter |
| POST /api/import/excel (1000行) | < 5s | 10 | JMeter |

### 8.5 安全测试

| 测试项 | 方法 | 通过标准 |
|--------|------|----------|
| **SQL 注入** | OWASP ZAP + 手动构造恶意参数 | 所有 N4 SQL 使用参数化查询，无注入点 |
| **XSS** | OWASP ZAP 扫描 | 输入参数转义，输出 HTML 编码 |
| **CSRF** | curl 模拟跨站请求 | REST API 无状态，JWT 验证防止 CSRF |
| **权限绕过** | 使用 qcvmt-user Token 访问 admin API | 返回 403 |
| **JWT 伪造** | 使用伪造 Token 访问 API | 返回 401 |
| **敏感信息泄露** | 代码审查 + OWASP ZAP | API 响应不包含密码、密钥等敏感信息 |