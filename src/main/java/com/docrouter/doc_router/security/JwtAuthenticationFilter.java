package com.docrouter.doc_router.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Intercepts every request once, extracts the JWT from the Authorization header,
 * validates it, and sets the authentication in Spring Security's context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // Look for the Authorization header with a Bearer token
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Strip the "Bearer " prefix to get the raw token
            String token = authHeader.substring(7);

            if (jwtUtil.isValid(token)) {
                String username = jwtUtil.extractUsername(token);

                // Create an authentication object with no granted authorities for now
                // This tells Spring Security "this request is authenticated"
                var auth = new UsernamePasswordAuthenticationToken(
                        username, null, List.of());

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        // Continue the filter chain regardless — Spring Security's authorization
        // rules will reject unauthenticated requests to protected endpoints
        filterChain.doFilter(request, response);
    }
}
