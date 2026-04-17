package ru.katacan.registrationoffice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationRequestDto {
    @NotBlank(message = "Логин обязателен")
    @Size(min = 3, max = 64, message = "Логин должен содержать от 3 до 64 символов")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Логин может содержать только латинские буквы, цифры, '.', '_' и '-' ")
    private String login;

    @NotBlank(message = "ФИО обязательно")
    @Size(min = 2, max = 120, message = "ФИО должно содержать от 2 до 120 символов")
    @Pattern(regexp = "^[\\p{L}\\s'\\-]+$", message = "ФИО содержит недопустимые символы")
    private String fio;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, max = 72, message = "Пароль должен содержать от 8 до 72 символов")
    private String password;
}

