# AI Document Router

## What this is
Java Spring Boot web app. Users upload documents → AI classifies them → files route to subfolders → activity logged to a .md file.

## Stack
- Java 25, Spring Boot 3.5.13, Maven
- Claude API for classification (Phase 2)
- Local filesystem storage for now

## Key commands
- Run: `mvn spring-boot:run`
- Build: `mvn clean package`

## Planned package structure
- `controller/` — HTTP endpoints only, no business logic
- `service/` — all business logic lives here
- `model/` — data classes

## Current state
Phase 1 complete: app boots on port 8080.
Next: POST /upload endpoint that accepts a file and returns filename + timestamp.