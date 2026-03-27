package ru.katacan.registrationoffice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DoctorCalendarDto {
    private List<WorkDayDto> workDays;

    @Data
    @Builder
    public static class WorkDayDto {
        private String date; // "2026-03-24"
        private Boolean hasAppointments;
        private Integer totalSlots;
        private Integer closedSlots;
    }
}