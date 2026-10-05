# Proposal

## Why

Сборка проекта запускает `mvn test` и рапортует `Tests run: 0`, то есть проходит успешно, не выполнив ни одного теста. Причина: в проекте не задан `maven-surefire-plugin`, поэтому Maven использует версию 2.12.4 из своего супер-POM, которая умеет запускать только JUnit 3 и 4. Тесты написаны на JUnit Jupiter 6.0.3, их движок молча игнорируется. Существующие 23 unit-теста (auth-service, product-service) не защищают код: сломанный тест не остановит сборку.

## What Changes

- В родительский `pom.xml` добавляется свойство `maven-surefire-plugin.version` со значением `3.5.5` — той же версией, которую задаёт Spring Boot BOM 4.0.6 в своём `pluginManagement`.
- В `pluginManagement` родительского `pom.xml` объявляется блок `maven-surefire-plugin` с версией из нового свойства, с поясняющим комментарием: почему версию нужно указывать явно (импорт BOM переносит только `dependencyManagement`, раздел `pluginManagement` не наследуется) и почему достаточно `pluginManagement` без подключения плагина в модулях.
- Дочерние модули (`backend/auth-service`, `backend/product-service`) не изменяются: `pluginManagement` родителя перекрывает версию и для плагинов, подключаемых Maven по жизненному циклу.
- Спецификация `build-configuration` дополняется требованием о том, что сборка обязана фактически выполнять тесты.

Не breaking: изменяется только то, какая версия surefire используется при сборке.

## Capabilities

### New Capabilities
<!-- Capabilities being introduced. Use kebab-case for path segments you introduce
     (e.g., user-auth or identity/user-auth) that follow the project's existing
     spec organization. Each creates specs/<capability-path>/spec.md. -->
Нет.

### Modified Capabilities
<!-- Existing capabilities whose REQUIREMENTS are changing (not just implementation).
     Only list here if spec-level behavior changes. Each needs a delta spec file.
     Use the exact existing path under openspec/specs/. Leave empty if no requirement
     changes. A change with no capabilities at all (pure refactor, tooling, docs)
     must set `skip_specs: true` in its .openspec.yaml - openspec validate rejects
     a zero-delta change without that marker. Do not invent a requirement just to
     satisfy validation. -->
- `build-configuration`: добавляется требование о выполнении тестов в сборке. Существующее требование «Centralized Maven build configuration in the parent POM» уже покрывает размещение плагинов в `pluginManagement`, поэтому новых требований к централизации не добавляется — покрывается лишь фактический запуск тестов, которого в спеке не было описано вовсе.

## Impact

- `pom.xml` (родительский) — свойство версии и блок `pluginManagement`.
- `openspec/specs/build-configuration/spec.md` — появляется требование о выполнении тестов после архивации change.
- Затронуты все дочерние модули сборки: после изменения `mvn test` начинает выполнять 23 теста вместо нуля.
- Внешние API не затрагиваются. Новые зависимости не добавляются.
- Побочный эффект: падение теста теперь провалит сборку. Это ожидаемое поведение, но к нему нужно быть готовым — до этого момента сборка была зелёной независимо от состояния тестов.