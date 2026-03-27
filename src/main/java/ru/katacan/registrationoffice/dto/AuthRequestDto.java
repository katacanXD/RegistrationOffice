package ru.katacan.registrationoffice.dto;

import lombok.Data;

@Data
public class AuthRequestDto {
    private String fio;
    private String password;
}