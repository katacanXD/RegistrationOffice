package ru.katacan.registrationoffice.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * HTTP filter scaffold for JWT auth.
 *
 * Purpose:
 * - read Bearer token from Authorization header;
 * - validate token via JwtProvider;
 * - put authenticated principal into SecurityContext.
 *
 * Implementation is intentionally not added yet.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain) throws ServletException, IOException {
		// JWT parsing/validation will be added later.
		filterChain.doFilter(request, response);
	}
}
