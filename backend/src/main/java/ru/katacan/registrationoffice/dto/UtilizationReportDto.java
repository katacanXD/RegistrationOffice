package ru.katacan.registrationoffice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class UtilizationReportDto {
    private LocalDate from;
    private LocalDate to;
    private List<UtilizationRowDto> rows;
    private int totalSlots;
    private int totalBooked;
    private double totalUtilization;
}
