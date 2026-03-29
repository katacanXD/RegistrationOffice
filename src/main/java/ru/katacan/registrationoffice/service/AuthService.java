package ru.katacan.registrationoffice.service;

import ru.katacan.registrationoffice.dto.LoginRequestDto;
import ru.katacan.registrationoffice.dto.LoginResponseDto;
import ru.katacan.registrationoffice.dto.RegistrationRequestDto;
import ru.katacan.registrationoffice.dto.RegistrationResponseDto;

public interface AuthService {
    RegistrationResponseDto register(RegistrationRequestDto request);
    LoginResponseDto login(LoginRequestDto request);
}
