# AI Document Router

## What this is
Java Spring Boot web app. Users register/login → upload documents → AI classifies them → files route to per-user subfolders → activity logged.

## Stack
- Java 25, Spring Boot 3.5.13, Maven
- Spring Security + JWT (jjwt 0.12.6) for auth
- Claude API (claude-sonnet-4-6) for classification
- PostgreSQL for user persistence
- Vanilla HTML/CSS/JS frontend with warm dark theme

## Key commands
- Run: `mvn spring-boot:run`
- Build: `mvn clean package`

## Package structure
- `config/` — Spring Security configuration
- `controller/` — HTTP endpoints only, no business logic
- `service/` — all business logic lives here
- `model/` — JPA entities
- `repository/` — Spring Data JPA interfaces
- `security/` — JWT utilities and filters

## Secrets
All secrets live in `application-local.properties` (gitignored). Never commit API keys, JWT secrets, or DB credentials.

## Portfolio / Resume
For interview prep, talking points, and detailed technical explanations of every component, see [PORTFOLIO.md](PORTFOLIO.md).
