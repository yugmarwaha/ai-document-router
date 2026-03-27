package com.docrouter.doc_router.controller;

import com.docrouter.doc_router.model.User;
import com.docrouter.doc_router.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // In-memory user store — ConcurrentHashMap for thread safety since
    // multiple requests could register simultaneously
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public AuthController(JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        // Reject if either field is missing
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Username and password are required"));
        }

        // Check if the username is already taken
        if (users.containsKey(username)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Username already taken"));
        }

        // Hash the password with BCrypt before storing — never store plaintext
        String hash = passwordEncoder.encode(password);
        users.put(username, new User(username, hash));

        // Generate a JWT so the user is immediately logged in after registration
        String token = jwtUtil.generateToken(username);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        // Look up the user in the in-memory store
        User user = users.get(username);

        // If user doesn't exist or password doesn't match the stored hash, reject
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Invalid username or password"));
        }

        // Credentials are valid — issue a JWT
        String token = jwtUtil.generateToken(username);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "message", "Login successful"));
    }
}
