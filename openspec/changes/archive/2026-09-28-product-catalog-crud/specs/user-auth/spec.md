# Spec Delta

## MODIFIED Requirements

### Requirement: Login and JWT issuance

The system SHALL authenticate a registered user by email and password and issue a bearer JWT token valid for a configured expiration period. The token SHALL be signed with RS256 using a private key held only by the auth-service, SHALL identify the issuing service in the `iss` claim, and SHALL carry the authenticated user's role in a `role` claim, so that other services can authorize the token holder without reading the auth database. The default token lifetime SHALL be 15 minutes. Requests with a password shorter than 6 or longer than 72 characters SHALL be rejected with HTTP 400; requests with an email longer than 255 characters SHALL be rejected with HTTP 400; email SHALL be normalized (whitespace trimmed, lowercased) before lookup.

#### Scenario: Successful login
- **WHEN** a user sends `POST /api/auth/login` with a registered email and the correct password
- **THEN** the system returns HTTP 200 with the issued JWT token, its type "Bearer", and the user data

#### Scenario: Issued token carries the user's role
- **WHEN** a user sends `POST /api/auth/login` with valid credentials
- **THEN** the issued token contains the authenticated user's role in a `role` claim

#### Scenario: Login with an email differing only in letter case
- **WHEN** a user sends `POST /api/auth/login` with a registered email typed in a different letter case
- **THEN** the system returns HTTP 200 with the issued JWT token, its type "Bearer", and the user data

#### Scenario: Login with a password shorter than 6 characters
- **WHEN** a user sends `POST /api/auth/login` with a password shorter than 6 characters
- **THEN** the system returns HTTP 400 with field-level validation details

#### Scenario: Login with a password longer than 72 characters
- **WHEN** a user sends `POST /api/auth/login` with a password longer than 72 characters
- **THEN** the system returns HTTP 400 with field-level validation details

#### Scenario: Login with an email longer than 255 characters
- **WHEN** a user sends `POST /api/auth/login` with an email longer than 255 characters
- **THEN** the system returns HTTP 400 with field-level validation details

#### Scenario: Login with an unknown email
- **WHEN** a user sends `POST /api/auth/login` with an email that is not registered
- **THEN** the system returns HTTP 404 with an error response

#### Scenario: Login with a wrong password
- **WHEN** a user sends `POST /api/auth/login` with a registered email and an incorrect password
- **THEN** the system returns HTTP 401 with an error response

### Requirement: JWT validation of protected requests

Protected endpoints SHALL require a valid bearer token in the `Authorization` header in the form `Bearer <token>`. The token SHALL be verified by signature and expiration, and a service other than the issuer SHALL reject a token whose `iss` claim does not match the expected issuer. auth-service SHALL resolve the authenticated user from the token's subject against its own user database. A service that owns a separate database, such as product-service, SHALL take the role from the token's `role` claim and SHALL NOT query any user database; a validly signed unexpired token is therefore accepted there until it expires, which means that a user deleted in the meantime keeps access in such a service for no longer than the remaining token lifetime. The signing private key SHALL NOT be distributed to verifying services: they SHALL obtain the public key from the auth-service JWKS endpoint and verify tokens locally.

#### Scenario: Request with an expired or invalid token
- **WHEN** a request to a protected endpoint carries an expired or otherwise invalid bearer token
- **THEN** the system denies access

#### Scenario: Request with a token signed by another key
- **WHEN** a request carries a bearer token whose signature does not match the issuer's public key
- **THEN** the system denies access

#### Scenario: Request with a token for a deleted user
- **WHEN** a request to an auth-service endpoint carries a valid token whose subject does not match any existing user
- **THEN** the system denies access

#### Scenario: Role is derived from the token
- **WHEN** product-service verifies a valid bearer token that carries a `role` claim
- **THEN** the system grants the authorities derived from that claim without querying any user database

> **Note:** В auth-service токен удалённого пользователя не приводит к исключению в фильтре: запрос просто не аутентифицируется, и защищённый эндпоинт возвращает чистый отказ (HTTP 401 через `AuthenticationEntryPoint`). В сервисах с отдельной базой (product-service) такой отзыв невозможен без обращения к auth-service: там действует только срок токена. Следующая задача вводит проверку отзыва через Redis.
