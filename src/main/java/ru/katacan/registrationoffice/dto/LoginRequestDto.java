package ru.katacan.registrationoffice.dto;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String login;
    private String password;
}

