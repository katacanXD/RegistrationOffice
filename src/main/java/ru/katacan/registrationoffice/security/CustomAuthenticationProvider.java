package ru.katacan.registrationoffice.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * AuthenticationProvider scaffold.
 *
 * Purpose:
 * - verify login credentials against stored password hash;
 * - build authenticated Authentication object on success;
 * - centralize credential checks for /login flow.
 *
 * Implementation is intentionally not added yet.
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

