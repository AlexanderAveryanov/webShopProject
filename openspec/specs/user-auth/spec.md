# user-auth Specification

## Purpose

Handles user authentication in the auth-service: registration of new users, login with JWT issuance, and retrieval of the currently authenticated user.

## Requirements

### Requirement: Registration of a new user

The system SHALL allow a guest to register with email and password and create a user account with role USER. Email SHALL be unique and in a valid format; password SHALL be at least 6 characters long. The password SHALL NOT be returned in any response.

#### Scenario: Successful registration
- **WHEN** a guest sends `POST /api/auth/register` with a valid unique email and a password of at least 6 characters
- **THEN** the system creates a user with role USER and returns HTTP 201 with the user data (id, email, firstName, lastName, role) and no password

#### Scenario: Registration with an existing email
- **WHEN** a guest sends `POST /api/auth/register` with an email that is already registered
- **THEN** the system returns HTTP 409 with an error response

#### Scenario: Registration with an email differing only in letter case
- **WHEN** a guest sends `POST /api/auth/register` with an email that matches an already registered address, differing only in letter case (e.g. `Foo@x.com` vs `foo@x.com`)
- **THEN** the system returns HTTP 409 with an error response

#### Scenario: Registration with an invalid email format
- **WHEN** a guest sends `POST /api/auth/register` with an email that does not match a valid email format
- **THEN** the system returns HTTP 400 with field-level validation details

#### Scenario: Registration with a too-short password
- **WHEN** a guest sends `POST /api/auth/register` with a password shorter than 6 characters
- **THEN** the system returns HTTP 400 with field-level validation details

### Requirement: Login and JWT issuance

The system SHALL authenticate a registered user by email and password and issue a bearer JWT token valid for a configured expiration period.

#### Scenario: Successful login
- **WHEN** a user sends `POST /api/auth/login` with a registered email and the correct password
- **THEN** the system returns HTTP 200 with the issued JWT token, its type "Bearer", and the user data

#### Scenario: Login with an email differing only in letter case
- **WHEN** a user sends `POST /api/auth/login` with a registered email typed in a different letter case
- **THEN** the system returns HTTP 200 with the issued JWT token, its type "Bearer", and the user data

#### Scenario: Login with an unknown email
- **WHEN** a user sends `POST /api/auth/login` with an email that is not registered
- **THEN** the system returns HTTP 404 with an error response

#### Scenario: Login with a wrong password
- **WHEN** a user sends `POST /api/auth/login` with a registered email and an incorrect password
- **THEN** the system returns HTTP 401 with an error response

### Requirement: Retrieval of the current user

The system SHALL return the data of the currently authenticated user identified by the bearer token.

#### Scenario: Authenticated user retrieves their own data
- **WHEN** a user sends `GET /api/auth/me` with a valid bearer token
- **THEN** the system returns HTTP 200 with the user data (id, email, firstName, lastName, role)

#### Scenario: Unauthenticated request to current-user endpoint
- **WHEN** a request without a valid bearer token is sent to `GET /api/auth/me`
- **THEN** the system denies access

### Requirement: JWT validation of protected requests

Protected endpoints SHALL require a valid bearer token in the `Authorization` header in the form `Bearer <token>`. The token SHALL be verified by signature and expiration; the authenticated user SHALL be resolved from the token's subject.

#### Scenario: Request with an expired or invalid token
- **WHEN** a request to a protected endpoint carries an expired or otherwise invalid bearer token
- **THEN** the system denies access

#### Scenario: Request with a token for a deleted user
- **WHEN** a request carries a valid token whose subject does not match any existing user
- **THEN** the system denies access

> **Note:** Токен удалённого пользователя не приводит к исключению в фильтре: запрос просто не аутентифицируется, и защищённый эндпоинт возвращает чистый отказ (HTTP 401 через `AuthenticationEntryPoint`).