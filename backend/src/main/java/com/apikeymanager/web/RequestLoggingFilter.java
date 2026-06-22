package com.apikeymanager.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Assigns a request ID (also echoed back as the X-Request-Id response
 * header), puts it in the logging MDC so every log line during the request
 * can be correlated, and logs one line per request on completion with
 * method/path/status/duration. Deliberately never logs request/response
 * bodies -- those can contain API keys or passwords.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String MDC_KEY = "requestId";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString();
        MDC.put(MDC_KEY, requestId);
        response.setHeader("X-Request-Id", requestId);

        long startedAt = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            double durationMs = (System.nanoTime() - startedAt) / 1_000_000.0;
            log.info(
                    "{} {} -> {} ({}ms)",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    String.format("%.2f", durationMs));
            MDC.remove(MDC_KEY);
        }
    }
}
