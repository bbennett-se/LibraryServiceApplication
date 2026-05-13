# Library Management System

A microservices-based library management system demonstrating Spring Boot,
Docker, Kubernetes, and OAuth2 authentication patterns.

## Architecture

- **Catalog Service** (port 8081) - Manages books and physical copies
- **Circulation Service** (port 8082) - Handles checkouts and returns *(coming soon)*
- **User Service** (port 8083) - Patron and staff management *(coming soon)*
- **Notification Service** (port 8084) - Async notifications *(coming soon)*
- **API Gateway** (port 8080) - JWT validation and routing *(coming soon)*

## Tech Stack

- Java 21 / Spring Boot 3.3
- PostgreSQL 16
- Docker & Docker Compose
- Kubernetes *(coming soon)*
- Keycloak for OAuth2/OIDC *(coming soon)*

## Local Development

### Prerequisites
- Java 21
- Maven 3.9+
- Docker Desktop

### Running

1. Start infrastructure: