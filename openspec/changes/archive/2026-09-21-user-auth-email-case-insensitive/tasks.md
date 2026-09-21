# Tasks

## 1. Normalize email in the application layer

- [x] 1.1 Add `AuthService.normalizeEmail(String)` (`toLowerCase(Locale.ROOT)`) with javadoc
- [x] 1.2 Use normalized email in `register` for the uniqueness check and persisting, verified by `mvn clean package`
- [x] 1.3 Use normalized email in `login` for the lookup
- [x] 1.4 Extract the normalized email into a local variable in `register`

## 2. Enforce case-insensitive uniqueness in the database

- [x] 2.1 In `V1__create_users_table.sql` remove the redundant `idx_users_email` and add unique index `idx_users_email_lower ON users (LOWER(email))`

## 3. Update the spec

- [x] 3.1 Add case-insensitivity scenarios (registration → 409, login → 200) to `openspec/specs/user-auth/spec.md`
- [x] 3.2 Sync the delta spec to the main spec and archive the change