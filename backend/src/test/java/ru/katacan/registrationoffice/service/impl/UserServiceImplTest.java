package ru.katacan.registrationoffice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.katacan.registrationoffice.dto.UpdateUserRequestDto;
import ru.katacan.registrationoffice.dto.UpdateUserResponseDto;
import ru.katacan.registrationoffice.entity.DictRole;
import ru.katacan.registrationoffice.entity.User;
import ru.katacan.registrationoffice.exception.BadRequestException;
import ru.katacan.registrationoffice.mapper.AppointmentMapper;
import ru.katacan.registrationoffice.mapper.UserMapper;
import ru.katacan.registrationoffice.repository.AppointmentRepository;
import ru.katacan.registrationoffice.repository.DictRoleRepository;
import ru.katacan.registrationoffice.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private DictRoleRepository dictRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository,
                userMapper,
                appointmentRepository,
                appointmentMapper,
                dictRoleRepository,
                passwordEncoder
        );
    }

    @Test
    void updateUser_rejects_empty_patch() {
        UpdateUserRequestDto request = new UpdateUserRequestDto();
        User existingUser = userWithRole(1L, "patient", "1234567890123456", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.updateUser(1L, request));

        assertEquals("Не указаны поля для обновления", ex.getMessage());
    }

    @Test
    void updateUser_rejects_policy_for_non_patient_role() {
        UpdateUserRequestDto request = new UpdateUserRequestDto();
        request.setPolicyNumber("1234567890123456");

        User existingUser = userWithRole(2L, "doctor", null, "Терапевт");
        when(userRepository.findById(2L)).thenReturn(Optional.of(existingUser));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.updateUser(2L, request));

        assertEquals("Полис можно указывать только для роли patient", ex.getMessage());
    }

    @Test
    void updateUser_changes_role_and_clears_incompatible_fields() {
        UpdateUserRequestDto request = new UpdateUserRequestDto();
        request.setRole("doctor");

        User existingUser = userWithRole(3L, "patient", "1234567890123456", "старое значение");
        DictRole doctorRole = new DictRole();
        doctorRole.setName("doctor");

        when(userRepository.findById(3L)).thenReturn(Optional.of(existingUser));
        when(dictRoleRepository.findByName("doctor")).thenReturn(Optional.of(doctorRole));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserResponseDto result = userService.updateUser(3L, request);

        assertEquals("doctor", result.getRole());
        assertNull(result.getPolicyNumber());
        verify(userRepository).save(existingUser);
    }

    private static User userWithRole(Long id, String roleName, String policyNumber, String speciality) {
        DictRole role = new DictRole();
        role.setName(roleName);

        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setPolicyNumber(policyNumber);
        user.setSpeciality(speciality);
        user.setFullName("Тестовый Пользователь");

        return user;
    }
}

