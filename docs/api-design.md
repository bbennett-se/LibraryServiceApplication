# API Design

## Overview

All services expose REST APIs over HTTP. JSON is the only supported media
type. Resource paths are plural nouns. Cross-service calls follow the same
conventions as external calls — there is no separate "internal" API.

Authentication will be added in Week 3 via Keycloak and an API Gateway. For
Week 2, all endpoints are unauthenticated.

## Conventions

- All timestamps are ISO-8601 with timezone (`2026-05-18T14:30:00Z`).
- IDs in URLs are UUIDs.
- Validation errors return `400` with a body listing each invalid field.
- Missing resources return `404`.
- Conflicts (e.g. checking out an unavailable copy) return `409`.
- Upstream service failures return `503` with a message identifying which
  dependency failed.

## Catalog Service — `http://localhost:8081`

*Existing from Week 1. Listed here for completeness.*

| Method | Path                              | Purpose                  |
|--------|-----------------------------------|--------------------------|
| POST   | `/api/catalog/items`              | Create an item           |
| GET    | `/api/catalog/items`              | List items               |
| GET    | `/api/catalog/items/{id}`         | Get an item              |
| PUT    | `/api/catalog/items/{id}`         | Update an item           |
| DELETE | `/api/catalog/items/{id}`         | Delete an item           |

### New in Week 2 — copy status endpoints

These are needed by Circulation to update copy availability on checkout/return.

#### `GET /api/catalog/copies/{id}`

Returns a single copy with its current status.

```json
{
  "id": "8c3f...",
  "itemId": "1a2b...",
  "barcode": "B0001",
  "status": "AVAILABLE",
  "acquiredAt": "2024-09-15T00:00:00Z"
}
```

#### `PATCH /api/catalog/copies/{id}/status`

Updates only the status field. Used by Circulation on checkout/return.

Request:
```json
{ "status": "CHECKED_OUT" }
```

Response: `200 OK` with the updated copy resource.

Returns `409` if the requested transition is invalid (e.g. checking out a
copy that is already `CHECKED_OUT`).

## User Service — `http://localhost:8083`

#### `POST /api/users`

Create a new user.

Request:
```json
{
  "email": "jane.doe@example.com",
  "firstName": "Jane",
  "lastName": "Doe",
  "role": "PATRON"
}
```

Response: `201 Created` with the full user resource. `status` defaults to
`ACTIVE`.

#### `GET /api/users/{id}`

Returns a single user.

```json
{
  "id": "f4e1...",
  "email": "jane.doe@example.com",
  "firstName": "Jane",
  "lastName": "Doe",
  "role": "PATRON",
  "status": "ACTIVE",
  "createdAt": "2026-05-18T10:00:00Z",
  "updatedAt": "2026-05-18T10:00:00Z"
}
```

#### `GET /api/users`

List users. Supports `?role=` and `?status=` query parameters.

#### `PUT /api/users/{id}`

Update name, email, or role. Status is updated via a separate endpoint.

#### `PATCH /api/users/{id}/status`

Update only the status. Used to suspend or reactivate a user.

Request:
```json
{ "status": "SUSPENDED" }
```

#### `DELETE /api/users/{id}`

Hard delete. In a real system this would be a soft delete; for the portfolio
project, hard delete is fine and simpler.

## Circulation Service — `http://localhost:8082`

#### `POST /api/circulation/checkout`

Check out a copy to a user.

Request:
```json
{
  "userId": "f4e1...",
  "itemCopyId": "8c3f..."
}
```

Validation flow:
1. Call `GET /api/users/{userId}` on User Service.
   - `404` → return `400 "User not found"`
   - User status `SUSPENDED` → return `409 "User is suspended"`
2. Call `GET /api/catalog/copies/{itemCopyId}` on Catalog Service.
   - `404` → return `400 "Copy not found"`
   - Status not `AVAILABLE` → return `409 "Copy is not available"`
3. Create `Loan` with `checkoutDate = now`, `dueDate = now + 14 days`,
   `status = ACTIVE`.
4. Call `PATCH /api/catalog/copies/{itemCopyId}/status` with
   `{"status": "CHECKED_OUT"}`.
   - On failure: roll back the Loan creation and return `503`.

Response: `201 Created`
```json
{
  "id": "9d2a...",
  "userId": "f4e1...",
  "itemCopyId": "8c3f...",
  "checkoutDate": "2026-05-18T14:30:00Z",
  "dueDate": "2026-06-01T14:30:00Z",
  "returnedDate": null,
  "status": "ACTIVE"
}
```

#### `POST /api/circulation/returns/{loanId}`

Mark a loan as returned.

Flow:
1. Fetch the Loan. If status is not `ACTIVE`, return `409`.
2. Set `returnedDate = now`, `status = RETURNED`.
3. Call `PATCH /api/catalog/copies/{itemCopyId}/status` with
   `{"status": "AVAILABLE"}`.
4. On Catalog failure: log a warning and return `200` anyway — the loan is
   closed; copy status will be reconciled by a background job (Week 4).

Response: `200 OK` with the updated loan.

#### `GET /api/circulation/loans/{id}`

Fetch a single loan.

#### `GET /api/circulation/loans`

List loans. Supports `?userId=`, `?status=`, and `?overdue=true` query
parameters.

#### `GET /api/circulation/users/{userId}/active-loans`

Convenience endpoint for "what does this patron currently have out?"

## Inter-Service Calls Summary

| Caller        | Callee   | When                              |
|---------------|----------|-----------------------------------|
| Circulation   | User     | On checkout — verify user is active |
| Circulation   | Catalog  | On checkout — verify copy is available |
| Circulation   | Catalog  | On checkout — mark copy as `CHECKED_OUT` |
| Circulation   | Catalog  | On return — mark copy as `AVAILABLE` |

## Error Response Format

All error responses follow the same shape:

```json
{
  "timestamp": "2026-05-18T14:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Copy is not available",
  "path": "/api/circulation/checkout"
}
```

For `400` validation errors, an additional `fieldErrors` array is included:

```json
{
  "timestamp": "2026-05-18T14:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/users",
  "fieldErrors": [
    { "field": "email", "message": "must be a valid email address" }
  ]
}
```

## Open Questions for Week 3+

- Authentication: how will the API Gateway propagate the user identity to
  downstream services? (Standard pattern: JWT forwarded as `Authorization`
  header; services validate signature only, trust gateway routing.)
- Idempotency: should checkout accept an idempotency key? Not needed yet but
  worth noting before Week 4.
- Service discovery: hardcoded URLs work for docker-compose; Kubernetes
  service DNS will replace them in Week 5.
