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