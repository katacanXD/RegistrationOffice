package ru.katacan.registrationoffice.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PatientListDto {
    public List<PatientDto> patients;
    private long totalElements;
    private long totalFound;
    private int totalPages;
    private int currentPage;

    @Data
    @Builder
    public static class PatientDto {
        private Long userId;
        private String fio;
        private String policyNumber;
        private long totalElements; // Всего записей
        private int totalPages;    // Всего страниц
        private int currentPage;

    }
}