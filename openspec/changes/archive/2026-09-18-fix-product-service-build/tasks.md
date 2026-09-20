# Tasks

## 1. Fix product-service build configuration

- [x] 1.1 Add `spring-boot-maven-plugin` and `maven-compiler-plugin` (with `annotationProcessorPaths` for Lombok) to `backend/product-service/pom.xml`, mirroring `backend/auth-service/pom.xml`, and verify `mvn -pl backend/product-service compile` succeeds with Lombok processing active
- [x] 1.2 Add `spring-boot-maven-plugin` `repackage` execution to `backend/product-service/pom.xml` and `backend/auth-service/pom.xml` (the plugin was not bound to the `package` phase because the project does not inherit `spring-boot-starter-parent`), and verify `mvn clean package` produces a repackaged executable JAR with `BOOT-INF` and `Main-Class` for both modules
- [x] 1.3 Verify the whole multi-module build with `mvn install` from the repository root succeeds, confirming the module packages as an executable JAR