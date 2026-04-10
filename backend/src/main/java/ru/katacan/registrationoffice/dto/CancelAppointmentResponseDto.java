package ru.katacan.registrationoffice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CancelAppointmentResponseDto {
    private Long appointmentId;
    private String status;
    private String message;
}