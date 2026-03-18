package ru.katacan.registrationoffice.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DoctorListDto {
    private List<DoctorShortDto> doctors;
    private Long totalFound;
    private long totalElements; // Всего записей
    private int totalPages;    // Всего страниц
    private int currentPage;   // Текущая страница
}

