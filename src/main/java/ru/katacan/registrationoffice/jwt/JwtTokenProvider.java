package ru.katacan.registrationoffice.jwt;

/**
 * Canonical scaffold for token operations.
 *
 * Purpose:
 * - keep an explicit place for token generation and validation logic;
 * - parse JWT claims into application principal representation;
 * - isolate JWT library usage from service/controller layers.
 *
 * Implementation is intentionally not added yet.
 */
public class JwtTokenProvider {

	public String generateToken(Long userId, String login, String roleName) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public boolean isTokenValid(String token) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public JwtUserPrincipal extractPrincipal(String token) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
