package ru.katacan.registrationoffice.security;

public record JwtUserPrincipal(Long userId, String fullName) {
}

