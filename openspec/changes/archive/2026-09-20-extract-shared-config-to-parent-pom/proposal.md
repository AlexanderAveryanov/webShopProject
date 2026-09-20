# Proposal

## Why

The project is a multi-module Maven build (webShopProject: auth-service, product-service, and a future recommendation module). Each service previously duplicated the full plugin configuration (versions, `repackage` binding, Java version, Lombok annotation processing) in its own `pom.xml`. That leads to copy-paste drift and makes changes tedious. The convention should be: everything that can be shared lives in the parent POM, and child modules only reference it.

## What Changes

- Move shared plugin configuration (`spring-boot-maven-plugin`, `maven-compiler-plugin`) into the parent POM under `<pluginManagement>`.
- Child modules (`auth-service`, `product-service`) declare plugins with just `groupId`/`artifactId` — versions and configuration inherit from the parent.
- Document this convention as a new capability so future modules (e.g., recommendation-service) follow it.

## Capabilities

### New Capabilities
- `build-configuration`: Shared Maven build configuration for the multi-module project — shared dependencies and plugins are declared once in the parent POM (`dependencyManagement`/`pluginManagement`) and child modules reference them without duplicating versions or configuration.

### Modified Capabilities
- none

## Impact

- Parent POM (`pom.xml`): adds `<build><pluginManagement>` block.
- `backend/auth-service/pom.xml`, `backend/product-service/pom.xml`: plugin declarations simplified.
- Future modules: new services declare only their module-specific dependencies; shared build config is inherited.