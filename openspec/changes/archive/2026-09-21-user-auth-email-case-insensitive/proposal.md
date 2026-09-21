# Proposal

## Why

Email uniqueness and lookup were case-sensitive: `Foo@x.com` and `foo@x.com` were two different accounts, and login only worked with the exact stored letter case. Emails are conventionally case-insensitive, so the auth-service must normalize and treat them uniformly.

## What Changes

- `AuthService.register`: email is normalized to lowercase before the uniqueness check and before persisting.
- `AuthService.login`: email is normalized to lowercase before lookup.
- `V1__create_users_table.sql`: a functional unique index `idx_users_email_lower ON users (LOWER(email))` enforces case-insensitive uniqueness at the database level; the redundant non-unique index `idx_users_email` is removed.

## Capabilities

### New Capabilities
- none

### Modified Capabilities
- `user-auth`: email is case-insensitive for registration and login.

## Impact

- `backend/auth-service/src/main/java/com/shop/auth/service/AuthService.java`: normalization logic.
- `backend/auth-service/src/main/resources/db/migration/V1__create_users_table.sql`: case-insensitive unique index.
- `openspec/specs/user-auth/spec.md`: case-insensitivity scenarios added.