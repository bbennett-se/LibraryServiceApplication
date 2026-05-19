# Data Model

## Overview

The Library Management System is split into independently deployable services,
each owning its own database schema. No service reads or writes to another
service's database directly. All cross-service data access happens through
REST APIs.

This document defines the entities owned by each service and the rationale for
key design decisions.

## Service Data Ownership

| Service              | Database        | Owns                                  |
|----------------------|-----------------|---------------------------------------|
| Catalog Service      | `catalog_db`    | `Item`, `ItemCopy`                    |
| User Service         | `user_db`       | `User`                                |
| Circulation Service  | `circulation_db`| `Loan`                                |
| Notification Service | `notification_db` *(Week 4)* | `Notification` *(Week 4)* |

All databases run inside the same PostgreSQL container during local
development for simplicity. In production each service would typically have
its own database instance.

## Entities

### Catalog Service

#### `Item`
Represents a bibliographic record — the abstract concept of a book.

| Field         | Type         | Constraints                  |
|---------------|--------------|------------------------------|
| `id`          | UUID         | Primary key                  |
| `isbn`        | String(20)   | Not null, indexed            |
| `title`       | String(255)  | Not null                     |
| `author`      | String(255)  | Not null                     |
| `genre`       | String(100)  | Nullable                     |
| `publishedYear` | Integer    | Nullable                     |
| `createdAt`   | Timestamp    | Not null, auto-set           |
| `updatedAt`   | Timestamp    | Not null, auto-set           |

#### `ItemCopy`
Represents a physical copy of an Item. A library may own multiple copies of
the same book.

| Field         | Type         | Constraints                                  |
|---------------|--------------|----------------------------------------------|
| `id`          | UUID         | Primary key                                  |
| `itemId`      | UUID         | Foreign key → `Item.id`, not null            |
| `barcode`     | String(50)   | Not null, unique                             |
| `status`      | Enum         | `AVAILABLE`, `CHECKED_OUT`, `LOST`, `DAMAGED`|
| `acquiredAt`  | Timestamp    | Not null                                     |

### User Service

#### `User`
Represents a library patron or staff member.

| Field         | Type         | Constraints                                  |
|---------------|--------------|----------------------------------------------|
| `id`          | UUID         | Primary key                                  |
| `email`       | String(255)  | Not null, unique, valid email format         |
| `firstName`   | String(100)  | Not null                                     |
| `lastName`    | String(100)  | Not null                                     |
| `role`        | Enum         | `PATRON`, `STAFF`                            |
| `status`      | Enum         | `ACTIVE`, `SUSPENDED`                        |
| `createdAt`   | Timestamp    | Not null, auto-set                           |
| `updatedAt`   | Timestamp    | Not null, auto-set                           |

### Circulation Service

#### `Loan`
Represents a checkout event — links a user to a physical copy for a period.

| Field          | Type         | Constraints                                   |
|----------------|--------------|-----------------------------------------------|
| `id`           | UUID         | Primary key                                   |
| `userId`       | UUID         | Not null, indexed *(reference, not FK)*       |
| `itemCopyId`   | UUID         | Not null, indexed *(reference, not FK)*       |
| `checkoutDate` | Timestamp    | Not null, auto-set                            |
| `dueDate`      | Timestamp    | Not null                                      |
| `returnedDate` | Timestamp    | Nullable                                      |
| `status`       | Enum         | `ACTIVE`, `RETURNED`, `OVERDUE`               |

## Design Decisions

### ADR-001: Each service owns its own database

**Decision:** Circulation stores `userId` and `itemCopyId` as plain UUID
columns rather than foreign keys into the User or Catalog databases.

**Rationale:**
- Foreign keys across databases are impossible in PostgreSQL — services run
  in separate logical (and eventually physical) databases.
- This enforces the microservices boundary at the data layer. Circulation
  cannot accidentally `JOIN` against the User table because it has no access.
- Cross-service validation happens through REST calls at the service layer,
  which makes failure modes explicit and forces graceful degradation.

**Trade-off:** Referential integrity is enforced in application code rather
than the database. This is intentional — the alternative (shared database)
would defeat the purpose of separating the services.

### ADR-002: Loan status is denormalized from ItemCopy.status

**Decision:** When a loan is created, the Circulation Service updates
`ItemCopy.status` to `CHECKED_OUT` via a REST call to the Catalog Service.
On return, it sets the status back to `AVAILABLE`.

**Rationale:**
- `ItemCopy.status` is queried frequently to determine availability before
  checkout. Computing it dynamically by querying Circulation on every catalog
  read would create tight coupling.
- The Catalog Service remains the source of truth for *physical* state of
  copies; Circulation owns the *transactional* record of loans.

**Trade-off:** Two services hold related state that must stay consistent. If
the REST call to Catalog fails after the Loan is created, the data drifts.
Week 4 will address this with event-driven updates via RabbitMQ.

### ADR-003: UUID primary keys

**Decision:** All entity IDs are UUIDs, not auto-increment integers.

**Rationale:**
- IDs are generated by the service that owns the entity, no central
  coordination needed.
- IDs are safe to expose in URLs and pass between services without leaking
  information about row counts.
- Easier to merge data across environments for testing.

**Trade-off:** Slightly larger storage and index size. Negligible at this
scale.

## Schema Evolution

Hibernate's `ddl-auto: update` is fine for week 2 development. Before week 5
(Kubernetes), this will be replaced with Flyway migrations to make schema
changes reproducible and version-controlled.
