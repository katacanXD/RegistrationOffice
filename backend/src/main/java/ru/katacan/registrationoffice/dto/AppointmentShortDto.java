package ru.katacan.registrationoffice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppointmentShortDto {
    private Long appointmentId;
    private String speciality;
    private String doctorFio;
    private String datetime; // "2026-11-02T18:30"
    private String address; // константа
    private String status; // "booked", "canceled", "completed"
}
