package ru.katacan.registrationoffice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.katacan.registrationoffice.repository.JwtTokenRepository;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenIncidentService {

    private final JwtTokenRepository jwtTokenRepository;

    @Transactional
    public int revokeAllActiveForUser(Long userId) {
        int affected = jwtTokenRepository.revokeAllActiveByUserId(userId, Instant.now());
        log.warn("security.incident.revoke_user_tokens userId={} affected={}", userId, affected);
        return affected;
    }

    @Transactional
    public int revokeAllActiveForAllUsers() {
        int affected = jwtTokenRepository.revokeAllActive(Instant.now());
        log.error("security.incident.revoke_all_tokens affected={}", affected);
        return affected;
    }
}

