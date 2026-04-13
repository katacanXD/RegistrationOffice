package ru.katacan.registrationoffice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CsvExportResultDto {
    private String fileName;
    private Integer rowsExported;
    private LocalDateTime timestamp;
    private String status; // SUCCESS, PARTIAL, FAILED
    private String message;
    private String csvContent; // Base64 encoded или сырой контент
}
