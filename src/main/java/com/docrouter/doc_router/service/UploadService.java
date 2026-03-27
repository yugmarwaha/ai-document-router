package com.docrouter.doc_router.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Map;

@Service
public class UploadService {

    private static final Path UPLOAD_DIR = Paths.get("uploads");

    /**
     * Saves the uploaded file to the uploads/ directory and returns response metadata.
     */
    public Map<String, String> saveFile(MultipartFile file) throws IOException {
        // Ensure uploads/ directory exists; no-op if it already does
        Files.createDirectories(UPLOAD_DIR);

        String filename = file.getOriginalFilename();

        // Resolve the target path and normalize to prevent path traversal (e.g. "../../etc/passwd")
        Path target = UPLOAD_DIR.resolve(filename).normalize();
        if (!target.startsWith(UPLOAD_DIR)) {
            throw new IOException("Invalid filename: " + filename);
        }

        // REPLACE_EXISTING avoids FileAlreadyExistsException on duplicate uploads
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return Map.of(
            "filename", filename,
            "timestamp", Instant.now().toString(),
            "message", "File uploaded successfully"
        );
    }
}
