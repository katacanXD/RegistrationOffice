package ru.katacan.registrationoffice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.katacan.registrationoffice.dto.AuthRequestDto;
import ru.katacan.registrationoffice.dto.RegistrationResponseDto;
import ru.katacan.registrationoffice.dto.UserInfoDto;
import ru.katacan.registrationoffice.entity.User;
import ru.katacan.registrationoffice.mapper.UserMapper;
import ru.katacan.registrationoffice.repository.UserRepository;
import ru.katacan.registrationoffice.service.AuthService;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @PostMapping("/registration")
    public ResponseEntity<RegistrationResponseDto> register(
            @RequestBody AuthRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody AuthRequestDto request
    ) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoDto> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> currentUser = userRepository.findByFullName(authentication.getName());

        if (currentUser.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(userMapper.toUserInfo(currentUser.get()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh() {
        String token = "";
        return ResponseEntity.status(HttpStatus.OK)
                .body(token);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // В Spring Security сессия будет автоматически завершена при вызове /logout, если настроить его в конфигурации безопасности.
        // Здесь можно добавить дополнительную логику, если необходимо, например, для очистки токенов или других данных.
        return ResponseEntity.status(HttpStatus.OK)
                .body("Пользователь успешно вышел из системы");
    }
}