# Design

## Context

The auth-service exposes authentication endpoints (`AuthController`) and admin endpoints. Previously the ADMIN-only user listing/lookup was implemented in `AuthController` at `/api/auth/users*`. The admin panel (`AdminController` at `/api/admin`) existed but had no endpoints. The implementation is already merged; this document records the design decisions retroactively.

## Goals / Non-Goals

**Goals**
- Admin user-management endpoints live under `/api/admin/**` with a single access-control mechanism.
- Controllers use the service layer, not repositories directly.

**Non-Goals**
- Introducing new admin capabilities (e.g., create/update/delete) — future work.
- Changing the public user-auth flow.

## Decisions

- **New `AdminService`** holds `getAllUsers()` and `getUserById(id)` (+ mapping). `AuthService` keeps only authentication concerns; its admin methods were removed.
- **`AdminController`** (`@RequestMapping("/api/admin")`) exposes `GET /users` and `GET /users/{id}` and enforces ADMIN via class-level `@PreAuthorize("hasRole('ADMIN')")` — the same annotation style already used for admin methods in `AuthController`.
- **`SecurityConfig`**: the URL rule `.requestMatchers("/api/admin/**").hasRole("ADMIN")` was removed. Role checks happen via method security; `.anyRequest().authenticated()` remains as the base authentication gate (unauth → 401, non-ADMIN on admin endpoints → 403).
- **Mapping duplication** between `AuthService` and `AdminService` (`mapToUserResponse`) is accepted temporarily until a mapper (e.g., MapStruct) is introduced.

## Risks / Trade-offs

- Two sources of truth for authorization avoided (URL rule + annotation) → single source: `@PreAuthorize`.
- Admin endpoints moved to the admin panel; any external clients of `/api/auth/users*` must switch to `/api/admin/users*`.

## Migration Plan

Already applied (PR #17) and merged into `main`.

## Open Questions

None.