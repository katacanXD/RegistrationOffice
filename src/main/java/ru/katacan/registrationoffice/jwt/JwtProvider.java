package ru.katacan.registrationoffice.jwt;

/**
 * JWT utility scaffold.
 *
 * Purpose:
 * - create access token for authenticated user;
 * - validate token signature and expiration;
 * - extract user claims from token payload.
 *
 * Implementation is intentionally not added yet.
 */
public class JwtProvider {

	public String generateToken(Long userId, String fullName, String roleName) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public boolean isTokenValid(String token) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public JwtUserPrincipal extractPrincipal(String token) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
