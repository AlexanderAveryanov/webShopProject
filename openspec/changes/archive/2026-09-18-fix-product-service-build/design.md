# Design

## Context

See proposal.md - Why. `backend/product-service/pom.xml` lacks the build plugins that `backend/auth-service/pom.xml` already declares.

## Goals / Non-Goals

**Goals:**
- Make `product-service` package as an executable Spring Boot JAR.
- Guarantee Lombok annotation processing runs under Java 25.

**Non-Goals:**
- No runtime/behavioral changes; build configuration only.

## Decisions

**Mirror the `auth-service` build block in `product-service/pom.xml`.** The child module needs its own `<build>` section with `spring-boot-maven-plugin` (for repackaging into an executable JAR) and `maven-compiler-plugin` (to pin source/target and register Lombok explicitly through `annotationProcessorPaths` on Java 25).

**Alternative considered:** Move both plugin definitions into the parent `pom.xml` under `pluginManagement` so both services inherit them. Not chosen: it widens the change's blast radius and alters `auth-service`'s established config; a later cross-cutting "deduplicate build config" change can do that.

**Bind `repackage` explicitly in both modules.** This project inherits from a custom `webShopProject` parent, not from `spring-boot-starter-parent`, so declaring `spring-boot-maven-plugin` without an `<executions>` block does NOT attach the `repackage` goal to the `package` phase. Discovery during apply showed neither module produced an executable JAR (thin jars, no `BOOT-INF`, no `Main-Class`). Fix: add an execution invoking the `repackage` goal in `product-service/pom.xml` and, since `auth-service` had the identical latent bug, in `auth-service/pom.xml` too (user-approved scope).

## Risks / Trade-offs

[Risk] Keeping the plugin config duplicated across two module POMs can drift → Acceptable for now; the build blocks are copied verbatim from the fixed config, and both modules share the parent's version properties. Consolidation is captured as a deliberate non-goal.

[Risk] Repackage leaves the thin console-classpath JAR untouched and replaces the artifact → By design in Spring Boot; with no tests bound to the thin JAR today there is no impact. Revisit if tests are added that assert on the thin artifact.

## Migration Plan

No runtime deployment; a successful `mvn install` from the repository root is the rollout signal. Rollback = revert the pom.xml changes.

## Open Questions

None.