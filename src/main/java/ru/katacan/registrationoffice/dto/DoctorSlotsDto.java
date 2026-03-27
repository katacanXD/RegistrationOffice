package ru.katacan.registrationoffice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DoctorSlotsDto {
    private Long doctorId;
    private String date; // "2026-03-24"
    private List<SlotDto> slots;
    private BreakDto break_;

    @Data
    @Builder
    public static class SlotDto {
        private String time; // "13:15"
        private Boolean isAvailable;
    }

    @Data
    @Builder
    public static class BreakDto {
        private String start;
        private String end;
    }
}