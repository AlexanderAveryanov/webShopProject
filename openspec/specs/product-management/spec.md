# product-management Specification

## Purpose

Manages the clothing catalog in the product-service: categories and products, their creation, listing, retrieval, update and deletion, category-filtered product listing and stock updates, all behind role-based access control.

## Requirements

### Requirement: Role-based access to the catalog

Every catalog endpoint SHALL require a valid bearer token. Read operations (listing and retrieval) SHALL be available to a requester with role USER or ADMIN; create, update, delete and stock operations SHALL be restricted to a requester with role ADMIN. Requests without a valid token SHALL be denied with HTTP 401; requests with a valid token whose role is not permitted for the operation SHALL be denied with HTTP 403. The catalog SHALL NOT be readable without authentication.

#### Scenario: USER reads the catalog
- **WHEN** a user with role USER sends `GET /api/categories` or `GET /api/products` with a valid bearer token
- **THEN** the system returns HTTP 200 with the requested collection

#### Scenario: USER attempts to create a product
- **WHEN** a user with role USER sends `POST /api/products` with a valid bearer token and a valid body
- **THEN** the system returns HTTP 403 and creates nothing

#### Scenario: ADMIN writes to the catalog
- **WHEN** a user with role ADMIN sends `POST /api/categories`, `PUT`, `DELETE` or `PATCH` with a valid bearer token
- **THEN** the system performs the operation and returns the corresponding success status

#### Scenario: Unauthenticated catalog request
- **WHEN** a request without a valid bearer token is sent to any catalog endpoint
- **THEN** the system returns HTTP 401

#### Scenario: Request with an invalid or expired token
- **WHEN** a request carries an invalid or expired bearer token
- **THEN** the system returns HTTP 401

### Requirement: Listing categories

The system SHALL return all categories for a permitted requester. Each returned category SHALL contain its id, name, description, creation timestamp and last update timestamp.

#### Scenario: Listing all categories
- **WHEN** a permitted requester sends `GET /api/categories` with a valid bearer token
- **THEN** the system returns HTTP 200 with a list of all categories (id, name, description, createdAt, updatedAt)

#### Scenario: Listing categories on an empty catalog
- **WHEN** a permitted requester sends `GET /api/categories` and no category exists
- **THEN** the system returns HTTP 200 with an empty list

### Requirement: Retrieval of a category by id

The system SHALL return a single category by its id for a permitted requester, including its id, name, description, creation timestamp and last update timestamp.

#### Scenario: Retrieving an existing category
- **WHEN** a permitted requester sends `GET /api/categories/{id}` with the id of an existing category
- **THEN** the system returns HTTP 200 with the category data (id, name, description, createdAt, updatedAt)

#### Scenario: Retrieving a category with an unknown id
- **WHEN** a permitted requester sends `GET /api/categories/{id}` with an id that does not exist
- **THEN** the system returns HTTP 404 with an error response

#### Scenario: Retrieving a category with a non-numeric id
- **WHEN** a requester sends `GET /api/categories/{id}` with a non-numeric id
- **THEN** the system returns HTTP 400 with an error response

### Requirement: Creating a category

The system SHALL allow a requester with role ADMIN to create a category from a name and an optional description. The name SHALL be required and SHALL NOT be blank; it SHALL be at most 100 characters long. The name SHALL be unique across categories, compared case-insensitively: a name that differs from an existing category name only in letter case SHALL be rejected. On success the system SHALL return HTTP 201 with the created category, including its id, name, description, creation timestamp and last update timestamp.

#### Scenario: ADMIN creates a category
- **WHEN** a user with role ADMIN sends `POST /api/categories` with a name of at most 100 characters and an optional description
- **THEN** the system creates the category and returns HTTP 201 with the created category data (id, name, description, createdAt, updatedAt)

#### Scenario: Creating a category without a name
- **WHEN** a user with role ADMIN sends `POST /api/categories` with a missing or blank name
- **THEN** the system returns HTTP 400 with field-level validation details

#### Scenario: Creating a category with a name longer than 100 characters
- **WHEN** a user with role ADMIN sends `POST /api/categories` with a name longer than 100 characters
- **THEN** the system returns HTTP 400 with field-level validation details

#### Scenario: Creating a category with an already used name
- **WHEN** a user with role ADMIN sends `POST /api/categories` with a name that already exists for another category
- **THEN** the system returns HTTP 409 with an error response and creates nothing

#### Scenario: Creating a category with a name differing only in letter case
- **WHEN** a user with role ADMIN sends `POST /api/categories` with a name that matches an existing category name, differing only in letter case
- **THEN** the system returns HTTP 409 with an error response and creates nothing

### Requirement: Updating a category

The system SHALL allow a requester with role ADMIN to replace the name and description of an existing category identified by its id. The name SHALL be required, SHALL NOT be blank, SHALL be at most 100 characters long and SHALL remain unique across categories, compared case-insensitively. On success the system SHALL return HTTP 200 with the updated category, including an updated last update timestamp.

#### Scenario: ADMIN updates a category
- **WHEN** a user with role ADMIN sends `PUT /api/categories/{id}` with a valid name and description for an existing category
- **THEN** the system updates the category and returns HTTP 200 with the updated category data and a last update timestamp later than the creation timestamp

#### Scenario: Updating a category with an unknown id
- **WHEN** a user with role ADMIN sends `PUT /api/categories/{id}` with an id that does not exist
- **THEN** the system returns HTTP 404 with an error response

#### Scenario: Updating a category with invalid field values
- **WHEN** a user with role ADMIN sends `PUT /api/categories/{id}` with a blank name or a name longer than 100 characters
- **THEN** the system returns HTTP 400 with field-level validation details and changes nothing

#### Scenario: Updating a category to a name already used by another category
- **WHEN** a user with role ADMIN sends `PUT /api/categories/{id}` with a name already used by a different category, including a name differing only in letter case
- **THEN** the system returns HTTP 409 with an error response and changes nothing

#### Scenario: Updating a category keeping its own name
- **WHEN** a user with role ADMIN sends `PUT /api/categories/{id}` with the same name the category already has, in the same or different letter case
- **THEN** the system updates the remaining fields and returns HTTP 200

### Requirement: Deleting a category

The system SHALL allow a requester with role ADMIN to delete an existing category. Deleting a category SHALL also delete every product assigned to it, in a single atomic operation: either both the category and its products are removed, or nothing is changed. The consequence that products are removed with the category SHALL be confirmed by the client before it sends the request; the system SHALL NOT require a separate confirmation parameter. On success the system SHALL return HTTP 204 with no body.

#### Scenario: ADMIN deletes an empty category
- **WHEN** a user with role ADMIN sends `DELETE /api/categories/{id}` for a category that contains no products
- **THEN** the system deletes the category and returns HTTP 204 with no body

#### Scenario: Deleting a category that still contains products
- **WHEN** a user with role ADMIN sends `DELETE /api/categories/{id}` for a category that has at least one product, after the client has confirmed the deletion of those products
- **THEN** the system deletes the category together with all of its products and returns HTTP 204 with no body, and a later `GET /api/products` no longer returns those products

#### Scenario: Deleting a category with an unknown id
- **WHEN** a user with role ADMIN sends `DELETE /api/categories/{id}` with an id that does not exist
- **THEN** the system returns HTTP 404 with an error response and deletes nothing

### Requirement: Listing products with an optional category filter

The system SHALL return all products for a permitted requester, each including its id, name, description, price, category id, image URL, stock quantity, creation timestamp and last update timestamp. When a category id is supplied as a query parameter, the system SHALL return only the products assigned to that category. A category id that matches no category SHALL yield an empty list rather than an error.

#### Scenario: Listing all products
- **WHEN** a permitted requester sends `GET /api/products` without a category filter
- **THEN** the system returns HTTP 200 with a list of all products (id, name, description, price, categoryId, imageUrl, stockQuantity, createdAt, updatedAt)

#### Scenario: Listing products of one category
- **WHEN** a permitted requester sends `GET /api/products?categoryId={id}` with the id of an existing category
- **THEN** the system returns HTTP 200 with only the products assigned to that category

#### Scenario: Listing products of an unknown category
- **WHEN** a permitted requester sends `GET /api/products?categoryId={id}` with an id that matches no category
- **THEN** the system returns HTTP 200 with an empty list

#### Scenario: Listing products on an empty catalog
- **WHEN** a permitted requester sends `GET /api/products` and no product exists
- **THEN** the system returns HTTP 200 with an empty list

### Requirement: Retrieval of a product by id

The system SHALL return a single product by its id for a permitted requester, including its id, name, description, price, category id, image URL, stock quantity, creation timestamp and last update timestamp.

#### Scenario: Retrieving an existing product
- **WHEN** a permitted requester sends `GET /api/products/{id}` with the id of an existing product
- **THEN** the system returns HTTP 200 with the product data (id, name, description, price, categoryId, imageUrl, stockQuantity, createdAt, updatedAt)

#### Scenario: Retrieving a product with an unknown id
- **WHEN** a permitted requester sends `GET /api/products/{id}` with an id that does not exist
- **THEN** the system returns HTTP 404 with an error response

### Requirement: Creating a product

The system SHALL allow a requester with role ADMIN to create a product from a name, an optional description, a price, a category id, an optional image URL and an optional stock quantity. The name SHALL be required, SHALL NOT be blank and SHALL be at most 255 characters long. The price SHALL be required, SHALL be greater than zero, SHALL have at most 8 digits before the decimal point and at most 2 digits after it. The category id SHALL be required and SHALL reference an existing category. The image URL SHALL be at most 500 characters long. The stock quantity SHALL be a non-negative integer and SHALL default to 0 when omitted. On success the system SHALL return HTTP 201 with the created product.

#### Scenario: ADMIN creates a product
- **WHEN** a user with role ADMIN sends `POST /api/products` with a name of at most 255 characters, a positive price of at most 2 fraction digits, the id of an existing category and an optional stock quantity
- **THEN** the system creates the product and returns HTTP 201 with the created product data

#### Scenario: Creating a product without a stock quantity
- **WHEN** a user with role ADMIN sends `POST /api/products` without a stock quantity
- **THEN** the system creates the product with stock quantity 0 and returns HTTP 201

#### Scenario: Creating a product with invalid field values
- **WHEN** a user with role ADMIN sends `POST /api/products` with a blank name, a name longer than 255 characters, a missing or non-positive price, a price with more than 2 fraction digits, a missing category id, a negative stock quantity or an image URL longer than 500 characters
- **THEN** the system returns HTTP 400 with field-level validation details and creates nothing

#### Scenario: Creating a product in an unknown category
- **WHEN** a user with role ADMIN sends `POST /api/products` with a category id that matches no existing category
- **THEN** the system returns HTTP 404 with an error response and creates nothing

### Requirement: Updating a product

The system SHALL allow a requester with role ADMIN to replace the name, description, price, category id, image URL and stock quantity of an existing product identified by its id. The same field constraints as for creation SHALL apply. On success the system SHALL return HTTP 200 with the updated product, including an updated last update timestamp.

#### Scenario: ADMIN updates a product
- **WHEN** a user with role ADMIN sends `PUT /api/products/{id}` with valid values for an existing product
- **THEN** the system updates the product and returns HTTP 200 with the updated product data and a last update timestamp later than the creation timestamp

#### Scenario: Updating a product with an unknown id
- **WHEN** a user with role ADMIN sends `PUT /api/products/{id}` with an id that does not exist
- **THEN** the system returns HTTP 404 with an error response

#### Scenario: Updating a product with invalid field values
- **WHEN** a user with role ADMIN sends `PUT /api/products/{id}` with a blank name, a name longer than 255 characters, a non-positive price, a negative stock quantity or an image URL longer than 500 characters
- **THEN** the system returns HTTP 400 with field-level validation details and changes nothing

#### Scenario: Moving a product to an unknown category
- **WHEN** a user with role ADMIN sends `PUT /api/products/{id}` with a category id that matches no existing category
- **THEN** the system returns HTTP 404 with an error response and changes nothing

### Requirement: Updating the stock quantity of a product

The system SHALL allow a requester with role ADMIN to set the stock quantity of an existing product via `PATCH /api/products/{id}/stock`. The request body SHALL carry the resulting stock quantity as a non-negative integer, which SHALL replace the current value rather than adjust it by a delta. A missing or negative stock quantity SHALL be rejected with HTTP 400. On success the system SHALL return HTTP 200 with the full product data, including the new stock quantity and an updated last update timestamp.

#### Scenario: ADMIN sets a new stock quantity
- **WHEN** a user with role ADMIN sends `PATCH /api/products/{id}/stock` with a non-negative stock quantity for an existing product
- **THEN** the system replaces the stored stock quantity with the submitted value and returns HTTP 200 with the full product data

#### Scenario: Setting a stock quantity to zero
- **WHEN** a user with role ADMIN sends `PATCH /api/products/{id}/stock` with a stock quantity of 0
- **THEN** the system stores 0 and returns HTTP 200 with the full product data

#### Scenario: Setting a negative stock quantity
- **WHEN** a user with role ADMIN sends `PATCH /api/products/{id}/stock` with a negative stock quantity or without the field
- **THEN** the system returns HTTP 400 with field-level validation details and changes nothing

#### Scenario: Setting the stock quantity of an unknown product
- **WHEN** a user with role ADMIN sends `PATCH /api/products/{id}/stock` with an id that does not exist
- **THEN** the system returns HTTP 404 with an error response and changes nothing

### Requirement: Deleting a product

The system SHALL allow a requester with role ADMIN to delete an existing product. On success the system SHALL return HTTP 204 with no body; the category the product belonged to SHALL be kept and SHALL become empty.

#### Scenario: ADMIN deletes a product
- **WHEN** a user with role ADMIN sends `DELETE /api/products/{id}` for an existing product
- **THEN** the system deletes the product and returns HTTP 204 with no body

#### Scenario: Deleting a product with an unknown id
- **WHEN** a user with role ADMIN sends `DELETE /api/products/{id}` with an id that does not exist
- **THEN** the system returns HTTP 404 with an error response

### Requirement: Error response format

Every unsuccessful catalog request SHALL return a JSON error body containing a timestamp, the numeric status, the status reason phrase, a human-readable message and, for field validation failures, a map of field names to validation messages. An unexpected server-side failure SHALL be logged and reported as HTTP 500 with a generic message that SHALL NOT expose internal exception details or database information.

#### Scenario: Validation failure response body
- **WHEN** a catalog request fails bean validation
- **THEN** the system returns HTTP 400 with an error body that maps each rejected field to its message

#### Scenario: Domain failure response body
- **WHEN** a catalog request fails because an entity does not exist or a name is already used
- **THEN** the system returns the corresponding error status with an error body containing the reason

#### Scenario: Unexpected failure response body
- **WHEN** a catalog request fails with an unexpected error
- **THEN** the system logs the error and returns HTTP 500 with a generic error body that contains no internal details

### Requirement: Paginated catalog listing

Catalog listing endpoints SHALL support pagination through the `page` and `size` query parameters, where `page` is zero-based and defaults to 0, and `size` defaults to 20 and SHALL NOT exceed 100. A request without pagination parameters SHALL return the complete collection. This requirement is delivered by a follow-up change: until then the listing endpoints SHALL behave as unpaginated and SHALL return the complete collection.

#### Scenario: Listing a page of products
- **WHEN** a permitted requester sends `GET /api/products?page=0&size=20`
- **THEN** the system returns the first page of at most 20 products together with the total number of matching products and the total number of pages

#### Scenario: Listing products with a page size above the maximum
- **WHEN** a permitted requester sends `GET /api/categories?size=101`
- **THEN** the system returns HTTP 400 with field-level validation details

#### Scenario: Listing a page beyond the last page
- **WHEN** a permitted requester sends `GET /api/products?page=100&size=20` on a smaller catalog
- **THEN** the system returns an empty page together with the total number of matching products
