package ru.katacan.registrationoffice.dto;

import lombok.Data;

@Data
public class CreateAppointmentRequestDto {
    private Long doctorId;
    private Long patientId;
    private String slotDatetime; // "2026-03-24T13:15"
    private String status;
}
