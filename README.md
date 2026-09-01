# Product Management RESTful API Service
**Technical Evaluation Assignment — Prepared for Zest India IT Pvt Ltd**

---

## 📌 Executive Summary

This repository contains a **production-ready, enterprise-grade RESTful API** for managing Products and inventory Items built with **Java 17**, **Spring Boot 3.3.x**, **Spring Data JPA**, **PostgreSQL**, and **Spring Security (Stateless JWT with Refresh Token Rotation)**.

The architecture emphasizes **Clean Architecture**, **SOLID principles**, strict **resource-oriented REST conventions**, comprehensive **auditing**, **asynchronous event processing**, **robust automated testing**, and **Docker containerization**.

---

## 🛠️ Technology Stack

| Component | Technology | Description |
|---|---|---|
| **Language** | Java 17 / 21+ | Modern LTS Java runtime features |
| **Framework** | Spring Boot 3.3.5 | Core dependency injection & web framework |
| **Persistence** | Spring Data JPA / Hibernate 6 | Object-relational mapping and auditing |
| **Databases** | PostgreSQL 16 & H2 | PostgreSQL for Dev/Prod/Docker, In-Memory H2 for Tests |
| **Security** | Spring Security 6 & JJWT 0.12.6 | Stateless JWT authentication + Refresh Token Rotation |
| **Validation** | Jakarta Bean Validation | Declarative request payload validation |
| **API Docs** | SpringDoc OpenAPI 3 / Swagger UI | Interactive API documentation |
| **Testing** | JUnit 5, Mockito, AssertJ, MockMvc | Comprehensive unit and integration test coverage |
| **DevOps** | Docker, Multi-Stage Dockerfile, Docker Compose | Production containerization & orchestration |

---

## 🏛️ Architecture & Design Patterns

The codebase is organized following **Clean Layered Architecture**:

```
com.zestindia.productservice
├── config/         # Security, OpenAPI, JPA Auditing, Async, CORS configuration
├── controller/     # Versioned REST Controllers (/api/v1/...)
├── dto/            # Request and Response transfer models (Encapsulation)
│   ├── request/    # Strongly validated request payloads
│   └── response/   # Uniform API envelopes (ApiResponse<T>, PagedResponse<T>, ErrorResponse)
├── entity/         # JPA Entities and Auditing Superclasses
├── event/          # Decoupled domain events & asynchronous event listeners
├── exception/      # Domain-specific exceptions & Global Exception Handler
├── repository/     # Spring Data JPA interfaces with custom queries
├── security/       # JWT token provider, security filter, authentication entrypoint
├── service/        # Business logic interface contracts
│   └── impl/       # Concrete transactional implementations
└── util/           # Shared application constants
```

### Key Design Highlights:
1. **Stateless JWT with Refresh Token Rotation**:
   - Access tokens have a short lifespan (1 hour).
   - Refresh tokens are securely stored in the database.
   - When a refresh token is exchanged, a **new** refresh token is issued and the old one is rotated/invalidated, guarding against replay attacks.
2. **Spring Data JPA Auditing**:
   - `created_by`, `created_on`, `modified_by`, `modified_on` are automatically populated using `@EnableJpaAuditing` and `AuditorAware<String>` linked to the `SecurityContextHolder`.
3. **Standard Response Envelope**:
   - Every API returns a consistent structure: `{ success, message, data, timestamp }`.
   - Collections return `{ content, pageNo, pageSize, totalElements, totalPages, last }`.
   - Errors return RFC-7807 inspired `{ timestamp, status, error, message, path, validationErrors }`.
4. **Asynchronous Processing**:
   - Activity events (`ProductActivityEvent`) are fired and handled asynchronously via a configured `ThreadPoolTaskExecutor` without blocking HTTP response threads.
5. **Database Indexing Strategy**:
   - Indexes on frequently queried columns: `product(product_name)`, `product(created_on)`, `item(product_id)`, `refresh_token(token)`.

---

## 🗄️ Database Schema

### `product` Table
```sql
CREATE TABLE product (
    id SERIAL PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP NOT NULL,
    modified_by VARCHAR(100),
    modified_on TIMESTAMP
);
CREATE INDEX idx_product_name ON product(product_name);
CREATE INDEX idx_product_created_on ON product(created_on);
```

### `item` Table
```sql
CREATE TABLE item (
    id SERIAL PRIMARY KEY,
    product_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity >= 0),
    CONSTRAINT fk_item_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);
CREATE INDEX idx_item_product_id ON item(product_id);
```

---

## 🚀 Getting Started & Setup Guide

### Prerequisites
- **JDK 17 or higher**
- **Apache Maven 3.8+**
- **Docker & Docker Compose** (Optional for containerized run)

---

### Option 1: Run with Docker Compose (Recommended)

To spin up both PostgreSQL and the Spring Boot application with a single command:

```bash
docker compose up --build -d
```

- **Application URL**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

To shut down:
```bash
docker compose down
```

---

### Option 2: Run Locally with Maven

1. **Start PostgreSQL database** (or configure local PostgreSQL with DB `product_db`):
   ```bash
   docker run --name local-postgres -e POSTGRES_DB=product_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:16-alpine
   ```

2. **Build and Run**:
   ```bash
   mvn clean spring-boot:run
   ```

---

## 🧪 Running Automated Tests

The project comes with a comprehensive suite of **Unit Tests (Mockito)** and **Integration Tests (MockMvc + In-Memory H2)**:

```bash
mvn clean test
```

### Test Coverage Highlights:
- `ProductServiceTest`: Unit testing service CRUD, pagination mapping, exception handling.
- `AuthServiceTest`: Unit testing user registration, password hashing, JWT creation, token rotation.
- `ProductControllerTest`: Integration testing all Product endpoints with MockMvc, auditing assertions, and role authorization checks.
- `AuthControllerTest`: Integration testing login, registration, and invalid credentials handling.

---

## 🔐 Default Credentials (Pre-seeded)

For convenience in evaluation, default users are automatically initialized on startup:

| Role | Username | Password | Email |
|---|---|---|---|
| **ADMIN** | `admin` | `Admin@123` | `admin@zestindia.com` |
| **USER** | `user` | `User@123` | `user@zestindia.com` |

---

## 📖 API Documentation & Endpoints

### 1. Authentication Endpoints (`/api/v1/auth`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Public | Register a new user |
| `POST` | `/api/v1/auth/login` | Public | Authenticate user & get Access + Refresh tokens |
| `POST` | `/api/v1/auth/refresh-token` | Public | Rotate refresh token and get a new access token |
| `POST` | `/api/v1/auth/logout` | Public | Revoke active refresh token |

#### Example: Login Request (`POST /api/v1/auth/login`)
```json
{
  "usernameOrEmail": "admin",
  "password": "Admin@123"
}
```

#### Example: Login Response
```json
{
  "success": true,
  "message": "Authentication successful!",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "48dfaa07-cf68-45be-a6b1-09411dd5d5fa",
    "tokenType": "Bearer",
    "id": 1,
    "username": "admin",
    "email": "admin@zestindia.com",
    "roles": ["ROLE_ADMIN", "ROLE_USER"]
  },
  "timestamp": "2026-09-01T12:00:00.000"
}
```

---

### 2. Product Endpoints (`/api/v1/products`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/v1/products` | Public | Paginated list of products (`?page=0&size=10&sortBy=id&sortDir=asc&search=laptop`) |
| `GET` | `/api/v1/products/{id}` | Public | Get product details by ID (including items) |
| `POST` | `/api/v1/products` | USER, ADMIN | Create a new product (Auditing fields set automatically) |
| `PUT` | `/api/v1/products/{id}` | USER, ADMIN | Update product details (Updates `modifiedBy`, `modifiedOn`) |
| `DELETE` | `/api/v1/products/{id}` | ADMIN | Delete product and its associated items |
| `GET` | `/api/v1/products/{id}/items` | Public | Get all items belonging to a product |
| `POST` | `/api/v1/products/{id}/items` | USER, ADMIN | Add an item to a product |

#### Example: Create Product (`POST /api/v1/products`)
*Header:* `Authorization: Bearer <JWT_ACCESS_TOKEN>`
```json
{
  "productName": "Dell XPS 15 Laptop"
}
```

#### Example: Create Product Response
```json
{
  "success": true,
  "message": "Product created successfully!",
  "data": {
    "id": 1,
    "productName": "Dell XPS 15 Laptop",
    "createdBy": "admin",
    "createdOn": "2026-09-01T12:05:00.000",
    "modifiedBy": "admin",
    "modifiedOn": "2026-09-01T12:05:00.000",
    "items": []
  },
  "timestamp": "2026-09-01T12:05:00.000"
}
```

---

### 3. Item Endpoints (`/api/v1/items`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/v1/items/{id}` | Public | Get individual item by ID |
| `PUT` | `/api/v1/items/{id}` | USER, ADMIN | Update item quantity |
| `DELETE` | `/api/v1/items/{id}` | ADMIN | Delete item by ID |

---

## 🛡️ Error Handling Envelope

Standard error response returned across all endpoints:

```json
{
  "timestamp": "2026-09-01T12:08:00.000",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "path": "/api/v1/products",
  "validationErrors": {
    "productName": "Product name is required"
  }
}
```

---

## 📋 Evaluation Checklist & Submission Guide

- [x] Full CRUD operations for Products & Items
- [x] Java 17+ and Spring Boot 3.3.x with Clean Architecture
- [x] Spring Data JPA with database schema matching the specifications
- [x] Automated JPA Auditing (`createdBy`, `createdOn`, `modifiedBy`, `modifiedOn`)
- [x] Spring Security 6 with JWT & Refresh Token Rotation
- [x] Role-Based Authorization (`ROLE_USER`, `ROLE_ADMIN`)
- [x] Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Min`, etc.)
- [x] Standardized API response envelopes & Global Exception Handling
- [x] Database Indexing strategy on key search fields
- [x] Asynchronous decoupled event processing (`@EnableAsync`, `ThreadPoolTaskExecutor`)
- [x] JUnit 5 & Mockito Unit Tests + Spring Boot MockMvc Integration Tests with H2
- [x] SpringDoc OpenAPI 3 / Swagger documentation configured with Bearer Auth
- [x] Multi-stage `Dockerfile` and `docker-compose.yml`
- [x] Comprehensive README with setup and architecture guide
