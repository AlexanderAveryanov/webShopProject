# Proposal

## Why

Login and registration validate only a minimum password length (6), while BCrypt silently truncates passwords longer than 72 bytes — two different long passwords can hash identically. Email normalization lowercases but does not trim surrounding whitespace, so addresses entered with leading/trailing spaces are stored literally and become unreachable without those spaces. The spec does not document these behaviors.

## What Changes

- Add an upper bound to password length: `@Size(min = 6, max = 72)` on `RegisterRequest.password` and `LoginRequest.password`, with a corresponding validation message.
- Normalize email with `trim()` before lowercasing in `AuthService.normalizeEmail`, so leading/trailing whitespace is removed before uniqueness checks, lookups, and persistence.
- Update the `user-auth` spec: document HTTP 400 responses for password outside the 6–72 range (login and registration) and normalization of emails with leading/trailing whitespace.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `user-auth`: registration and login password length bounded to 6–72 characters (HTTP 400 on violation); email normalized by trimming leading/trailing whitespace and lowercasing.

## Impact

- `backend/auth-service/src/main/java/com/shop/auth/dto/RegisterRequest.java` — `@Size(min = 6, max = 72, ...)`.
- `backend/auth-service/src/main/java/com/shop/auth/dto/LoginRequest.java` — `@Size(min = 6, max = 72, ...)`.
- `backend/auth-service/src/main/java/com/shop/auth/service/AuthService.java` — `normalizeEmail` adds `trim()` before `toLowerCase(Locale.ROOT)`.
- `openspec/specs/user-auth/spec.md` — new validation scenarios and messages.

Note: messages stay in Russian, consistent with existing validation texts. `@Size` counts characters; BCrypt's 72-char limit holds for ASCII, and the residual byte-truncation edge for multi-byte passwords is accepted for this project (documented in design.md).