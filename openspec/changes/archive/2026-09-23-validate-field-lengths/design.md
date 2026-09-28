# Design

## Context

Auth-service registration currently maps every `DataIntegrityViolationException` thrown by `UserRepository.save` to `EmailAlreadyExistsException` (AuthService.java:79-81). Draft DTOs lack upper length bounds, so long email/name values pass validation and are swallowed by that catch as a bogus HTTP 409. The `User` entity omits explicit `length` in `@Column`, disagreeing with the Flyway schema under `ddl-auto: validate`. See proposal.md — Why for motivation.

## Goals / Non-Goals

**Goals:**
- Reject overly long email (255), first name, last name (100) with HTTP 400 via bean validation.
- Keep entity column widths explicit and aligned with the Flyway V1 schema.
- Map DB unique-violations to 409 only when they actually concern the email uniqueness constraint.

**Non-Goals:**
- No DB schema or Flyway migration changes — columns already have the correct widths.
- No change to password length rules (`@Size(min = 6, max = 72)` already present). The BCrypt 72-byte edge case for multi-byte passwords is a known, previously accepted limitation and stays out of scope here.
- No new tests framework or test scaffold.

## Decisions

- **DTO validation via `@Size`, not service-level checks.** Bean validation already runs through `@Valid` on the controllers and produces the standard 400 field-details response (MessageSource `GlobalExceptionHandler`). Adding `@Size(max = 255)` / `@Size(max = 100)` is consistent with the existing password validation pattern.
- **Email limit 255, matching the DB column exactly.** The email setter trims before validation, so the value Bean Validation sees is the one persisted; an address of ≤255 trimmed characters always fits `VARCHAR(255)`.
- **Narrow the `register` catch by typing the exception, not by parsing text.** Instead of mapping every `DataIntegrityViolationException` to 409, or searching the error message for index/constraint names, the root cause is typed: a `PSQLException` with `getSQLState() == "23505"` is the standard `unique_violation`. Since `email` is the only unique column in `users`, any unique violation during save means an occupied email → `EmailAlreadyExistsException`; any other state (22001 value too long, 23502 NOT NULL, …) rethrows so `GlobalExceptionHandler` reports the real violation. SQLState codes are stable across PostgreSQL versions, unlike message text. This requires the `postgresql` driver on the compile classpath (`PSQLException` is referenced in code), so its scope changes from `runtime` to `compile`. Alternatives considered: substring match on the DB message (fragile — rejected), `existsByEmail` only (racy, still needs a catch), and Hibernate `ConstraintViolationException.getConstraintName()` (works but needs walking the cause chain and may yield `null` in some versions). With the new `@Size` guards, the non-unique path is effectively unreachable — the catch is a defensive safety net for the parallel-insert race on email.
- **Explicit `@Column(length = …)` on the entity** for `email` (255), `password` (255), `first_name`/`last_name` (100), `role` (50). This aligns Hibernate schema validation with the Flyway-created columns. No DDL change is needed — `ddl-auto: validate` only checks, it does not alter.

## Risks / Trade-offs

- **Intentional PostgreSQL coupling via SQLState** — the code checks a PostgreSQL-specific `PSQLException` and its `getSQLState()`. → Mitigation: `23505` is a standardized SQL/PostgreSQL code not expected to change; the project is PostgreSQL-only (Flyway `postgresql` module, `auth_db`), so the coupling is acceptable.
- **`@Size` counts characters, not bytes** — same unit as the VARCHAR(n) columns, so no truncation risk; multi-byte email/name characters count once but PostgreSQL `VARCHAR(255)` is character-based, so alignment is correct by construction.
- **Behavior change for 409 consumers**: previously any DB error during save surfaced as "email exists"; now genuine non-email violations surface as a generic 409 DB message. Given the new validation guards, this path is effectively unreachable in normal use.