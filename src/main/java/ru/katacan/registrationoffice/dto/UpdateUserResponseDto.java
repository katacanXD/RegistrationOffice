package ru.katacan.registrationoffice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserResponseDto {
    private String fio;
    private String policyNumber;
    private String speciality;
    private String role;
}