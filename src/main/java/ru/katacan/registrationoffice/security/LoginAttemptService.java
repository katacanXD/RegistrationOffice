package ru.katacan.registrationoffice.security;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(1);

    private final Map<String, AttemptState> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String login) {
        AttemptState state = attempts.get(login);
        if (state == null || state.lockedUntil == null) {
            return false;
        }
        if (Instant.now().isAfter(state.lockedUntil)) {
            attempts.remove(login);
            return false;
        }
        return true;
    }

    public long getRemainingLockSeconds(String login) {
        AttemptState state = attempts.get(login);
        if (state == null || state.lockedUntil == null) {
            return 0;
        }
        long seconds = Duration.between(Instant.now(), state.lockedUntil).getSeconds();
        return Math.max(seconds, 0);
    }

    public void recordFailure(String login) {
        attempts.compute(login, (key, state) -> {
            AttemptState current = state == null ? new AttemptState() : state;
            if (current.lockedUntil != null && Instant.now().isAfter(current.lockedUntil)) {
                current.failedAttempts = 0;
                current.lockedUntil = null;
            }

            current.failedAttempts++;
            if (current.failedAttempts >= MAX_ATTEMPTS) {
                current.lockedUntil = Instant.now().plus(LOCK_DURATION);
            }
            return current;
        });
    }

    public void reset(String login) {
        attempts.remove(login);
    }

    private static final class AttemptState {
        private int failedAttempts;
        private Instant lockedUntil;
    }
}

