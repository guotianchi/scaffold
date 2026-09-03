# 技术设计

## 上下文（Context）

- 当前状态：`springboot-scaffold` 为空 Git 仓库，仅有 spec-superflow 工作流文件；无业务代码。
- 约束条件：JDK 17、Spring Boot 2.7.18、Maven 多模块（parent 含 module）、groupId/包名 `ai.openbuy`、MyBatis-Plus、MySQL 配置占位、禁止 JPA、沟通上每个关键决策先问。
- 利益相关者：后续在此脚手架上开发 OpenBuy 相关服务的开发者。

## 目标（Goals）

- 提供可编译的 parent + `core` + `app` 结构。
- `app` 作为唯一可启动进程，暴露 `GET /hello`。
- 持久化栈预留 MyBatis-Plus + MySQL，且编译不依赖真实数据库连通。

## 非目标

- 不实现业务表、Mapper XML 示例 SQL、代码生成器运行时调用。
- 不引入鉴权、统一响应、配置中心。
- 不把 `core` 做成可执行应用。

## 决策（Decisions）

### 决策 1：Maven 聚合而非单模块或 Gradle

- **选择**：根 POM `packaging=pom`，子模块 `core`、`app`；依赖版本由 Spring Boot 2.7.18 parent/BOM 管理。
- **理由**：用户要求 project 包含 module；Java 生态与 IDE 对 Maven 多模块支持成熟；2.7.18 是 2.7 线末版，与 JDK 17 官方兼容，替代了最初的 2.4。
- **考虑的替代方案**：Gradle 多项目（偏离“module”约定）；Spring Boot 2.4 + JDK 17（官方支持不足）；仅 parent + app 单业务模块（几乎不是多模块）。

### 决策 2：app 可运行、core 为库

- **选择**：`@SpringBootApplication`、Web、Hello 控制器、MyBatis-Plus、数据源配置只放在 `app`；`core` 提供空的基础包（例如 `ai.openbuy.core` 占位类），被 `app` compile 依赖。
- **理由**：启动边界清晰，后续业务可把可复用代码下沉到 `core`，而不把进程入口拆散。
- **考虑的替代方案**：三个模块 web/api/core（超出骨架）；把 MyBatis 放到 `core`（过早，且数据源属于运行进程）。

### 决策 3：MyBatis-Plus + yml 占位，测试不连库

- **选择**：`app` 依赖 `mybatis-plus-boot-starter`（与 Boot 2.7 兼容的 3.5.x 线）和 `mysql-connector-java`；`application.yml` 写明 JDBC URL/用户名/密码占位。自动化验证以 `mvn package` 与不启动完整数据源上下文的切片测试为主；`GET /hello` 用 `@WebMvcTest`（或不加载数据源的测试切片）断言，避免 CI 无 MySQL 失败。
- **理由**：用户明确要 Plus + MySQL 占位，不要 JPA；又要求无真实 MySQL 也能 package。
- **考虑的替代方案**：H2 内存库（可自测但偏离 MySQL 占位）；官方 mybatis-spring-boot-starter 无 Plus；排除 DataSource 自动配置导致占位配置无意义。

### 决策 4：坐标与模块名

- **选择**：`groupId=ai.openbuy`；根 `artifactId=springboot-scaffold`；`core` / `app` 同名 artifact；Java 包 `ai.openbuy.core` 与 `ai.openbuy.app`。
- **理由**：用户指定包名 `ai.openbuy`；模块名 `app` 经确认保留。
- **考虑的替代方案**：`com.example`（已否决）；将 `app` 改名为 `web`/`boot`（已否决）。

## 风险与权衡（Risks And Trade-Offs）

- `spring-boot:run` 或加载完整 ApplicationContext 可能因 MySQL 不可达而失败 → 缓解：规格只强制 `package` 成功；README 不作为本变更必交付（范围外文档可在实现时加最短注释于 yml）。
- MyBatis-Plus 3.5 与 Boot 2.7 版本需钉死 → 缓解：在 parent 属性中固定 `mybatis-plus.version`，实现时选用 3.5.5 一类已知 2.7 兼容版本。
- `core` 几乎为空可能被看成多余 → 缓解：保留一个公开占位类型，证明模块依赖边真实存在。

## 迁移计划

- 上线步骤：本变更是新仓基线，合并即成为默认结构。
- 回滚步骤：丢弃本仓提交即可；无生产数据迁移。

## 待明确问题

- 无。MySQL 占位的具体 host/库名可在 yml 使用 `jdbc:mysql://127.0.0.1:3306/openbuy` 作为文档式默认，不视为未决。
