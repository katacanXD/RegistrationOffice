package ru.katacan.registrationoffice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DoctorScheduleDto {
    private String date; // "2026-03-24"
    private List<ScheduleSlotDto> slots;
    private BreakDto break_;

    @Data
    @Builder
    public static class ScheduleSlotDto {
        private String time; // "13:15"
        private String type; // "closed", "open"
        private Long appointmentId;
    }

    @Data
    @Builder
    public static class BreakDto {
        private String start;
        private String end;
    }
}