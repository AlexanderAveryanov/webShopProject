# Tasks

## 1. Implement admin user-management endpoints

- [x] 1.1 Create `AdminService` with `getAllUsers()`, `getUserById(id)` and mapping, verified by `mvn clean package`
- [x] 1.2 Fill `AdminController` with `GET /api/admin/users` and `GET /api/admin/users/{id}`, injecting `AdminService` instead of `UserRepository`
- [x] 1.3 Remove `/api/auth/users*` endpoints from `AuthController` and the now-unused methods from `AuthService`

## 2. Consolidate access control

- [x] 2.1 Remove the dead URL rule `.requestMatchers("/api/admin/**").hasRole("ADMIN")` from `SecurityConfig`; keep `@PreAuthorize("hasRole('ADMIN')")` on `AdminController`, verified by build and manual checks (401 unauthenticated, 403 non-ADMIN)

## 3. Update the spec

- [x] 3.1 Update `openspec/specs/user-management/spec.md` paths to `/api/admin/users*`
- [x] 3.2 Sync the delta spec to the main spec and archive the change