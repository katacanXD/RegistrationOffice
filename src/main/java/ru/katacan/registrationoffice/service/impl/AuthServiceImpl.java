package ru.katacan.registrationoffice.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import ru.katacan.registrationoffice.dto.LoginRequestDto;
import ru.katacan.registrationoffice.dto.LoginResponseDto;
import ru.katacan.registrationoffice.dto.RegistrationRequestDto;
import ru.katacan.registrationoffice.dto.RegistrationResponseDto;
import ru.katacan.registrationoffice.entity.DictRole;
import ru.katacan.registrationoffice.entity.User;
import ru.katacan.registrationoffice.exception.AccountLockedException;
import ru.katacan.registrationoffice.exception.BadCredentialsException;
import ru.katacan.registrationoffice.exception.BadRequestException;
import ru.katacan.registrationoffice.exception.SystemConfigurationException;
import ru.katacan.registrationoffice.mapper.UserMapper;
import ru.katacan.registrationoffice.repository.DictRoleRepository;
import ru.katacan.registrationoffice.repository.UserRepository;
import ru.katacan.registrationoffice.security.LoginAttemptService;
import ru.katacan.registrationoffice.service.AuthService;
import ru.katacan.registrationoffice.util.LoginNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final DictRoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;

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
            throw new AccountLockedException("Слишком много неудачных попыток входа. Повторите через " + seconds + " сек.");
        }

        Optional<User> userOptional = userRepository.findByLoginIgnoreCase(login);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                loginAttemptService.recordFailure(login);
                throw new BadCredentialsException("Неверные учетные данные");
            }
        } else {
            loginAttemptService.recordFailure(login);
            throw new BadCredentialsException("Неверные учетные данные");
        }

        loginAttemptService.reset(login);

        return userMapper.toLoginResponse(userOptional.get(), "dummy-token");
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