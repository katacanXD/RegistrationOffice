package ru.katacan.registrationoffice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuccessResponseDto {
    private Boolean success;
    private String message;
}