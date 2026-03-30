package ru.katacan.registrationoffice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.katacan.registrationoffice.repository.JwtTokenRepository;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private final JwtTokenRepository jwtTokenRepository;

    @Scheduled(cron = "${security.jwt.cleanup-cron:0 0 3 * * *}")
    @Transactional
    public void purgeExpiredRefreshTokens() {
        long deleted = jwtTokenRepository.deleteByExpiryDateBefore(Instant.now());
        if (deleted > 0) {
            log.info("security.refresh.cleanup deleted={} expired tokens", deleted);
        } else {
            log.debug("security.refresh.cleanup no expired tokens");
        }
    }
}

