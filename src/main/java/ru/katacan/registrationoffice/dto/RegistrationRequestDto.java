package ru.katacan.registrationoffice.dto;

import lombok.Data;

@Data
public class RegistrationRequestDto {
    private String login;
    private String fio;
    private String password;
}

