package ru.katacan.registrationoffice.dto;

import lombok.Data;

@Data
public class CreateOwnAppointmentRequestDto {
    private Long doctorId;
    private String slotDatetime; // "2026-03-24T13:15"
}

