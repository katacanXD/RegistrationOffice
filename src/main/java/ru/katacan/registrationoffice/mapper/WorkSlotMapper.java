package ru.katacan.registrationoffice.mapper;

import ru.katacan.registrationoffice.dto.*;
import ru.katacan.registrationoffice.entity.WorkSlot;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class WorkSlotMapper {

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    public DoctorSlotsDto toDoctorSlots(Long doctorId, String date, List<WorkSlot> slots,
                                        DoctorSlotsDto.BreakDto breakDto) {
        List<DoctorSlotsDto.SlotDto> slotDtos = slots.stream()
                .flatMap(slot -> generateTimeSlots(slot).stream())
                .collect(Collectors.toList());

        return DoctorSlotsDto.builder()
                .doctorId(doctorId)
                .date(date)
                .slots(slotDtos)
                .break_(breakDto)
                .build();
    }

    private List<DoctorSlotsDto.SlotDto> generateTimeSlots(WorkSlot slot) {
        // Логика генерации слотов по slotMinutes
        // TODO: реализовать генерацию временных слотов
        return List.of();
    }

    /*
    public DoctorSlotsDto toDoctorSlots(Long doctorId, String date, List<WorkSlot> slots,
                                        DoctorSlotsDto.BreakDto breakDto, Set<LocalTime> bookedTimes) {
        List<DoctorSlotsDto.SlotDto> slotDtos = slots.stream()
                .flatMap(slot -> generateTimeSlots(slot, bookedTimes).stream())
                .collect(Collectors.toList());

        return DoctorSlotsDto.builder()
                .doctorId(doctorId)
                .date(date)
                .slots(slotDtos)
                .break_(breakDto)
                .build();
    }

    private List<DoctorSlotsDto.SlotDto> generateTimeSlots(WorkSlot slot, Set<LocalTime> bookedTimes) {
        List<DoctorSlotsDto.SlotDto> result = new java.util.ArrayList<>();
        LocalTime current = slot.getStartTime();
        LocalTime end = slot.getEndTime();
        int minutes = slot.getSlotMinutes();

        while (!current.plusMinutes(minutes).isAfter(end)) {
            LocalTime slotEnd = current.plusMinutes(minutes);

            boolean isDuringBreak = slot.getBreakStart() != null && slot.getBreakEnd() != null
                    && current.isBefore(slot.getBreakEnd())
                    && slotEnd.isAfter(slot.getBreakStart());

            if (!isDuringBreak) {
                result.add(DoctorSlotsDto.SlotDto.builder()
                        .time(current.format(timeFormatter))
                        .isAvailable(!bookedTimes.contains(current))
                        .build());
            }
            current = current.plusMinutes(minutes);
        }
        return result;
    }
    */

    public DoctorScheduleDto toDoctorSchedule(String date, List<WorkSlot> slots,
                                              DoctorScheduleDto.BreakDto breakDto) {
        List<DoctorScheduleDto.ScheduleSlotDto> slotDtos = slots.stream()
                .flatMap(slot -> generateScheduleSlots(slot).stream())
                .collect(Collectors.toList());

        return DoctorScheduleDto.builder()
                .date(date)
                .slots(slotDtos)
                .break_(breakDto)
                .build();
    }

    private List<DoctorScheduleDto.ScheduleSlotDto> generateScheduleSlots(WorkSlot slot) {
        // Логика генерации слотов для расписания
        // TODO: реализовать
        return List.of();
    }

    public DoctorCalendarDto toDoctorCalendar(List<WorkSlot> slots) {
        List<DoctorCalendarDto.WorkDayDto> workDays = slots.stream()
                .collect(Collectors.groupingBy(WorkSlot::getDate))
                .entrySet().stream()
                .map(entry -> {
                    // TODO: рассчитать статистику по дню
                    return DoctorCalendarDto.WorkDayDto.builder()
                            .date(entry.getKey().format(dateFormatter))
                            .hasAppointments(false)
                            .totalSlots(0)
                            .closedSlots(0)
                            .build();
                })
                .collect(Collectors.toList());

        return DoctorCalendarDto.builder()
                .workDays(workDays)
                .build();
    }

    /*
    public DoctorCalendarDto toDoctorCalendar(List<WorkSlot> slots, Map<LocalDate, Integer> bookedCountByDate) {
        List<DoctorCalendarDto.WorkDayDto> workDays = slots.stream()
                .collect(Collectors.groupingBy(WorkSlot::getDate))
                .entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    int totalSlots = entry.getValue().stream()
                            .mapToInt(this::calculateSlotsCount)
                            .sum();
                    int closedSlots = bookedCountByDate.getOrDefault(date, 0);

                    return DoctorCalendarDto.WorkDayDto.builder()
                            .date(date.format(dateFormatter))
                            .hasAppointments(closedSlots > 0)
                            .totalSlots(totalSlots)
                            .closedSlots(closedSlots)
                            .build();
                })
                .collect(Collectors.toList());

        return DoctorCalendarDto.builder()
                .workDays(workDays)
                .build();
    }

    private int calculateSlotsCount(WorkSlot slot) {
        long workMinutes = java.time.Duration.between(slot.getStartTime(), slot.getEndTime()).toMinutes();
        if (slot.getBreakStart() != null && slot.getBreakEnd() != null) {
            workMinutes -= java.time.Duration.between(slot.getBreakStart(), slot.getBreakEnd()).toMinutes();
        }
        return slot.getSlotMinutes() > 0 ? (int) (workMinutes / slot.getSlotMinutes()) : 0;
    }
    */
}