package com.capstone.backend;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Requires a shared API key on the detonation trigger — the one endpoint that executes real malware.
 * Closes the "unauthenticated trigger" (Spoofing) finding in the threat model: previously anyone who
 * could reach the port could detonate. Rejected attempts are logged for the audit trail.
 *
 * Production would use Spring Security / OAuth2; a focused filter keeps the blast radius small here.
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthFilter.class);
    private static final String HEADER = "X-API-Key";
    private static final String GUARDED_PREFIX = "/api/start-simulation/";

    private final String apiKey;

    public ApiKeyAuthFilter(@Value("${security.api-key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().startsWith(GUARDED_PREFIX));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (apiKey == null || apiKey.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        if (apiKey.equals(request.getHeader(HEADER))) {
            chain.doFilter(request, response);
            return;
        }

        logger.warn("Rejected unauthenticated detonation attempt from {} for {}",
                request.getRemoteAddr(), request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":\"error\",\"message\":\"Missing or invalid API key.\"}");
    }
}
