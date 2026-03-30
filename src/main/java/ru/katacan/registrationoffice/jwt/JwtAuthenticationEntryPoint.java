package ru.katacan.registrationoffice.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Canonical scaffold for unauthorized response handling.
 *
 * Purpose:
 * - keep a dedicated class for 401 response contract;
 * - separate auth-failure formatting from controllers;
 * - reserve extension point for custom error payload.
 *
 * Produces a safe 401 response payload for unauthorized requests.
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request,
						 HttpServletResponse response,
						 AuthenticationException authException) throws IOException {
		log.debug("Unauthorized request to {}: {}", request.getRequestURI(), authException.getMessage());
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("{\"message\":\"Требуется авторизация\"}");
	}
}
