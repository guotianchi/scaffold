# 分层多模块脚手架规格

## Purpose

This spec defines the Maven multi-module refactor from `core`+`app` to `common`, `service`, `dao`, and `controller` with a strict dependency chain, preserved `GET /hello` behavior, MyBatis-Plus + MySQL placeholders, and no JWT/auth code.

## ADDED Requirements

### Requirement: Parent Aggregates Four Layered Modules

The repository SHALL be a Maven parent whose root `pom.xml` lists `common`, `service`, `dao`, and `controller` as modules with `packaging` `pom`, and MUST NOT list `core` or `app`.

#### Scenario: Root pom lists layered modules only

- **WHEN** a developer inspects the root `pom.xml`
- **THEN** `<modules>` contains `common`, `service`, `dao`, and `controller` only

### Requirement: Strict Module Dependency Chain

Module compile dependencies MUST follow `controller → service → dao → common` with no reverse or skip-layer dependencies (except test-scoped deps).

#### Scenario: Dependency edges match layers

- **WHEN** module POMs are inspected
- **THEN** `controller` depends on `service`, `service` depends on `dao`, `dao` depends on `common`, and `common` depends on no other project module

### Requirement: Common Is Shared Library

The `common` module SHALL compile as a non-bootable jar containing shared types under `ai.openbuy.common` and MUST NOT contain `@SpringBootApplication`, `@Service`, `@RestController`, or MyBatis mapper interfaces.

#### Scenario: Common marker exists

- **WHEN** `common/src/main/java` is inspected
- **THEN** a public type `ai.openbuy.common.CommonMarker` exists

### Requirement: Dao Holds MyBatis Plus

The `dao` module SHALL depend on MyBatis-Plus Spring Boot starter, declare mapper types under `ai.openbuy.dao.mapper`, and MUST NOT depend on `spring-boot-starter-web`.

#### Scenario: Dao exposes mapper package

- **WHEN** `dao/src/main/java` is inspected
- **THEN** package `ai.openbuy.dao.mapper` exists with a public placeholder type proving the module boundary

#### Scenario: No web starter in dao

- **WHEN** `dao/pom.xml` is inspected
- **THEN** `spring-boot-starter-web` is absent

### Requirement: Service Holds Business Layer

The `service` module SHALL contain `@Service` types under `ai.openbuy.service` and MUST depend on `dao` (and transitively `common`).

#### Scenario: Hello service returns greeting

- **WHEN** `HelloService#hello()` is invoked in a unit test
- **THEN** it returns the non-empty string `"hello"`

### Requirement: Controller Boots Web And Delegates To Service

The `controller` module SHALL provide the sole `@SpringBootApplication`, depend on `spring-boot-starter-web`, expose `GET /hello` via a `@RestController` that delegates to `HelloService`, and configure `@MapperScan` for `ai.openbuy.dao.mapper`.

#### Scenario: Hello endpoint responds via service

- **WHEN** a client sends `GET /hello` to the running application
- **THEN** the response status is 200 and the body is the non-empty string `"hello"`

#### Scenario: Only controller has boot main

- **WHEN** all module sources are searched for `@SpringBootApplication`
- **THEN** exactly one class exists and it resides in the `controller` module

### Requirement: MyBatis Plus Without Jpa

The boot classpath (via `controller` module dependencies) SHALL include MyBatis-Plus and MUST NOT include `spring-boot-starter-data-jpa` or `hibernate-core`.

#### Scenario: Persistence stack is MyBatis-Plus only

- **WHEN** `controller/pom.xml` and transitive dependencies are inspected
- **THEN** MyBatis-Plus starter is present and no JPA/Hibernate starter is present

### Requirement: Mysql Placeholder Config

The `controller` module SHALL ship `application.yml` containing MySQL datasource placeholders for URL, username, and password.

#### Scenario: Datasource keys exist

- **WHEN** a developer opens `controller/src/main/resources/application.yml`
- **THEN** the file contains `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password` values intended for MySQL

### Requirement: Package Without Live Mysql

`mvn -pl controller -am package` SHALL succeed on a machine with JDK 17 and Maven, even when no MySQL server is reachable.

#### Scenario: Offline compile package

- **WHEN** a developer runs `mvn -pl controller -am package` without a running MySQL instance
- **THEN** the build exits 0 and produces `controller/target/*.jar`

### Requirement: No Jwt Or Auth Residuals

The repository MUST NOT contain JWT support classes, auth filters, login HTML, or auth REST endpoints under the new module tree after refactor.

#### Scenario: Auth artifacts absent

- **WHEN** a developer searches `common/`, `service/`, `dao/`, and `controller/` for `JwtSupport`, `AuthController`, `JwtAuthFilter`, or `login.html`
- **THEN** no such files exist

## MODIFIED Requirements

### Requirement: Multi Module Scaffold Structure

The scaffold structure defined by `multi-module-scaffold` is MODIFIED from parent + `core` + `app` to parent + `common` + `service` + `dao` + `controller`.

#### Scenario: Legacy modules removed

- **WHEN** the repository root is listed
- **THEN** directories `core/` and `app/` do not exist

## REMOVED Requirements

None (legacy `core`/`app` requirements are superseded via MODIFIED above).
