# Proposal

## Why

Registration DTOs lack upper bounds for email and name lengths. Values longer than the underlying columns (`email VARCHAR(255)`, `first_name`/`last_name VARCHAR(100)`) pass bean validation, fail at the database with a length error, and are misreported by `AuthService.register` as HTTP 409 "email already exists" because its `catch (DataIntegrityViolationException)` converts every integrity violation into `EmailAlreadyExistsException`. The entity also omits explicit `length` in `@Column`, so it disagrees with the Flyway schema under `ddl-auto: validate`.

## What Changes

- Add `@Size(max = 255)` to `email` in `RegisterRequest` and `LoginRequest` with a Russian validation message. Email is trimmed in the setter before validation, so a ≤255 character value always fits the DB column.
- Add `@Size(max = 100)` to `firstName` and `lastName` in `RegisterRequest` with Russian validation messages (currently unvalidated).
- Declare explicit `length` in the `User` entity columns (`email` 255, `password` 255, `first_name`/`last_name` 100, `role` 50) so the entity matches the Flyway schema.
- Narrow `AuthService.register` error handling: map `DataIntegrityViolationException` to `EmailAlreadyExistsException` only for violations of the unique email constraint; otherwise let the global handler report the real cause instead of a misleading 409.
- Update the `user-auth` spec: registration and login reject emails longer than 255 characters and names longer than 100 characters with HTTP 400 and field-level details.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `user-auth`: guardrails for field lengths in registration and login — email up to 255 characters; first/last name up to 100 characters (registration only); violations return HTTP 400. Added in the registration and login requirements and their scenarios.

## Impact

- `backend/auth-service/src/main/java/com/shop/auth/dto/RegisterRequest.java` — `@Size(max = 255)` on email, `@Size(max = 100)` on firstName/lastName.
- `backend/auth-service/src/main/java/com/shop/auth/dto/LoginRequest.java` — `@Size(max = 255)` on email.
- `backend/auth-service/src/main/java/com/shop/auth/entity/User.java` — explicit `length` on string columns.
- `backend/auth-service/src/main/java/com/shop/auth/service/AuthService.java` — narrowed `DataIntegrityViolationException` handling in `register`.
- `openspec/specs/user-auth/spec.md` — new validation scenarios via delta.