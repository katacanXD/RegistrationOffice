package ru.katacan.registrationoffice.service;
import org.springframework.data.domain.Pageable;
import ru.katacan.registrationoffice.dto.*;

public interface UserService {
    DoctorListDto searchDoctors(String search, Pageable pageable);
    PatientListDto searchPatients(String search, Pageable pageable);
    UserListDto searchUsers(String search, Pageable pageable);

    UserInfoDto getCurrentUser(String login);
    SuccessResponseDto changeOwnPassword(String login, ChangePasswordRequestDto request);

    UserInfoDto getUserById(Long userId);
    UpdateUserResponseDto updateUser(Long userId, UpdateUserRequestDto request);
    SuccessResponseDto deleteUser(Long userId);

    PatientDashboardDto getDashboardByLogin(String login);

    PolicyResponseDto updatePolicyByLogin(String login, PolicyRequestDto request);
}
