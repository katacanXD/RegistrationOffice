package ru.katacan.registrationoffice.controller;

import jakarta.validation.Valid;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.katacan.registrationoffice.dto.LoginRequestDto;
import ru.katacan.registrationoffice.dto.LoginResponseDto;
import ru.katacan.registrationoffice.dto.RegistrationRequestDto;
import ru.katacan.registrationoffice.dto.RegistrationResponseDto;
import ru.katacan.registrationoffice.dto.UserInfoDto;
import ru.katacan.registrationoffice.entity.User;
import ru.katacan.registrationoffice.mapper.UserMapper;
import ru.katacan.registrationoffice.repository.UserRepository;
import ru.katacan.registrationoffice.service.AuthService;
import ru.katacan.registrationoffice.util.LoginNormalizer;

import java.util.Optional;
import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Value("${security.jwt.refresh-cookie-name:refresh_token}")
    private String refreshCookieName;

    @Value("${security.jwt.cookie-secure:true}")
    private boolean cookieSecure;

    @Value("${security.jwt.cookie-same-site:Lax}")
    private String cookieSameSite;

    @Value("${security.jwt.cookie-path:/api/v1/auth}")
    private String cookiePath;

    @Value("${security.jwt.refresh-ttl-seconds}")
    private long refreshTtlSeconds;

    @PostMapping("/registration")
    public ResponseEntity<RegistrationResponseDto> register(
            @Valid @RequestBody RegistrationRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        LoginResponseDto response = authService.login(request);
        String refreshToken = response.getRefreshToken();

        return ResponseEntity.status(HttpStatus.OK)
                .header("Set-Cookie", buildRefreshCookie(refreshToken).toString())
                .body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoDto> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }

        String normalizedLogin = LoginNormalizer.normalize(authentication.getName());
        Optional<User> currentUser = userRepository.findByLoginIgnoreCase(normalizedLogin);

        if (currentUser.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(userMapper.toUserInfo(currentUser.get()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(
            HttpServletRequest request
    ) {
        String refreshToken = extractRefreshToken(request);
        LoginResponseDto response = authService.refresh(refreshToken);
        String nextRefreshToken = response.getRefreshToken();

        return ResponseEntity.status(HttpStatus.OK)
                .header("Set-Cookie", buildRefreshCookie(nextRefreshToken).toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request
    ) {
        String refreshToken = extractRefreshToken(request);
        authService.logout(refreshToken);
        return ResponseEntity.status(HttpStatus.OK)
                .header("Set-Cookie", clearRefreshCookie().toString())
                .body("Пользователь успешно вышел из системы");
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (refreshCookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private ResponseCookie buildRefreshCookie(String refreshToken) {
        return ResponseCookie.from(refreshCookieName, refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path(cookiePath)
                .maxAge(Duration.ofSeconds(refreshTtlSeconds))
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path(cookiePath)
                .maxAge(Duration.ZERO)
                .build();
    }
}