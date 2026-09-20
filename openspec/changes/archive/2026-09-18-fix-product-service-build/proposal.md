# Proposal

## Why

The `product-service` module cannot be built as a Spring Boot executable JAR and may silently fail to process Lombok annotations. Its `pom.xml` is missing the `spring-boot-maven-plugin` (present in `auth-service`) and lacks the `maven-compiler-plugin` configuration with `annotationProcessorPaths` for Lombok, which is required for annotation processing on Java 25.

## What Changes

- Add `spring-boot-maven-plugin` to `backend/product-service/pom.xml` so the module can be packaged as an executable JAR.
- Add `maven-compiler-plugin` with `<source>`/`<target>` set to the project Java version and `annotationProcessorPaths` containing Lombok, mirroring the `auth-service` module.
- Verify the module compiles and batch `mvn install` succeeds.

## Capabilities

### New Capabilities
- none

### Modified Capabilities
- none

This change is build/tooling only; `skip_specs: true` is set (no spec-level behavior changes).

## Impact

- Files modified: `backend/product-service/pom.xml`
- Build tooling only; no runtime behavior, API, or infrastructure changes.