# AGENTS.md

Инструкции для ИИ-агентов, работающих в репозитории webShopProject.

## Git workflow (обязательно)

Все изменения вносятся **только через отдельную ветку + merge request**:

1. Перед началом работы создать ветку от `main`: `git checkout main && git pull --ff-only && git checkout -b <branch-name>`
   - Именование веток: `<type>/<описание>`, например `iss/fix/доработка-pom-в-сервисах-auth-и-product-notask`.
   - `<type>`: `fix`, `feat`, `chore`, `docs` и т.п. (совпадает с типом conventional-коммита). Префикс `iss/` — если задача связана с задачей в трекере.
2. Коммитить изменения только в этой ветке.
3. После завершения работы — запушить ветку и оформить MR/PR.
4. Свои коммиты в `main` напрямую — запрещены.

## Структура проекта

- Мультимодульный Maven-проект (Java 25, Spring Boot 4.0.6).
- Родительский `pom.xml` централизует общую конфигурацию: версии зависимостей в `dependencyManagement`, плагины в `pluginManagement` (см. `openspec/specs/build-configuration/spec.md`).
- Дочерние модули: `backend/auth-service`, `backend/product-service` — только подключают общую конфигурацию, не дублируют её.
- Спецификации проекта живут в `openspec/specs/` (OpenSpec).

## Прочее

- Язык коммитов и комментариев — русский.
- Коммит-сообщения по conventional commits: `fix(...)`, `feat(...)`, `chore(...)` и т.п. + задача `#<номер>` или `#notask`.