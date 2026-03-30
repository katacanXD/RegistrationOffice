package ru.katacan.registrationoffice.mapper;

import org.springframework.data.domain.Page;
import ru.katacan.registrationoffice.dto.*;
import ru.katacan.registrationoffice.entity.User;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    private final String CLINIC_ADDRESS = "г. Москва, ул. Менделеева, д. 15с2";

    public RegistrationResponseDto toRegistrationResponse(User user, String message) {
        return RegistrationResponseDto.builder()
                .userId(user.getId())
                .fio(user.getFullName())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .message(message)
                .build();
    }

    public LoginResponseDto toLoginResponse(User user, String token) {
        return LoginResponseDto.builder()
                .accessToken(token)
                .user(UserInfoDto.builder()
                        .userId(user.getId())
                        .fio(user.getFullName())
                        .role(user.getRole() != null ? user.getRole().getName() : null)
                        .policyNumber(user.getPolicyNumber())
                        .build())
                .build();
    }


    public UserInfoDto toUserInfo(User user) {
        return UserInfoDto.builder()
                .userId(user.getId())
                .fio(user.getFullName())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .policyNumber(user.getPolicyNumber())
                .build();
    }

    public PatientUserInfoDto toPatientUserInfo(User user) {
        return PatientUserInfoDto.builder()
                .fio(user.getFullName())
                .policyNumber(user.getPolicyNumber())
                .isPolicyVerified(user.getPolicyNumber() != null && !user.getPolicyNumber().isEmpty())
                .build();
    }

    public DoctorShortDto toDoctorShort(User doctor) {
        return DoctorShortDto.builder()
                .doctorId(doctor.getId())
                .fio(doctor.getFullName())
                .speciality(doctor.getSpeciality())
                .build();
    }

    public PatientListDto.PatientDto toPatientDto(User patient) {
        return PatientListDto.PatientDto.builder()
                .userId(patient.getId())
                .fio(patient.getFullName())
                .policyNumber(patient.getPolicyNumber())
                .build();
    }

    public AppointmentDetailDto.PatientInfoDto toPatientInfo(User patient) {
        return AppointmentDetailDto.PatientInfoDto.builder()
                .fio(patient.getFullName())
                .policyNumber(patient.getPolicyNumber())
                .build();
    }

    public AppointmentDetailDto.DoctorInfoDto toDoctorInfo(User doctor) {
        return AppointmentDetailDto.DoctorInfoDto.builder()
                .fio(doctor.getFullName())
                .build();
    }

    public DoctorListDto toDoctorList(List<DoctorShortDto> doctorDtos, org.springframework.data.domain.Page<ru.katacan.registrationoffice.entity.User> doctorPage) {
        return DoctorListDto.builder()
                .doctors(doctorDtos)
                .totalFound(doctorPage.getTotalElements())
                .totalPages(doctorPage.getTotalPages())
                .currentPage(doctorPage.getNumber())
                .build();
    }

    public UserListDto toUserList(List<UserInfoDto> dtos, Page<User> page) {
        return UserListDto.builder()
                .users(dtos)
                .totalFound(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .build();
    }

    public PatientListDto toPatientList(List<PatientListDto.PatientDto> dtos, Page<User> page) {
        return PatientListDto.builder()
                .patients(dtos)
                .totalFound(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .build();
    }
}