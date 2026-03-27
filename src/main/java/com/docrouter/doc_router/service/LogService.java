package com.docrouter.doc_router.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class LogService {

    private static final Path LOG_FILE = Paths.get("activity-log.md");

    // 12-hour format with AM/PM to match the required log line format
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");

    /**
     * Appends one activity line to activity-log.md.
     * Format: [2026-03-27 10:42 AM] test | invoice → file.txt → uploads/test/invoices/file.txt
     */
    public void logActivity(String username, String filename, String category, String destination) throws IOException {
        // Build the log entry with timestamp, username, category, filename, and destination path
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String entry = "[%s] %s | %s → %s → %s%n".formatted(timestamp, username, category, filename, destination);

        // CREATE — creates the file if it doesn't exist; APPEND — adds to the end, never overwrites
        Files.writeString(LOG_FILE, entry, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
