package ru.katacan.registrationoffice.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import ru.katacan.registrationoffice.dto.LoginRequestDto;
import ru.katacan.registrationoffice.dto.LoginResponseDto;
import ru.katacan.registrationoffice.dto.RegistrationRequestDto;
import ru.katacan.registrationoffice.dto.RegistrationResponseDto;
import ru.katacan.registrationoffice.entity.DictRole;
import ru.katacan.registrationoffice.entity.User;
import ru.katacan.registrationoffice.jwt.JwtTokenProvider;
import ru.katacan.registrationoffice.mapper.UserMapper;
import ru.katacan.registrationoffice.repository.DictRoleRepository;
import ru.katacan.registrationoffice.repository.JwtTokenRepository;
import ru.katacan.registrationoffice.repository.UserRepository;
import ru.katacan.registrationoffice.security.LoginAttemptService;
import ru.katacan.registrationoffice.service.impl.AuthServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private DictRoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private LoginAttemptService loginAttemptService;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenRepository jwtTokenRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_success() {
        RegistrationRequestDto req = new RegistrationRequestDto();
        req.setLogin("user1");
        req.setFio("Иван Иванов");
        req.setPassword("password123");

        when(userRepository.existsByLoginIgnoreCase("user1")).thenReturn(false);
        DictRole role = new DictRole();
        role.setName("patient");
        when(roleRepository.findByName("patient")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password123")).thenReturn("encoded-pass");

        User saved = new User();
        saved.setId(10L);
        saved.setLogin("user1");
        saved.setFullName("Иван Иванов");
        saved.setRole(role);

        when(userRepository.save(any())).thenReturn(saved);
        when(userMapper.toRegistrationResponse(saved, "Пользователь успешно зарегистрирован"))
                .thenReturn(RegistrationResponseDto.builder().userId(10L).message("Пользователь успешно зарегистрирован").build());

        RegistrationResponseDto resp = authService.register(req);

        assertThat(resp).isNotNull();
        assertThat(resp.getUserId()).isEqualTo(10L);
        verify(userRepository).save(any());
    }

    @Test
    void login_success() {
        LoginRequestDto req = new LoginRequestDto();
        req.setLogin("user1");
        req.setPassword("password123");

        when(loginAttemptService.isBlocked("user1")).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenReturn(null);

        DictRole role = new DictRole();
        role.setName("patient");
        User user = new User();
        user.setId(20L);
        user.setLogin("user1");
        user.setFullName("Иван Иванов");
        user.setRole(role);

        when(userRepository.findByLoginIgnoreCase("user1")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(20L, "user1", "patient")).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(20L, "user1")).thenReturn("refresh-token");
        when(userMapper.toLoginResponse(any(), eq("access-token")))
                .thenReturn(LoginResponseDto.builder().accessToken("access-token").build());

        LoginResponseDto response = authService.login(req);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        verify(jwtTokenRepository, atLeastOnce()).save(any());
    }
}
