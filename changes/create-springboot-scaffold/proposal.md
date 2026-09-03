# 变更提案

## 背景（Why）

需要一份从空仓库开始的 Spring Boot 多模块脚手架，而不是在已有 springbootdemo 上继续堆功能。团队要用 Maven parent 聚合多个 module、固定 JDK 17 与 Spring Boot 2.7 线，并预留 MyBatis-Plus 访问 MySQL 的配置占位，以便后续业务模块按同一结构扩展。

现在就要做，是因为后续业务代码不应再从单模块工程拆分，脚手架本身必须先成为可编译、可启动的基线。

## 变更内容（What Changes）

- 新建 Maven 多模块工程：根工程 `springboot-scaffold` 作为 parent/aggregator，包含 `core` 与 `app` 两个 module。
- 锁定 Java 17 与 Spring Boot 2.7.18 依赖管理；坐标与包名为 `ai.openbuy`。
- `app` 提供可启动 Web 应用：`spring-boot-starter-web`、启动类、以及 `GET /hello` 探活接口。
- `app` 引入 MyBatis-Plus，并在 `application.yml` 中给出 MySQL 数据源占位配置；不引入 JPA。
- `core` 作为被 `app` 依赖的基础库 module，不包含独立启动类。

## 能力（Capabilities）

### 新增能力

- `multi-module-scaffold`

### 修改能力

- 无（空仓库，无既有能力）

## 范围（Scope）

### 范围内（In Scope）

- 根 `pom.xml` 聚合与依赖管理（Spring Boot BOM、Java 17、模块列表）
- `core` 模块骨架（可被 `app` 编译依赖）
- `app` 模块：启动类、Web、`GET /hello`、MyBatis-Plus、MySQL 占位配置
- 根 `.gitignore`，使 `mvn -pl app -am package` 可在无真实 MySQL 的情况下完成编译打包

### 范围外（Out of Scope）

- JPA / Spring Data
- 统一返回体、全局异常处理、鉴权与登录
- Nacos / 配置中心、Redis、消息队列
- Gradle、Spring Boot 3.x
- 真实业务 CRUD、自动建库、强制本机已安装并可连接 MySQL 才能编译
- 从 `Documents/project/springbootdemo` 迁移代码

## 影响（Impact）

- 影响的代码区域：全新仓库根目录、`core/`、`app/`，尚无既有业务代码
- 影响的 API 或接口：新增 `GET /hello`；数据源仅为配置占位，不对外提供持久化 API
- 依赖或涉及的外部系统：编译期依赖 Maven Central；运行期若启用数据源则依赖外部 MySQL（本变更不要求连通）
