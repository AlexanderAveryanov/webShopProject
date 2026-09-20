# Proposal

## Why

The auth-service is fully implemented (registration, login, JWT, RBAC, error handling) but has no OpenSpec specifications yet. The project's specs directory is empty, so there is no contract describing the existing behavior. Before planning further work, document the current auth-service capabilities as ground-truth specs.

## What Changes

- Add OpenSpec delta specs covering the existing auth-service behavior:
  - Registration of a new USER
  - Login with JWT issuance
  - Retrieving the current user
  - ADMIN-only user list and user-by-id endpoints
  - Role-based access control (USER/ADMIN)
  - Standardized error responses
- These spec files become the basis of the main specs after sync.

## Capabilities

### New Capabilities
- `user-auth`: Registration, login/JWT issuance, current-user retrieval
- `user-management`: ADMIN-only user listing and lookup, role-based access control

### Modified Capabilities
- none

## Impact

- Affected code: none (documentation only).
- Files added: `openspec/changes/auth-service-specs/specs/user-auth/spec.md`, `openspec/changes/auth-service-specs/specs/user-management/spec.md`.
- No API, dependency, or infrastructure changes.