package ru.katacan.registrationoffice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import ru.katacan.registrationoffice.dto.LoginRequestDto;
import ru.katacan.registrationoffice.dto.LoginResponseDto;
import ru.katacan.registrationoffice.dto.RegistrationRequestDto;
import ru.katacan.registrationoffice.dto.RegistrationResponseDto;
import ru.katacan.registrationoffice.entity.DictRole;
import ru.katacan.registrationoffice.entity.JwtToken;
import ru.katacan.registrationoffice.entity.User;
import ru.katacan.registrationoffice.exception.AccountLockedException;
import ru.katacan.registrationoffice.exception.BadCredentialsException;
import ru.katacan.registrationoffice.exception.BadRequestException;
import ru.katacan.registrationoffice.exception.SystemConfigurationException;
import ru.katacan.registrationoffice.jwt.JwtUserPrincipal;
import ru.katacan.registrationoffice.jwt.JwtTokenProvider;
import ru.katacan.registrationoffice.mapper.UserMapper;
import ru.katacan.registrationoffice.repository.DictRoleRepository;
import ru.katacan.registrationoffice.repository.JwtTokenRepository;
import ru.katacan.registrationoffice.repository.UserRepository;
import ru.katacan.registrationoffice.security.LoginAttemptService;
import ru.katacan.registrationoffice.service.AuthService;
import ru.katacan.registrationoffice.util.LoginNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final DictRoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenRepository jwtTokenRepository;

    @Override
    public RegistrationResponseDto register(RegistrationRequestDto request) {
        String login = normalizeLogin(requireValue(request.getLogin(), "Логин обязателен"));
        String fio = requireValue(request.getFio(), "ФИО обязательно");
        String rawPassword = requireValue(request.getPassword(), "Пароль обязателен");

        if (userRepository.existsByLoginIgnoreCase(login)) {
            throw new BadRequestException("Пользователь с таким логином уже существует");
        }

        DictRole patientRole = roleRepository.findByName("patient")
                .orElseThrow(() -> new SystemConfigurationException("Роль patient не найдена"));

        User user = new User();
        user.setLogin(login);
        user.setFullName(fio);
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);
        user.setRole(patientRole);

        User savedUser = userRepository.save(user);

        return userMapper.toRegistrationResponse(savedUser,
                "Пользователь успешно зарегистрирован");
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request) {
        String login = normalizeLogin(requireValue(request.getLogin(), "Логин обязателен"));
        String rawPassword = requireValue(request.getPassword(), "Пароль обязателен");

        if (loginAttemptService.isBlocked(login)) {
            long seconds = loginAttemptService.getRemainingLockSeconds(login);
            log.warn("security.login.blocked login={} retryAfterSec={}", login, seconds);
            throw new AccountLockedException("Слишком много неудачных попыток входа. Повторите через " + seconds + " сек.");
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, rawPassword));
        } catch (AuthenticationException ex) {
            loginAttemptService.recordFailure(login);
            log.warn("security.login.fail login={} reason=bad_credentials", login);
            throw new BadCredentialsException("Неверные учетные данные");
        }

        User user = userRepository.findByLoginIgnoreCase(login)
                .orElseThrow(() -> new BadCredentialsException("Неверные учетные данные"));

        loginAttemptService.reset(login);
        String refreshToken = issueAndStoreRefreshToken(user);
        String roleName = user.getRole() != null ? user.getRole().getName() : null;
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getLogin(), roleName);

        LoginResponseDto response = userMapper.toLoginResponse(user, accessToken);
        response.setRefreshToken(refreshToken);
        log.info("security.login.success userId={} login={}", user.getId(), user.getLogin());
        return response;
    }

    @Override
    public LoginResponseDto refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("security.refresh.fail reason=empty_cookie");
            throw new BadCredentialsException("Неверный refresh token");
        }

        if (!jwtTokenProvider.isTokenValid(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            log.warn("security.refresh.fail reason=invalid_token jti={}", safeJti(refreshToken));
            throw new BadCredentialsException("Неверный refresh token");
        }

        String refreshTokenHash = hashRefreshToken(refreshToken);
        JwtToken existingToken = jwtTokenRepository.findByTokenHash(refreshTokenHash)
                .orElseThrow(() -> {
                    log.warn("security.refresh.fail reason=unknown_token jti={}", safeJti(refreshToken));
                    return new BadCredentialsException("Неверный refresh token");
                });

        if (existingToken.isRevoked()) {
            revokeAllActiveUserTokens(existingToken.getUser().getId());
            log.error("security.refresh.reuse_detected userId={} jti={} action=revoke_all_active",
                    existingToken.getUser().getId(), safeJti(refreshToken));
            throw new BadCredentialsException("Refresh token отозван");
        }

        if (existingToken.isExpired()) {
            existingToken.setRevokedAt(Instant.now());
            jwtTokenRepository.save(existingToken);
            log.warn("security.refresh.fail reason=expired userId={} jti={}",
                    existingToken.getUser().getId(), safeJti(refreshToken));
            throw new BadCredentialsException("Refresh token истек");
        }

        JwtUserPrincipal principal = jwtTokenProvider.extractPrincipal(refreshToken);
        User user = existingToken.getUser();
        if (!user.getId().equals(principal.userId())) {
            log.warn("security.refresh.fail reason=subject_mismatch tokenUserId={} dbUserId={}",
                    principal.userId(), user.getId());
            throw new BadCredentialsException("Неверный refresh token");
        }

        String nextRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getLogin());
        String nextRefreshHash = hashRefreshToken(nextRefreshToken);

        existingToken.setRevokedAt(Instant.now());
        existingToken.setReplacedByTokenHash(nextRefreshHash);
        jwtTokenRepository.save(existingToken);

        JwtToken replacement = new JwtToken();
        replacement.setUser(user);
        replacement.setTokenHash(nextRefreshHash);
        replacement.setExpiryDate(jwtTokenProvider.getExpiration(nextRefreshToken));
        jwtTokenRepository.save(replacement);

        String roleName = user.getRole() != null ? user.getRole().getName() : null;
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getLogin(), roleName);

        LoginResponseDto response = userMapper.toLoginResponse(user, accessToken);
        response.setRefreshToken(nextRefreshToken);
        log.info("security.refresh.success userId={} login={} oldJti={} newJti={}",
                user.getId(), user.getLogin(), safeJti(refreshToken), safeJti(nextRefreshToken));
        return response;
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            log.debug("security.logout.skip reason=empty_cookie");
            return;
        }

        if (!jwtTokenProvider.isTokenValid(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            log.debug("security.logout.skip reason=invalid_token jti={}", safeJti(refreshToken));
            return;
        }

        String refreshTokenHash = hashRefreshToken(refreshToken);
        jwtTokenRepository.findByTokenHash(refreshTokenHash).ifPresent(token -> {
            if (!token.isRevoked()) {
                token.setRevokedAt(Instant.now());
                jwtTokenRepository.save(token);
                log.info("security.logout.success userId={} jti={}", token.getUser().getId(), safeJti(refreshToken));
            } else {
                log.debug("security.logout.skip reason=already_revoked userId={} jti={}",
                        token.getUser().getId(), safeJti(refreshToken));
            }
        });
    }

    private String issueAndStoreRefreshToken(User user) {
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getLogin());
        JwtToken token = new JwtToken();
        token.setUser(user);
        token.setTokenHash(hashRefreshToken(refreshToken));
        token.setExpiryDate(jwtTokenProvider.getExpiration(refreshToken));
        jwtTokenRepository.save(token);
        return refreshToken;
    }

    private void revokeAllActiveUserTokens(Long userId) {
        Instant now = Instant.now();
        int affected = jwtTokenRepository.revokeAllActiveByUserId(userId, now);
        log.warn("security.refresh.revoke_all_user_tokens userId={} affected={}", userId, affected);
    }

    private String safeJti(String token) {
        try {
            if (!jwtTokenProvider.isTokenValid(token)) {
                return "n/a";
            }
            return jwtTokenProvider.getJti(token);
        } catch (Exception ex) {
            return "n/a";
        }
    }

    private String hashRefreshToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashedBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 не поддерживается", ex);
        }
    }

    private String requireValue(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }
        return value.trim();
    }

    private String normalizeLogin(String login) {
        return LoginNormalizer.normalize(login);
    }
}