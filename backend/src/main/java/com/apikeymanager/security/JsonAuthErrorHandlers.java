package com.apikeymanager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Makes Spring Security's 401/403 responses match the rest of the API's
 * JSON error shape, instead of the framework's default empty body.
 */
public final class JsonAuthErrorHandlers {

    private JsonAuthErrorHandlers() {
    }

    public static AuthenticationEntryPoint unauthorizedEntryPoint(ObjectMapper objectMapper, Clock clock) {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) ->
                writeJsonError(response, objectMapper, clock, 401, "UNAUTHORIZED", "Authentication required");
    }

    public static AccessDeniedHandler forbiddenHandler(ObjectMapper objectMapper, Clock clock) {
        return (HttpServletRequest request, HttpServletResponse response, org.springframework.security.access.AccessDeniedException accessDeniedException) ->
                writeJsonError(response, objectMapper, clock, 403, "FORBIDDEN", "Access denied");
    }

    private static void writeJsonError(
            HttpServletResponse response, ObjectMapper objectMapper, Clock clock, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = Map.of(
                "code", code,
                "message", message,
                "timestamp", clock.instant().toString());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
