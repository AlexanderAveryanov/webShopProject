# Tasks

## 1. Валидация длины пароля

- [x] 1.1 Добавить `@Size(min = 6, max = 72, message = "Пароль должен содержать от 6 до 72 символов")` в `RegisterRequest.password` и `LoginRequest.password` и убедиться, что `mvn clean package` в `backend/auth-service` проходит

## 2. Нормализация email

- [x] 2.1 Добавить вручную написанный `setEmail(String)` (с `trim()`) в `RegisterRequest` и `LoginRequest` и убедиться, что Lombok не генерирует дубликат сеттера (сборка проходит)
- [x] 2.2 Дополнить `AuthService.normalizeEmail` вызовом `trim()` перед `toLowerCase(Locale.ROOT)` и убедиться, что `mvn clean package` в `backend/auth-service` проходит

## 3. Проверка сценариев на запущенном приложении

- [x] 3.1 Запустить `auth-service` и через `curl` проверить `POST /api/auth/register` и `/api/auth/login`: пароль <6 и >72 символов → HTTP 400 с деталями валидации
- [x] 3.2 Через `curl` проверить `POST /api/auth/register` с email, содержащим пробелы по краям: HTTP 201, в ответе и при повторном логине email нормализован (без пробелов, в нижнем регистре)