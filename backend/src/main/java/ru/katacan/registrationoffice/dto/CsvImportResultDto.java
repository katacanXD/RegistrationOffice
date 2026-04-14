package ru.katacan.registrationoffice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CsvImportResultDto {
    private Integer rowsProcessed;
    private Integer rowsSuccessful;
    private Integer rowsFailed;
    private List<CsvImportErrorDto> errors;
    private LocalDateTime timestamp;
    private String status; // SUCCESS, PARTIAL, FAILED
    private String message;
}
