package ru.katacan.registrationoffice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDto {
    private String accessToken;

    // Internal transport field for controller cookie handling.
    @JsonIgnore
    private String refreshToken;

    private UserInfoDto user;
}