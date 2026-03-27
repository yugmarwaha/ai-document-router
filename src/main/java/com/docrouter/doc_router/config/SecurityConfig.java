package com.docrouter.doc_router.config;

import com.docrouter.doc_router.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless JWT APIs where the token itself
            // serves as the proof of authenticity (no cookies, no session to hijack)
            .csrf(csrf -> csrf.disable())

            // Define which endpoints are public vs protected
            .authorizeHttpRequests(auth -> auth
                // Auth endpoints are open so users can register and log in without a token
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                // Serve the frontend UI without authentication
                .requestMatchers("/", "/index.html", "/favicon.ico").permitAll()
                // Everything else (including POST /api/upload) requires a valid JWT
                .anyRequest().authenticated()
            )

            // Stateless session — Spring Security won't create or use HTTP sessions.
            // Each request must carry its own JWT; the server stores no session state.
            // This is the standard setup for REST APIs with token-based auth.
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Insert our JWT filter before Spring's default username/password filter.
            // This ensures the JWT is validated and the SecurityContext is populated
            // before Spring Security checks authorization rules.
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // BCrypt for hashing passwords — used by the auth controller during registration/login
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
