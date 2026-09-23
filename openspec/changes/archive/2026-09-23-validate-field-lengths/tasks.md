# Tasks

## 1. DTO validation for field lengths

- [x] 1.1 Add `@Size(max = 255, message = "Email не должен превышать 255 символов")` to `RegisterRequest.email` (alongside `@NotBlank`/`@Email`) and verify `mvn -pl backend/auth-service compile` succeeds
- [x] 1.2 Add `@Size(max = 100, message = "Имя не должно превышать 100 символов")` to `RegisterRequest.firstName` and verify compilation
- [x] 1.3 Add `@Size(max = 100, message = "Фамилия не должна превышать 100 символов")` to `RegisterRequest.lastName` and verify compilation
- [x] 1.4 Add `@Size(max = 255, message = "Email не должен превышать 255 символов")` to `LoginRequest.email` and verify compilation

## 2. Entity schema alignment

- [x] 2.1 Add explicit `length` to `User` `@Column` annotations: `email` 255, `password` 255, `first_name`/`last_name` 100, `role` 50 and verify `mvn -pl backend/auth-service compile` succeeds and `ddl-auto: validate` still passes at startup

## 3. Error-handling narrowing

- [x] 3.1 In `AuthService.register`, type the exception instead of parsing text: map `DataIntegrityViolationException` to `EmailAlreadyExistsException` only when the root cause is a `PSQLException` with `SQLState 23505` (unique_violation); rethrow otherwise. Verify `mvn -pl backend/auth-service compile` succeeds (postgresql driver scope changed `runtime` → `compile`)

## 4. Behavior verification

- [x] 4.1 Start the service against a local Postgres (`docker compose up -d postgres-auth`, then `mvn -pl backend/auth-service spring-boot:run`) and verify registration of an email longer than 255 characters returns HTTP 400 with field details
- [x] 4.2 Verify registration with `firstName`/`lastName` longer than 100 characters returns HTTP 400 with field details
- [x] 4.3 Verify login with an email longer than 255 characters returns HTTP 400 with field details
- [x] 4.4 Verify a normal registration (`email ≤255`, names ≤100, password 6–72) still returns HTTP 201 and a re-registration of the same email still returns HTTP 409
- [x] 4.5 Run `mvn clean package` on the whole project and confirm the build passes