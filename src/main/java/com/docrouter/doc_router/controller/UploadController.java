package com.docrouter.doc_router.controller;

import com.docrouter.doc_router.service.UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final UploadService uploadService;

    // Constructor injection — Spring resolves the UploadService bean automatically
    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        // Extract the authenticated username from the security context
        // (set by JwtAuthenticationFilter during token validation)
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, String> result = uploadService.saveFile(file, username);
        return ResponseEntity.ok(result);
    }
}
