package ru.katacan.registrationoffice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateAppointmentResponseDto {
    private Long doctorId;
    private String slotDatetime;
    private String status;
    private String message;
}

