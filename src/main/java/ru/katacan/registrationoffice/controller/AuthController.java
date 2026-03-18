package ru.katacan.registrationoffice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.katacan.registrationoffice.service.AuthService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

//    @PostMapping("/registration")
//    public ResponseEntity<RegistrationResponseDto> register(@RequestBody AuthRequestDto request) {
//    }
//
//    @PostMapping("/auth/login")
//    public ResponseEntity<?> login(@RequestBody AuthRequestDto request) {
//    }
}