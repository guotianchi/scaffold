# 执行合同

## Intent Lock

- **变更名称**：`split-layered-modules`
- **要解决的问题**：将脚手架从 `core`+`app` 两模块重构为 `common`/`service`/`dao`/`controller` 四层 Maven 结构，对齐 Controller/Service/DAO 分层边界，并清理磁盘 JWT 残留。
- **范围内**：在 `main` 分支删除 `core`/`app` 及 orphan 残留；新建四 module 与严格依赖链 `controller→service→dao→common`；迁移 `GET /hello`（经 `HelloService`）与 MySQL yml 占位；MyBatis-Plus 在 `dao`；`@MapperScan` 在 `controller` 启动类；JDK 17、Boot 2.7.18、`ai.openbuy` 坐标；无 MySQL 时 `mvn -pl controller -am package` 成功。
- **范围外**：JWT/Auth/登录页；真实 Mapper/Entity/SQL；JPA；Spring Security；新建 git 分支；Nacos/Redis/MQ；Boot 3；修改 `changes/create-springboot-scaffold/` 历史工件；强制本机 MySQL 可达才能编译。

## Scope Fence

禁止在实现中加入：JWT、`AuthController`、`JwtAuthFilter`、`login.html`；JPA/Hibernate；鉴权与 Spring Security；把 `@SpringBootApplication` 放在非 `controller` 模块；`controller` 跳过 `service` 直连 `dao` 的业务调用；恢复 `core`/`app` 模块；新建 git 分支或 worktree isolate（除非用户后续显式变更 DP-0）。

## Approved Behavior

- **已批准需求摘要**：
  1. 根 POM 仅聚合 `common`、`service`、`dao`、`controller`
  2. 依赖链 `controller→service→dao→common`，无反向/跳层 compile 依赖
  3. `common` 含 `CommonMarker`，无 Boot/Service/Controller/Mapper
  4. `dao` 含 MyBatis-Plus、`ai.openbuy.dao.mapper` 占位，无 web starter
  5. `service` 含 `@Service` 的 `HelloService#hello()` 返回 `"hello"`
  6. `controller` 为唯一 Boot 入口，`GET /hello` 委托 `HelloService`，含 `@MapperScan("ai.openbuy.dao.mapper")`
  7. classpath 有 MyBatis-Plus，无 JPA
  8. `application.yml` 含 MySQL `url`/`username`/`password` 占位
  9. 无 MySQL 时 `mvn -pl controller -am package` 退出码 0
  10. 四模块树中无 JWT/Auth/login 残留
  11. `core/`、`app/` 目录不存在
- **关键场景**：检查根 POM 模块与依赖边；`HelloServiceTest`；`DaoConfigTest`；`HelloControllerTest`（MockBean）；`ScaffoldContractTest`（CommonMarker、Plus、无 JPA、yml 键）。
- **验收检查**：`mvn -pl controller -am package` 通过；全模块测试绿色。

## Design Constraints

- **架构约束**：唯一 `@SpringBootApplication` 在 `controller`；HTTP 在 `controller`，业务在 `service`，持久化类型在 `dao`，共享类型在 `common`。
- **接口约束**：对外 HTTP 仅承诺 `GET /hello` 返回 `"hello"`；数据源 yml 为占位，不构成持久化 API。
- **依赖约束**：`spring-boot-starter-parent` 2.7.18；`mybatis-plus-boot-starter` 3.5.5；`mysql-connector-j` runtime；`service` 依赖 `spring-boot-starter`（无 web）；禁止 `spring-boot-starter-data-jpa`。
- **数据约束**：yml 占位与现 `app` 等价（`jdbc:mysql://127.0.0.1:3306/openbuy`，`root`/`root`）；`HelloControllerTest` 用 `@WebMvcTest` + `@MockBean HelloService`，不加载完整 DataSource。

## Task Batches

### Batch 1

- **目标**：删除 `core`/`app`/orphan 残留，更新根 POM 四模块
- **输入**：现 `core+app` 结构与 orphan 目录
- **输出**：根 POM 声明 `common`/`service`/`dao`/`controller`；旧目录已删
- **完成标准**：grep 四 module；`core/`、`app/` 不存在；无 JWT 残留文件

### Batch 2

- **目标**：`common` 模块与 `CommonMarker`
- **输入**：Batch 1 根 POM
- **输出**：`ai.openbuy:common:0.0.1-SNAPSHOT` jar
- **完成标准**：`mvn -pl common -am package` 成功

### Batch 3

- **目标**：`dao` 模块、MyBatis-Plus、`MapperPackage`、`DaoConfigTest`
- **输入**：`common` 构件
- **输出**：`ai.openbuy:dao:0.0.1-SNAPSHOT`；`BaseMapper` classpath 测试通过
- **完成标准**：`mvn -pl dao -am test -Dtest=DaoConfigTest` 成功

### Batch 4

- **目标**：`service` 模块与 `HelloService`
- **输入**：`dao` 构件
- **输出**：`HelloService#hello()` → `"hello"`
- **完成标准**：`mvn -pl service -am test -Dtest=HelloServiceTest` 成功

### Batch 5

- **目标**：`controller` Boot、Web、`/hello`、yml、`ScaffoldContractTest`
- **输入**：`service` 构件
- **输出**：可执行 JAR；全量测试通过
- **完成标准**：`mvn -pl controller -am package` 成功

## Test Obligations

- **必须先从失败测试开始的行为**：各 batch 按 tasks.md TDD 五步；`HelloService#hello()`；`GET /hello`；`BaseMapper` classpath；无 JPA；无 Auth 文件。
- **必需的边界情况**：无 MySQL 仍能 `package`；仅一个 `@SpringBootApplication`；orphan JWT 目录已清。
- **回归敏感区域**：模块列表与依赖链、Boot/Java 版本、`ai.openbuy` 坐标、禁止 JPA/Auth。

## Execution Mode

- **模式**：`Batch Inline`（DP-4 最终确认）；**在当前 `main` 分支直接修改**，不使用 git worktree isolate。
- **选择理由**：5 个顺序批次、删旧建新、有明确测试义务；用户 DP-0 约束不新建分支。

## Verification Dimensions

| 维度 | 状态 | 发现 |
|------|------|------|
| Completeness | Pending | 规格 11 条 SHALL/MUST 均映射 Batch 1–5 |
| Correctness | Pending | — |
| Coherence | Pending | POM 模块、design 决策、tasks 文件列表一致 |

**总体结论**：Pending（待实现后由 release-archivist 填）

## Review Gates

- **强制审查点**：每批结束后对照本契约与对应需求；Batch 5 后全量 `mvn -pl controller -am package`。
- **阻塞类别**：引入 JWT/JPA/Auth；恢复 `core`/`app`；破坏依赖链；`package` 依赖真实 MySQL；在非 `main` 分支 isolate 实施（违反 DP-0）。

## Escalation Rules

- **何时回退到 `specifying`**：增加第五模块、引入鉴权、改 Boot 大版本、新增业务 API 超出 hello。
- **何时回退到 `bridging`**：批次目标或测试义务与本契约不一致。
- **何时不得继续实现**：DP-3 未批准；契约与 proposal/specs 漂移；测试失败未先走 bug-investigator。

## Unmapped Requirements

- 无。规格 11 条均落入 Batch 1–5。
