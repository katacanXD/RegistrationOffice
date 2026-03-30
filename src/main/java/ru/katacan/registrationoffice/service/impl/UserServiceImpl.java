
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
    public DoctorListDto searchDoctors(String search, String specialty, Pageable pageable) {
        String searchParam = (search == null) ? "" : search;
        String specialtyParam = (specialty == null) ? "" : specialty;

        Page<User> doctorPage = userRepository.searchDoctors(searchParam, specialtyParam, pageable);

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с id " + userId + " не найден"));

        if (request.getPolicyNumber() != null) {
            user.setPolicyNumber(request.getPolicyNumber());
        }
        if (request.getSpeciality() != null) {
            user.setSpeciality(request.getSpeciality());
        }
        if (request.getRole() != null) {
            DictRole role = dictRoleRepository.findByName(request.getRole())
                    .orElseThrow(() -> new BadRequestException("Роль '" + request.getRole() + "' не найдена"));
            user.setRole(role);
        }

        User updatedUser = userRepository.save(user);
        return UpdateUserResponseDto.builder()
                .fio(updatedUser.getFullName())
                .policyNumber(updatedUser.getPolicyNumber())
                .speciality(updatedUser.getSpeciality())
                .role(updatedUser.getRole() != null ? updatedUser.getRole().getName() : null)
                .build();
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
