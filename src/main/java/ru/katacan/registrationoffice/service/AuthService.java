package ru.katacan.registrationoffice.service;

import ru.katacan.registrationoffice.dto.AuthRequestDto;
import ru.katacan.registrationoffice.dto.LoginResponseDto;
import ru.katacan.registrationoffice.dto.RegistrationResponseDto;

public interface AuthService {
    RegistrationResponseDto register(AuthRequestDto request);
    LoginResponseDto login(AuthRequestDto request);
}
