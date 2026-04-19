
package ru.katacan.registrationoffice.service.impl;

import ru.katacan.registrationoffice.dto.*;
import ru.katacan.registrationoffice.entity.Appointment;
import ru.katacan.registrationoffice.entity.DictRole;
import ru.katacan.registrationoffice.entity.User;
import ru.katacan.registrationoffice.exception.BadCredentialsException;
import ru.katacan.registrationoffice.exception.BadRequestException;
import ru.katacan.registrationoffice.exception.ResourceNotFoundException;
import ru.katacan.registrationoffice.mapper.UserMapper;
import ru.katacan.registrationoffice.repository.DictRoleRepository;
import ru.katacan.registrationoffice.repository.UserRepository;
import ru.katacan.registrationoffice.service.UserService;
import ru.katacan.registrationoffice.repository.AppointmentRepository;
import ru.katacan.registrationoffice.mapper.AppointmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import ru.katacan.registrationoffice.util.LoginNormalizer;

import java.util.Locale;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final DictRoleRepository dictRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserListDto searchUsers(String search, Pageable pageable) {
        Page<User> userPage = userRepository.searchUsers(search != null ? search : "", pageable);

        List<UserInfoDto> userDtos = userPage.getContent().stream()
                .map(userMapper::toUserInfo)
                .collect(Collectors.toList());

        return userMapper.toUserList(userDtos, userPage);
    }

    @Override
    public PatientListDto searchPatients(String search, Pageable pageable) {
        Page<User> patientPage = userRepository.searchPatients(search != null ? search : "", pageable);

        List<PatientListDto.PatientDto> patientDtos = patientPage.getContent().stream()
                .map(userMapper::toPatientDto)
                .collect(Collectors.toList());

        return userMapper.toPatientList(patientDtos, patientPage);
    }

    @Override
    public DoctorListDto searchDoctors(String search, Pageable pageable) {
        String searchParam = (search == null) ? "" : search;

        Page<User> doctorPage = userRepository.searchDoctors(searchParam, pageable);

        List<DoctorShortDto> doctorDtos = doctorPage.getContent().stream()
                .map(userMapper::toDoctorShort)
                .collect(Collectors.toList());

        return userMapper.toDoctorList(doctorDtos, doctorPage);
    }

    @Override
    public UserInfoDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с id " + userId + " не найден"));
        return userMapper.toUserInfo(user);
    }

    @Override
    public UserInfoDto getCurrentUser(String login) {
        User user = findByLogin(login);
        return userMapper.toUserInfo(user);
    }

    @Override
    public UpdateUserResponseDto updateUser(Long userId, UpdateUserRequestDto request) {
        if (request == null) {
            throw new BadRequestException("Тело запроса не должно быть пустым");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с id " + userId + " не найден"));

        String normalizedRole = normalizeOptionalField(request.getRole(), "role");
        String normalizedPolicy = normalizeOptionalField(request.getPolicyNumber(), "policyNumber");
        String normalizedSpeciality = normalizeOptionalField(request.getSpeciality(), "speciality");

        if (normalizedRole == null && normalizedPolicy == null && normalizedSpeciality == null) {
            throw new BadRequestException("Не указаны поля для обновления");
        }

        DictRole targetRole = user.getRole();
        if (normalizedRole != null) {
            String roleName = normalizedRole.toLowerCase(Locale.ROOT);
            targetRole = dictRoleRepository.findByName(roleName)
                    .orElseThrow(() -> new BadRequestException("Роль '" + roleName + "' не найдена"));
            user.setRole(targetRole);
        }

        String targetRoleName = targetRole != null ? targetRole.getName() : null;
        if (normalizedPolicy != null && !"patient".equals(targetRoleName)) {
            throw new BadRequestException("Полис можно указывать только для роли patient");
        }
        if (normalizedSpeciality != null && !"doctor".equals(targetRoleName)) {
            throw new BadRequestException("Специальность можно указывать только для роли doctor");
        }

        if (normalizedPolicy != null) {
            user.setPolicyNumber(normalizedPolicy);
        }
        if (normalizedSpeciality != null) {
            user.setSpeciality(normalizedSpeciality);
        }

        // Поддерживаем консистентность профиля при смене роли.
        if (normalizedRole != null && !"patient".equals(targetRoleName)) {
            user.setPolicyNumber(null);
        }
        if (normalizedRole != null && !"doctor".equals(targetRoleName)) {
            user.setSpeciality(null);
        }

        User updatedUser = userRepository.save(user);
        return UpdateUserResponseDto.builder()
                .fio(updatedUser.getFullName())
                .policyNumber(updatedUser.getPolicyNumber())
                .speciality(updatedUser.getSpeciality())
                .role(updatedUser.getRole() != null ? updatedUser.getRole().getName() : null)
                .build();
    }

    private String normalizeOptionalField(String value, String fieldName) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new BadRequestException("Поле '" + fieldName + "' не должно быть пустым");
        }

        return trimmed;
    }

    @Override
    public SuccessResponseDto deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Пользователь с id " + userId + " не найден");
        }
        userRepository.deleteById(userId);

        return SuccessResponseDto.builder()
                .success(true)
                .message("Пользователь успешно удален")
                .build();
    }

    @Override
    public SuccessResponseDto changeOwnPassword(String login, ChangePasswordRequestDto request) {
        User user = findByLogin(login);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Старый пароль неверный");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return SuccessResponseDto.builder()
                .success(true)
                .message("Пароль успешно изменен")
                .build();
    }

    @Override
    public PatientDashboardDto getDashboardByLogin(String login) {
        User patient = findByLogin(login);

        List<Appointment> appointments = appointmentRepository
                .findByPatientAndStatus_NameIn(patient, List.of("booked"));

        return PatientDashboardDto.builder()
                .user(userMapper.toPatientUserInfo(patient))
                .appointments(appointments.stream()
                        .map(appointmentMapper::toAppointmentShort)
                        .collect(Collectors.toList()))
                .build();
    }

                    @Override
                    public PatientDashboardDto getDashboardById(Long patientId) {
                        User patient = userRepository.findById(patientId)
                                .orElseThrow(() -> new ResourceNotFoundException("Пациент с id " + patientId + " не найден"));

                        if (patient.getRole() == null || !"patient".equals(patient.getRole().getName())) {
                            throw new BadRequestException("Пользователь не является пациентом");
                        }

                        List<Appointment> appointments = appointmentRepository
                                .findByPatientAndStatus_NameIn(patient, List.of("booked"));

                        return PatientDashboardDto.builder()
                                .user(userMapper.toPatientUserInfo(patient))
                                .appointments(appointments.stream()
                                        .map(appointmentMapper::toAppointmentShort)
                                        .collect(Collectors.toList()))
                                .build();
                    }


    @Override
    public PolicyResponseDto updatePolicyByLogin(String login, PolicyRequestDto request) {
        User patient = findByLogin(login);
        patient.setPolicyNumber(request.getPolicyNumber());
        userRepository.save(patient);

        return PolicyResponseDto.builder()
                .success(true)
                .message("Полис успешно привязан. Теперь вы можете записываться на прием.")
                .build();
    }

    private User findByLogin(String login) {
        String normalizedLogin = LoginNormalizer.normalize(login);
        return userRepository.findByLoginIgnoreCase(normalizedLogin)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
    }
}
