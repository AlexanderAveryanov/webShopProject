# build-configuration Specification

## Purpose

Defines the shared Maven build configuration convention of the multi-module project (webShopProject): shared versions and plugin configuration are declared once in the parent POM, and child service modules only reference them instead of duplicating them.

## Requirements

### Requirement: Centralized Maven build configuration in the parent POM
The parent POM (webShopProject) SHALL declare the shared project-wide configuration: dependency versions in `dependencyManagement` and reusable plugin definitions (e.g., `spring-boot-maven-plugin`, `maven-compiler-plugin`) with their versions and settings in `pluginManagement`.

#### Scenario: Parent POM declares reusable plugin configuration
- **WHEN** the parent POM defines a shared plugin in `pluginManagement`
- **THEN** the plugin version and its configuration (such as the `repackage` execution and the Java-source/target version) are defined in that single place

#### Scenario: Parent POM manages dependency versions
- **WHEN** a shared dependency or BOM is used by more than one module
- **THEN** its version is managed once in the parent POM so child modules do not specify it

### Requirement: Child modules reference shared configuration without duplication
Each child service module (auth-service, product-service, and any future module such as resource recommendation) SHALL declare shared plugins by `groupId`/`artifactId` only and SHALL NOT repeat versions or configuration already managed by the parent POM.

#### Scenario: Child module activates a shared plugin without redefining it
- **WHEN** a child module's `pom.xml` declares `spring-boot-maven-plugin` or `maven-compiler-plugin`
- **THEN** it declares only `groupId` and `artifactId`, and the plugin inherits its version and configuration from the parent POM

#### Scenario: New child module follows the shared configuration
- **WHEN** a new service module is added to the project
- **THEN** it builds with the shared parent configuration without duplicating plugin versions or settings

### Requirement: Build executes the tests of every module

A `mvn test` (or `mvn package`) run SHALL actually discover and execute the unit tests of every module, and SHALL NOT report a passing build while executing zero tests because of an incompatible test-runner plugin version. A failing test SHALL fail the build.

#### Scenario: Tests are executed during a build
- **WHEN** a developer runs `mvn test` on the project and the modules contain unit tests
- **THEN** the build report lists the executed test classes and a non-zero total count of executed tests

#### Scenario: Unit tests in every module are covered
- **WHEN** a developer runs `mvn test` and every module has unit tests
- **THEN** tests from all modules are executed, not only those of a single module

#### Scenario: Test-runner version is pinned in the parent POM
- **WHEN** the parent POM is inspected for the configuration of the test-runner plugin
- **THEN** an explicit version of that plugin is declared in the parent POM, and the version is taken from a single declared property rather than from Maven's built-in default

#### Scenario: BOM plugin management is not relied upon for the test runner
- **WHEN** a shared BOM is imported only as a dependency version catalog in `dependencyManagement`
- **THEN** the build does not assume the BOM's plugin version declarations are inherited, and any plugin whose version must be controlled declares it in the parent POM's `pluginManagement`

#### Scenario: A failing test breaks the build
- **WHEN** a unit test in any module fails
- **THEN** the Maven build fails with a non-zero exit code instead of reporting success

#### Scenario: Child modules do not declare the test runner
- **WHEN** a child module's `pom.xml` is inspected
- **THEN** it contains no test-runner plugin declaration or version, and relies on the parent POM configuration