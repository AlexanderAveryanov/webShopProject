# user-management Specification

## Purpose

Manages registered users in the auth-service with role-based access control, restricting user listing and lookup to users with the ADMIN role.

## Requirements

### Requirement: Listing all users

The system SHALL return all registered users for a requester with the ADMIN role.

#### Scenario: ADMIN retrieves all users
- **WHEN** a user with role ADMIN sends `GET /api/auth/users` with a valid bearer token
- **THEN** the system returns HTTP 200 with a list of all users (id, email, firstName, lastName, role) without passwords

#### Scenario: Non-ADMIN attempts to retrieve all users
- **WHEN** a user without the ADMIN role sends `GET /api/auth/users` with a valid bearer token
- **THEN** the system denies access

#### Scenario: Unauthenticated request to list users
- **WHEN** a request without a valid bearer token is sent to `GET /api/auth/users`
- **THEN** the system denies access

### Requirement: Retrieval of a user by id

The system SHALL return a single user by its id for a requester with the ADMIN role.

#### Scenario: ADMIN retrieves an existing user by id
- **WHEN** a user with role ADMIN sends `GET /api/auth/users/{id}` with a valid bearer token and the id of an existing user
- **THEN** the system returns HTTP 200 with the user data (id, email, firstName, lastName, role) without password

#### Scenario: ADMIN retrieves a user with an unknown id
- **WHEN** a user with role ADMIN sends `GET /api/auth/users/{id}` with a valid bearer token and an id that does not exist
- **THEN** the system returns HTTP 404 with an error response

#### Scenario: Non-ADMIN attempts to retrieve a user by id
- **WHEN** a user without the ADMIN role sends `GET /api/auth/users/{id}` with a valid bearer token
- **THEN** the system denies access

### Requirement: Role-based access control

The system SHALL assign role USER to newly registered users and SHALL enforce the role when granting access to ADMIN-only endpoints. Access decisions SHALL be based on the role of the bearer-token holder.

#### Scenario: New user is created with role USER
- **WHEN** a guest registers a new account
- **THEN** the created user has the role USER

#### Scenario: ADMIN role holders access admin endpoints
- **WHEN** a request to an ADMIN-only endpoint is authenticated as a user with role ADMIN
- **THEN** the request is permitted