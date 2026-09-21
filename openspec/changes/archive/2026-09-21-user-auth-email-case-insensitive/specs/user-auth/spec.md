# Spec Delta

## Purpose

Handles user authentication in the auth-service: registration of new users, login with JWT issuance, and retrieval of the currently authenticated user.

## ADDED Requirements

### Requirement: Case-insensitive email handling

The system SHALL treat email addresses as case-insensitive: email SHALL be normalized to lowercase on registration and login, and uniqueness SHALL be enforced regardless of letter case. The stored and returned email is the normalized (lowercase) form.

#### Scenario: Registration with an email differing only in letter case
- **WHEN** a guest sends `POST /api/auth/register` with an email that matches an already registered address, differing only in letter case (e.g. `Foo@x.com` vs `foo@x.com`)
- **THEN** the system returns HTTP 409 with an error response

#### Scenario: Login with an email differing only in letter case
- **WHEN** a user sends `POST /api/auth/login` with a registered email typed in a different letter case
- **THEN** the system returns HTTP 200 with the issued JWT token, its type "Bearer", and the user data