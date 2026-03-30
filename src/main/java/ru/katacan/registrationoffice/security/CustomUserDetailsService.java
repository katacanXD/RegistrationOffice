package ru.katacan.registrationoffice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.katacan.registrationoffice.entity.User;
import ru.katacan.registrationoffice.repository.UserRepository;
import ru.katacan.registrationoffice.util.LoginNormalizer;

import java.util.List;

/**
 * UserDetailsService scaffold.
 *
 * Purpose:
 * - load user record from database by login identifier;
 * - map domain User to Spring Security UserDetails;
 * - provide user authorities for authorization checks.
 *
 * Loads users from database and maps role name as authority.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedLogin = LoginNormalizer.normalize(username);
        User user = userRepository.findByLoginIgnoreCase(normalizedLogin)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        String roleName = user.getRole() != null ? user.getRole().getName() : "";
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleName));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getLogin())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
}

