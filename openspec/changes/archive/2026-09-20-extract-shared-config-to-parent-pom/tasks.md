# Tasks

## 1. Centralize shared build configuration in the parent POM

- [x] 1.1 Add `<pluginManagement>` to the parent `pom.xml` declaring `spring-boot-maven-plugin` (version `${spring-boot.version}` and the `repackage` execution) and `maven-compiler-plugin` (version, Java source/target, Lombok `annotationProcessorPaths`), verified via `mvn validate`
- [x] 1.2 Simplify `backend/auth-service/pom.xml` so both plugins are declared with `groupId`/`artifactId` only, verified by `mvn -pl backend/auth-service help:effective-pom` resolving the version and configuration from the parent

## 2. Propagate the convention to existing modules

- [x] 2.1 Simplify `backend/product-service/pom.xml` the same way (without adding comments), verified by `mvn validate`
- [x] 2.2 Confirm the whole multi-module build still packages executable JARs, verified by `mvn clean package` from the repository root producing a successful build

## 3. Document the convention in specs

- [x] 3.1 Add the `build-configuration` capability spec describing the parent-pom-centralization convention and how child modules reference shared plugins, verified by `openspec validate`
- [x] 3.2 Sync the delta spec to the main spec and archive the change