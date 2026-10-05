# Spec Delta

## ADDED Requirements

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