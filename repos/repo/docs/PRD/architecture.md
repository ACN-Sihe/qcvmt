# 系统架构文档（Architecture.md）

## 1. 概述

本项目 **QCVMT**（Quay Crane Vessel Management Terminal）是一个用于码头岸桥（Quay Crane）集装箱作业管理的 Web 应用程序。系统采用传统的 **单体架构（Monolithic）** 风格，基于 Java EE 技术栈构建，部署为 WAR 包运行在应用服务器上。

系统核心业务围绕集装箱装卸作业的可视化监控与管理，包括：
- 岸桥作业序列的实时展示（装船/卸船）
- 船舶舱位矩阵（Cell Matrix）的配置与管理
- 用户权限与操作日志管理
- 船舶加油区域配置
- 危险品集装箱标识

由于业务领域相对集中且规模适中，当前采用单体架构是合理的选择。未分解为限界上下文的主要原因包括：
1. 业务边界清晰但耦合度较高，所有功能共享同一数据源
2. 用户群体单一（码头操作人员和管理员），无需独立的服务隔离
3. 系统复杂度主要体现在业务逻辑而非分布式协调

## 2. 架构风格与决策

- **风格**：单体架构（Monolithic）
- **理由**：
  - 系统服务于单一业务场景（岸桥作业管理），业务边界明确
  - 所有模块共享同一 Oracle 数据库，数据一致性要求高
  - 团队规模和运维复杂度适合单体部署
  - 历史技术选型（Spring 3.0 + Hibernate 3）倾向于传统分层架构

- **主要架构决策记录（ADR）**：
  1. **ADR-001**: 采用 Spring MVC 作为 Web 框架，实现请求路由和视图渲染
  2. **ADR-002**: 使用 Hibernate 3 作为 ORM 框架，配合 JPA 注解进行实体映射
  3. **ADR-003**: 采用 C3P0 连接池管理数据库连接，支持多环境配置（DEV/SIT/UAT/Prod）
  4. **ADR-004**: 通过 JDBC Template 直接查询外部 N4 系统（Navis 4 TOS）获取实时作业数据
  5. **ADR-005**: 使用 Session 存储用户登录状态，通过 SecurityInterceptor 实现访问控制
  6. **ADR-006**: 采用 JSP + JSTL 作为视图层技术，支持国际化（zh_CN/zh_TW/en）

## 3. 技术栈总览

- **语言**：Java 7
- **框架**：
  - Spring Framework 3.0.1（MVC、ORM、Transaction、AOP）
  - Hibernate 3.2.6（ORM）
  - Spring Data JDBC Core 1.2.1
- **数据库**：Oracle 10g/11g（通过 ojdbc6 驱动）
- **连接池**：C3P0 0.9.2.1
- **Web 容器**：Servlet 3.1 API（WAR 包部署）
- **视图技术**：JSP + JSTL 1.2
- **日志**：Log4j 1.2.14 + SLF4J 1.5.6
- **其他依赖**：
  - Apache POI 3.9（Excel 导入导出）
  - Quartz 1.5.2（定时任务）
  - Commons 系列工具库
  - Ehcache 1.2.3（缓存）

- **消息中间件**：无（系统未使用消息队列）
- **容器编排**：无（传统 WAR 包部署，无 Docker/K8s 配置）

## 4. 限界上下文与模块映射

| 上下文 | 类型 | 模块/服务名 | 端口 |
|--------|------|-------------|------|
| 用户认证与权限管理 | 支撑域 | com.springMVC.control.UserControl | 8080 (HTTP) |
| 岸桥作业监控 | 核心域 | com.springMVC.control.CellControl + Busihandler | 8080 (HTTP/XML) |
| 船舶配置管理 | 支撑域 | com.springMVC.control.CellControl (Vessel部分) | 8080 (HTTP) |
| 操作日志审计 | 通用域 | com.springMVC.dao.UserDao (ShowLog) | N/A |

**说明**：
- 系统未采用微服务架构，所有功能集中在单个 WAR 包中
- 端口号为应用服务器默认 HTTP 端口（通常为 8080）
- Busihandler 提供 XML 格式的实时作业数据接口，供前端轮询调用



## 5. 分层架构（以核心上下文为例）

系统采用经典的 **四层架构**，依赖方向遵循：`interfaces -> application -> domain <- infrastructure`

### 5.1 岸桥作业监控上下文（核心域）

#### Interfaces Layer（入站适配器）
- **位置**：`com.springMVC.control.CellControl`
- **职责**：
  - 接收 HTTP 请求（GET/POST），解析参数
  - 调用应用服务层执行业务逻辑
  - 返回 ModelAndView 或 XML 响应
- **关键端点**：
  - `/user/setbay` - 查询舱位尺寸配置
  - `/user/updateBay` - 更新舱位矩阵
  - `/user/BusiQuery` - 实时作业数据查询（XML 响应）
  - `/user/allColSet` - 颜色配置列表
  - `/user/allVessel` - 船舶配置列表
  - `/user/allVesselRefuel` - 加油配置列表
  - `/user/allVesselCol` - 船舶颜色配置列表

#### Application Layer（应用服务）
- **位置**：`com.accenture.vmt.Busihandler`
- **职责**：
  - 编排领域服务调用，协调多个 DAO 操作
  - 处理业务用例：获取作业序列、构建舱位表格、生成 XML 响应
  - 异常处理和日志记录
- **关键方法**：
  - `returnResponse()` - 核心业务流程：获取 QC 作业顺序 → 查询序列详情 → 获取 ROB 列表 → 构建 HTML 表格 → 返回 XML

#### Domain Layer（领域模型）
- **位置**：`com.springMVC.entity.*`
- **实体**：
  - `CellMatrix` - 舱位矩阵（行/层配置）
  - `SequenceVO` - 作业序列值对象（当前槽位、计划槽位、状态等）
  - `Vessel` - 船舶配置（ vesselid, deck_hold, bay, row/tier 范围）
  - `VesselCol` - 船舶颜色配置
  - `VesselRefuel` - 船舶加油区域配置
  - `ColSet` - 箱型颜色映射
  - `BaySize` - 舱位尺寸配置
- **特点**：
  - 实体使用 JPA 注解（@Entity, @Table, @Id）
  - 贫血模型（Anemic Domain Model），业务逻辑主要在 DAO 层

#### Infrastructure Layer（出站适配器）
- **位置**：`com.springMVC.dao.*Impl`
- **实现**：
  - `CellDaoImpl` - 核心数据访问，包含复杂业务逻辑
    - 查询 N4 系统获取作业队列（LOAD/DISCHARGE）
    - 计算剩余集装箱数量
    - 构建舱位 HTML 表格
    - 处理危险品标识（DG Indicator）
  - `VesselDaoImpl` - 船舶配置 CRUD
  - `UserDaoImpl` - 用户认证与日志
- **技术**：
  - Hibernate Template（ORM 操作）
  - JDBC Template（直接 SQL 查询 N4 系统）
  - 事务管理：@Transactional 注解

**依赖原则**：
```
interfaces (CellControl) 
    ↓ calls
application (Busihandler)
    ↓ calls
domain (Entity classes)
    ↑ mapped by
infrastructure (DAO implementations)
    ↓ uses
Hibernate/JDBC → Oracle Database + N4 External System
```

### 5.2 用户管理上下文（支撑域）

#### Interfaces Layer
- **位置**：`com.springMVC.control.UserControl`
- **关键端点**：
  - `/user/index` - 登录页面
  - `/user/login` - 登录验证
  - `/user/all` - 用户列表（管理员）
  - `/user/add` / `/user/save` - 新增用户
  - `/user/modify` / `/user/update` - 修改用户
  - `/user/del` - 删除用户
  - `/user/log` - 操作日志查询
  - `/user/logout` - 登出
  - `/user/exportLogs` - 导出日志（Excel）
  - `/user/importVessel` - 导入船舶配置（Excel）

#### Application Layer
- 内嵌在 Control 层，未独立分离

#### Domain Layer
- `User` - 用户实体（username, password, role, qcid）
- `ShowLog` - 操作日志实体

#### Infrastructure Layer
- `UserDaoImpl` - 用户 CRUD、登录验证、日志记录

### 5.3 安全拦截器
- **位置**：`com.springMVC.filter.SecurityInterceptor`
- **职责**：
  - 检查 Session 中是否存在登录用户
  - 排除公开 URL（login, index, logout, changeLan）
  - 未登录用户重定向到登录页

## 6. 跨上下文交互

### 同步调用
- **内部调用**：所有模块在同一 JVM 内，通过 Spring Bean 注入直接调用
  - `CellControl` → `CellDao` / `VesselDao`
  - `UserControl` → `UserDao` / `CellDao`
  - `Busihandler` → `CellDao`

- **外部系统调用**：
  - **N4 系统（Navis 4 TOS）**：通过 JDBC 直接查询 N4 数据库表（MN4O_QC_* 系列表）
    - 获取岸桥作业队列（inv_wq, inv_wi）
    - 获取集装箱信息（inv_unit, inv_unit_fcy_visit）
    - 获取船舶访问信息（argo_carrier_visit）
    - 获取设备信息（ref_equipment）
  - 调用方式：JDBC Template 执行原生 SQL
  - 超时处理：捕获 `RecoverableDataAccessException` 抛出 `GeneralException`

### 异步事件
- **无消息队列**：系统未使用异步事件机制
- **定时任务**：Quartz 配置用于数据交换（N4.dataExchange.executeTime），但代码中未见具体实现

### 数据共享
- **单一数据库**：所有上下文共享同一 Oracle 数据库
- **表前缀区分**：
  - `T_USER`, `T_VESSEL`, `T_CELL MATRIX` 等为本系统表
  - `MN4O_QC_*` 为 N4 系统只读表
- **数据隔离策略**：无明确隔离，通过业务逻辑控制访问权限



## 7. 数据持久化策略

### 7.1 数据存储方案

| 上下文 | 存储类型 | 表名 | 说明 |
|--------|----------|------|------|
| 用户管理 | Oracle 关系型数据库 | T_USER, T_SHOWLOG | 用户信息、操作日志 |
| 岸桥作业监控 | Oracle + N4 外部系统 | T_CELL MATRIX, T_VESSEL, MN4O_QC_* | 舱位配置、船舶配置、实时作业数据 |
| 船舶配置 | Oracle 关系型数据库 | T_VESSEL, T_VESSELCOL, T_VESSELREFUEL | 船舶舱位范围、颜色配置、加油区域 |
| 颜色配置 | Oracle 关系型数据库 | T_COLSET | 箱型与颜色映射 |

**特点**：
- **ORM 映射**：使用 Hibernate 3 + JPA 注解进行实体-表映射
- **序列生成器**：主键使用 Oracle Sequence（user_seq, vessel_seq）
- **连接池**：C3P0 配置最小 10 连接，最大 40-50 连接
- **只读外部数据**：N4 系统表仅查询，不写入

### 7.2 事务管理
- **策略**：Spring 声明式事务（@Transactional）
- **传播行为**：
  - 查询操作：`Propagation.SUPPORTS`（DAO 层默认）
  - 写操作：`Propagation.REQUIRED`（save/update/delete 方法）
- **事务管理器**：`HibernateTransactionManager`

### 7.3 事件溯源
- **不适用**：系统未采用事件溯源模式
- **审计日志**：通过 `T_SHOWLOG` 和 `T_OPERATIONLOG` 表记录关键操作（登录、登出、配置变更）

## 8. 部署与横切关注点

### 8.1 服务注册与发现
- **无服务注册**：单体应用，无需服务发现机制

### 8.2 配置管理
- **配置文件**：
  - `db.properties` - 数据库连接配置（多环境：DEV/SIT/UAT/Prod）
  - `system.properties` - 系统参数（limitAccount, company）
  - `log4j.properties` - 日志配置
  - `messages_*.properties` - 国际化资源（zh_CN, zh_TW, en）
- **Spring 配置**：`springMVC-servlet.xml` 集中管理 Bean 定义

### 8.3 日志与监控
- **日志框架**：Log4j 1.2.14 + SLF4J
- **日志内容**：
  - 业务操作日志（登录、登出、配置变更）存储在数据库
  - 应用日志输出到文件（log4j.properties 配置）
  - Busihandler 记录 IP 地址和 Session ID 用于追踪
- **监控**：无专门监控系统，依赖应用服务器日志

### 8.4 安全
- **认证**：基于 Session 的用户名/密码验证
- **授权**：角色-based（ADMIN/USER），通过 SecurityInterceptor 拦截未授权访问
- **密码存储**：明文存储（⚠️ 安全风险）
- **SQL 注入防护**：使用 Hibernate Parameterized Query 和 JDBC PreparedStatement
- **XSS 防护**：依赖 Servlet 容器和 JSP 转义

### 8.5 国际化
- **支持语言**：简体中文（zh_CN）、繁体中文（zh_TW）、英文（en）
- **实现**：Spring ResourceBundleMessageSource + SessionLocaleResolver
- **切换方式**：URL 参数 `?local=zh_CN`

## 9. 容器图（C4 文字描述）

### Container 层级关系

```
[Browser Client]
    ↓ HTTP (HTML/JSP)
[Web Application Server (Tomcat/WebLogic)]
    │
    ├── [QCVMT WAR Application]
    │       │
    │       ├── Spring MVC DispatcherServlet
    │       ├── Controllers (CellControl, UserControl)
    │       ├── Services (Busihandler)
    │       ├── DAOs (CellDaoImpl, UserDaoImpl, VesselDaoImpl)
    │       └── Entities (User, Vessel, CellMatrix, etc.)
    │
    ↓ JDBC (C3P0 Pool)
[Oracle Database]
    ├── T_USER (用户表)
    ├── T_SHOWLOG (操作日志)
    ├── T_CELL MATRIX (舱位矩阵)
    ├── T_VESSEL (船舶配置)
    ├── T_VESSELCOL (船舶颜色配置)
    ├── T_VESSELREFUEL (加油配置)
    └── T_COLSET (颜色配置)
    │
    ↓ JDBC (Direct Query)
[N4 System Database (Navis 4 TOS)]
    ├── MN4O_QC_inv_wq (作业队列)
    ├── MN4O_QC_inv_wi (作业项)
    ├── MN4O_QC_inv_unit (集装箱)
    ├── MN4O_QC_argo_carrier_visit (船舶访问)
    └── ... (其他只读表)
```

**交互说明**：
1. **Browser → QCVMT**：用户通过浏览器访问 JSP 页面，提交表单或发起 AJAX 请求
2. **QCVMT → Oracle DB**：通过 C3P0 连接池执行 CRUD 操作（Hibernate + JDBC）
3. **QCVMT → N4 DB**：通过 JDBC Template 直接查询 N4 系统获取实时作业数据（只读）
4. **QCVMT → Browser**：返回 HTML 页面或 XML 数据（Busihandler 接口）

**部署拓扑**：
- 单个 WAR 包部署在应用服务器上
- 应用服务器与 Oracle 数据库通过网络连接
- N4 系统为独立的外部系统，QCVMT 仅读取其数据
