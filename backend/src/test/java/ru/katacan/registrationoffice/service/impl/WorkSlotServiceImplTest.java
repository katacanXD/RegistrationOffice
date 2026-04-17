package ru.katacan.registrationoffice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.katacan.registrationoffice.dto.DoctorCalendarDto;
import ru.katacan.registrationoffice.dto.DoctorScheduleDto;
import ru.katacan.registrationoffice.dto.DoctorSlotsDto;
import ru.katacan.registrationoffice.entity.Appointment;
import ru.katacan.registrationoffice.entity.DictStatus;
import ru.katacan.registrationoffice.entity.User;
import ru.katacan.registrationoffice.entity.WorkSlot;
import ru.katacan.registrationoffice.mapper.WorkSlotMapper;
import ru.katacan.registrationoffice.repository.AppointmentRepository;
import ru.katacan.registrationoffice.repository.UserRepository;
import ru.katacan.registrationoffice.repository.WorkSlotRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkSlotServiceImplTest {

    @Mock
    private WorkSlotRepository workSlotRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    private WorkSlotServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WorkSlotServiceImpl(
                workSlotRepository,
                userRepository,
                new WorkSlotMapper(),
                appointmentRepository
        );
    }

    @Test
    void getDoctorSlots_reads_only_without_creating_work_slots() {
        LocalDate date = LocalDate.of(2026, 4, 20);
        User doctor = doctor(10L);
        List<WorkSlot> daySlots = defaultDaySlots(doctor, date);

        Appointment booked = appointment(date.atTime(10, 0), "booked");
        Appointment canceled = appointment(date.atTime(15, 0), "canceled");

        when(userRepository.findById(doctor.getId())).thenReturn(Optional.of(doctor));
        when(workSlotRepository.findByDoctorAndDate(doctor, date)).thenReturn(daySlots);
        when(appointmentRepository.findByDoctorIdAndDate(doctor.getId(), date)).thenReturn(List.of(booked, canceled));

        DoctorSlotsDto result = service.getDoctorSlots(doctor.getId(), date);

        assertNotNull(result);
        assertNotNull(result.getBreak_());
        assertEquals("13:00", result.getBreak_().getStart());
        assertEquals("14:00", result.getBreak_().getEnd());
        assertEquals(8, result.getSlots().size());

        boolean tenIsUnavailable = result.getSlots().stream()
                .anyMatch(slot -> "10:00".equals(slot.getTime()) && !slot.getIsAvailable());
        boolean fifteenIsAvailable = result.getSlots().stream()
                .anyMatch(slot -> "15:00".equals(slot.getTime()) && slot.getIsAvailable());

        assertTrue(tenIsUnavailable);
        assertTrue(fifteenIsAvailable);

        verify(workSlotRepository).findByDoctorAndDate(doctor, date);
        verify(workSlotRepository, never()).save(any());
        verify(workSlotRepository, never()).saveAll(any());
    }

    @Test
    void getDoctorSchedule_reads_only_without_creating_work_slots() {
        LocalDate date = LocalDate.of(2026, 4, 21);
        User doctor = doctor(20L);
        List<WorkSlot> daySlots = defaultDaySlots(doctor, date);

        when(userRepository.findById(doctor.getId())).thenReturn(Optional.of(doctor));
        when(workSlotRepository.findByDoctorAndDate(doctor, date)).thenReturn(daySlots);
        when(appointmentRepository.findByDoctorIdAndDate(doctor.getId(), date)).thenReturn(List.of());

        DoctorScheduleDto result = service.getDoctorSchedule(doctor.getId(), date);

        assertNotNull(result);
        assertEquals(8, result.getSlots().size());
        assertNotNull(result.getBreak_());
        assertEquals("13:00", result.getBreak_().getStart());
        assertEquals("14:00", result.getBreak_().getEnd());

        verify(workSlotRepository).findByDoctorAndDate(doctor, date);
        verify(workSlotRepository, never()).save(any());
        verify(workSlotRepository, never()).saveAll(any());
    }

    @Test
    void getDoctorSlotsBetweenDates_reads_only_without_creating_work_slots() {
        LocalDate start = LocalDate.of(2026, 4, 22);
        LocalDate end = LocalDate.of(2026, 4, 22);
        User doctor = doctor(30L);
        List<WorkSlot> daySlots = defaultDaySlots(doctor, start);

        Appointment booked = appointment(start.atTime(9, 0), "booked");

        when(userRepository.findById(doctor.getId())).thenReturn(Optional.of(doctor));
        when(workSlotRepository.findByDoctorAndDateBetween(doctor, start, end)).thenReturn(daySlots);
        when(appointmentRepository.findByDoctorAndSlotDateTimeBetween(
                doctor,
                start.atStartOfDay(),
                end.atTime(23, 59, 59)
        )).thenReturn(List.of(booked));

        DoctorCalendarDto result = service.getDoctorSlotsBetweenDates(doctor.getId(), start, end);

        assertNotNull(result);
        assertEquals(1, result.getWorkDays().size());
        assertEquals(8, result.getWorkDays().getFirst().getTotalSlots());
        assertEquals(1, result.getWorkDays().getFirst().getClosedSlots());

        verify(workSlotRepository).findByDoctorAndDateBetween(doctor, start, end);
        verify(workSlotRepository, never()).save(any());
        verify(workSlotRepository, never()).saveAll(any());
    }

    private static User doctor(Long id) {
        User doctor = new User();
        doctor.setId(id);
        return doctor;
    }

    private static Appointment appointment(LocalDateTime dateTime, String statusName) {
        Appointment appointment = new Appointment();
        appointment.setSlotDateTime(dateTime);

        DictStatus status = new DictStatus();
        status.setName(statusName);
        appointment.setStatus(status);

        return appointment;
    }

    private static List<WorkSlot> defaultDaySlots(User doctor, LocalDate date) {
        WorkSlot morning = new WorkSlot();
        morning.setDoctor(doctor);
        morning.setDate(date);
        morning.setStartTime(LocalTime.of(9, 0));
        morning.setEndTime(LocalTime.of(13, 0));
        morning.setSlotMinutes(60);
        morning.setBreakStart(LocalTime.of(13, 0));
        morning.setBreakEnd(LocalTime.of(14, 0));

        WorkSlot evening = new WorkSlot();
        evening.setDoctor(doctor);
        evening.setDate(date);
        evening.setStartTime(LocalTime.of(14, 0));
        evening.setEndTime(LocalTime.of(18, 0));
        evening.setSlotMinutes(60);
        evening.setBreakStart(LocalTime.of(13, 0));
        evening.setBreakEnd(LocalTime.of(14, 0));

        return List.of(morning, evening);
    }
}

