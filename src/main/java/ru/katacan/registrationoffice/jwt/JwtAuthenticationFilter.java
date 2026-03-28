package ru.katacan.registrationoffice.jwt;

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
public class JwtAuthenticationFilter {

	public void doFilter() {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
