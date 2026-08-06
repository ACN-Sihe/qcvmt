# QCVMT 迁移开发任务清单

## 阶段一：基础设施与构建系统（12个任务）

### 1.1 Gradle 构建系统搭建
- **目标**：从 Maven WAR 迁移到 Gradle Kotlin DSL
- **新增文件**：
  - `/build.gradle.kts`
  - `/settings.gradle.kts`
  - `/gradle/wrapper/gradle-wrapper.jar`
  - `/gradle/wrapper/gradle-wrapper.properties`
  - `/gradlew`
  - `/gradlew.bat`
- **删除文件**：`/pom.xml`
- **关键配置**：
  ```kotlin
  plugins {
      java
      id("org.springframework.boot") version "3.5.16"
      id("io.spring.dependency-management") version "1.1.7"
  }
  
  java {
      sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17
  }
  
  dependencies {
      implementation("org.springframework.boot:spring-boot-starter-web")
      implementation("org.springframework.boot:spring-boot-starter-data-jpa")
      implementation("org.springframework.boot:spring-boot-starter-jdbc")
      implementation("org.springframework.boot:spring-boot-starter-security")
      implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
      implementation("org.springframework.boot:spring-boot-starter-validation")
      implementation("org.springframework.boot:spring-boot-starter-actuator")
      runtimeOnly("com.mysql:mysql-connector-j:8.4.0")
      runtimeOnly("com.oracle.database.jdbc:ojdbc11:23.6.0.24.10")
      compileOnly("org.projectlombok:lombok")
      annotationProcessor("org.projectlombok:lombok")
      implementation("org.mapstruct:mapstruct:1.6.3")
      annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
  }
  ```
- **验证**：执行 `./gradlew build` 确认构建成功
- **验收**：Gradle 构建成功，无编译错误

### 1.2 Spring Boot 启动类
- **目标**：创建应用入口
- **新增文件**：`/src/main/java/com/mtl/qcvmt/QcvmtApplication.java`
- **实现要求**：
  ```java
  package com.mtl.qcvmt;
  
  @SpringBootApplication
  public class QcvmtApplication {
      public static void main(String[] args) {
          SpringApplication.run(QcvmtApplication.class, args);
      }
  }
  ```
- **验证**：`./gradlew bootRun` 启动无报错
- **依赖**：1.1

### 1.3 多环境配置文件
- **目标**：配置 Spring Boot 多环境
- **新增文件**：
  - `/src/main/resources/application.yml`
  - `/src/main/resources/application-dev.yml`
  - `/src/main/resources/application-sit.yml`
  - `/src/main/resources/application-uat.yml`
  - `/src/main/resources/application-prod.yml`
- **关键配置**（application.yml）：
  ```yaml
  spring:
    application:
      name: qcvmt
    profiles:
      active: dev
    security:
      oauth2:
        resourceserver:
          jwt:
            issuer-uri: ${KEYCLOAK_URL:http://localhost:8180}/realms/${KEYCLOAK_REALM:qcvmt}
            jwk-set-uri: ${KEYCLOAK_URL:http://localhost:8180}/realms/${KEYCLOAK_REALM:qcvmt}/protocol/openid-connect/certs
  
  server:
    port: 8080
  
  qcvmt:
    cors:
      allowed-origins: ${CORS_ORIGINS:http://localhost:5173,http://localhost:3000}
      allowed-methods: GET,POST,PUT,DELETE,OPTIONS
      allowed-headers: "*"
      max-age: 3600
    datasource:
      mysql:
        url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DB:qcvmt}?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4
        username: ${MYSQL_USER:root}
        password: ${MYSQL_PASSWORD}
        driver-class-name: com.mysql.cj.jdbc.Driver
        hikari:
          maximum-pool-size: 40
          minimum-idle: 10
          idle-timeout: 600000
          connection-timeout: 10000
      n4:
        url: jdbc:oracle:thin:@${N4_HOST}:${N4_PORT:1661}/${N4_SERVICE}
        username: ${N4_USER}
        password: ${N4_PASSWORD}
        driver-class-name: oracle.jdbc.OracleDriver
        hikari:
          maximum-pool-size: 50
          minimum-idle: 10
          read-only: true
    keycloak:
      server-url: ${KEYCLOAK_URL:http://localhost:8180}
      realm: ${KEYCLOAK_REALM:qcvmt}
      client-id: ${KEYCLOAK_CLIENT_ID:qcvmt-admin-api}
      client-secret: ${KEYCLOAK_CLIENT_SECRET}
    n4:
      company: ${N4_COMPANY:MTL}
      retry-max-attempts: 3
      retry-interval: 500
      connection-timeout: 10000
      read-timeout: 30000
  ```
- **验证**：启动时正确加载对应 profile 配置
- **依赖**：1.1

### 1.4 MySQL 数据库 Schema
- **目标**：创建 MySQL 表结构
- **新增文件**：`/src/main/resources/schema.sql`
- **关键 SQL**：
  ```sql
  CREATE TABLE IF NOT EXISTS t_user (
      id INT AUTO_INCREMENT PRIMARY KEY,
      keycloak_id VARCHAR(36) UNIQUE,
      qcid VARCHAR(20),
      name VARCHAR(20),
      role VARCHAR(10),
      parent VARCHAR(20),
      createtime DATETIME,
      UNIQUE KEY uk_username (name)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  
  CREATE TABLE IF NOT EXISTS t_vessel (
      id INT AUTO_INCREMENT PRIMARY KEY,
      vesselid VARCHAR(10),
      deck_hold VARCHAR(10),
      bay VARCHAR(10),
      row_start VARCHAR(10),
      row_end VARCHAR(10),
      tier_start VARCHAR(10),
      tier_end VARCHAR(10),
      version INT DEFAULT 0,
      UNIQUE KEY uk_vessel (vesselid, deck_hold, bay)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  
  CREATE TABLE IF NOT EXISTS t_col_set (
      id INT AUTO_INCREMENT PRIMARY KEY,
      boxcase VARCHAR(20),
      color VARCHAR(20),
      version INT DEFAULT 0,
      UNIQUE KEY uk_boxcase (boxcase)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  
  CREATE TABLE IF NOT EXISTS t_vessel_col (
      id INT AUTO_INCREMENT PRIMARY KEY,
      vesselid VARCHAR(10),
      deck_hold VARCHAR(10),
      bay VARCHAR(10),
      row_start VARCHAR(10),
      row_end VARCHAR(10),
      tier_start VARCHAR(10),
      tier_end VARCHAR(10),
      version INT DEFAULT 0
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  
  CREATE TABLE IF NOT EXISTS t_vessel_refuel (
      id INT AUTO_INCREMENT PRIMARY KEY,
      vesselid VARCHAR(10),
      is_refuel VARCHAR(10),
      version INT DEFAULT 0
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  
  CREATE TABLE IF NOT EXISTS t_cell_matrix (
      id INT AUTO_INCREMENT PRIMARY KEY,
      type VARCHAR(10),
      row VARCHAR(10),
      tier VARCHAR(10),
      tier_start VARCHAR(10),
      tier_end VARCHAR(10),
      active VARCHAR(1) DEFAULT '1'
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  
  CREATE TABLE IF NOT EXISTS t_showlog (
      id INT AUTO_INCREMENT PRIMARY KEY,
      userid INT,
      username VARCHAR(20),
      qcid VARCHAR(20),
      login_time DATETIME,
      operation VARCHAR(20),
      INDEX idx_userid_logintime (userid, login_time)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  
  CREATE TABLE IF NOT EXISTS t_operation_log (
      id INT AUTO_INCREMENT PRIMARY KEY,
      userid INT,
      username VARCHAR(20),
      function_name VARCHAR(50),
      action_type VARCHAR(20),
      old_values TEXT,
      new_values TEXT,
      timestamp DATETIME,
      INDEX idx_timestamp (timestamp)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  ```
- **验证**：在 dev 环境执行 schema.sql，所有表创建成功
- **依赖**：1.3

### 1.5 多语言资源文件
- **目标**：保留原有 i18n 支持
- **复制文件**：从 `src/main/resources/` 复制
  - `messages_en.properties`
  - `messages_zh_CN.properties`
  - `messages_zh_TW.properties`
- **目标位置**：`/src/main/resources/`
- **验证**：Spring MessageSource 能正确加载多语言文件
- **依赖**：1.3

### 1.6 MySQL 数据源配置
- **目标**：配置主数据源（JPA + 事务）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/config/MysqlDataSourceConfig.java`
- **实现要求**：
  - @Configuration + @EnableTransactionManagement
  - @EnableJpaRepositories 指向 `com.mtl.qcvmt.repository`
  - EntityManagerFactory 扫描 `com.mtl.qcvmt.entity`
  - mysqlDataSource Bean 使用 @ConfigurationProperties("qcvmt.datasource.mysql")
  - mysqlTransactionManager Bean 配置 JPA 事务
- **验证**：启动时无数据源连接错误
- **依赖**：1.3, 1.4

### 1.7 N4 Oracle 数据源配置
- **目标**：配置只读数据源（JdbcTemplate）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/config/N4OracleDataSourceConfig.java`
- **实现要求**：
  - n4DataSource Bean 使用 @ConfigurationProperties("qcvmt.datasource.n4")
  - n4JdbcTemplate Bean 设置 queryTimeout=30
  - @Qualifier("n4JdbcTemplate") 用于注入
- **验证**：启动时无 Oracle 连接错误
- **依赖**：1.3

### 1.8 Spring Security 配置
- **目标**：配置 Keycloak OIDC 资源服务器
- **新增文件**：`/src/main/java/com/mtl/qcvmt/config/SecurityConfig.java`
- **实现要求**：
  - @EnableWebSecurity
  - SecurityFilterChain 配置 OAuth2 Resource Server
  - JWT 验证使用 JwtAuthConverter
  - CSRF 禁用（前后端分离）
  - CORS 启用
  - 路由权限：
    - `/api/users/**`, `/api/operation-logs/**`, `/api/import/**`, `/api/export/**` → hasRole('qcvmt-admin')
    - `/api/vessels/**`, `/api/color-sets/**`, `/api/vessel-colors/**`, `/api/vessel-refuels/**`, `/api/bay-config/**` → hasRole('qcvmt-admin')
    - `/api/terminal/**` → hasAnyRole('qcvmt-admin', 'qcvmt-user')
    - `/actuator/health`, `/v3/api-docs/**`, `/swagger-ui/**` → permitAll
    - OPTIONS 请求 → permitAll
- **验证**：无效 JWT 返回 401，有效 JWT 根据角色授权
- **依赖**：1.3, 1.7

### 1.9 CORS 配置
- **目标**：配置前后端分离跨域
- **新增文件**：`/src/main/java/com/mtl/qcvmt/config/CorsConfig.java`
- **实现要求**：
  - implements WebMvcConfigurer
  - addCorsMappings 读取 `qcvmt.cors.allowed-origins`
  - allowCredentials(true)
  - maxAge(3600)
- **验证**：跨域请求携带正确 Origin 头返回 Access-Control-Allow-Origin
- **依赖**：1.3

### 1.10 WebConfig 配置
- **目标**：配置 Web MVC 通用设置
- **新增文件**：`/src/main/java/com/mtl/qcvmt/config/WebConfig.java`
- **实现要求**：
  - implements WebMvcConfigurer
  - 配置 MessageSource 多语言
- **验证**：多语言消息正确解析
- **依赖**：1.5

### 1.11 Keycloak Admin Client 配置
- **目标**：配置 Keycloak 管理客户端（用户管理）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/config/KeycloakConfig.java`
- **实现要求**：
  - @Bean Keycloak 使用 KeycloakBuilder
  - CLIENT_CREDENTIALS grant type
  - 读取 keycloak.server-url, realm, client-id, client-secret
- **验证**：Keycloak Bean 初始化成功
- **依赖**：1.3

### 1.12 .gitignore 配置
- **目标**：配置 Git 忽略规则
- **新增/修改文件**：`/.gitignore`
- **内容**：
  ```
  build/
  .gradle/
  *.class
  *.jar
  *.war
  .idea/
  .vscode/
  *.log
  application-local.yml
  ```
- **验证**：Git 正确忽略编译产物和本地配置
- **依赖**：无

---

## 阶段二：实体层与 Repository（15个任务）

### 2.1 User 实体迁移
- **目标**：从 Oracle SEQUENCE 迁移到 MySQL IDENTITY，新增 keycloak_id
- **删除文件**：`src/main/java/com/springMVC/entity/User.java`
- **新增文件**：`/src/main/java/com/mtl/qcvmt/entity/User.java`
- **关键变更**：
  - @Table(name = "t_user")
  - @Id @GeneratedValue(strategy = IDENTITY)
  - 移除 password 字段
  - 新增 keycloakId (VARCHAR(36), unique)
  - createtime: String → LocalDateTime
  - @Column(name="name") username
- **字段映射**：
  | 原字段 | 新字段 | 类型变更 |
  |--------|--------|----------|
  | userid | id | Integer IDENTITY |
  | QCID | qcid | String(20) |
  | NAME | username | String(20) |
  | PASSWORD | - | 移除 |
  | ROLE | role | String(10) |
  | PARENT | parent | String(20) |
  | CREATETIME | createtime | String → LocalDateTime |
  | - | keycloak_id | 新增 String(36) |
- **验证**：实体类编译通过，@Entity 校验无错误
- **依赖**：1.4

### 2.2 Vessel 实体迁移
- **目标**：迁移到 JPA IDENTITY，添加唯一约束
- **删除文件**：`src/main/java/com/springMVC/entity/Vessel.java`
- **新增文件**：`/src/main/java/com/mtl/qcvmt/entity/Vessel.java`
- **关键变更**：
  - @Table(name = "t_vessel", uniqueConstraints = @UniqueConstraint(columnNames = {"vesselid", "deck_hold", "bay"}))
  - vmid → id (IDENTITY)
  - 添加 @Version 乐观锁
  - 列名保持原始：vesselid, deck_hold, bay, rowstart, rowend, tierstart, tierend
- **验证**：实体类编译通过，唯一约束生效
- **依赖**：1.4

### 2.3 VesselCol 实体迁移
- **目标**：迁移到 MySQL JPA
- **删除文件**：`src/main/java/com/springMVC/entity/VesselCol.java`
- **新增文件**：`/src/main/java/com/mtl/qcvmt/entity/VesselCol.java`
- **关键变更**：
  - @Table(name = "t_vessel_col")
  - IDENTITY 策略
  - 添加 @Version
- **验证**：实体类编译通过
- **依赖**：1.4

### 2.4 VesselRefuel 实体迁移
- **目标**：迁移到 MySQL JPA
- **删除文件**：`src/main/java/com/springMVC/entity/VesselRefuel.java`
- **新增文件**：`/src/main/java/com/mtl/qcvmt/entity/VesselRefuel.java`
- **关键变更**：
  - @Table(name = "t_vessel_refuel")
  - IDENTITY 策略
  - 添加 @Version
- **验证**：实体类编译通过
- **依赖**：1.4

### 2.5 ColSet 实体迁移
- **目标**：迁移到 MySQL JPA，添加 boxcase 唯一约束
- **删除文件**：`src/main/java/com/springMVC/entity/ColSet.java`
- **新增文件**：`/src/main/java/com/mtl/qcvmt/entity/ColSet.java`
- **关键变更**：
  - @Table(name = "t_col_set")
  - IDENTITY 策略
  - boxcase @Column(unique = true)
  - 添加 @Version
- **验证**：实体类编译通过
- **依赖**：1.4

### 2.6 CellMatrix 实体迁移
- **目标**：迁移到 MySQL JPA
- **删除文件**：`src/main/java/com/springMVC/entity/CellMatrix.java`
- **新增文件**：`/src/main/java/com/mtl/qcvmt/entity/CellMatrix.java`
- **关键变更**：
  - @Table(name = "t_cell_matrix")
  - IDENTITY 策略
  - 字段：type, row, tier, tier_start, tier_end, active
- **验证**：实体类编译通过
- **依赖**：1.4

### 2.7 ShowLog 实体迁移
- **目标**：迁移到 MySQL JPA，时间字段类型转换
- **删除文件**：`src/main/java/com/springMVC/entity/ShowLog.java`
- **新增文件**：`/src/main/java/com/mtl/qcvmt/entity/ShowLog.java`
- **关键变更**：
  - @Table(name = "t_showlog")
  - IDENTITY 策略
  - loginTime: String → LocalDateTime
- **验证**：实体类编译通过
- **依赖**：1.4

### 2.8 OperationLog 实体迁移
- **目标**：迁移到 MySQL JPA，时间字段类型转换
- **删除文件**：`src/main/java/com/springMVC/entity/OperationLog.java`
- **新增文件**：`/src/main/java/com/mtl/qcvmt/entity/OperationLog.java`
- **关键变更**：
  - @Table(name = "t_operation_log")
  - IDENTITY 策略
  - timestamp: Date → LocalDateTime
  - 字段重命名：FUNCTION → function_name, ACTIONTYPE → action_type, VALUECHANGE → old_values + new_values
- **验证**：实体类编译通过
- **依赖**：1.4

### 2.9 BaySize DTO
- **目标**：保留非实体 DTO
- **删除文件**：`src/main/java/com/springMVC/entity/BaySize.java`
- **新增文件**：`/src/main/java/com/mtl/qcvmt/entity/BaySize.java`
- **实现**：纯 POJO，无 @Entity 注解
- **验证**：编译通过
- **依赖**：无

### 2.10 SequenceVO DTO
- **目标**：保留非实体 DTO（N4 作业序列）
- **删除文件**：`src/main/java/com/springMVC/entity/SequenceVO.java`
- **新增文件**：`/src/main/java/com/mtl/qcvmt/entity/SequenceVO.java`
- **字段**：currentPosSlot, plannedPosSlot, qtype, qdeck, qrow, status, bay, isOog, isPowered, isTank, isDg, isQuad, isTandem, isTwin, isSingle
- **验证**：编译通过
- **依赖**：无

### 2.11 UserRepository
- **目标**：创建 Spring Data JPA Repository
- **新增文件**：`/src/main/java/com/mtl/qcvmt/repository/UserRepository.java`
- **接口定义**：
  ```java
  public interface UserRepository extends JpaRepository<User, Integer> {
      Optional<User> findByKeycloakId(String keycloakId);
      Optional<User> findByUsername(String username);
      Page<User> findAllByOrderByUsernameAsc(Pageable pageable);
  }
  ```
- **验证**：Spring Data JPA 自动实现，启动无报错
- **依赖**：2.1

### 2.12 VesselRepository
- **目标**：创建 Spring Data JPA Repository
- **新增文件**：`/src/main/java/com/mtl/qcvmt/repository/VesselRepository.java`
- **接口定义**：
  ```java
  public interface VesselRepository extends JpaRepository<Vessel, Integer> {
      List<Vessel> findByVesselidAndDeckHoldAndBay(String vesselid, String deckHold, String bay);
      Page<Vessel> findAllByOrderByVesselidAscDeckHoldAsc(Pageable pageable);
      Page<Vessel> findByVesselidContainingOrDeckHoldContainingOrBayContaining(
          String vesselid, String deckHold, String bay, Pageable pageable);
      List<Vessel> findByVesselid(String vesselid);
  }
  ```
- **验证**：分页查询方法正确
- **依赖**：2.2

### 2.13 VesselColRepository
- **目标**：创建 Spring Data JPA Repository
- **新增文件**：`/src/main/java/com/mtl/qcvmt/repository/VesselColRepository.java`
- **接口定义**：
  ```java
  public interface VesselColRepository extends JpaRepository<VesselCol, Integer> {
      Page<VesselCol> findAllByOrderByVesselidAscDeckHoldAsc(Pageable pageable);
      Page<VesselCol> findByVesselidContainingOrBayContaining(
          String vesselid, String bay, Pageable pageable);
  }
  ```
- **验证**：分页查询方法正确
- **依赖**：2.3

### 2.14 VesselRefuelRepository
- **目标**：创建 Spring Data JPA Repository
- **新增文件**：`/src/main/java/com/mtl/qcvmt/repository/VesselRefuelRepository.java`
- **接口定义**：
  ```java
  public interface VesselRefuelRepository extends JpaRepository<VesselRefuel, Integer> {
      Page<VesselRefuel> findAllByOrderByVesselidAsc(Pageable pageable);
      Page<VesselRefuel> findByVesselidContainingOrIsRefuelContaining(
          String vesselid, String isRefuel, Pageable pageable);
  }
  ```
- **验证**：分页查询方法正确
- **依赖**：2.4

### 2.15 ColSetRepository
- **目标**：创建 Spring Data JPA Repository
- **新增文件**：`/src/main/java/com/mtl/qcvmt/repository/ColSetRepository.java`
- **接口定义**：
  ```java
  public interface ColSetRepository extends JpaRepository<ColSet, Integer> {
      Optional<ColSet> findByBoxcase(String boxcase);
  }
  ```
- **验证**：唯一键查询正确
- **依赖**：2.5

### 2.16 CellMatrixRepository
- **目标**：创建 Spring Data JPA Repository
- **新增文件**：`/src/main/java/com/mtl/qcvmt/repository/CellMatrixRepository.java`
- **接口定义**：
  ```java
  public interface CellMatrixRepository extends JpaRepository<CellMatrix, Integer> {
      List<CellMatrix> findByTypeAndActiveOrderByIdDesc(String type, String active);
      List<CellMatrix> findByTypeAndRowBetweenOrderByRowAsc(String type, String rowStart, String rowEnd);
  }
  ```
- **验证**：查询条件正确
- **依赖**：2.6

### 2.17 ShowLogRepository
- **目标**：创建 Spring Data JPA Repository
- **新增文件**：`/src/main/java/com/mtl/qcvmt/repository/ShowLogRepository.java`
- **接口定义**：
  ```java
  public interface ShowLogRepository extends JpaRepository<ShowLog, Integer> {
      Page<ShowLog> findByUseridAndLoginTimeBetweenOrderByLoginTimeDesc(
          Integer userid, LocalDateTime start, LocalDateTime end, Pageable pageable);
      List<ShowLog> findByQcidIsNotNullAndLoginTimeBetween(LocalDateTime from, LocalDateTime to);
      void deleteByUserid(Integer userid);
  }
  ```
- **验证**：时间范围查询正确，deleteByUserid 级联删除
- **依赖**：2.7

### 2.18 OperationLogRepository
- **目标**：创建 Spring Data JPA Repository
- **新增文件**：`/src/main/java/com/mtl/qcvmt/repository/OperationLogRepository.java`
- **接口定义**：
  ```java
  public interface OperationLogRepository extends JpaRepository<OperationLog, Integer> {
      Page<OperationLog> findAllByOrderByTimestampDesc(Pageable pageable);
  }
  ```
- **验证**：按时间倒序分页
- **依赖**：2.8

---

## 阶段三：Security 与 Keycloak 集成（4个任务）

### 3.1 JwtAuthConverter
- **目标**：实现 JWT Token → Spring Security Authentication 转换
- **新增文件**：`/src/main/java/com/mtl/qcvmt/security/JwtAuthConverter.java`
- **实现要求**：
  - implements Converter<Jwt, AbstractAuthenticationToken>
  - extractRoles(): 从 JWT realm_access.roles 提取以 `qcvmt-` 开头的角色
  - 转换为 ROLE_qcvmt-admin / ROLE_qcvmt-user
  - 使用 preferred_username 作为 principal
- **验证**：有效 JWT Token 正确解析角色
- **依赖**：1.8

### 3.2 SecurityContextHelper
- **目标**：提供安全上下文工具类
- **新增文件**：`/src/main/java/com/mtl/qcvmt/security/SecurityContextHelper.java`
- **实现要求**：
  - getCurrentUsername(): 从 SecurityContext 获取 JWT preferred_username
  - getKeycloakSub(): 获取 JWT subject (Keycloak user UUID)
  - isAdmin(): 检查是否包含 ROLE_qcvmt-admin
- **验证**：在 Controller 中注入并调用方法正确
- **依赖**：3.1

### 3.3 KeycloakUserSyncService
- **目标**：实现首次登录用户自动同步
- **新增文件**：`/src/main/java/com/mtl/qcvmt/service/keycloak/KeycloakUserSyncService.java`
- **实现要求**：
  - getOrCreateLocalUser(): 按 keycloakId 查找，不存在则创建
  - 创建时设置：keycloakId, username, role (根据 isAdmin() 判断 ADMIN/USER), createtime=now
  - 注入 UserRepository, SecurityContextHelper
- **验证**：首次访问 API 自动创建 t_user 记录
- **依赖**：2.11, 3.2

### 3.4 KeycloakRoleMappingService
- **目标**：实现 Keycloak Admin API 用户和角色管理
- **新增文件**：`/src/main/java/com/mtl/qcvmt/service/keycloak/KeycloakRoleMappingService.java`
- **实现要求**：
  - createUserInKeycloak(username, password, role): 调用 Keycloak Admin API 创建用户
  - assignRole(keycloakUserId, roleName): 分配 realm role
  - removeRole(keycloakUserId, roleName): 移除 realm role
  - getUserRoles(keycloakUserId): 查询用户有效角色
  - 注入 Keycloak Bean
- **验证**：创建用户并分配角色成功
- **依赖**：1.11

---

## 阶段四：N4 Oracle Service 层（5个任务）

### 4.1 N4QueryRepository
- **目标**：封装 N4 Oracle JdbcTemplate 操作
- **新增文件**：`/src/main/java/com/mtl/qcvmt/n4/N4QueryRepository.java`
- **实现要求**：
  - @Repository
  - 注入 @Qualifier("n4JdbcTemplate") JdbcTemplate
  - 提供 queryForList, queryForObject 等封装方法
  - 统一异常处理：DataAccessException → N4ConnectionException
- **验证**：N4 查询返回正确结果
- **依赖**：1.7

### 4.2 N4TableConstants
- **目标**：定义 N4 Schema 常量
- **新增文件**：`/src/main/java/com/mtl/qcvmt/n4/N4TableConstants.java`
- **实现要求**：
  - 定义所有 N4 表名前缀 `MN4O_QC_`
  - 定义常用表名常量：INV_WQ, INV_WI, INV_UNIT, ARGO_CARRIER_VISIT 等
- **验证**：常量值正确
- **依赖**：无

### 4.3 N4WorkQueueService
- **目标**：实现作业队列查询（从 CellDaoImpl 迁移）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/service/n4/N4WorkQueueService.java`
- **迁移方法**：
  | 原方法 | 新方法 | N4 表 |
  |--------|--------|-------|
  | getLoadOrder() | getLoadOrder(qcid) | inv_wq, inv_wi, inv_unit_yrd_visit, inv_unit_fcy_visit, inv_unit, xps_craneshift, xps_pointofwork, ref_equipment, inv_goods, argo_carrier_visit |
  | getDischargeOrder() | getDischargeOrder(qcid) | 同上 |
  | getQorder() | getCurrentWorkQueue(qcNum) | 同上 |
  | getSequenceList() | getSequenceList(qorder, qtype) | 同上 |
  | checkSequenceList() | checkSequenceList(qorder) | 同上 |
- **关键 SQL**（getLoadOrder 示例）：
  ```java
  SELECT MIN(iq.qorder) as qorder
  FROM MN4O_QC_inv_wq iq
  JOIN MN4O_QC_inv_wi iw ON iw.work_queue_gkey = iq.gkey
  JOIN MN4O_QC_inv_unit_yrd_visit iuyv ON iw.uyv_gkey = iuyv.gkey
  JOIN MN4O_QC_inv_unit_fcy_visit iufv ON iuyv.ufv_gkey = iufv.gkey
  JOIN MN4O_QC_inv_unit iu ON iufv.unit_gkey = iu.gkey
  JOIN MN4O_QC_xps_craneshift xcs ON iq.first_shift_pkey = xcs.pkey
  JOIN MN4O_QC_xps_pointofwork xpow ON xcs.owner_pow = xpow.pkey
  JOIN MN4O_QC_ref_equipment re ON re.gkey = iu.eq_gkey
  JOIN MN4O_QC_inv_goods ig ON ig.gkey = iu.goods
  JOIN MN4O_QC_argo_carrier_visit acv ON iq.pos_locid = acv.id
  WHERE acv.phase NOT IN ('60DEPARTED', '70CLOSED', '80CANCELED', '90ARCHIVED')
    AND iq.qtype IN ('LOAD')
    AND iq.qdeck IN ('A', 'B')
    AND iq.pos_loctype = 'VESSEL'
    AND iq.is_blue = '1'
    AND iw.move_kind != 'YARD'
    AND iw.move_kind != 'SHFT'
    AND iw.move_stage = 'COMPLETE'
    AND iufv.time_move BETWEEN SYSDATE - 1/1440 AND SYSDATE
    AND xpow.name = ?
  ```
- **SQL 参数化**：所有查询使用 PreparedStatement 参数化，禁止字符串拼接
- **验证**：返回 WorkQueueResult 包含 qorder, vesselId, bay, deckHold, sequences
- **依赖**：4.1

### 4.4 N4ContainerQueryService
- **目标**：实现集装箱查询（从 CellDaoImpl 迁移）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/service/n4/N4ContainerQueryService.java`
- **迁移方法**：
  | 原方法 | 新方法 | N4 表 |
  |--------|--------|-------|
  | getROBList() | getROBList(vesselId, minBay) | inv_unit_fcy_visit, inv_unit, argo_carrier_visit |
  | getROBListByBayNew() | getROBListByBay(vesselId, bay) | 同上 |
  | getHazardList() | getHazardList(unitId) | ref_hazardous_material, inv_unit |
  | getTwentyUnitList() | getTwentyUnitList(qcid, vesselId, bay) | inv_unit |
- **关键 SQL**（getROBList 示例）：
  ```java
  SELECT iufv.flex_string01 as bay
  FROM MN4O_QC_inv_unit_fcy_visit iufv
  JOIN MN4O_QC_inv_unit iu ON iufv.unit_gkey = iu.gkey
  JOIN MN4O_QC_argo_carrier_visit acv ON iufv.facility_gkey = acv.facility_gkey
  WHERE acv.id = ?
    AND SUBSTR(iufv.flex_string01, 1, 2) >= ?
    AND iufv.departure_visit_gkey IS NULL
  ```
- **SQL 参数化**：使用 ? 占位符
- **验证**：返回 List<RobContainer> 包含 bay, row, tier 信息
- **依赖**：4.1

### 4.5 N4VesselQueryService
- **目标**：实现船舶查询与 CellMatrix 回退逻辑（从 CellDaoImpl + VesselDaoImpl 迁移）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/service/n4/N4VesselQueryService.java`
- **迁移方法**：
  | 原方法 | 新方法 | 数据源 |
  |--------|--------|--------|
  | getCellMatrixFromnN4() | getCellMatrix(vesselId, bay, qdeck) | Oracle + MySQL 回退 |
  | getN4VesselNameById() | getVesselName(vesselId) | Oracle |
- **关键逻辑**（getCellMatrix）：
  1. 查询 N4 Oracle: argo_carrier_visit JOIN vsl_vsl_visit_details JOIN vsl_vessels JOIN t_vessel
  2. 如果 Oracle 查询返回空 → 回退到 MySQL: CellMatrixRepository.findByTypeAndActiveOrderByIdDesc
  3. 合并 rowStart/rowEnd/tierStart/tierEnd 范围
  4. 返回 List<CellMatrix>
- **SQL 参数化**：所有 SQL 使用 PreparedStatement
- **验证**：Oracle 成功时返回 N4 数据，失败时回退到 MySQL 数据
- **依赖**：4.1, 2.16

### 4.6 N4FacilityQueryService
- **目标**：实现设施查询（从 UserDaoImpl 迁移）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/service/n4/N4FacilityQueryService.java`
- **迁移方法**：
  | 原方法 | 新方法 | N4 表 |
  |--------|--------|-------|
  | queryQcId() | queryQcId() | xps_pointofwork, argo_yard, argo_facility |
  | queryFacilityByQcId() | queryFacilityByQcId(qcid) | argo_facility, argo_yard, xps_pointofwork |
- **关键 SQL**（queryQcId）：
  ```java
  SELECT DISTINCT xpow.name as qcid
  FROM MN4O_QC_xps_pointofwork xpow
  WHERE xpow.yard IN (
      SELECT gkey FROM MN4O_QC_argo_yard
      WHERE fcy_gkey IN (
          SELECT gkey FROM MN4O_QC_argo_facility
          WHERE name = ?
      )
  )
  ```
- **SQL 参数化**：company 名称参数化（原代码使用 PropertiesUtil）
- **验证**：返回 List<String> qcid 列表
- **依赖**：4.1

---

## 阶段五：MySQL Service 层（7个任务）

### 5.1 UserService
- **目标**：实现用户 CRUD + 登录日志（从 UserControl + UserDaoImpl 迁移）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/service/local/UserService.java`
- **迁移方法**：
  | 原方法 | 新方法 | 数据源 |
  |--------|--------|--------|
  | userDao.getAllUser() | findAll(pageable) | MySQL |
  | userDao.getUserById() | findById(id) | MySQL |
  | userDao.save() | save(user) | MySQL |
  | userDao.update() | updateQcId(id, qcid) | MySQL |
  | userDao.deleteById() | delete(id) | MySQL (级联删除 ShowLog) |
  | userDao.getUserByName() | findByUsername(username) | MySQL |
  | userDao.add(ShowLog) | recordLoginLog(user, qcid) | MySQL |
  | userDao.logout() | recordLogoutLog(user, qcid) | MySQL |
  | userDao.getUserLog() | getUserLogs(userId, start, end, pageable) | MySQL |
  | userDao.getUserLogByPeriod() | getLogsByPeriod(from, to) | MySQL |
- **关键逻辑**：
  - delete(id): 先删除 t_showlog 中 userid=id 的记录，再删除 t_user
  - recordLoginLog: 创建 ShowLog，设置 operation="LOGIN"
  - recordLogoutLog: 创建 ShowLog，设置 operation="LOGOUT"
- **验证**：CRUD 操作正确，级联删除生效
- **依赖**：2.11, 2.17

### 5.2 VesselService
- **目标**：实现船舶 CRUD + 搜索（从 CellControl + VesselDaoImpl 迁移）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/service/local/VesselService.java`
- **迁移方法**：
  | 原方法 | 新方法 | 数据源 |
  |--------|--------|--------|
  | vesselDao.getAllVessel() | findAll(pageable) | MySQL |
  | vesselDao.save() | save(vessel) | MySQL |
  | vesselDao.update() | update(id, vessel) | MySQL |
  | vesselDao.deleteById() | delete(id) | MySQL |
  | vesselDao.getVesselById() | findById(id) | MySQL |
  | vesselDao.getVesselByCondition() | findByCondition(vesselid, deckHold, bay) | MySQL |
  | vesselDao.searchVessel() | search(key, pageable) | MySQL |
  | vesselDao.getVesselListByName() | findByVesselid(vesselid) | MySQL |
- **关键逻辑**：
  - save: 使用 @UniqueConstraint(vesselid, deck_hold, bay) 约束，saveOrUpdate 逻辑
  - search: 支持 vesselid LIKE %key% OR deck_hold LIKE %key% OR bay LIKE %key%
- **验证**：唯一约束生效，模糊搜索正确
- **依赖**：2.12

### 5.3 VesselColorService
- **目标**：实现船舶颜色 CRUD（从 CellControl + VesselDaoImpl 迁移）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/service/local/VesselColorService.java`
- **迁移方法**：
  | 原方法 | 新方法 | 数据源 |
  |--------|--------|--------|
  | vesselDao.getAllVesselCol() | findAll(pageable) | MySQL |
  | vesselDao.searchVesselCol() | search(key, pageable) | MySQL |
  | vesselDao.getVesselColById() | findById(id) | MySQL |
  | vesselDao.saveOrUpdateVesselCol() | save(vesselCol) | MySQL |
  | vesselDao.deleteVesselColById() | delete(id) | MySQL |
- **验证**：CRUD 和模糊搜索正确
- **依赖**：2.13

### 5.4 VesselRefuelService
- **目标**：实现船舶加油 CRUD（从 CellControl + VesselDaoImpl 迁移）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/service/local/VesselRefuelService.java`
- **迁移方法**：
  | 原方法 | 新方法 | 数据源 |
  |--------|--------|--------|
  | vesselDao.getAllVesselRefuel() | findAll(pageable) | MySQL |
  | vesselDao.searchVesselRefuel() | search(key, pageable) | MySQL |
  | vesselDao.getVesselRefuelById() | findById(id) | MySQL |
  | vesselDao.saveOrUpdateVesselRefuel() | save(vesselRefuel) | MySQL |
  | vesselDao.deleteVesselRefuelById() | delete(id) | MySQL |
- **验证**：CRUD 和模糊搜索正确
- **依赖**：2.14

### 5.5 ColorSetService
- **目标**：实现颜色集 CRUD（从 CellControl + CellDaoImpl 迁移）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/service/local/ColorSetService.java`
- **迁移方法**：
  | 原方法 | 新方法 | 数据源 |
  |--------|--------|--------|
  | cellDao.getColSet() | findAll() | MySQL |
  | cellDao.getAllCol() | findAll() | MySQL |
  | cellDao.saveOrUpdateColSet() | save(colSet) | MySQL |
  | cellDao.updateColSet() | update(id, colSet) | MySQL |
  | cellDao.delColSet() | delete(id) | MySQL |
- **关键逻辑**：
  - save: boxcase 唯一约束
- **验证**：CRUD 正确，boxcase 唯一约束生效
- **依赖**：2.15

### 5.6 CellMatrixService
- **目标**：实现 CellMatrix 查询 + BaySize（从 CellDaoImpl 迁移）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/service/local/CellMatrixService.java`
- **迁移方法**：
  | 原方法 | 新方法 | 数据源 |
  |--------|--------|--------|
  | cellDao.getCellMatrix() | findByType(type) | MySQL |
  | cellDao.getBaySize() | getBaySize() | MySQL |
  | cellDao.updateCellMatrix() | updateBaySize(baySize) | MySQL |
  | vesselDao.getVesselByCondition() | findVesselConfig(vesselid, bay, deckHold) | MySQL |
- **关键逻辑**：
  - findByType: 查询 t_cell_matrix WHERE type=? AND active='1' ORDER BY id DESC
  - findVesselConfig: 从 t_vessel 查询 vesselid, deck_hold, bay 配置
- **验证**：查询正确，返回 List<CellMatrix> 和 List<Vessel>
- **依赖**：2.16, 2.12

### 5.7 OperationLogService
- **目标**：实现操作日志记录（从 VesselDaoImpl 迁移）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/service/local/OperationLogService.java`
- **迁移方法**：
  | 原方法 | 新方法 | 数据源 |
  |--------|--------|--------|
  | vesselDao.saveOperationLog() | saveLog(log) | MySQL |
  | - | findAll(pageable) | MySQL |
- **关键逻辑**：
  - saveLog: 使用 SecurityContextHelper.getCurrentUsername() 获取当前用户
  - 记录 function_name, action_type, old_values, new_values, timestamp
- **验证**：日志记录正确，包含当前用户名
- **依赖**：2.18, 3.2

---

## 阶段六：DTO 与响应对象（15个任务）

### 6.1 ApiResponse 通用响应
- **目标**：定义统一 JSON 响应格式
- **新增文件**：`/src/main/java/com/mtl/qcvmt/dto/ApiResponse.java`
- **实现要求**：
  ```java
  public class ApiResponse<T> {
      private int code;
      private String message;
      private T data;
      private long timestamp;
      
      public static <T> ApiResponse<T> success(T data) {
          ApiResponse<T> response = new ApiResponse<>();
          response.setCode(200);
          response.setMessage("success");
          response.setData(data);
          response.setTimestamp(System.currentTimeMillis());
          return response;
      }
      
      public static <T> ApiResponse<T> error(int code, String message) {
          ApiResponse<T> response = new ApiResponse<>();
          response.setCode(code);
          response.setMessage(message);
          return response;
      }
  }
  ```
- **验证**：JSON 序列化正确
- **依赖**：无

### 6.2 PageResponse 分页响应
- **目标**：定义分页包装器
- **新增文件**：`/src/main/java/com/mtl/qcvmt/dto/response/PageResponse.java`
- **实现要求**：
  ```java
  public class PageResponse<T> {
      private List<T> content;
      private int page;
      private int size;
      private long totalElements;
      private int totalPages;
      
      public static <T> PageResponse<T> from(Page<T> page) {
          PageResponse<T> response = new PageResponse<>();
          response.setContent(page.getContent());
          response.setPage(page.getNumber());
          response.setSize(page.getSize());
          response.setTotalElements(page.getTotalElements());
          response.setTotalPages(page.getTotalPages());
          return response;
      }
  }
  ```
- **验证**：分页信息正确
- **依赖**：无

### 6.3 UserResponse DTO
- **目标**：定义用户响应（排除敏感字段）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/dto/response/UserResponse.java`
- **字段**：id, keycloakId, qcid, username, role, parent, createtime
- **验证**：不包含 password（已移除）
- **依赖**：2.1

### 6.4 VesselResponse DTO
- **目标**：定义船舶响应
- **新增文件**：`/src/main/java/com/mtl/qcvmt/dto/response/VesselResponse.java`
- **字段**：id, vesselid, deckHold, bay, rowStart, rowEnd, tierStart, tierEnd
- **验证**：JSON 字段名驼峰
- **依赖**：2.2

### 6.5 ColSetResponse DTO
- **目标**：定义颜色集响应
- **新增文件**：`/src/main/java/com/mtl/qcvmt/dto/response/ColSetResponse.java`
- **字段**：id, boxcase, color
- **验证**：JSON 字段名正确
- **依赖**：2.5

### 6.6 VesselColResponse DTO
- **目标**：定义船舶颜色响应
- **新增文件**：`/src/main/java/com/mtl/qcvmt/dto/response/VesselColResponse.java`
- **字段**：id, vesselid, deckHold, bay, rowStart, rowEnd, tierStart, tierEnd
- **验证**：JSON 字段名正确
- **依赖**：2.3

### 6.7 VesselRefuelResponse DTO
- **目标**：定义船舶加油响应
- **新增文件**：`/src/main/java/com/mtl/qcvmt/dto/response/VesselRefuelResponse.java`
- **字段**：id, vesselid, isRefuel
- **验证**：JSON 字段名正确
- **依赖**：2.4

### 6.8 TerminalView DTO
- **目标**：定义终端查询响应（替代原 Busihandler HTML 表格）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/dto/response/TerminalView.java`
- **字段**：
  ```java
  public class TerminalView {
      private List<Vessel> vessels;
      private WorkQueueResult workQueue;
      private List<ColSet> colorSets;
      private List<RobContainer> robContainers;
      private String vesselId;
      private String bay;
      private String deckHold;
      private String qType;
      private int remainContainers;
  }
  ```
- **关键逻辑**：合并 MySQL Vessel 配置 + N4 作业队列 + ColSet 颜色 → 结构化 JSON
- **验证**：JSON 包含所有必要字段
- **依赖**：2.2, 2.5

### 6.9 WorkQueueResult DTO
- **目标**：定义作业队列结果
- **新增文件**：`/src/main/java/com/mtl/qcvmt/dto/response/WorkQueueResult.java`
- **字段**：
  ```java
  public class WorkQueueResult {
      private String qType;
      private String qorder;
      private String vesselId;
      private String minBay;
      private String maxBay;
      private String deckHold;
      private List<SequenceVO> sequences;
  }
  ```
- **验证**：包含 qorder, vesselId, sequences
- **依赖**：2.10

### 6.10 RobContainer DTO
- **目标**：定义 ROB 集装箱响应
- **新增文件**：`/src/main/java/com/mtl/qcvmt/dto/response/RobContainer.java`
- **字段**：bay, row, tier, containerId, operatorCode
- **验证**：JSON 字段名正确
- **依赖**：无

### 6.11 CreateUserRequest DTO
- **目标**：定义创建用户请求
- **新增文件**：`/src/main/java/com/mtl/qcvmt/dto/request/CreateUserRequest.java`
- **字段**：username, password, role, qcid
- **验证**：@NotNull username, password, role
- **依赖**：无

### 6.12 UpdateUserRequest DTO
- **目标**：定义更新用户请求
- **新增文件**：`/src/main/java/com/mtl/qcvmt/dto/request/UpdateUserRequest.java`
- **字段**：qcid, role
- **验证**：@NotNull qcid
- **依赖**：无

### 6.13 CreateVesselRequest DTO
- **目标**：定义创建船舶请求
- **新增文件**：`/src/main/java/com/mtl/qcvmt/dto/request/CreateVesselRequest.java`
- **字段**：vesselid, deckHold, bay, rowStart, rowEnd, tierStart, tierEnd
- **验证**：@NotNull vesselid, deckHold, bay
- **依赖**：无

### 6.14 UpdateVesselRequest DTO
- **目标**：定义更新船舶请求
- **新增文件**：`/src/main/java/com/mtl/qcvmt/dto/request/UpdateVesselRequest.java`
- **字段**：vesselid, deckHold, bay, rowStart, rowEnd, tierStart, tierEnd
- **验证**：@NotNull vesselid, deckHold, bay
- **依赖**：无

### 6.15 CreateColSetRequest DTO
- **目标**：定义创建颜色集请求
- **新增文件**：`/src/main/java/com/mtl/qcvmt/dto/request/CreateColSetRequest.java`
- **字段**：boxcase, color
- **验证**：@NotNull boxcase, color
- **依赖**：无

### 6.16 UpdateColSetRequest DTO
- **目标**：定义更新颜色集请求
- **新增文件**：`/src/main/java/com/mtl/qcvmt/dto/request/UpdateColSetRequest.java`
- **字段**：boxcase, color
- **验证**：@NotNull boxcase, color
- **依赖**：无

### 6.17 SaveVesselColRequest DTO
- **目标**：定义保存船舶颜色请求
- **新增文件**：`/src/main/java/com/mtl/qcvmt/dto/request/SaveVesselColRequest.java`
- **字段**：vesselid, deckHold, bay, rowStart, rowEnd, tierStart, tierEnd
- **验证**：@NotNull vesselid, deckHold, bay
- **依赖**：无

### 6.18 SaveVesselRefuelRequest DTO
- **目标**：定义保存船舶加油请求
- **新增文件**：`/src/main/java/com/mtl/qcvmt/dto/request/SaveVesselRefuelRequest.java`
- **字段**：vesselid, isRefuel
- **验证**：@NotNull vesselid, isRefuel
- **依赖**：无

### 6.19 UpdateBaySizeRequest DTO
- **目标**：定义更新 Bay 尺寸请求
- **新增文件**：`/src/main/java/com/mtl/qcvmt/dto/request/UpdateBaySizeRequest.java`
- **字段**：type, bay, deckHold, rowStart, rowEnd, tierStart, tierEnd
- **验证**：@NotNull type, bay, deckHold
- **依赖**：无

---

## 阶段七：Controller 层（10个任务）

### 7.1 UserController
- **目标**：实现用户管理 API（从 UserControl 迁移）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/controller/UserController.java`
- **删除文件**：`src/main/java/com/springMVC/control/UserControl.java`
- **路由映射**：
  | 原路由 | 新方法 | 新路由 |
  |--------|--------|--------|
  | POST /user/save | create() | POST /api/users |
  | GET /user/modify | getById() | GET /api/users/{id} |
  | POST /user/update | update() | PUT /api/users/{id} |
  | GET /user/del | delete() | DELETE /api/users/{id} |
  | GET /user/all | list() | GET /api/users |
  | GET /user/log | getUserLogs() | GET /api/users/{id}/logs |
  | - | currentUser() | GET /api/users/me |
- **权限**：@PreAuthorize("hasRole('qcvmt-admin')")，currentUser() 除外
- **关键逻辑**：
  - create: 调用 KeycloakRoleMappingService.createUserInKeycloak → 保存本地 t_user
  - update: 仅更新 qcid（不允许修改 role，由 Keycloak 管理）
  - delete: 级联删除 ShowLog
  - currentUser: 调用 KeycloakUserSyncService.getOrCreateLocalUser
- **验证**：CRUD 操作正确，权限控制生效
- **依赖**：5.1, 3.3, 3.4

### 7.2 VesselController
- **目标**：实现船舶管理 API（从 CellControl 迁移）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/controller/VesselController.java`
- **路由映射**：
  | 原路由 | 新方法 | 新路由 |
  |--------|--------|--------|
  | GET /user/allVessel | list() | GET /api/vessels |
  | GET /user/modifyVessel | getById() | GET /api/vessels/{id} |
  | POST /user/saveVessel | create() | POST /api/vessels |
  | POST /user/updateVessel | update() | PUT /api/vessels/{id} |
  | GET /user/delVessel | delete() | DELETE /api/vessels/{id} |
  | GET /user/searchVessel | search() | GET /api/vessels?search={key} |
- **权限**：@PreAuthorize("hasRole('qcvmt-admin')")
- **关键逻辑**：
  - list: 分页查询，支持搜索
  - search: 调用 vesselService.search(key, pageable)
- **验证**：CRUD 和搜索正确
- **依赖**：5.2

### 7.3 ColorSetController
- **目标**：实现颜色集管理 API（从 CellControl 迁移）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/controller/ColorSetController.java`
- **路由映射**：
  | 原路由 | 新方法 | 新路由 |
  |--------|--------|--------|
  | GET /user/allColSet | list() | GET /api/color-sets |
  | GET /user/modifyColSet | getById() | GET /api/color-sets/{id} |
  | POST /user/saveColSet | create() | POST /api/color-sets |
  | POST /user/updateColSet | update() | PUT /api/color-sets/{id} |
  | GET /user/delColSet | delete() | DELETE /api/color-sets/{id} |
- **权限**：@PreAuthorize("hasRole('qcvmt-admin')")
- **验证**：CRUD 正确，boxcase 唯一约束
- **依赖**：5.5

### 7.4 VesselColorController
- **目标**：实现船舶颜色管理 API（从 CellControl 迁移）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/controller/VesselColorController.java`
- **路由映射**：
  | 原路由 | 新方法 | 新路由 |
  |--------|--------|--------|
  | GET /user/allVesselCol | list() | GET /api/vessel-colors |
  | GET /user/modifyVesselCol | getById() | GET /api/vessel-colors/{id} |
  | POST /user/saveVesselCol | save() | POST /api/vessel-colors |
  | GET /user/delVesselCol | delete() | DELETE /api/vessel-colors/{id} |
- **权限**：@PreAuthorize("hasRole('qcvmt-admin')")
- **验证**：CRUD 正确
- **依赖**：5.3

### 7.5 VesselRefuelController
- **目标**：实现船舶加油管理 API（从 CellControl 迁移）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/controller/VesselRefuelController.java`
- **路由映射**：
  | 原路由 | 新方法 | 新路由 |
  |--------|--------|--------|
  | GET /user/allVesselRefuel | list() | GET /api/vessel-refuels |
  | GET /user/modifyVesselRefuel | getById() | GET /api/vessel-refuels/{id} |
  | POST /user/updateVesselRefuelStatus | save() | POST /api/vessel-refuels |
  | GET /user/delVesselRefuel | delete() | DELETE /api/vessel-refuels/{id} |
- **权限**：@PreAuthorize("hasRole('qcvmt-admin')")
- **验证**：CRUD 正确
- **依赖**：5.4

### 7.6 BayConfigController
- **目标**：实现 Bay 配置管理 API（从 CellControl 迁移）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/controller/BayConfigController.java`
- **路由映射**：
  | 原路由 | 新方法 | 新路由 |
  |--------|--------|--------|
  | GET /user/setbay | getBayConfig() | GET /api/bay-config |
  | POST /user/updateBay | updateBaySize() | PUT /api/bay-config |
- **权限**：@PreAuthorize("hasRole('qcvmt-admin')")
- **验证**：BaySize 查询和更新正确
- **依赖**：5.6

### 7.7 TerminalController
- **目标**：实现终端实时查询 API（从 CellControl.busiQuery + Busihandler 迁移）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/controller/TerminalController.java`
- **删除文件**：`src/main/java/com/accenture/vmt/Busihandler.java`
- **路由映射**：
  | 原路由 | 新方法 | 新路由 |
  |--------|--------|--------|
  | GET /user/BusiQuery | query() | GET /api/terminal/query |
- **权限**：@PreAuthorize("hasAnyRole('qcvmt-admin', 'qcvmt-user')")
- **关键逻辑**：
  1. 调用 keycloakUserSyncService.getOrCreateLocalUser() 获取当前用户
  2. 调用 n4WorkQueueService.getCurrentWorkQueue(qcNum) 获取作业队列
  3. 调用 cellMatrixService.findVesselConfig(vesselId, bay, deckHold) 获取船舶配置
  4. 调用 colorSetService.findAll() 获取颜色集
  5. 调用 n4ContainerQueryService.getROBList(vesselId, minBay) 获取 ROB 集装箱
  6. 合并数据构建 TerminalView JSON 响应
- **验证**：返回 TerminalView 包含 vessels, workQueue, colorSets, robContainers
- **依赖**：5.6, 5.5, 4.3, 4.4, 3.3

### 7.8 OperationLogController
- **目标**：实现操作日志查询 API（从 VesselDaoImpl 迁移）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/controller/OperationLogController.java`
- **路由映射**：
  | 原路由 | 新方法 | 新路由 |
  |--------|--------|--------|
  | - | list() | GET /api/operation-logs |
- **权限**：@PreAuthorize("hasRole('qcvmt-admin')")
- **验证**：分页查询正确
- **依赖**：5.7

### 7.9 ImportExportController
- **目标**：实现导入导出 API（从 UserControl 迁移）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/controller/ImportExportController.java`
- **路由映射**：
  | 原路由 | 新方法 | 新路由 |
  |--------|--------|--------|
  | GET /user/exportLogs | exportLogs() | GET /api/export/logs |
  | POST /user/importVessel | importVessel() | POST /api/import/vessel |
- **权限**：@PreAuthorize("hasRole('qcvmt-admin')")
- **关键逻辑**：
  - exportLogs: 调用 userService.getLogsByPeriod(from, to) → 生成 XLSX
  - importVessel: 接收 Excel 文件 → 解析 → 批量保存 t_vessel
- **验证**：导出 XLSX 正确，导入解析正确
- **依赖**：5.1, 5.2

### 7.10 删除旧 Controller 文件
- **目标**：清理旧 Spring MVC 代码
- **删除文件**：
  - `src/main/java/com/springMVC/control/CellControl.java`
  - `src/main/java/com/springMVC/control/UserControl.java`（已在 7.1 删除）
- **验证**：旧代码完全移除
- **依赖**：7.1-7.9

---

## 阶段八：异常处理与工具类（5个任务）

### 8.1 BusinessException
- **目标**：定义业务异常基类
- **新增文件**：`/src/main/java/com/mtl/qcvmt/exception/BusinessException.java`
- **实现要求**：
  ```java
  public class BusinessException extends RuntimeException {
      private String messageKey;
      
      public BusinessException(String messageKey) {
          super(messageKey);
          this.messageKey = messageKey;
      }
      
      public String getMessageKey() {
          return messageKey;
      }
  }
  ```
- **验证**：异常可以正确抛出
- **依赖**：无

### 8.2 N4ConnectionException
- **目标**：定义 N4 Oracle 连接异常
- **新增文件**：`/src/main/java/com/mtl/qcvmt/exception/N4ConnectionException.java`
- **实现要求**：
  ```java
  public class N4ConnectionException extends RuntimeException {
      public N4ConnectionException(String message) {
          super(message);
      }
      
      public N4ConnectionException(String message, Throwable cause) {
          super(message, cause);
      }
  }
  ```
- **验证**：Oracle 连接失败时抛出
- **依赖**：无

### 8.3 GlobalExceptionHandler
- **目标**：统一异常处理，返回 ApiResponse
- **新增文件**：`/src/main/java/com/mtl/qcvmt/exception/GlobalExceptionHandler.java`
- **实现要求**：
  - @RestControllerAdvice
  - @ExceptionHandler(BusinessException.class) → ApiResponse.error(400, message)
  - @ExceptionHandler(N4ConnectionException.class) → ApiResponse.error(503, "n4_connection_error")
  - @ExceptionHandler(AccessDeniedException.class) → ApiResponse.error(403, "access_denied")
  - @ExceptionHandler(MethodArgumentNotValidException.class) → ApiResponse.error(400, validation errors)
  - @ExceptionHandler(Exception.class) → ApiResponse.error(500, "internal_error")
- **验证**：所有异常转换为 JSON 响应
- **依赖**：8.1, 8.2

### 8.4 WebUtil 工具类迁移
- **目标**：保留必要的 Web 工具方法
- **删除文件**：
  - `src/main/java/com/springMVC/util/WebUtil.java`（保留需要的方法）
  - `src/main/java/com/springMVC/util/CookiesUtil.java`（移除，Cookie 由前端管理）
  - `src/main/java/com/springMVC/util/PropertiesUtil.java`（移除，使用 @Value）
  - `src/main/java/com/springMVC/util/LogUtil.java`（移除，使用 SLF4J）
  - `src/main/java/com/springMVC/util/Constants.java`（移除，使用常量类）
  - `src/main/java/com/springMVC/util/MessageUtil.java`（移除，使用 MessageSource）
  - `src/main/java/com/springMVC/util/GeneralException.java`（移除，使用 BusinessException）
- **新增文件**：`/src/main/java/com/mtl/qcvmt/util/WebUtil.java`
- **保留方法**：
  - getDateTimeNow(): 返回当前时间 ISO 格式
  - getTime(): 返回 LocalDateTime.now()
  - getPreMonthTime(): 返回一个月前的 LocalDateTime
  - DataFormatTransfer(String dateStr): 日期格式转换
- **验证**：工具方法正常工作
- **依赖**：无

### 8.5 ImportHandler 工具类迁移
- **目标**：迁移船舶导入逻辑
- **删除文件**：`src/main/java/com/springMVC/util/ImportHandler.java`
- **新增文件**：`/src/main/java/com/mtl/qcvmt/util/ImportHandler.java`
- **实现要求**：
  - uploadFile(request): 接收 MultipartFile，保存到临时目录
  - importVessel(file): 使用 Apache POI 解析 Excel，批量保存 t_vessel
  - 注入 VesselService
- **关键逻辑**：
  - 解析 Excel 列：vesselid, deck_hold, bay, row_start, row_end, tier_start, tier_end
  - 调用 vesselService.save(vessel) 逐个保存
- **验证**：Excel 文件正确解析，数据保存到 MySQL
- **依赖**：5.2

### 8.6 ExportHandler 工具类迁移
- **目标**：迁移日志导出逻辑
- **删除文件**：`src/main/java/com/springMVC/util/ExportHandler.java`
- **新增文件**：`/src/main/java/com/mtl/qcvmt/util/ExportHandler.java`
- **实现要求**：
  - exportQCLog(response, logs): 生成 XLSX 文件，设置 Content-Disposition
  - 使用 Apache POI 创建 Workbook
- **关键逻辑**：
  - 列：username, qcid, loginTime, operation
  - 设置响应头：Content-Type=application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
- **验证**：XLSX 文件正确生成，可下载
- **依赖**：无

---

## 阶段九：测试与验证（8个任务）

### 9.1 User 实体单元测试
- **目标**：测试 User 实体 JPA 映射
- **新增文件**：`/src/test/java/com/mtl/qcvmt/entity/UserTest.java`
- **测试用例**：
  - testUserCreation: 创建 User 并保存
  - testUserFindByKeycloakId: 按 keycloak_id 查询
  - testUserUniqueKeycloakId: 唯一约束测试
- **验证**：@DataJpaTest 通过
- **依赖**：2.1, 1.4

### 9.2 UserService 单元测试
- **目标**：测试用户 CRUD 逻辑
- **新增文件**：`/src/test/java/com/mtl/qcvmt/service/local/UserServiceTest.java`
- **测试用例**：
  - testFindAll: 分页查询
  - testSave: 创建用户
  - testUpdateQcId: 更新 qcid
  - testDelete: 级联删除 ShowLog
  - testRecordLoginLog: 记录登录日志
  - testGetUserLogs: 查询用户日志
- **验证**：@SpringBootTest + @Transactional 通过
- **依赖**：5.1

### 9.3 VesselService 单元测试
- **目标**：测试船舶 CRUD 逻辑
- **新增文件**：`/src/test/java/com/mtl/qcvmt/service/local/VesselServiceTest.java`
- **测试用例**：
  - testFindAll: 分页查询
  - testSave: 创建船舶
  - testUpdate: 更新船舶
  - testDelete: 删除船舶
  - testUniqueConstraint: 唯一约束测试
  - testSearch: 模糊搜索
- **验证**：@SpringBootTest + @Transactional 通过
- **依赖**：5.2

### 9.4 N4WorkQueueService 集成测试
- **目标**：测试 N4 Oracle 查询
- **新增文件**：`/src/test/java/com/mtl/qcvmt/service/n4/N4WorkQueueServiceTest.java`
- **测试用例**：
  - testGetLoadOrder: 查询 LOAD 作业顺序
  - testGetDischargeOrder: 查询 DISCH 作业顺序
  - testGetCurrentWorkQueue: 获取当前作业队列
  - testGetSequenceList: 查询作业序列
  - testCheckSequenceList: 跨 Bay 校验
- **验证**：@SpringBootTest + @ActiveProfiles("test") 通过（需要 Oracle 测试环境）
- **依赖**：4.3

### 9.5 TerminalController 集成测试
- **目标**：测试终端查询 API
- **新增文件**：`/src/test/java/com/mtl/qcvmt/controller/TerminalControllerTest.java`
- **测试用例**：
  - testQueryWithValidJwt: 有效 JWT 查询成功
  - testQueryWithInvalidJwt: 无效 JWT 返回 401
  - testQueryWithUserRole: qcvmt-user 角色可访问
  - testQueryWithAdminRole: qcvmt-admin 角色可访问
- **验证**：@WebMvcTest + @WithMockJwt 通过
- **依赖**：7.7, 1.8

### 9.6 SecurityConfig 测试
- **目标**：测试安全配置
- **新增文件**：`/src/test/java/com/mtl/qcvmt/config/SecurityConfigTest.java`
- **测试用例**：
  - testAdminRoutesRequireAdminRole: /api/users/** 需要 qcvmt-admin
  - testTerminalRoutesAccessibleByUser: /api/terminal/** qcvmt-user 可访问
  - testActuatorHealthPublic: /actuator/health 无需认证
  - testSwaggerUiPublic: /swagger-ui/** 无需认证
- **验证**：@WebMvcTest 通过
- **依赖**：1.8

### 9.7 数据库迁移验证脚本
- **目标**：验证 MySQL Schema 正确性
- **新增文件**：`/src/test/resources/test-data.sql`
- **内容**：
  ```sql
  INSERT INTO t_user (keycloak_id, qcid, name, role, parent, createtime)
  VALUES ('test-uuid-1', 'QC83', 'testuser', 'USER', 'admin', NOW());
  
  INSERT INTO t_vessel (vesselid, deck_hold, bay, row_start, row_end, tier_start, tier_end)
  VALUES ('V123456', 'H', '17H', '1', '19', '82', '90');
  
  INSERT INTO t_col_set (boxcase, color)
  VALUES ('EMPTY', 'white');
  ```
- **验证**：测试数据插入成功
- **依赖**：1.4

### 9.8 完整集成测试
- **目标**：端到端流程验证
- **新增文件**：`/src/test/java/com/mtl/qcvmt/QcvmtApplicationIntegrationTest.java`
- **测试用例**：
  - testApplicationStarts: 应用启动无错误
  - testHealthEndpoint: GET /actuator/health 返回 UP
  - testFullUserWorkflow: 创建用户 → 查询 → 更新 → 删除
  - testFullVesselWorkflow: 创建船舶 → 查询 → 更新 → 删除
  - testTerminalQueryFlow: JWT 登录 → 终端查询 → 返回 TerminalView
- **验证**：@SpringBootTest(webEnvironment = RANDOM_PORT) 所有测试通过
- **依赖**：所有阶段

---

## 阶段十：清理与文档（3个任务）

### 10.1 清理旧代码
- **目标**：删除所有旧 Spring MVC 代码
- **删除文件**：
  - `src/main/java/com/springMVC/dao/CellDao.java`
  - `src/main/java/com/springMVC/dao/CellDaoImpl.java`
  - `src/main/java/com/springMVC/dao/UserDao.java`
  - `src/main/java/com/springMVC/dao/UserDaoImpl.java`
  - `src/main/java/com/springMVC/dao/VesselDao.java`
  - `src/main/java/com/springMVC/dao/VesselDaoImpl.java`
  - `src/main/java/com/springMVC/filter/SecurityInterceptor.java`
  - `src/main/java/com/accenture/vmt/ClientIdentifierConnectionPreparer.java`
  - `src/main/java/com/springMVC/entity/Cell.java`
  - `src/main/java/com/springMVC/entity/PageManage.java`
  - 所有旧 `src/main/java/com/springMVC/util/*.java`（已在 8.4 删除）
- **验证**：`src/main/java/com/springMVC/` 目录完全删除
- **依赖**：7.10, 8.4

### 10.2 清理旧配置文件
- **目标**：删除旧 Spring XML 配置和 JSP
- **删除目录**：
  - `src/main/webapp/WEB-INF/jsp/`（所有 JSP）
  - `src/main/webapp/WEB-INF/springMVC-servlet.xml`
  - `src/main/webapp/WEB-INF/web.xml`
  - `src/main/resources/db.properties`
- **验证**：旧配置文件完全删除
- **依赖**：1.3

### 10.3 README 更新
- **目标**：更新项目文档
- **修改文件**：`/README.md`
- **新增内容**：
  - 技术栈说明：Spring Boot 3.5.16, Spring Data JPA, Keycloak OIDC
  - 构建命令：`./gradlew build`
  - 启动命令：`./gradlew bootRun`
  - 环境变量说明：
    - MYSQL_HOST, MYSQL_PORT, MYSQL_DB, MYSQL_USER, MYSQL_PASSWORD
    - N4_HOST, N4_PORT, N4_SERVICE, N4_USER, N4_PASSWORD
    - KEYCLOAK_URL, KEYCLOAK_REALM, KEYCLOAK_CLIENT_ID, KEYCLOAK_CLIENT_SECRET
    - CORS_ORIGINS
  - API 文档：http://localhost:8080/swagger-ui.html
  - 健康检查：http://localhost:8080/actuator/health
- **验证**：文档完整，命令可执行
- **依赖**：所有阶段

---

## 风险缓解与质量保证

### 高风险项
1. **N4 Oracle SQL 语义变更**（CellDaoImpl 1884 行，12 个复杂 SQL）
   - **缓解**：逐一对照原始 SQL，使用 N4TableConstants 确保表名正确
   - **验收**：N4WorkQueueServiceTest 所有测试通过

2. **双数据源事务冲突**
   - **缓解**：MysqlDataSourceConfig 配置事务，N4OracleDataSourceConfig 仅查询
   - **验收**：TerminalController 跨数据源查询无事务错误

3. **Keycloak 用户同步失败**
   - **缓解**：KeycloakUserSyncService 在 Filter 中自动创建
   - **验收**：首次访问 API 自动创建 t_user

4. **CellMatrix N4 回退逻辑**
   - **缓解**：N4VesselQueryService.getCellMatrix() 实现 Oracle → MySQL 回退
   - **验收**：Oracle 连接失败时返回 MySQL 数据

### SQL 注入漏洞修复
- **现状**：CellDaoImpl 中大量字符串拼接（`iq.qorder='" + qorder + "'`）
- **修复**：所有 N4 SQL 使用 PreparedStatement 参数化
- **验收**：代码审查无字符串拼接 SQL

### 兼容性注意事项
1. **列名映射**：
   | 原表名 | 新表名 | 列名变更 |
   |--------|--------|----------|
   | T_USER | t_user | userid → id, NAME → name |
   | T_Vessel | t_vessel | vmid → id, rowstart → row_start |
   | T_VESSELCOL | t_vessel_col | vcid → id |
   | T_VESSELREFUEL | t_vessel_refuel | vrid → id |
   | T_COLSET | t_col_set | colsetid → id |
   | T_CELLMATRIX | t_cell_matrix | matrixid → id |
   | T_SHOWLOG | t_showlog | userlogid → id, LOGINTIME → login_time |
   | T_OPERATIONLOG | t_operation_log | OPERLOGID → id, TIME → timestamp |

2. **时间格式**：
   - 原格式：`yyyyMMddHHmmss` (String)
   - 新格式：`yyyy-MM-dd HH:mm:ss` (LocalDateTime)
   - 迁移脚本需转换：`STR_TO_DATE(createtime, '%Y%m%d%H%i%s')`

3. **主键策略**：
   - Oracle: SEQUENCE (user_seq, vessel_seq)
   - MySQL: IDENTITY (AUTO_INCREMENT)
   - 外键引用需更新（如 t_showlog.userid 引用 t_user.id）

---

## 执行顺序与依赖关系

```
阶段一（基础设施）
├── 1.1 Gradle 构建
├── 1.2 启动类（依赖 1.1）
├── 1.3 配置文件（依赖 1.1）
├── 1.4 Schema（依赖 1.3）
├── 1.5 多语言文件（依赖 1.3）
├── 1.6 MySQL 数据源（依赖 1.3, 1.4）
├── 1.7 N4 数据源（依赖 1.3）
├── 1.8 Security（依赖 1.3, 1.7）
├── 1.9 CORS（依赖 1.3）
├── 1.10 WebConfig（依赖 1.5）
├── 1.11 KeycloakConfig（依赖 1.3）
└── 1.12 .gitignore

阶段二（实体层）
├── 2.1-2.8 实体类（依赖 1.4）
├── 2.9-2.10 DTO（无依赖）
└── 2.11-2.18 Repository（依赖 2.1-2.8）

阶段三（Security）
├── 3.1-3.2 JWT（依赖 1.8）
├── 3.3 UserSync（依赖 2.11, 3.2）
└── 3.4 RoleMapping（依赖 1.11）

阶段四（N4 Service）
├── 4.1-4.2 N4Query（依赖 1.7）
└── 4.3-4.6 N4 Service（依赖 4.1）

阶段五（MySQL Service）
├── 5.1-5.7 Service（依赖 Repository）

阶段六（DTO）
├── 6.1-6.19 DTO（无依赖）

阶段七（Controller）
├── 7.1-7.9 Controller（依赖 Service, DTO）
└── 7.10 删除旧代码（依赖 7.1-7.9）

阶段八（异常处理）
├── 8.1-8.3 Exception（无依赖）
├── 8.4-8.6 工具类（依赖 Service）

阶段九（测试）
├── 9.1-9.8 测试（依赖所有）

阶段十（清理）
├── 10.1-10.3 清理文档（依赖 7.10, 8.4）
```

---

## 验收标准

### 功能验收
- [ ] 所有 81 个 Java 文件编译通过
- [ ] 应用启动无报错
- [ ] Swagger UI 可访问，API 文档完整
- [ ] 健康检查端点返回 UP
- [ ] 所有 REST API 端点可访问
- [ ] JWT Token 验证正确
- [ ] 角色权限控制生效
- [ ] 双数据源正常工作
- [ ] N4 Oracle 查询返回数据
- [ ] MySQL CRUD 操作正确
- [ ] 导入导出功能正常

### 性能验收
- [ ] API 响应时间 < 500ms
- [ ] MySQL 连接池最大 40 连接
- [ ] Oracle 连接池最大 50 连接
- [ ] 分页查询支持大数据量

### 安全验收
- [ ] 无 SQL 注入漏洞
- [ ] 密码不存储在本地数据库
- [ ] CORS 配置正确
- [ ] CSRF 禁用（前后端分离）
- [ ] 敏感端点权限控制

### 代码质量
- [ ] 单元测试覆盖率 > 70%
- [ ] 集成测试全部通过
- [ ] 旧代码完全删除
- [ ] 代码符合 Spring Boot 3 最佳实践
- [ ] 无编译警告

---

## 补全

### Task 11.1 OperationLog 导出 API（Excel 下载）
- **目标**：补充 OperationLog 的 Excel 导出端点 `GET /api/operation-logs/export`，design.md §4.1.8 定义但 tasks.md 原任务 7.8 仅实现查询接口，遗漏导出。
- **文件**：
  - Modify: `/src/main/java/com/mtl/qcvmt/controller/OperationLogController.java`（由任务 7.8 新建，本任务追加 export 方法）
  - Modify: `/src/main/java/com/mtl/qcvmt/service/local/OperationLogService.java`（由任务 5.7 新建，追加按时间范围查询方法）
  - Modify: `/src/main/java/com/mtl/qcvmt/util/ExportHandler.java`（由任务 8.6 新建，新增 `exportOperationLog` 方法，列：username、function_name、action_type、values_before、values_after、create_time）
- **涉及类/方法**：
  - `OperationLogController.exportOperationLogs(HttpServletResponse, LocalDateTime startTime, LocalDateTime endTime)`
  - `OperationLogService.findByTimeRange(LocalDateTime from, LocalDateTime to): List<OperationLog>`
  - `OperationLogRepository.findByCreateTimeBetweenOrderByCreateTimeDesc(LocalDateTime, LocalDateTime): List<OperationLog>`
- **实施步骤**：
  - [ ] 在 OperationLogRepository 添加 `findByCreateTimeBetweenOrderByCreateTimeDesc` 方法
  - [ ] 在 OperationLogService 添加 `findByTimeRange(from, to)` 方法，调用 Repository
  - [ ] 在 ExportHandler 添加 `exportOperationLogs(HttpServletResponse, List<OperationLog>)`，使用 Apache POI 生成 XLSX
  - [ ] 在 OperationLogController 添加 `@GetMapping("/export") exportOperationLogs(@RequestParam startTime, @RequestParam endTime, HttpServletResponse response)`，调用 Service + ExportHandler
  - [ ] 设置响应头 `Content-Type=application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`，`Content-Disposition=attachment; filename=operation_logs.xlsx`
- **权限**：`@PreAuthorize("hasRole('qcvmt-admin')")`
- **测试命令**：`curl -H "Authorization: Bearer <admin-jwt>" "http://localhost:8080/api/operation-logs/export?startTime=2026-01-01T00:00:00&endTime=2026-12-31T23:59:59" -o logs.xlsx`
- **预期结果**：返回 200，下载文件可被 Excel 打开，包含指定时间范围的操作日志
- **验收标准**：导出端点返回合法 XLSX，未登录或 qcvmt-user 角色返回 401/403
- **依赖**：7.8、5.7、8.6、1.1（POI 依赖）
- **兼容性**：与 7.9 的 exportLogs（登录日志导出）保持接口风格一致

### Task 11.2 N4QueryRepository 重试机制 + OptimisticLockException 处理
- **目标**：补充 design.md §5.2.2 定义的 N4 连接重试机制（@Retryable，最大 3 次、指数退避）以及 §5.3 定义的乐观锁冲突 409 响应，tasks.md 原任务 4.1 和 8.3 均未覆盖。
- **文件**：
  - Modify: `/build.gradle.kts`（追加 spring-retry + spring-boot-starter-aop 依赖）
  - Modify: `/src/main/java/com/mtl/qcvmt/n4/N4QueryRepository.java`（由任务 4.1 新建，追加 @Retryable）
  - Modify: `/src/main/java/com/mtl/qcvmt/exception/GlobalExceptionHandler.java`（由任务 8.3 新建，追加 OptimisticLockException 处理）
  - Modify: `/src/main/java/com/mtl/qcvmt/QcvmtApplication.java`（由任务 1.2 新建，追加 `@EnableRetry`）
- **涉及类/方法**：
  - `N4QueryRepository.queryForList` / `queryForMap`：追加 `@Retryable(value = N4ConnectionException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))`
  - `N4QueryRepository.isConnectionException(DataAccessException ex)`：识别 SQLTransientConnectionException / "Connection refused" / "Connection timed out"，抛 N4ConnectionException；其余抛 N4QueryException
  - `GlobalExceptionHandler.handleOptimisticLockException(OptimisticLockException ex)`：返回 `ApiResponse.error(409, "数据已被其他用户修改，请刷新后重试")`
- **实施步骤**：
  - [ ] 在 build.gradle.kts dependencies 块追加 `implementation("org.springframework.retry:spring-retry")` 和 `implementation("org.springframework.boot:spring-boot-starter-aop")`
  - [ ] 在 QcvmtApplication 追加 `@EnableRetry` 注解
  - [ ] 在 N4QueryRepository.queryForList 方法追加 @Retryable 注解与 @Recover 兜底方法
  - [ ] 实现 `isConnectionException`：判断 DataAccessException.getCause() instanceof SQLTransientConnectionException 或消息包含 "Connection refused"/"Connection timed out"
  - [ ] 在 GlobalExceptionHandler 追加 `@ExceptionHandler(OptimisticLockException.class)` 方法，返回 ApiResponse.error(409, "乐观锁冲突")
- **测试命令**：
  - `./gradlew test --tests "com.mtl.qcvmt.n4.N4QueryRepositoryTest.testRetryOnConnectionFailure"`
  - `./gradlew test --tests "com.mtl.qcvmt.exception.GlobalExceptionHandlerTest.testOptimisticLockReturns409"`
- **预期结果**：
  - N4 连接异常时自动重试最多 3 次，最终失败抛 N4ConnectionException（非 N4QueryException）
  - OptimisticLockException 触发时返回 HTTP 409 + ApiResponse.code=409
- **验收标准**：重试日志可观测（最多 3 次），最终失败走 GlobalExceptionHandler → 50001；乐观锁冲突返回 409 JSON
- **依赖**：4.1、8.3、1.2
- **兼容性**：@Retryable 依赖 spring-aop，仅作用于 N4QueryRepository 方法，不影响 MySQL JPA 操作；OptimisticLockException 为 jakarta.persistence 标准异常，JPA @Version 自动抛出

---

## 附录：文件清单（共 81 个文件）

### 构建与配置（7个）
1. build.gradle.kts
2. settings.gradle.kts
3. gradle wrapper (jar + properties + scripts)
4. schema.sql
5. application.yml + 4 个 profile
6. messages_*.properties (3个)
7. .gitignore

### Java 源文件（74个）
- **config/** (7): MysqlDataSourceConfig, N4OracleDataSourceConfig, SecurityConfig, KeycloakConfig, CorsConfig, WebConfig
- **entity/** (10): User, Vessel, VesselCol, VesselRefuel, ColSet, CellMatrix, ShowLog, OperationLog, BaySize, SequenceVO
- **repository/** (8): UserRepository, VesselRepository, VesselColRepository, VesselRefuelRepository, ColSetRepository, CellMatrixRepository, ShowLogRepository, OperationLogRepository
- **n4/** (2): N4QueryRepository, N4TableConstants
- **security/** (2): JwtAuthConverter, SecurityContextHelper
- **service/** (15): UserService, VesselService, VesselColorService, VesselRefuelService, ColorSetService, CellMatrixService, OperationLogService, KeycloakUserSyncService, KeycloakRoleMappingService, N4VesselQueryService, N4WorkQueueService, N4ContainerQueryService, N4FacilityQueryService, KeycloakConfig
- **dto/** (19): ApiResponse, PageResponse, UserResponse, VesselResponse, ColSetResponse, VesselColResponse, VesselRefuelResponse, TerminalView, WorkQueueResult, RobContainer, CreateUserRequest, UpdateUserRequest, CreateVesselRequest, UpdateVesselRequest, CreateColSetRequest, UpdateColSetRequest, SaveVesselColRequest, SaveVesselRefuelRequest, UpdateBaySizeRequest
- **controller/** (9): UserController, VesselController, ColorSetController, VesselColorController, VesselRefuelController, BayConfigController, TerminalController, OperationLogController, ImportExportController
- **exception/** (3): BusinessException, N4ConnectionException, GlobalExceptionHandler
- **util/** (3): WebUtil, ImportHandler, ExportHandler

### 测试文件（9个）
- UserTest, UserServiceTest, VesselServiceTest
- N4WorkQueueServiceTest, TerminalControllerTest, SecurityConfigTest
- test-data.sql, QcvmtApplicationIntegrationTest

### 需删除的旧文件（共 33 个）
- `src/main/java/com/springMVC/` 整个目录（33 个 .java）
- `src/main/java/com/accenture/vmt/` 整个目录（2 个 .java）
- `src/main/webapp/` 整个目录（JSP + XML 配置）
- `pom.xml`