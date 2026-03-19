package ru.katacan.registrationoffice.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import ru.katacan.registrationoffice.dto.AuthRequestDto;
import ru.katacan.registrationoffice.dto.LoginResponseDto;
import ru.katacan.registrationoffice.dto.RegistrationResponseDto;
import ru.katacan.registrationoffice.entity.DictRole;
import ru.katacan.registrationoffice.entity.User;
import ru.katacan.registrationoffice.exception.BadCredentialsException;
import ru.katacan.registrationoffice.exception.BadRequestException;
import ru.katacan.registrationoffice.mapper.UserMapper;
import ru.katacan.registrationoffice.repository.DictRoleRepository;
import ru.katacan.registrationoffice.repository.UserRepository;
import ru.katacan.registrationoffice.service.AuthService;
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

    @Override
    public RegistrationResponseDto register(AuthRequestDto request) {
        if (userRepository.findByFullName(request.getFio()).isPresent()) {
            throw new RuntimeException("Пользователь с таким ФИО уже существует");
        }

        DictRole patientRole = roleRepository.findByName("patient")
                .orElseThrow(() -> new RuntimeException("Роль patient не найдена"));

        User user = new User();
        user.setFullName(request.getFio());
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);
        user.setRole(patientRole);

        User savedUser = userRepository.save(user);

        return userMapper.toRegistrationResponse(savedUser,
                "Пользователь успешно зарегистрирован");
    }

    @Override
    public LoginResponseDto login(AuthRequestDto request) {
        Optional<User> userOptional = userRepository.findByFullName(
                request.getFio());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Неверные учетные данные");
            }
        } else {
            throw new BadCredentialsException("Неверные учетные данные");
        }

        return userMapper.toLoginResponse(userOptional.get(), "dummy-token");
    }
}