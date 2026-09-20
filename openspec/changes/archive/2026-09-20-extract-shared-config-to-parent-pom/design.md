# Design

## Context

Multi-module Maven project (parent `webShopProject`, modules `auth-service`, `product-service`, future `recommendation` module). Each module previously duplicated the full `spring-boot-maven-plugin` and `maven-compiler-plugin` configuration. The implementation is already in place (see proposal.md); this design documents the approach so the convention is explicit and repeatable.

## Goals / Non-Goals

**Goals**
- Single source of truth in the parent POM for shared plugin versions/settings and dependency versions.
- Child modules keep minimal declarations.

**Non-Goals**
- Moving module-specific configuration (e.g., per-service dependency sets) into the parent.
- Changing the dependency versions themselves.

## Decisions

- **Use Maven `pluginManagement` in the parent POM** for shared plugins. Analogous to `dependencyManagement`: it defines versions/configuration but does not bind the plugin; each child explicitly "activates" it via a minimal `<plugin>` declaration (`groupId`/`artifactId` only). This keeps the parent reusable without forcing every module to get every plugin.
- **`spring-boot-maven-plugin` stays declared explicitly in each child** (without version/`<executions>`). It must be bound per-module to enable the `repackage` goal; minimal declaration avoids duplicating the binding defined once in the parent.
- **`maven-compiler-plugin` likewise** resolves its version, Java source/target, and Lombok `annotationProcessorPaths` from the parent's `pluginManagement`, leaving children with a one-line declaration.
- **Alternatives considered**: inheriting `spring-boot-starter-parent` — rejected, the project deliberately has its own parent POM; a fully implicit plugin via default lifecycle — rejected because the default version/annotation processing would not match the project's pinned configuration and Lombok would not be wired as an annotation processor.

## Risks / Trade-offs

- Forgetting to declare a plugin in a child (it stays in `pluginManagement`, unused) → Mitigation: convention documented in spec `build-configuration`; build is verified per module.
- Over-centralizing per-service config → Scope guard: only truly shared settings move to the parent; service-specific dependencies stay in the child.

## Migration Plan

Already applied to `auth-service`, `product-service`, and the parent `pom.xml`. New modules copy the minimal child pattern.

## Open Questions

None.