package ru.katacan.registrationoffice.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", "change-me-super-secret-key-32-bytes-min");
        ReflectionTestUtils.setField(provider, "accessTtlSeconds", 3600L);
        ReflectionTestUtils.setField(provider, "refreshTtlSeconds", 7200L);
        provider.init();
    }

    @Test
    void tokens_generation_and_validation() {
        String access = provider.generateAccessToken(1L, "user1", "patient");
        assertThat(provider.isTokenValid(access)).isTrue();
        assertThat(provider.isAccessToken(access)).isTrue();
        assertThat(provider.isRefreshToken(access)).isFalse();
        JwtUserPrincipal p = provider.extractPrincipal(access);
        assertThat(p.userId()).isEqualTo(1L);
        assertThat(p.login()).isEqualTo("user1");

        String refresh = provider.generateRefreshToken(1L, "user1");
        assertThat(provider.isTokenValid(refresh)).isTrue();
        assertThat(provider.isRefreshToken(refresh)).isTrue();
        Instant exp = provider.getExpiration(refresh);
        assertThat(exp).isAfter(Instant.now());
    }
}
package ru.katacan.registrationoffice.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    @Test
    void valid_access_token_is_generated_and_parsed() {
        JwtTokenProvider provider = createProvider("abcdefghijklmnopqrstuvwxyz123456", 900L, 1209600L);

        String token = provider.generateAccessToken(7L, "ivan", "doctor");

        assertTrue(provider.isTokenValid(token));
        assertTrue(provider.isAccessToken(token));
        assertFalse(provider.isRefreshToken(token));
        assertNotNull(provider.getJti(token));

        Instant expiration = provider.getExpiration(token);
        assertTrue(expiration.isAfter(Instant.now()));

        JwtUserPrincipal principal = provider.extractPrincipal(token);
        assertEquals(7L, principal.userId());
        assertEquals("ivan", principal.login());
    }

    @Test
    void expired_token_is_invalid() {
        JwtTokenProvider provider = createProvider("abcdefghijklmnopqrstuvwxyz123456", -1L, 1209600L);

        String token = provider.generateAccessToken(10L, "maria", "patient");

        assertFalse(provider.isTokenValid(token));
    }

    @Test
    void token_with_wrong_signature_is_invalid() {
        JwtTokenProvider issuer = createProvider("abcdefghijklmnopqrstuvwxyz123456", 900L, 1209600L);
        JwtTokenProvider validator = createProvider("abcdefghijklmnopqrstuvwxyz654321", 900L, 1209600L);

        String token = issuer.generateAccessToken(11L, "alex", "admin");

        assertFalse(validator.isTokenValid(token));
    }

    @Test
    void refresh_token_has_refresh_type() {
        JwtTokenProvider provider = createProvider("abcdefghijklmnopqrstuvwxyz123456", 900L, 1209600L);

        String refreshToken = provider.generateRefreshToken(5L, "olga");

        assertTrue(provider.isTokenValid(refreshToken));
        assertTrue(provider.isRefreshToken(refreshToken));
        assertFalse(provider.isAccessToken(refreshToken));
    }

    private JwtTokenProvider createProvider(String secret, long accessTtlSeconds, long refreshTtlSeconds) {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", secret);
        ReflectionTestUtils.setField(provider, "accessTtlSeconds", accessTtlSeconds);
        ReflectionTestUtils.setField(provider, "refreshTtlSeconds", refreshTtlSeconds);
        provider.init();
        return provider;
    }
}

