package ru.katacan.registrationoffice.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Canonical scaffold for token operations.
 *
 * Purpose:
 * - keep an explicit place for token generation and validation logic;
 * - parse JWT claims into application principal representation;
 * - isolate JWT library usage from service/controller layers.
 *
 * Implements signing/parsing helpers for access and refresh tokens.
 */
@Component
public class JwtTokenProvider {

	private static final String CLAIM_UID = "uid";
	private static final String CLAIM_ROLE = "role";
	private static final String CLAIM_TYPE = "typ";
	private static final String TYPE_ACCESS = "access";
	private static final String TYPE_REFRESH = "refresh";

	@Value("${security.jwt.secret}")
	private String jwtSecret;

	@Value("${security.jwt.access-ttl-seconds}")
	private long accessTtlSeconds;

	@Value("${security.jwt.refresh-ttl-seconds}")
	private long refreshTtlSeconds;

	private SecretKey signingKey;

	@PostConstruct
	public void init() {
		byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
		signingKey = Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateToken(Long userId, String login, String roleName) {
		return generateAccessToken(userId, login, roleName);
	}

	public String generateAccessToken(Long userId, String login, String roleName) {
		Instant now = Instant.now();
		Instant expiration = now.plusSeconds(accessTtlSeconds);

		return Jwts.builder()
				.subject(login)
				.claim(CLAIM_UID, userId)
				.claim(CLAIM_ROLE, roleName)
				.claim(CLAIM_TYPE, TYPE_ACCESS)
				.id(UUID.randomUUID().toString())
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiration))
				.signWith(signingKey)
				.compact();
	}

	public String generateRefreshToken(Long userId, String login) {
		Instant now = Instant.now();
		Instant expiration = now.plusSeconds(refreshTtlSeconds);

		return Jwts.builder()
				.subject(login)
				.claim(CLAIM_UID, userId)
				.claim(CLAIM_TYPE, TYPE_REFRESH)
				.id(UUID.randomUUID().toString())
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiration))
				.signWith(signingKey)
				.compact();
	}

	public boolean isTokenValid(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException ex) {
			return false;
		}
	}

	public JwtUserPrincipal extractPrincipal(String token) {
		Claims claims = parseClaims(token);
		Long userId = claims.get(CLAIM_UID, Long.class);
		String login = claims.getSubject();
		return new JwtUserPrincipal(userId, login);
	}

	public boolean isAccessToken(String token) {
		return TYPE_ACCESS.equals(parseClaims(token).get(CLAIM_TYPE, String.class));
	}

	public boolean isRefreshToken(String token) {
		return TYPE_REFRESH.equals(parseClaims(token).get(CLAIM_TYPE, String.class));
	}

	public String getJti(String token) {
		return parseClaims(token).getId();
	}

	public Instant getExpiration(String token) {
		return parseClaims(token).getExpiration().toInstant();
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
