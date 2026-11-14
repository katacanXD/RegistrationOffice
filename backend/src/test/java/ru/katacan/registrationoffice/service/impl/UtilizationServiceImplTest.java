package ru.katacan.registrationoffice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.katacan.registrationoffice.dto.UtilizationReportDto;
import ru.katacan.registrationoffice.entity.Appointment;
import ru.katacan.registrationoffice.entity.DictRole;
import ru.katacan.registrationoffice.entity.DictStatus;
import ru.katacan.registrationoffice.entity.User;
import ru.katacan.registrationoffice.entity.WorkSlot;
import ru.katacan.registrationoffice.repository.AppointmentRepository;
import ru.katacan.registrationoffice.repository.UserRepository;
import ru.katacan.registrationoffice.repository.WorkSlotRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilizationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkSlotRepository workSlotRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    private UtilizationServiceImpl utilizationService;

    @BeforeEach
    void setUp() {
        utilizationService = new UtilizationServiceImpl(
                userRepository,
                workSlotRepository,
                appointmentRepository
        );
    }

    @Test
    void buildReport_counts_generated_slots_and_active_appointments() {
        LocalDate date = LocalDate.of(2026, 11, 13);
        User doctor = doctor(1L, "Иванов Иван Иванович", "Терапевт");
        WorkSlot workSlot = workSlot(doctor, date);

        Appointment booked = appointment(doctor, date.atTime(9, 0), "booked");
        Appointment canceled = appointment(doctor, date.atTime(10, 0), "canceled");

        when(userRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(workSlotRepository.findByDoctorAndDateBetween(doctor, date, date))
                .thenReturn(List.of(workSlot));
        when(appointmentRepository.findByDoctorIdAndDate(1L, date))
                .thenReturn(List.of(booked, canceled));

        UtilizationReportDto report = utilizationService.buildReport(date, date, 1L, null);

        assertEquals(1, report.getRows().size());
        assertEquals(4, report.getTotalSlots());
        assertEquals(1, report.getTotalBooked());
        assertEquals(0.25, report.getTotalUtilization());
        assertEquals(4, report.getRows().get(0).getSlotsTotal());
        assertEquals(1, report.getRows().get(0).getSlotsBooked());
        assertEquals(0.25, report.getRows().get(0).getUtilization());
    }

    @Test
    void buildCsv_uses_required_columns() {
        LocalDate date = LocalDate.of(2026, 11, 13);
        User doctor = doctor(1L, "Иванов Иван Иванович", "Терапевт");
        WorkSlot workSlot = workSlot(doctor, date);
        Appointment booked = appointment(doctor, date.atTime(9, 0), "booked");

        when(userRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(workSlotRepository.findByDoctorAndDateBetween(doctor, date, date))
                .thenReturn(List.of(workSlot));
        when(appointmentRepository.findByDoctorIdAndDate(1L, date))
                .thenReturn(List.of(booked));

        String csv = utilizationService.buildCsv(date, date, 1L, null);

        assertTrue(csv.startsWith("doctor_id,date,slots_total,slots_booked,utilization\n"));
        assertTrue(csv.contains("1,2026-11-13,4,1,0.2500"));
    }

    private static User doctor(Long id, String fio, String speciality) {
        DictRole role = new DictRole();
        role.setName("doctor");

        User doctor = new User();
        doctor.setId(id);
        doctor.setFullName(fio);
        doctor.setSpeciality(speciality);
        doctor.setRole(role);
        return doctor;
    }

    private static WorkSlot workSlot(User doctor, LocalDate date) {
        WorkSlot slot = new WorkSlot();
        slot.setDoctor(doctor);
        slot.setDate(date);
        slot.setStartTime(LocalTime.of(9, 0));
        slot.setEndTime(LocalTime.of(13, 0));
        slot.setSlotMinutes(60);
        return slot;
    }

    private static Appointment appointment(User doctor, LocalDateTime time, String statusName) {
        DictStatus status = new DictStatus();
        status.setName(statusName);

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(new User());
        appointment.setSlotDateTime(time);
        appointment.setStatus(status);
        return appointment;
    }
}
