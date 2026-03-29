package ru.katacan.registrationoffice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Использует стойкий алгоритм BCrypt
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/registration").permitAll()
                .requestMatchers("/api/v1/users/me/**", "/api/v1/auth/me").authenticated()
                .requestMatchers("/api/v1/patients/me/**").hasAuthority("patient")
                .requestMatchers("/api/v1/admins/**").hasAuthority("admin")
                .requestMatchers("/api/v1/users/**").hasAnyAuthority("admin", "registrar")
                .requestMatchers("/api/v1/patients").hasAnyAuthority("admin", "registrar")
                .anyRequest().authenticated()
        );
        return http.build();
    }
}