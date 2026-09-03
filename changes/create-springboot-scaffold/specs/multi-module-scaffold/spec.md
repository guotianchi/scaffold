# 多模块脚手架规格

## Purpose

This spec defines the net-new Maven multi-module Spring Boot scaffold: parent aggregation, `core` library module, `app` bootable web module, JDK 17, Spring Boot 2.7.18, `ai.openbuy` coordinates, a `GET /hello` probe, MyBatis-Plus, and MySQL datasource placeholders without JPA.

## ADDED Requirements

### Requirement: Parent Aggregates Core And App

The repository SHALL be a Maven parent/aggregator whose `pom.xml` lists `core` and `app` as modules, and whose packaging is `pom`.

#### Scenario: Root pom lists both modules

- **WHEN** a developer inspects the root `pom.xml`
- **THEN** the file declares `packaging` `pom` and includes `<module>core</module>` and `<module>app</module>`

### Requirement: Java 17 And Spring Boot 2.7.18

The parent POM SHALL set `java.version` to `17` and import Spring Boot dependency management at version `2.7.18`.

#### Scenario: Compiler and BOM versions are pinned

- **WHEN** a developer inspects the parent POM properties and `dependencyManagement`
- **THEN** `java.version` is `17` and Spring Boot BOM version is `2.7.18`

### Requirement: Openbuy Coordinates

Root, `core`, and `app` artifacts SHALL use `groupId` `ai.openbuy`, and Java sources SHALL live under package `ai.openbuy`.

#### Scenario: Group and packages match

- **WHEN** a developer inspects module POMs and Java source directories
- **THEN** every module `groupId` is `ai.openbuy` and application/library classes are under `ai.openbuy`

### Requirement: Core Is A Library Module

The `core` module SHALL compile as a non-bootable library that `app` depends on, and MUST NOT contain a Spring Boot `main` method.

#### Scenario: App depends on core

- **WHEN** `app/pom.xml` is inspected
- **THEN** it declares a compile dependency on `ai.openbuy:core`

#### Scenario: Core has no boot main

- **WHEN** `core/src` is searched for a Spring Boot application class
- **THEN** no class annotated with `@SpringBootApplication` exists in `core`

### Requirement: App Boots Web Hello

The `app` module SHALL provide a `@SpringBootApplication` entrypoint, depend on `spring-boot-starter-web`, and expose `GET /hello` that returns HTTP 200 with a non-empty body.

#### Scenario: Hello endpoint responds

- **WHEN** the app is running and a client sends `GET /hello`
- **THEN** the response status is 200 and the body is non-empty

### Requirement: MyBatis Plus Without Jpa

The `app` module SHALL depend on MyBatis-Plus Spring Boot starter and MUST NOT depend on `spring-boot-starter-data-jpa` or `hibernate-core`.

#### Scenario: Persistence stack is MyBatis-Plus only

- **WHEN** `app/pom.xml` dependencies are inspected
- **THEN** a MyBatis-Plus starter is present and no JPA/Hibernate starter is present

### Requirement: Mysql Placeholder Config

The `app` module SHALL ship `application.yml` (or `application.yaml`) containing MySQL datasource placeholders for URL, username, and password.

#### Scenario: Datasource keys exist

- **WHEN** a developer opens `app/src/main/resources/application.yml`
- **THEN** the file contains `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password` values intended for MySQL

### Requirement: Package Without Live Mysql

`mvn -pl app -am package` SHALL succeed on a machine that has JDK 17 and Maven, even when no MySQL server is reachable.

#### Scenario: Offline compile package

- **WHEN** a developer runs `mvn -pl app -am package` without a running MySQL instance
- **THEN** the build exits 0 and produces `app/target/*.jar`

## MODIFIED Requirements

None.

## REMOVED Requirements

None.
