# Spec Delta

## MODIFIED Requirements

### Requirement: Registration of a new user

The system SHALL allow a guest to register with email and password and create a user account with role USER. Email SHALL be unique, in a valid format and normalized (leading/trailing whitespace trimmed, lowercased); password SHALL be between 6 and 72 characters long. The password SHALL NOT be returned in any response.

#### Scenario: Successful registration
- **WHEN** a guest sends `POST /api/auth/register` with a valid unique email and a password of at least 6 characters
- **THEN** the system creates a user with role USER and returns HTTP 201 with the user data (id, email, firstName, lastName, role) and no password

#### Scenario: Registration with an existing email
- **WHEN** a guest sends `POST /api/auth/register` with an email that is already registered
- **THEN** the system returns HTTP 409 with an error response

#### Scenario: Registration with an email differing only in letter case
- **WHEN** a guest sends `POST /api/auth/register` with an email that matches an already registered address, differing only in letter case (e.g. `Foo@x.com` vs `foo@x.com`)
- **THEN** the system returns HTTP 409 with an error response

#### Scenario: Registration with an email containing leading or trailing whitespace
- **WHEN** a guest sends `POST /api/auth/register` with an email that has leading or trailing whitespace
- **THEN** the system returns HTTP 201 and stores and returns the email normalized (whitespace trimmed and lowercased)

#### Scenario: Registration with an invalid email format
- **WHEN** a guest sends `POST /api/auth/register` with an email that does not match a valid email format
- **THEN** the system returns HTTP 400 with field-level validation details

#### Scenario: Registration with a too-short password
- **WHEN** a guest sends `POST /api/auth/register` with a password shorter than 6 characters
- **THEN** the system returns HTTP 400 with field-level validation details

#### Scenario: Registration with a password longer than 72 characters
- **WHEN** a guest sends `POST /api/auth/register` with a password longer than 72 characters
- **THEN** the system returns HTTP 400 with field-level validation details

### Requirement: Login and JWT issuance

The system SHALL authenticate a registered user by email and password and issue a bearer JWT token valid for a configured expiration period. Requests with a password shorter than 6 or longer than 72 characters SHALL be rejected with HTTP 400; email SHALL be normalized (whitespace trimmed, lowercased) before lookup.

#### Scenario: Successful login
- **WHEN** a user sends `POST /api/auth/login` with a registered email and the correct password
- **THEN** the system returns HTTP 200 with the issued JWT token, its type "Bearer", and the user data

#### Scenario: Login with an email differing only in letter case
- **WHEN** a user sends `POST /api/auth/login` with a registered email typed in a different letter case
- **THEN** the system returns HTTP 200 with the issued JWT token, its type "Bearer", and the user data

#### Scenario: Login with a password shorter than 6 characters
- **WHEN** a user sends `POST /api/auth/login` with a password shorter than 6 characters
- **THEN** the system returns HTTP 400 with field-level validation details

#### Scenario: Login with a password longer than 72 characters
- **WHEN** a user sends `POST /api/auth/login` with a password longer than 72 characters
- **THEN** the system returns HTTP 400 with field-level validation details

#### Scenario: Login with an unknown email
- **WHEN** a user sends `POST /api/auth/login` with an email that is not registered
- **THEN** the system returns HTTP 404 with an error response

#### Scenario: Login with a wrong password
- **WHEN** a user sends `POST /api/auth/login` with a registered email and an incorrect password
- **THEN** the system returns HTTP 401 with an error response