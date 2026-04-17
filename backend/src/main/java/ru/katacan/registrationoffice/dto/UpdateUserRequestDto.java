package ru.katacan.registrationoffice.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequestDto {
    @Pattern(regexp = "^\\d{16}$", message = "Номер полиса должен содержать 16 цифр")
    private String policyNumber;

    @Size(max = 120, message = "Специальность не должна превышать 120 символов")
    @Pattern(regexp = "^[\\p{L}\\d\\s.,()\\-]+$", message = "Специальность содержит недопустимые символы")
    private String speciality;

    @Size(min = 3, max = 30, message = "Роль должна содержать от 3 до 30 символов")
    @Pattern(regexp = "^[a-z_]+$", message = "Роль может содержать только латинские строчные буквы и '_'")
    private String role;
}