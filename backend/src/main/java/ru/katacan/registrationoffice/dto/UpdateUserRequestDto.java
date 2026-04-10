package ru.katacan.registrationoffice.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserRequestDto {
    @Pattern(regexp = "^\\d{16}$", message = "Номер полиса должен содержать 16 цифр")
    private String policyNumber;

    private String speciality;

    @Pattern(regexp = "^[a-z_]+$", message = "Роль может содержать только латинские строчные буквы и '_'")
    private String role;
}