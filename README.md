# AI Document Router

A Java Spring Boot web app that accepts document uploads, classifies them using the Claude API, routes them into category-based subfolders, and logs all activity.

## How it works

1. User uploads a file via `POST /api/upload`
2. File is saved to `uploads/`
3. Claude API reads the file content and classifies it as: `invoice`, `contract`, `receipt`, `report`, or `unknown`
4. File is moved to a subfolder matching the category (e.g. `uploads/invoices/`)
5. Activity is logged to `activity-log.md`

## Stack

- Java 25
- Spring Boot 3.5.13
- Maven
- Claude API (claude-sonnet-4-6) for classification
- Local filesystem storage

## Getting started

### Prerequisites

- Java 25
- Maven

### Setup

1. Clone the repo
2. Create `src/main/resources/application-local.properties` with your API key:
   ```properties
   anthropic.api.key=your-api-key-here
   ```
3. Run the app:
   ```bash
   mvn spring-boot:run
   ```
   The server starts on `http://localhost:8080`.

### Usage

Upload a file:
```bash
curl -X POST -F "file=@document.txt" http://localhost:8080/api/upload
```

Response:
```json
{
  "filename": "document.txt",
  "category": "invoice",
  "destination": "uploads/invoices/document.txt",
  "timestamp": "2026-03-27T15:42:00Z",
  "message": "File uploaded, classified, and routed successfully"
}
```

## Project structure

```
src/main/java/com/docrouter/doc_router/
  controller/
    UploadController.java    # POST /api/upload endpoint
  service/
    UploadService.java       # File saving, classification, and routing
    LogService.java          # Activity logging to activity-log.md
```
