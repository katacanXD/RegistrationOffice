package ru.katacan.registrationoffice.service;
import org.springframework.data.domain.Pageable;
import ru.katacan.registrationoffice.dto.*;
import ru.katacan.registrationoffice.entity.User;

public interface UserService {
    //DoctorListDto searchDoctors(String search);
    //PatientListDto searchPatients(String search);
    //UserListDto searchUsers(String search);
    UserInfoDto getUserById(Long userId);
    UpdateUserResponseDto updateUser(Long userId, UpdateUserRequestDto request);
    SuccessResponseDto deleteUser(Long userId);
    SuccessResponseDto changePassword(Long userId, ChangePasswordRequestDto request);
    PatientDashboardDto getDashboard(Long patientId);
    PolicyResponseDto updatePolicy(Long patientId, PolicyRequestDto request);
    //pageable
    DoctorListDto searchDoctors(String search, String specialty, Pageable pageable);
    PatientListDto searchPatients(String search, Pageable pageable);
    UserListDto searchUsers(String search, Pageable pageable);


}