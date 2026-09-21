# Design

## Context

The `users.email` column had a case-sensitive `UNIQUE` constraint and case-sensitive lookups. Two accounts with emails differing only in letter case could coexist. The project is new and the databases are empty, so the migration was edited in place (`V1`) rather than adding a follow-up migration. The implementation is already merged; this document records the design decisions retroactively.

## Goals / Non-Goals

**Goals**
- New registrations cannot collide with an existing email in any letter case.
- Login works regardless of the letter case typed by the user.
- Enforce case-insensitive uniqueness at the database level, not just in application code.

**Non-Goals**
- Changing the email storage format shown to clients (the normalized form is returned).
- Internationalized (Unicode) case folding beyond `Locale.ROOT` lowercase.

## Decisions

- **Application normalization in one place**: `AuthService.normalizeEmail(String)` uses `toLowerCase(Locale.ROOT)` — locale-independent (guards against e.g. the "Turkish I" problem). It is applied in `register` (both uniqueness check and persist) and in `login` (lookup).
- **Database-level guarantee**: functional unique index `idx_users_email_lower ON users (LOWER(email))` rejects any row whose lowercased email equals an existing one, independent of the app path.
- **`UNIQUE` on the column kept**: keeps a plain index for fast lookups by the (normalized) email and stays compatible with `ddl-auto: validate` (`@Column(unique = true)` on the entity).
- **Redundant index removed**: `idx_users_email` duplicated the backing index of the `UNIQUE` constraint and was dropped.

## Risks / Trade-offs

- Existing rows with case-differing duplicates would break `UPDATE ... LOWER(email)`; accepted — databases are empty in this new project.
- Database schema edited in `V1` (not a new migration) — fine while databases are empty, but future schema changes should follow normal Flyway versioning.

## Migration Plan

Already applied (PR #18) and merged into `main`.

## Open Questions

None.