# 变更提案

## 背景（Why）

现有脚手架采用 `core` + `app` 两模块结构，职责边界模糊：`core` 仅为占位库，`app` 同时承担 Web 入口、配置与持久化依赖。后续业务将按 Controller / Service / DAO 分层扩展，需要先把 Maven 模块结构与包边界对齐，避免在错误分层上继续堆代码。

现在就要做，是因为磁盘上已存在未纳入 parent POM 的残留目录（含 JWT/Auth 等已废弃代码），与正式 `core`/`app` 结构并存，继续开发会产生漂移与合并风险。

## 变更内容（What Changes）

- 删除 Maven 模块 `core`、`app` 及其源码与测试。
- 新建四模块结构：`common`（公共代码）、`service`（`@Service` 业务层）、`dao`（MyBatis-Plus + MySQL 数据访问）、`controller`（`@Controller`/`@RestController` + Spring Boot 启动）。
- 依赖链：`controller → service → dao → common`（严格单向）。
- 将 `GET /hello` 与 MySQL 数据源占位配置迁移至新结构；`HelloController` 调用 `HelloService`。
- 清理磁盘上未纳入 POM 的残留 `common/`、`service/`、`dao/`、`controller/` 源码（含 JWT/登录），按规格重建。
- 在 `main` 分支直接实施，不新建 git 分支。

## 能力（Capabilities）

### 新增能力

- `layered-modules`

### 修改能力

- `multi-module-scaffold`（由 `core+app` 替换为四层模块）

### 移除能力

- 无（`multi-module-scaffold` 被新结构 supersede，非独立运行时能力）

## 范围（Scope）

### 范围内（In Scope）

- 根 `pom.xml` 模块列表与 `dependencyManagement` 更新为四个新 module
- 各 module 的 `pom.xml`、包名 `ai.openbuy.*`、占位类型（`CommonMarker`、`MapperPackage` 等）
- `controller` 启动类、`GET /hello`、`application.yml` MySQL 占位
- `service` 层 `HelloService` 及单元/切片测试
- `dao` 层 MyBatis-Plus 依赖与 `@MapperScan` 包路径预留
- 删除 `core/`、`app/` 目录；删除残留 JWT/Auth/登录页代码
- `mvn -pl controller -am package` 在无真实 MySQL 时成功

### 范围外（Out of Scope）

- JWT、登录页、鉴权 Filter、`AuthService` 等业务
- 真实 Mapper、Entity、SQL 与数据库表
- JPA / Spring Data
- 新建 git 分支或 worktree isolate
- 修改 `changes/create-springboot-scaffold/` 内历史工件（本变更 supersede 其目标结构，标记为废弃即可）

## 影响（Impact）

- **构建**：Maven 模块名与 `-pl` 目标由 `app` 改为 `controller`
- **测试**：`ScaffoldContractTest` 断言目标由 `CoreMarker` 改为 `CommonMarker`；Hello 测试需 mock `HelloService`
- **历史变更**：`changes/create-springboot-scaffold` 描述的 `core+app` 结构不再适用，以本变更为准
- **运行**：`spring-boot:run` 仍可能因 MySQL 不可达失败；规格只强制 `package` 与切片测试通过
