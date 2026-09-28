# Proposal

## Why

The product-service has no catalog at all: it starts, connects to `product_db` and exposes nothing. There are no tables for categories and products, so the frontend has nothing to render and order-service has nothing to reference. The task (#8) also defines per-endpoint roles (USER for reads, ADMIN for writes), and today no service other than auth-service can evaluate a role at all: the issued JWT carries only the user id, and auth-service resolves the role by querying its own `users` table. A microservice layout with one database per service gives product-service no way to learn the role, so the role has to travel inside the token.

## What Changes

- Create the `categories` and `products` tables with Flyway migrations that actually apply to `product_db` (the drafted migrations in the feature branch are named with a single underscore and both claim version 1, so Flyway skips them and `ddl-auto: validate` then aborts startup on a missing table).
- Complete the catalog API in product-service: `CategoryController` + `CategoryService` for CRUD on categories, `ProductController` + `ProductService` for CRUD on products, stock update, and category filtering of the product list.
- Add the `error` package: `ErrorResponse`, `GlobalExceptionHandler` and domain exceptions, so 404/409/400 responses match the format auth-service already returns.
- Add the `role` claim to the issued JWT in auth-service, and configure product-service as an OAuth2 resource server that verifies the token locally (signature and expiration) and maps `role` to `ROLE_*` authorities. No request-time call to auth-service and no user data in product-service.
- Sign tokens with RS256 and let verifying services obtain the public key from a JWKS endpoint, so product-service holds nothing that could be used to mint a token. The key pair is generated on first start of auth-service and stored outside git. The token lifetime drops from 24 hours to 15 minutes to bound how long a deleted user's token keeps working in product-service.
- Enforce access on every catalog endpoint with method security: `USER` and `ADMIN` may read, `ADMIN` alone may create, update, delete and change stock.
- Declare explicit `length` values in the entity columns to match the Flyway schema, replace `CascadeType.ALL` on `Category.products` with an explicit delete of the category's products, and align DTO validation with the column widths.
- Deleting a category removes the products assigned to it, atomically, with the client confirming the consequence before sending the request.
- Make the category name unique case-insensitively (`UNIQUE` on the lowercased name), consistent with the email uniqueness already specced for `user-auth`.

## Capabilities

### New Capabilities

- `product-management`: category and product catalog in product-service — CRUD for both entities, category-filtered product listing, stock update, field validation limits, role-based access, and the HTTP status contract for every endpoint.

### Modified Capabilities

- `user-auth`: the issued JWT SHALL carry the user's role so that other services can authorize a request without reading the auth database; it SHALL be signed with RS256, SHALL name its issuer, and SHALL expire after 15 minutes.

## Impact

- `backend/product-service/src/main/java/com/shop/product/entity/{Category,Product}.java` — explicit `length`, `CascadeType.ALL` removed, relationships kept lazy.
- `backend/product-service/src/main/java/com/shop/product/dto/*` — `categoryId` rename, `createdAt`/`updatedAt` added to responses, `@Size`/`@Digits` limits, new `StockQuantityRequest` and `ErrorResponse`.
- `backend/product-service/src/main/java/com/shop/product/repository/*` — query methods aligned with the actual uniqueness of the columns.
- `backend/product-service/src/main/java/com/shop/product/controller/*`, new `service/*`, new `exception/*`, new `config/SecurityConfig.java` — the API itself.
- `backend/product-service/src/main/resources/db/migration/*` — renamed to `V1__create_categories_table.sql` and `V2__create_products_table.sql`, unique index on the lowercased category name added.
- `backend/product-service/pom.xml`, `src/main/resources/application.yml` — `spring-boot-starter-oauth2-resource-server` and `issuer-uri` / `jwk-set-uri` in place of the shared secret.
- `backend/product-service/src/main/java/com/shop/product/config/SecurityConfig.java` — no custom decoder any more, only the claim-to-authority mapping.
- `backend/auth-service/src/main/java/com/shop/auth/security/JwtTokenProvider.java` — `role` and `iss` claims added to the token, signing moved from jjwt/HS256 to `NimbusJwtEncoder`/RS256.
- `backend/auth-service/src/main/java/com/shop/auth/config/JwtKeyConfig.java` (new), `controller/JwksController.java` (new) — the key pair and its public JWKS endpoint.
- `backend/auth-service/pom.xml` — jjwt replaced by `spring-boot-starter-oauth2-resource-server`.
- `backend/auth-service/src/main/java/com/shop/auth/config/SecurityConfig.java` — JWKS endpoint opened without a token.
- `.gitignore` — the generated key directory is excluded.
- `openspec/specs/product-management/spec.md` — new spec, created on archive.
- `openspec/specs/user-auth/spec.md` — updated via the delta in this change.
