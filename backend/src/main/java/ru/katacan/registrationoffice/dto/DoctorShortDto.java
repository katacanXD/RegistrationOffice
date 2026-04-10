package ru.katacan.registrationoffice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorShortDto {
    private Long doctorId;
    private String fio;
    private String speciality;
}
