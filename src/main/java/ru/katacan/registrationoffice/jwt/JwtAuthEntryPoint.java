package ru.katacan.registrationoffice.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Entry point scaffold for unauthorized access handling.
 *
 * Purpose:
 * - handle requests where authentication is missing or invalid;
 * - return HTTP 401 in unified API format;
 * - isolate auth error formatting from business controllers.
 *
 * Implementation is intentionally not added yet.
 */
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}