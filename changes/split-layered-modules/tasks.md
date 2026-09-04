# 实现任务

## 文件结构

- `Modify: pom.xml` — 根聚合 POM：模块改为 `common`/`service`/`dao`/`controller`，更新 `dependencyManagement`
- `Delete: core/` — 移除旧 `core` 模块整目录
- `Delete: app/` — 移除旧 `app` 模块整目录
- `Delete: common/`、`service/`、`dao/`、`controller/`（若存在且无合法 POM）— 清理 JWT 等残留后重建
- `Create: common/pom.xml` — 公共库 module，无 Spring 依赖
- `Create: common/src/main/java/ai/openbuy/common/CommonMarker.java` — 公共层占位类型
- `Create: dao/pom.xml` — 数据访问 module：MyBatis-Plus、MySQL 驱动 runtime、依赖 `common`
- `Create: dao/src/main/java/ai/openbuy/dao/mapper/MapperPackage.java` — Mapper 包占位
- `Create: dao/src/test/java/ai/openbuy/dao/DaoConfigTest.java` — 断言 `BaseMapper` 在 classpath
- `Create: service/pom.xml` — 业务层 module：依赖 `dao`，含 `spring-boot-starter`
- `Create: service/src/main/java/ai/openbuy/service/HelloService.java` — `@Service`，返回 `"hello"`
- `Create: service/src/test/java/ai/openbuy/service/HelloServiceTest.java` — 单元测试 `HelloService#hello()`
- `Create: controller/pom.xml` — Web + Boot module：依赖 `service`，`spring-boot-maven-plugin`
- `Create: controller/src/main/java/ai/openbuy/controller/ControllerApplication.java` — `@SpringBootApplication` + `@MapperScan`
- `Create: controller/src/main/java/ai/openbuy/controller/web/HelloController.java` — `GET /hello` 委托 `HelloService`
- `Create: controller/src/main/resources/application.yml` — MySQL 数据源占位（与现 `app` 等价）
- `Create: controller/src/test/java/ai/openbuy/controller/web/HelloControllerTest.java` — `@WebMvcTest` + `@MockBean HelloService`
- `Create: controller/src/test/java/ai/openbuy/controller/ScaffoldContractTest.java` — 断言 `CommonMarker`、MyBatis-Plus、无 JPA、yml 占位键

## 接口

### Batch 1 → Batch 2
- **Produces**: 根 POM 四模块声明；`core/`、`app/` 及 orphan 残留已删除
- **Consumes**: 无

### Batch 2 → Batch 3
- **Produces**: `ai.openbuy:common:0.0.1-SNAPSHOT`；`ai.openbuy.common.CommonMarker`
- **Consumes**: Batch 1 根 POM

### Batch 3 → Batch 4
- **Produces**: `ai.openbuy:dao:0.0.1-SNAPSHOT`；包 `ai.openbuy.dao.mapper`
- **Consumes**: `ai.openbuy:common`

### Batch 4 → Batch 5
- **Produces**: `ai.openbuy:service:0.0.1-SNAPSHOT`；`HelloService#hello()` → `"hello"`
- **Consumes**: `ai.openbuy:dao`

### Batch 5
- **Produces**: 可启动 `controller` JAR；`GET /hello`；MySQL 占位 yml；全量测试通过
- **Consumes**: `ai.openbuy:service`

## 1. Batch 1: 清理旧结构并更新根 POM

Depends on: none

- [ ] **1.1 编写失败的测试**

```bash
grep -q '<module>controller</module>' pom.xml && ! test -d core && ! test -d app
```

**Files**: 根 `pom.xml` 仍为 `core`/`app`；`core/`、`app/` 仍存在

- [ ] **1.2 运行测试并确认失败**

Run: `grep -q '<module>controller</module>' pom.xml && ! test -d core && ! test -d app`
Expected: FAIL（模块列表未更新或旧目录仍在）

- [ ] **1.3 实现最小化代码**

删除 `core/`、`app/` 整目录。删除磁盘上无合法 Maven 结构的 orphan `common/`、`service/`、`dao/`、`controller/`（含 JWT/Auth/login 残留）。更新根 `pom.xml`：`<modules>` 为 `common`、`service`、`dao`、`controller`；`dependencyManagement` 声明四个 module 的 `ai.openbuy` 坐标；保留 `java.version=17`、`mybatis-plus.version=3.5.5`、Spring Boot parent `2.7.18`；移除 `core` 条目。

**Files**: `Modify: pom.xml`；`Delete: core/`，`Delete: app/`，`Delete:` orphan 四目录
**Interfaces**: Produces 四模块 parent 声明

- [ ] **1.4 运行测试并确认通过**

Run: `grep -q '<module>common</module>' pom.xml && grep -q '<module>service</module>' pom.xml && grep -q '<module>dao</module>' pom.xml && grep -q '<module>controller</module>' pom.xml && ! test -d core && ! test -d app`
Expected: PASS

- [ ] **1.5 提交**

（仅在用户明确要求提交时执行。）

## 2. Batch 2: common 模块

Depends on: Batch 1

- [ ] **2.1 编写失败的测试**

```bash
mvn -pl common -am -q validate
```

**Files**: `common/pom.xml` 尚不存在

- [ ] **2.2 运行测试并确认失败**

Run: `mvn -pl common -am -q validate`
Expected: FAIL（module 不存在）

- [ ] **2.3 实现最小化代码**

`common/pom.xml`：`artifactId` `common`，`packaging` `jar`，无 Spring 依赖。`CommonMarker.java`：`public final class CommonMarker { private CommonMarker() {} }`。

**Files**: `Create: common/pom.xml`，`Create: common/src/main/java/ai/openbuy/common/CommonMarker.java`
**Interfaces**: Produces `ai.openbuy.common.CommonMarker`

- [ ] **2.4 运行测试并确认通过**

Run: `mvn -pl common -am -q package`
Expected: PASS，生成 `common/target/common-0.0.1-SNAPSHOT.jar`

- [ ] **2.5 提交**

（仅在用户明确要求提交时执行。）

## 3. Batch 3: dao 模块

Depends on: Batch 2

- [ ] **3.1 编写失败的测试**

```java
@Test
void mybatisPlusIsOnClasspath() {
    assertDoesNotThrow(() -> Class.forName("com.baomidou.mybatisplus.core.mapper.BaseMapper"));
}
```

**Files**: `Create: dao/src/test/java/ai/openbuy/dao/DaoConfigTest.java`（先写测试）

- [ ] **3.2 运行测试并确认失败**

Run: `mvn -pl dao -am -q test -Dtest=DaoConfigTest`
Expected: FAIL（module 或类不存在）

- [ ] **3.3 实现最小化代码**

`dao/pom.xml`：依赖 `common`、`mybatis-plus-boot-starter`、`mysql-connector-j`（runtime）；无 `spring-boot-starter-web`。`MapperPackage.java` 于 `ai.openbuy.dao.mapper`。

**Files**: `Create: dao/pom.xml`，`Create: dao/src/main/java/ai/openbuy/dao/mapper/MapperPackage.java`，`Create: dao/src/test/java/ai/openbuy/dao/DaoConfigTest.java`
**Interfaces**: Produces `ai.openbuy.dao.mapper` 包边界

- [ ] **3.4 运行测试并确认通过**

Run: `mvn -pl dao -am -q test -Dtest=DaoConfigTest`
Expected: PASS

- [ ] **3.5 提交**

（仅在用户明确要求提交时执行。）

## 4. Batch 4: service 模块

Depends on: Batch 3

- [ ] **4.1 编写失败的测试**

```java
@Test
void helloReturnsHello() {
    assertEquals("hello", new HelloService().hello());
}
```

**Files**: `Create: service/src/test/java/ai/openbuy/service/HelloServiceTest.java`

- [ ] **4.2 运行测试并确认失败**

Run: `mvn -pl service -am -q test -Dtest=HelloServiceTest`
Expected: FAIL

- [ ] **4.3 实现最小化代码**

`service/pom.xml`：依赖 `dao`、`spring-boot-starter`（无 web）。`HelloService.java`：`@Service`，`public String hello() { return "hello"; }`。

**Files**: `Create: service/pom.xml`，`Create: service/src/main/java/ai/openbuy/service/HelloService.java`，`Create: service/src/test/java/ai/openbuy/service/HelloServiceTest.java`
**Interfaces**: Produces `HelloService#hello()` → `"hello"`

- [ ] **4.4 运行测试并确认通过**

Run: `mvn -pl service -am -q test -Dtest=HelloServiceTest`
Expected: PASS

- [ ] **4.5 提交**

（仅在用户明确要求提交时执行。）

## 5. Batch 5: controller 模块与契约测试

Depends on: Batch 4

- [ ] **5.1 编写失败的测试**

```java
@WebMvcTest(HelloController.class)
class HelloControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean HelloService helloService;
    @Test
    void helloReturnsOk() throws Exception {
        when(helloService.hello()).thenReturn("hello");
        mockMvc.perform(get("/hello")).andExpect(status().isOk()).andExpect(content().string("hello"));
    }
}
```

**Files**: `Create: controller/src/test/java/ai/openbuy/controller/web/HelloControllerTest.java`

- [ ] **5.2 运行测试并确认失败**

Run: `mvn -pl controller -am -q test -Dtest=HelloControllerTest`
Expected: FAIL

- [ ] **5.3 实现最小化代码**

`controller/pom.xml`：依赖 `service`、`spring-boot-starter-web`、`spring-boot-starter-test`（test）；`spring-boot-maven-plugin`。`ControllerApplication.java`：`@SpringBootApplication`，`@MapperScan("ai.openbuy.dao.mapper")`。`HelloController.java`：注入 `HelloService`，`GET /hello` 返回 `helloService.hello()`。`application.yml`：复制现 `app` 的 MySQL 占位。`ScaffoldContractTest.java`：断言 `CommonMarker`、MyBatis-Plus classpath、无 JPA、yml 含 datasource 键。

**Files**: `Create: controller/pom.xml`，`Create: controller/src/main/java/ai/openbuy/controller/ControllerApplication.java`，`Create: controller/src/main/java/ai/openbuy/controller/web/HelloController.java`，`Create: controller/src/main/resources/application.yml`，`Create: controller/src/test/java/ai/openbuy/controller/web/HelloControllerTest.java`，`Create: controller/src/test/java/ai/openbuy/controller/ScaffoldContractTest.java`
**Interfaces**: Produces 可执行 JAR 与 HTTP `/hello`

- [ ] **5.4 运行测试并确认通过**

Run: `mvn -pl controller -am -q package`
Expected: PASS；`controller/target/*.jar` 存在；全部测试绿色

- [ ] **5.5 提交**

（仅在用户明确要求提交时执行。）

## 需求映射

| 规格 Requirement | 任务 |
|---|---|
| Parent Aggregates Four Layered Modules | Batch 1 |
| Strict Module Dependency Chain | Batch 2–5 POM 依赖 |
| Common Is Shared Library | Batch 2 |
| Dao Holds MyBatis Plus | Batch 3 |
| Service Holds Business Layer | Batch 4 |
| Controller Boots Web And Delegates To Service | Batch 5 |
| MyBatis Plus Without Jpa | Batch 3 + 5 ScaffoldContractTest |
| Mysql Placeholder Config | Batch 5 application.yml |
| Package Without Live Mysql | Batch 5.4 |
| No Jwt Or Auth Residuals | Batch 1 删除 orphan |
| Multi Module Scaffold Structure (MODIFIED) | Batch 1 |
