package com.docrouter.doc_router.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UploadService {

    private static final Path UPLOAD_DIR = Paths.get("uploads");

    // Explicit category-to-folder mapping — add new entries here when categories expand
    private static final Map<String, String> CATEGORY_FOLDERS = Map.of(
            "invoice", "invoices",
            "contract", "contracts",
            "receipt", "receipts",
            "report", "reports",
            "unknown", "unknown"
    );
    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-sonnet-4-6";

    private final LogService logService;

    // Constructor injection for LogService
    public UploadService(LogService logService) {
        this.logService = logService;
    }

    // Injected from application-local.properties at startup
    @Value("${anthropic.api.key}")
    private String apiKey;

    /**
     * Saves the uploaded file to the uploads/ directory, classifies its content
     * via the Anthropic API, and returns response metadata including category.
     */
    public Map<String, String> saveFile(MultipartFile file, String username) throws IOException {
        // Build per-user upload directory: uploads/{username}/
        Path userDir = UPLOAD_DIR.resolve(username);
        Files.createDirectories(userDir);

        String filename = file.getOriginalFilename();

        // Resolve the target path and normalize to prevent path traversal (e.g. "../../etc/passwd")
        Path target = userDir.resolve(filename).normalize();
        if (!target.startsWith(userDir)) {
            throw new IOException("Invalid filename: " + filename);
        }

        // REPLACE_EXISTING avoids FileAlreadyExistsException on duplicate uploads
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // Read the saved file's text content for classification
        String content = Files.readString(target);

        // Classify the document using the Anthropic API
        String category = classifyDocument(content);

        // Move the file into the per-user category subfolder (e.g. uploads/test/invoices/)
        Path destination = routeFile(target, category, userDir);

        // Log the activity with username to activity-log.md
        logService.logActivity(username, filename, category, destination.toString());

        // LinkedHashMap preserves insertion order so JSON fields appear in a logical sequence
        Map<String, String> response = new LinkedHashMap<>();
        response.put("filename", filename);
        response.put("category", category);
        response.put("destination", destination.toString());
        response.put("timestamp", Instant.now().toString());
        response.put("message", "File uploaded, classified, and routed successfully");
        return response;
    }

    /**
     * Moves the file into a subfolder named after the category (pluralized).
     * e.g. uploads/invoices/report.pdf
     */
    private Path routeFile(Path source, String category, Path userDir) throws IOException {
        // Look up the folder name from the map; default to the category itself if missing
        String folderName = CATEGORY_FOLDERS.getOrDefault(category, category);

        // Build the subfolder path under the user's directory (e.g. uploads/test/invoices/)
        Path categoryDir = userDir.resolve(folderName);

        // Create the subfolder if it doesn't exist yet; no-op if it already does
        Files.createDirectories(categoryDir);

        // Destination is the category subfolder with the same filename
        Path destination = categoryDir.resolve(source.getFileName());

        // Move the file; REPLACE_EXISTING handles re-uploads of the same filename
        Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);

        return destination;
    }

    /**
     * Sends the document text to Claude and returns one of:
     * invoice, contract, receipt, report, or unknown.
     */
    private String classifyDocument(String content) throws IOException {
        // Truncate to ~4000 chars to stay well within token limits and keep costs low
        String trimmed = content.length() > 4000 ? content.substring(0, 4000) : content;

        // Escape special JSON characters in the document text so the request body stays valid
        String escaped = trimmed
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        // Build the Messages API request body with a system prompt that constrains the output
        String requestBody = """
                {
                    "model": "%s",
                    "max_tokens": 50,
                    "system": "You are a document classifier. Classify the document into exactly one of these categories: invoice, contract, receipt, report, unknown. Respond with ONLY the category name in lowercase, nothing else.",
                    "messages": [
                        {
                            "role": "user",
                            "content": "Classify this document:\\n\\n%s"
                        }
                    ]
                }
                """.formatted(MODEL, escaped);

        try {
            HttpClient client = HttpClient.newHttpClient();

            // Anthropic requires x-api-key and anthropic-version headers for authentication
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ANTHROPIC_API_URL))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                // Log the error but don't crash — fall back to "unknown"
                System.err.println("Anthropic API error (HTTP " + response.statusCode() + "): " + response.body());
                return "unknown";
            }

            // Extract the classification from the response JSON
            return parseCategory(response.body());

        } catch (InterruptedException e) {
            // Restore the interrupt flag so calling code can detect the interruption
            Thread.currentThread().interrupt();
            return "unknown";
        }
    }

    /**
     * Pulls the text value from the first content block in the API response.
     * Response shape: { "content": [ { "type": "text", "text": "invoice" } ] }
     * Uses simple string parsing to avoid adding a JSON library dependency.
     */
    private String parseCategory(String responseBody) {
        // Find the "text" field inside the first content block
        String marker = "\"text\":\"";
        int start = responseBody.indexOf(marker);
        if (start == -1) {
            // Also handle the case where there's a space after the colon
            marker = "\"text\": \"";
            start = responseBody.indexOf(marker);
        }
        if (start == -1) return "unknown";

        start += marker.length();
        int end = responseBody.indexOf("\"", start);
        if (end == -1) return "unknown";

        String category = responseBody.substring(start, end).trim().toLowerCase();

        // Only return recognized categories; anything else becomes "unknown"
        return switch (category) {
            case "invoice", "contract", "receipt", "report" -> category;
            default -> "unknown";
        };
    }
}
