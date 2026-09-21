# Proposal

## Why

The admin user-management endpoints (`GET /api/auth/users`, `GET /api/auth/users/{id}`) lived in `AuthController` alongside authentication endpoints. When the admin panel was introduced (`AdminController` with `AdminService`), user listing and lookup logically belong to the admin panel: they are ADMIN-only operations and should share the `/api/admin/**` path, covered by a single access-control mechanism (`@PreAuthorize` on `AdminController`).

## What Changes

- Move user-management endpoints from `/api/auth/users` and `/api/auth/users/{id}` to `/api/admin/users` and `/api/admin/users/{id}`.
- The endpoints are now provided by `AdminController` + `AdminService` instead of `AuthController` + `AuthService`.
- SecurityConfig's dead URL rule `/api/admin/**` is removed; ADMIN access is enforced via method security (`@PreAuthorize("hasRole('ADMIN')")`).

## Capabilities

### New Capabilities
- none

### Modified Capabilities
- `user-management`: listing and lookup endpoints move to the admin panel.

## Impact

- `backend/auth-service/src/main/java/com/shop/auth/controller/AdminController.java`, `AdminService.java`: endpoints implemented.
- `backend/auth-service/src/main/java/com/shop/auth/controller/AuthController.java`: `/api/auth/users*` removed.
- `backend/auth-service/src/main/java/com/shop/auth/config/SecurityConfig.java`: `/api/admin/**` URL rule removed.
- `openspec/specs/user-management/spec.md`: endpoint paths updated.