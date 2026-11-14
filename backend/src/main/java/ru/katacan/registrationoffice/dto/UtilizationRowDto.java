package ru.katacan.registrationoffice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UtilizationRowDto {
    private Long doctorId;
    private String doctorFio;
    private String speciality;
    private LocalDate date;
    private int slotsTotal;
    private int slotsBooked;
    private double utilization;
}
