package ru.katacan.registrationoffice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CsvImportErrorDto {
    private Integer rowNumber;
    private String errorMessage;
    private String data; // Сырые данные строки
}
