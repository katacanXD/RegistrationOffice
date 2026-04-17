package ru.katacan.registrationoffice.mapper;

import ru.katacan.registrationoffice.dto.*;
import ru.katacan.registrationoffice.entity.Appointment;
import ru.katacan.registrationoffice.entity.WorkSlot;
import ru.katacan.registrationoffice.util.SlotGenerator;
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
        List<LocalTime> slots = SlotGenerator.generateSlots(
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getSlotMinutes(),
                slot.getBreakStart(),
                slot.getBreakEnd()
        );

        return slots.stream()
                .map(time -> DoctorSlotsDto.SlotDto.builder()
                        .time(time.format(timeFormatter))
                        .isAvailable(true)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Генерирует DTO с учетом забронированных слотов
     */
    public DoctorSlotsDto toDoctorSlotsWithAvailability(Long doctorId, String date, List<WorkSlot> slots,
                                                        DoctorSlotsDto.BreakDto breakDto, Set<LocalTime> bookedTimes) {
        List<DoctorSlotsDto.SlotDto> slotDtos = slots.stream()
                .flatMap(slot -> generateTimeSlotsWithAvailability(slot, bookedTimes).stream())
                .collect(Collectors.toList());

        return DoctorSlotsDto.builder()
                .doctorId(doctorId)
                .date(date)
                .slots(slotDtos)
                .break_(breakDto)
                .build();
    }

    private List<DoctorSlotsDto.SlotDto> generateTimeSlotsWithAvailability(WorkSlot slot, Set<LocalTime> bookedTimes) {
        List<LocalTime> slots = SlotGenerator.generateSlots(
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getSlotMinutes(),
                slot.getBreakStart(),
                slot.getBreakEnd()
        );

        return slots.stream()
                .map(time -> DoctorSlotsDto.SlotDto.builder()
                        .time(time.format(timeFormatter))
                        .isAvailable(!bookedTimes.contains(time))
                        .build())
                .collect(Collectors.toList());
    }

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
        List<LocalTime> slots = SlotGenerator.generateSlots(
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getSlotMinutes(),
                slot.getBreakStart(),
                slot.getBreakEnd()
        );

        return slots.stream()
                .map(time -> DoctorScheduleDto.ScheduleSlotDto.builder()
                        .time(time.format(timeFormatter))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Генерирует расписание с информацией о забронированных слотах
     */
    public DoctorScheduleDto toDoctorScheduleWithBookings(String date, List<WorkSlot> slots,
                                                         DoctorScheduleDto.BreakDto breakDto,
                                                         Map<LocalTime, Appointment> appointmentsByTime) {
        List<DoctorScheduleDto.ScheduleSlotDto> slotDtos = slots.stream()
                .flatMap(slot -> generateScheduleSlotsWithBookings(slot, appointmentsByTime).stream())
                .collect(Collectors.toList());

        return DoctorScheduleDto.builder()
                .date(date)
                .slots(slotDtos)
                .break_(breakDto)
                .build();
    }

    private List<DoctorScheduleDto.ScheduleSlotDto> generateScheduleSlotsWithBookings(
            WorkSlot slot,
            Map<LocalTime, Appointment> appointmentsByTime) {
        List<LocalTime> slots = SlotGenerator.generateSlots(
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getSlotMinutes(),
                slot.getBreakStart(),
                slot.getBreakEnd()
        );

        return slots.stream()
                .map(time -> {
                    Appointment appointment = appointmentsByTime.get(time);
                    return DoctorScheduleDto.ScheduleSlotDto.builder()
                            .time(time.format(timeFormatter))
                            .type(appointment != null ? "closed" : "open")
                            .appointmentId(appointment != null ? appointment.getId() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public DoctorCalendarDto toDoctorCalendar(List<WorkSlot> slots) {
        List<DoctorCalendarDto.WorkDayDto> workDays = slots.stream()
                .collect(Collectors.groupingBy(WorkSlot::getDate))
                .entrySet().stream()
                .map(entry -> {
                    int totalSlots = entry.getValue().stream()
                            .mapToInt(this::calculateSlotsCount)
                            .sum();

                    return DoctorCalendarDto.WorkDayDto.builder()
                            .date(entry.getKey().format(dateFormatter))
                            .hasAppointments(false) // будет переопределено в сервисе
                            .totalSlots(totalSlots)
                            .closedSlots(0) // будет переопределено в сервисе
                            .build();
                })
                .collect(Collectors.toList());

        return DoctorCalendarDto.builder()
                .workDays(workDays)
                .build();
    }

    /**
     * Генерирует календарь с информацией о забронированных слотах
     */
    public DoctorCalendarDto toDoctorCalendarWithBookings(List<WorkSlot> slots, Map<LocalDate, Integer> bookedCountByDate) {
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

    /**
     * Рассчитывает количество слотов в рабочий день
     */
    private int calculateSlotsCount(WorkSlot slot) {
        return SlotGenerator.calculateTotalSlots(
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getSlotMinutes(),
                slot.getBreakStart(),
                slot.getBreakEnd()
        );
    }
}