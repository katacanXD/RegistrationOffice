package ru.katacan.registrationoffice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequestDto {
    @NotBlank(message = "Старый пароль обязателен")
    @Size(min = 8, max = 72, message = "Старый пароль должен содержать от 8 до 72 символов")
    private String oldPassword;

    @NotBlank(message = "Новый пароль обязателен")
    @Size(min = 8, max = 72, message = "Новый пароль должен содержать от 8 до 72 символов")
    private String newPassword;
}