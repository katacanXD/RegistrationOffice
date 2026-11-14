package ru.katacan.registrationoffice.service;

import ru.katacan.registrationoffice.dto.UtilizationReportDto;

import java.time.LocalDate;

public interface UtilizationService {

    UtilizationReportDto buildReport(LocalDate from, LocalDate to, Long doctorId, String speciality);

    String buildCsv(LocalDate from, LocalDate to, Long doctorId, String speciality);
}
