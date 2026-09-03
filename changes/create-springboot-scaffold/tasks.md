# 实现任务

## 文件结构

- `Create: pom.xml` — 根聚合 POM：Java 17、Spring Boot 2.7.18、模块 `core`/`app`、`groupId` `ai.openbuy`
- `Create: .gitignore` — 忽略 `target/`、IDE 与 `.DS_Store`
- `Create: core/pom.xml` — `core` 库模块，packaging `jar`，无 spring-boot 插件
- `Create: core/src/main/java/ai/openbuy/core/CoreMarker.java` — 证明 `core` 可被依赖的公开占位类型
- `Create: app/pom.xml` — `app` 可启动模块：依赖 `core`、web、MyBatis-Plus、MySQL 驱动、spring-boot 插件
- `Create: app/src/main/java/ai/openbuy/app/AppApplication.java` — Spring Boot 启动类
- `Create: app/src/main/java/ai/openbuy/app/web/HelloController.java` — `GET /hello`
- `Create: app/src/main/resources/application.yml` — MySQL 数据源占位
- `Create: app/src/test/java/ai/openbuy/app/web/HelloControllerTest.java` — WebMvc 切片测试 `/hello`
- `Create: app/src/test/java/ai/openbuy/app/ScaffoldContractTest.java` — 断言无 JPA、存在 MyBatis-Plus、存在 `CoreMarker`

## 接口

### Batch 1 → Batch 2
- **Produces**: 根坐标 `ai.openbuy:springboot-scaffold:0.0.1-SNAPSHOT`，`java.version=17`，Spring Boot `2.7.18`，模块列表 `core`、`app`
- **Consumes**: 无

### Batch 2 → Batch 3
- **Produces**: Maven 构件 `ai.openbuy:core:0.0.1-SNAPSHOT`；类型 `ai.openbuy.core.CoreMarker`
- **Consumes**: Batch 1 根 POM 与模块声明

### Batch 3 → Batch 4
- **Produces**: 可编译的 `app` 模块骨架与 `GET /hello` 行为
- **Consumes**: `ai.openbuy:core` 与 Spring Boot Web

### Batch 4
- **Produces**: MyBatis-Plus 依赖、MySQL 驱动、`application.yml` 占位键；`ScaffoldContractTest` 锁定无 JPA
- **Consumes**: Batch 3 的 `app` 源码树

## 1. Batch 1: 根聚合 POM

Depends on: none

- [ ] **1.1 编写失败的测试**

```bash
test -f pom.xml && grep -q '<module>core</module>' pom.xml
```

**Files**: 尚不存在 `pom.xml`

- [ ] **1.2 运行测试并确认失败**

Run: `test -f pom.xml && grep -q '<module>core</module>' pom.xml`
Expected: FAIL with exit code 1（文件不存在）

- [ ] **1.3 实现最小化代码**

根 `pom.xml`：`groupId` `ai.openbuy`，`artifactId` `springboot-scaffold`，`version` `0.0.1-SNAPSHOT`，`packaging` `pom`，`modules` 含 `core` 与 `app`，`parent` 为 `spring-boot-starter-parent` `2.7.18`，`java.version` `17`。同时创建 `.gitignore` 忽略 `target/`、`.idea/`、`*.iml`、`.DS_Store`。

**Files**: `Create: pom.xml`，`Create: .gitignore`
**Interfaces**: Produces parent GAV `ai.openbuy:springboot-scaffold:0.0.1-SNAPSHOT`

- [ ] **1.4 运行测试并确认通过**

Run: `grep -q '<packaging>pom</packaging>' pom.xml && grep -q '<module>core</module>' pom.xml && grep -q '<module>app</module>' pom.xml && grep -q '<java.version>17</java.version>' pom.xml && grep -q '2.7.18' pom.xml`
Expected: PASS（exit 0）

- [ ] **1.5 提交**

```bash
git add pom.xml .gitignore
git commit -m "chore: add Maven parent aggregator for core and app"
```

（仅在用户明确要求提交时执行。）

## 2. Batch 2: core 库模块

Depends on: Batch 1

- [ ] **2.1 编写失败的测试**

```bash
mvn -pl core -am -q validate
```

**Files**: `core/pom.xml` 尚不存在

- [ ] **2.2 运行测试并确认失败**

Run: `mvn -pl core -am -q validate`
Expected: FAIL with "Child module core of ... does not exist" 或无法解析 `core`

- [ ] **2.3 实现最小化代码**

`core/pom.xml` 继承根 POM，`artifactId` `core`，`packaging` `jar`。添加 `core/src/main/java/ai/openbuy/core/CoreMarker.java`：`public final class CoreMarker { private CoreMarker() {} }`。

**Files**: `Create: core/pom.xml`，`Create: core/src/main/java/ai/openbuy/core/CoreMarker.java`
**Interfaces**: Produces `ai.openbuy.core.CoreMarker`

- [ ] **2.4 运行测试并确认通过**

Run: `mvn -pl core -am -q package`
Expected: PASS，生成 `core/target/core-0.0.1-SNAPSHOT.jar`

- [ ] **2.5 提交**

```bash
git add core
git commit -m "feat: add core library module"
```

（仅在用户明确要求提交时执行。）

## 3. Batch 3: app Web 与 /hello

Depends on: Batch 2

- [ ] **3.1 编写失败的测试**

```java
@WebMvcTest(HelloController.class)
class HelloControllerTest {
  @Autowired MockMvc mockMvc;
  @Test
  void helloReturnsOk() throws Exception {
    mockMvc.perform(get("/hello")).andExpect(status().isOk())
      .andExpect(content().string(not(blankOrNullString())));
  }
}
```

**Files**: `Create: app/src/test/java/ai/openbuy/app/web/HelloControllerTest.java`

- [ ] **3.2 运行测试并确认失败**

Run: `mvn -pl app -am -q -Dtest=HelloControllerTest test`
Expected: FAIL（无 `HelloController` 或模块无法编译）

- [ ] **3.3 实现最小化代码**

`app/pom.xml`：依赖 `ai.openbuy:core`、`spring-boot-starter-web`、`spring-boot-starter-test`（test），`spring-boot-maven-plugin`。`AppApplication` 带 `@SpringBootApplication`。`HelloController` 映射 `GET /hello` 返回 `"hello"`。

**Files**: `Create: app/pom.xml`，`Create: app/src/main/java/ai/openbuy/app/AppApplication.java`，`Create: app/src/main/java/ai/openbuy/app/web/HelloController.java`
**Interfaces**: Produces `GET /hello` → `200` + 非空 body；Consumes `ai.openbuy:core`

- [ ] **3.4 运行测试并确认通过**

Run: `mvn -pl app -am -q -Dtest=HelloControllerTest test`
Expected: PASS

- [ ] **3.5 提交**

```bash
git add app
git commit -m "feat: add bootable app module with GET /hello"
```

（仅在用户明确要求提交时执行。）

## 4. Batch 4: MyBatis-Plus 与 MySQL 占位

Depends on: Batch 3

- [ ] **4.1 编写失败的测试**

```java
class ScaffoldContractTest {
  @Test
  void coreMarkerIsOnClasspath() {
    assertNotNull(ai.openbuy.core.CoreMarker.class);
  }
  @Test
  void mybatisPlusIsOnClasspath() {
    assertDoesNotThrow(() -> Class.forName("com.baomidou.mybatisplus.core.mapper.BaseMapper"));
  }
  @Test
  void jpaStarterIsNotOnClasspath() {
    assertThrows(ClassNotFoundException.class,
      () -> Class.forName("org.springframework.data.jpa.repository.JpaRepository"));
  }
}
```

**Files**: `Create: app/src/test/java/ai/openbuy/app/ScaffoldContractTest.java`

- [ ] **4.2 运行测试并确认失败**

Run: `mvn -pl app -am -q -Dtest=ScaffoldContractTest test`
Expected: FAIL on `mybatisPlusIsOnClasspath`

- [ ] **4.3 实现最小化代码**

在 parent 或 `app` POM 增加 `mybatis-plus-boot-starter` `3.5.5` 与 `mysql-connector-java`（runtime）。`app/src/main/resources/application.yml` 设置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/openbuy?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
```

不添加 `spring-boot-starter-data-jpa`。

**Files**: `Modify: app/pom.xml`，`Create: app/src/main/resources/application.yml`
**Interfaces**: Produces classpath MyBatis-Plus + yml 三键占位

- [ ] **4.4 运行测试并确认通过**

Run: `mvn -pl app -am -q -Dtest=ScaffoldContractTest,HelloControllerTest package`
Expected: PASS，exit 0，即使本机无 MySQL

- [ ] **4.5 提交**

```bash
git add app/pom.xml app/src/main/resources/application.yml app/src/test/java/ai/openbuy/app/ScaffoldContractTest.java
git commit -m "feat: add MyBatis-Plus and MySQL datasource placeholders"
```

（仅在用户明确要求提交时执行。）
