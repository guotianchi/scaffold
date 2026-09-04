# 技术设计

## 上下文（Context）

- **当前状态**：根 `pom.xml` 聚合 `core` + `app`；`app` 提供 Boot 入口、`GET /hello`、MyBatis-Plus 与 MySQL 占位。磁盘上另有未纳入 POM 的 `common/`、`service/`、`dao/`、`controller/` 残留（含 JWT/Auth/登录页），与正式结构冲突。
- **约束条件**：JDK 17、Spring Boot 2.7.18、`groupId`/`包名` `ai.openbuy`、MyBatis-Plus（非 JPA）、在 `main` 分支直接改、不新建 git 分支、不含鉴权业务。
- **利益相关者**：在此脚手架上按分层惯例扩展 OpenBuy 服务的开发者。

## 目标（Goals）

- 用四个 Maven module 表达 Controller / Service / DAO / Common 边界。
- 保留 `GET /hello` 行为与 MySQL 占位配置。
- 删除 `core`/`app` 及 JWT 残留，使 `mvn -pl controller -am package` 成为标准验证命令。

## 非目标

- 实现真实 Mapper、Entity、Repository 方法。
- JWT、登录页、Spring Security。
- 统一响应体、全局异常处理、配置中心。

## 决策（Decisions）

### 决策 1：四模块严格分层依赖

- **选择**：`controller → service → dao → common`，禁止跨层反向依赖。
- **理由**：与用户指定的模块职责一致；Maven Enforcer 式边界便于后续扩展与 code review。
- **考虑的替代方案**：`controller` 同时依赖 `dao`（跳过 service 层，违反分层）；扁平 `common`+`boot` 两模块（不符合四模块要求）。

### 决策 2：controller 为唯一 Boot 进程

- **选择**：`@SpringBootApplication`、`application.yml`、`spring-boot-maven-plugin`、`@MapperScan("ai.openbuy.dao.mapper")` 均在 `controller`；`HelloController` 注入 `HelloService`。
- **理由**：HTTP 与进程生命周期属于接入层；MyBatis 扫描需在 Boot 入口声明，但 Mapper 接口定义留在 `dao`。
- **考虑的替代方案**：独立 `boot` 第五模块（超出范围）；在 `dao` 放 `@SpringBootApplication`（混淆数据层与进程边界）。

### 决策 3：MyBatis-Plus 依赖放在 dao

- **选择**：`mybatis-plus-boot-starter` 与 `mysql-connector-j`（runtime）声明在 `dao/pom.xml`；`service` 通过依赖 `dao` 获得持久化 API；`controller` 通过 `service` 传递获得 starter。
- **理由**：持久化技术栈归属数据访问层；Boot 自动配置在启动模块 classpath 上生效。
- **考虑的 alternative**：starter 只放在 `controller`（dao 无法独立编译验证 MyBatis 类型）。

### 决策 4：占位类型证明模块边界

- **选择**：`common.CommonMarker`、`dao.mapper.MapperPackage` 作为各层可依赖的公开占位类；`ScaffoldContractTest` 断言 `CommonMarker` 与 MyBatis-Plus 在 classpath。
- **理由**：替代原 `CoreMarker` 角色；空模块在 CI 中易被误判为未接线。
- **考虑的替代方案**：无占位类（无法证明模块依赖边真实存在）。

### 决策 5：清理残留而非迁移 JWT

- **选择**：删除磁盘上所有 JWT/Auth/登录相关文件，按新规格从零创建四模块树。
- **理由**：DP-0/DP-1 已确认废弃鉴权实验代码；迁移会增加范围与测试负担。
- **考虑的替代方案**：保留 `JwtSupport` 于 `common`（与 non-goals 冲突）。

### 决策 6：测试策略

- **选择**：`HelloControllerTest` 使用 `@WebMvcTest(HelloController.class)` + `@MockBean HelloService`；`HelloServiceTest` 为纯单元测试；`ScaffoldContractTest` 在 `controller` 模块断言 classpath 契约；`DaoConfigTest` 在 `dao` 模块断言 `BaseMapper` 可加载。
- **理由**：与现有 `app` 测试等价，且不启动完整数据源；无 MySQL 时 `package` 仍成功。
- **考虑的替代方案**：`@SpringBootTest` 全上下文（MySQL 不可达时易失败）。

## 风险与权衡（Risks And Trade-Offs）

- **残留目录与 git 状态混乱** → 缓解：Batch 1 显式删除 `core/`、`app/` 及 orphan 树后再创建新 module。
- **`spring-boot:run` 因 MySQL 失败** → 缓解：规格只强制 `package`；yml 保持与现 `app` 相同占位值。
- **与 `create-springboot-scaffold` 规格漂移** → 缓解：proposal 声明 supersede；实现不修改旧 change 目录。
- **Hello 经 Service 多一层** → 缓解：Service 仅一行返回，成本可忽略，分层示范价值更高。

## 迁移计划

- **步骤**：按 tasks.md 批次删除旧 module → 创建四 module POM 与源码 → 运行 `mvn -pl controller -am package`。
- **回滚**：git revert 本变更提交即可；无数据迁移。

## 待明确问题

- 无。
