package ru.katacan.registrationoffice.service;

import ru.katacan.registrationoffice.dto.CsvExportResultDto;
import ru.katacan.registrationoffice.dto.CsvImportResultDto;

public interface CsvService {
    // Export methods
    CsvExportResultDto exportDoctors();
    CsvExportResultDto exportPatients();
    CsvExportResultDto exportAppointments();
    CsvExportResultDto exportWorkSlots();

    // Import methods
    CsvImportResultDto importDoctors(String csvContent);
    CsvImportResultDto importPatients(String csvContent);
    CsvImportResultDto importAppointments(String csvContent);
    CsvImportResultDto importWorkSlots(String csvContent);
}
