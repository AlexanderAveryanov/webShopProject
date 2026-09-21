# Design

## Context

See proposal.md — Why. The auth-service validates request payloads with Bean Validation (`@Size`), lowercase-normalizes email in `AuthService.normalizeEmail`, and stores passwords as BCrypt hashes. The registered email passes through Jackson binding → Bean Validation → service. Trimming must happen before validation, or whitespace-only variants are rejected by `@Email` before the service can normalize them.

## Goals / Non-Goals

**Goals:**
- Bound password length to 6–72 characters in both registration and login with a single clear message.
- Normalize email (trim leading/trailing whitespace, lowercase) so a whitespace-padded email registers/logs in as the clean address.

**Non-Goals:**
- Byte-level password max enforcement (see Risks).
- Rewriting email validation; `@Email` remains the format gate.
- Changing the max length semantics of `RegisterRequest` vs `LoginRequest` asymmetrically.

## Decisions

- **Single `@Size(min = 6, max = 72, message = "Пароль должен содержать от 6 до 72 символов")`** on `RegisterRequest.password` and `LoginRequest.password`.
  - Rationale: one constraint, one message for both bounds. `@Size` is not repeatable, so distinct per-bound texts would need a custom validator — rejected as overkill.
  - Consequence: the short-password message changes from «минимум 6 символов» to «от 6 до 72 символов». Spec scenarios only assert HTTP 400 with field-level details, so no spec impact.
  - Alternative considered: keep the existing min `@Size` and add a separate max constraint — impossible with a single non-repeatable annotation.
- **`max = 72` counts characters, not bytes.** BCrypt truncates at 72 bytes of UTF-8. For ASCII (the common case) the limits coincide. For multi-byte text (Cyrillic = 2 bytes/char, emoji = 4) a 72-char password still exceeds 72 bytes — a residual truncation edge, accepted for this project.
  - Alternative considered: custom byte-level validator — rejected as overkill for a test project.
- **Trim email in the DTO setter, lowercase in the service.** Add a hand-written `setEmail(String)` on `RegisterRequest` and `LoginRequest` (Lombok skips generating an existing setter) that trims; Jackson binding calls it, so validation sees the trimmed value and `@Email` passes. `AuthService.normalizeEmail` keeps `.trim().toLowerCase(Locale.ROOT)` as the service-level guarantee (covers `@Builder`/`@AllArgsConstructor`-built DTOs, which bypass setters).
  - Rationale: `@Email` (anchored regex) rejects leading/trailing whitespace; without pre-binding trim a whitespace-padded email would 400 instead of normalizing to 201. This makes the approved spec scenario observable.
- **Applied to both registration and login.** The DB functional unique index `idx_users_email_lower` is unaffected (stored value is already trimmed/lowercased).

## Risks / Trade-offs

- [BCrypt 72-byte truncation for multi-byte passwords] → documented; 72-char cap matches the ASCII limit; accepted for this project.
- [Short/long password at login returns 400 instead of 401/404] → intended; does not reveal whether an email is registered (400 is returned before any account lookup); already the shipped behaviour for short passwords.
- [Trim relies on the hand-written setter being used by Jackson] → Jackson uses public setters for bean binding; the service re-trims regardless, keeping the invariant even for builder-constructed DTOs.

## Migration Plan

N/A — new constraints and normalization are effective immediately; DB schema unchanged.

## Open Questions

None.