package ru.katacan.registrationoffice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppointmentDetailDto {
    private Long appointmentId;
    private String datetime; // "2026-11-03T13:15"
    private DoctorInfoDto doctor;
    private PatientInfoDto patient;
    private String address;

    @Data
    @Builder
    public static class DoctorInfoDto {
        private String fio;
    }

    @Data
    @Builder
    public static class PatientInfoDto {
        private String fio;
        private String policyNumber;
    }
}