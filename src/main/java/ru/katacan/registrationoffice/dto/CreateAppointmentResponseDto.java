package ru.katacan.registrationoffice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateAppointmentResponseDto {
    private Long appointmentId;
    private String message;
    private AppointmentDetailsDto details;

    @Data
    @Builder
    public static class AppointmentDetailsDto {
        private String doctorFio;
        private String speciality;
        private String address;
    }
}