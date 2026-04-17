package ru.katacan.registrationoffice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PolicyRequestDto {
    @NotBlank(message = "Номер полиса обязателен")
    @Pattern(regexp = "^\\d{16}$", message = "Номер полиса должен содержать 16 цифр")
    private String policyNumber;
}