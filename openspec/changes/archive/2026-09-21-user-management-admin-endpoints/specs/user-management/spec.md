# Spec Delta

## Purpose

Manages registered users in the auth-service with role-based access control, restricting user listing and lookup to users with the ADMIN role.

## MODIFIED Requirements

### Requirement: Listing all users

The system SHALL return all registered users for a requester with the ADMIN role.

#### Scenario: ADMIN retrieves all users
- **WHEN** a user with role ADMIN sends `GET /api/admin/users` with a valid bearer token
- **THEN** the system returns HTTP 200 with a list of all users (id, email, firstName, lastName, role) without passwords

#### Scenario: Non-ADMIN attempts to retrieve all users
- **WHEN** a user without the ADMIN role sends `GET /api/admin/users` with a valid bearer token
- **THEN** the system denies access

#### Scenario: Unauthenticated request to list users
- **WHEN** a request without a valid bearer token is sent to `GET /api/admin/users`
- **THEN** the system denies access

### Requirement: Retrieval of a user by id

The system SHALL return a single user by its id for a requester with the ADMIN role.

#### Scenario: ADMIN retrieves an existing user by id
- **WHEN** a user with role ADMIN sends `GET /api/admin/users/{id}` with a valid bearer token and the id of an existing user
- **THEN** the system returns HTTP 200 with the user data (id, email, firstName, lastName, role) without password

#### Scenario: ADMIN retrieves a user with an unknown id
- **WHEN** a user with role ADMIN sends `GET /api/admin/users/{id}` with a valid bearer token and an id that does not exist
- **THEN** the system returns HTTP 404 with an error response

#### Scenario: Non-ADMIN attempts to retrieve a user by id
- **WHEN** a user without the ADMIN role sends `GET /api/admin/users/{id}` with a valid bearer token
- **THEN** the system denies access