package ru.katacan.registrationoffice.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
 * Implementation is intentionally not added yet.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request,
						 HttpServletResponse response,
						 AuthenticationException authException) throws IOException, ServletException {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
