# 执行合同

## Intent Lock

- **变更名称**：`create-springboot-scaffold`
- **要解决的问题**：在空仓库建立 Maven 多模块 Spring Boot 脚手架，作为后续 OpenBuy 服务的基线，而不是改造 `springbootdemo`。
- **范围内**：根聚合 POM；`core` 库模块；`app` 可启动模块（Web、`GET /hello`、MyBatis-Plus、MySQL yml 占位）；JDK 17；Spring Boot 2.7.18；`groupId`/包名 `ai.openbuy`；无真实 MySQL 时 `mvn -pl app -am package` 成功。
- **范围外**：JPA/Hibernate；统一返回体与全局异常；鉴权；配置中心/Redis/MQ；Gradle；Spring Boot 3；业务 CRUD 与自动建库；从 springbootdemo 迁代码；强制本机 MySQL 可达才能编译。

## Scope Fence

禁止在实现中加入：JPA、鉴权、Nacos、示例业务表/Mapper SQL、把 `core` 做成可启动应用、升级到 Boot 3、改 groupId 离开 `ai.openbuy`。

## Approved Behavior

- **已批准需求摘要**：
  1. Parent 为 `pom` 并聚合 `core`、`app`
  2. `java.version=17` 且 Spring Boot `2.7.18`
  3. 坐标与包名 `ai.openbuy`
  4. `core` 无 `@SpringBootApplication`，`app` compile 依赖 `core`
  5. `app` 启动类 + Web + `GET /hello` → 200 且 body 非空
  6. 存在 MyBatis-Plus starter，不存在 JPA starter
  7. `application.yml` 含 `spring.datasource.url|username|password`（MySQL）
  8. 无 MySQL 进程时 `mvn -pl app -am package` 退出码 0
- **关键场景**：检查根 POM 模块列表；`HelloControllerTest` 调 `/hello`；`ScaffoldContractTest` 断言 Plus 在 classpath、JPA 不在、`CoreMarker` 可加载。
- **验收检查**：`mvn -pl app -am -Dtest=HelloControllerTest,ScaffoldContractTest package` 通过。

## Design Constraints

- **架构约束**：唯一进程入口在 `app`；`core` 仅库；根工程 packaging=`pom`。
- **接口约束**：仅承诺 `GET /hello`；数据源配置不构成对外持久化 API。
- **依赖约束**：`spring-boot-starter-parent` 2.7.18；`mybatis-plus-boot-starter` 3.5.5；`com.mysql:mysql-connector-j`（Boot 2.7.18 BOM）；禁止 `spring-boot-starter-data-jpa`。
- **数据约束**：yml 使用文档式占位（默认 `jdbc:mysql://127.0.0.1:3306/openbuy`，用户/密码 `root`）；测试不得要求真实库连通。`/hello` 使用 `@WebMvcTest`，不加载完整 DataSource 上下文。

## Task Batches

### Batch 1

- **目标**：根 `pom.xml` + `.gitignore`
- **输入**：空仓 + 本契约
- **输出**：聚合 POM，Java 17，Boot 2.7.18，模块 `core`/`app`
- **完成标准**：grep 校验 packaging、modules、java.version、2.7.18

### Batch 2

- **目标**：`core` 模块与 `CoreMarker`
- **输入**：Batch 1 根 POM
- **输出**：`ai.openbuy:core:0.0.1-SNAPSHOT` jar
- **完成标准**：`mvn -pl core -am package` 成功

### Batch 3

- **目标**：`app` Web 与 `/hello`
- **输入**：`core` 构件
- **输出**：启动类、`HelloController`、`HelloControllerTest`
- **完成标准**：`HelloControllerTest` 通过

### Batch 4

- **目标**：MyBatis-Plus、MySQL 驱动、yml 占位、`ScaffoldContractTest`
- **输入**：Batch 3 `app` 树
- **输出**：依赖与配置 + 契约测试
- **完成标准**：`HelloControllerTest,ScaffoldContractTest` 与 `package` 均通过

## Test Obligations

- **必须先从失败测试开始的行为**：`GET /hello`；classpath 上有 MyBatis-Plus、无 JPA、有 `CoreMarker`。
- **必需的边界情况**：无 MySQL 仍能 `package`；`core` 无 Boot 主类。
- **回归敏感区域**：模块列表、Boot/Java 版本、`ai.openbuy` 坐标、禁止 JPA。

## Execution Mode

- **模式**：`Batch Inline`（实现时由 DP-4 最终确认）
- **选择理由**：4 个顺序批次、新模块、有测试义务，适合按批实现与审查，无需完整 SDD 多 agent。

## Verification Dimensions

| 维度 | 状态 | 发现 |
|------|------|------|
| Completeness | Pending | 8 条 SHALL 均映射到 Batch 1–4 |
| Correctness | Pending | — |
| Coherence | Pending | POM 模块与任务文件列表一致 |

**总体结论**：Pending（待实现后由 release-archivist 填）

## Review Gates

- **强制审查点**：每批结束后对照本契约与对应需求；Batch 4 后做全量 `package`。
- **阻塞类别**：加入 JPA/鉴权/Boot 3；`package` 需要真实 MySQL；删除 `core` 或把启动类放入 `core`。

## Escalation Rules

- **何时回退到 `specifying`**：要增加业务 CRUD、H2、改模块切分、改 Spring Boot 大版本。
- **何时回退到 `bridging`**：批次目标或测试义务与本契约不一致。
- **何时不得继续实现**：DP-3 未批准；契约与 proposal/specs 漂移；测试失败未先走 bug-investigator。

## Unmapped Requirements

- 无。规格 8 条均落入 Batch 1–4。
