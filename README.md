# AI Document Router

A Java Spring Boot web app that accepts document uploads, classifies them using the Claude API, routes them into per-user category-based subfolders, and logs all activity. Secured with JWT authentication.

## How it works

1. User registers or logs in via the web UI or API
2. User uploads a file via the UI or `POST /api/upload` with a JWT
3. Claude API classifies the file as: `invoice`, `contract`, `receipt`, `report`, or `unknown`
4. File is routed to a per-user subfolder (e.g. `uploads/john/invoices/`)
5. Activity is logged to `activity-log.md`

## Stack

- Java 25, Spring Boot 3.5.13, Maven
- Spring Security with JWT (jjwt 0.12.6)
- Claude API (claude-sonnet-4-6) for classification
- PostgreSQL for user persistence
- BCrypt password hashing
- Local filesystem storage

## Getting started

### Prerequisites

- Java 25
- Maven
- PostgreSQL

### Setup

1. Clone the repo
2. Create the database:
   ```bash
   createdb docrouter
   ```
3. Create `src/main/resources/application-local.properties`:
   ```properties
   anthropic.api.key=your-api-key-here
   jwt.secret=your-32-char-secret-here
   spring.datasource.url=jdbc:postgresql://localhost:5432/docrouter
   spring.datasource.username=your-username
   spring.datasource.password=
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   ```
4. Run the app:
   ```bash
   mvn spring-boot:run
   ```
5. Open `http://localhost:8080` — register, log in, and upload.

### API usage

Register:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"pass123"}'
```

Upload with token:
```bash
curl -X POST http://localhost:8080/api/upload \
  -H "Authorization: Bearer <token>" \
  -F "file=@document.txt"
```

Response:
```json
{
  "filename": "document.txt",
  "category": "invoice",
  "destination": "uploads/test/invoices/document.txt",
  "timestamp": "2026-03-27T15:42:00Z",
  "message": "File uploaded, classified, and routed successfully"
}
```

## Project structure

```
src/main/java/com/docrouter/doc_router/
  config/
    SecurityConfig.java        # Spring Security + JWT filter chain
  controller/
    AuthController.java        # POST /api/auth/register, /api/auth/login
    UploadController.java      # POST /api/upload (authenticated)
  model/
    User.java                  # JPA entity
  repository/
    UserRepository.java        # Spring Data JPA interface
  security/
    JwtUtil.java               # JWT token generation and validation
    JwtAuthenticationFilter.java  # Extracts and validates Bearer tokens
  service/
    UploadService.java         # File saving, classification, and routing
    LogService.java            # Activity logging to activity-log.md
```
