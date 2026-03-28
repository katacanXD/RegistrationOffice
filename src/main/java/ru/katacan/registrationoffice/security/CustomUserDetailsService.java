package ru.katacan.registrationoffice.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UserDetailsService scaffold.
 *
 * Purpose:
 * - load user record from database by login identifier;
 * - map domain User to Spring Security UserDetails;
 * - provide user authorities for authorization checks.
 *
 * Implementation is intentionally not added yet.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

