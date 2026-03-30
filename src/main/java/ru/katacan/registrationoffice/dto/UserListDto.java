package ru.katacan.registrationoffice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserListDto {
    private List<UserInfoDto> users;
    private Long totalFound;
    private long totalElements; // Всего записей
    private int totalPages;    // Всего страниц
    private int currentPage;   // Текущая страница
}
