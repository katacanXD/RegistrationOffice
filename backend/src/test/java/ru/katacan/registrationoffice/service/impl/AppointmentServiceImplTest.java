package ru.katacan.registrationoffice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.katacan.registrationoffice.dto.CreateAppointmentRequestDto;
import ru.katacan.registrationoffice.dto.UpdateAppointmentRequestDto;
import ru.katacan.registrationoffice.entity.Appointment;
import ru.katacan.registrationoffice.entity.DictStatus;
import ru.katacan.registrationoffice.entity.User;
import ru.katacan.registrationoffice.entity.WorkSlot;
import ru.katacan.registrationoffice.exception.SlotNotAvailableException;
import ru.katacan.registrationoffice.mapper.AppointmentMapper;
import ru.katacan.registrationoffice.repository.AppointmentRepository;
import ru.katacan.registrationoffice.repository.DictStatusRepository;
import ru.katacan.registrationoffice.repository.UserRepository;
import ru.katacan.registrationoffice.repository.WorkSlotRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DictStatusRepository statusRepository;

    @Mock
    private WorkSlotRepository workSlotRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    private AppointmentServiceImpl appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentServiceImpl(
                appointmentRepository,
                userRepository,
                statusRepository,
                workSlotRepository,
                appointmentMapper
        );
    }

    @Test
    void createAppointment_translates_db_uniqueness_conflict_to_slot_not_available() {
        LocalDateTime slotTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        User doctor = userWithId(1L);
        User patient = userWithId(2L);
        DictStatus booked = status("booked");

        CreateAppointmentRequestDto request = new CreateAppointmentRequestDto();
        request.setDoctorId(doctor.getId());
        request.setPatientId(patient.getId());
        request.setSlotDatetime(slotTime.toString());

        when(userRepository.findById(doctor.getId())).thenReturn(Optional.of(doctor));
        when(userRepository.findById(patient.getId())).thenReturn(Optional.of(patient));
        when(workSlotRepository.findContainingTime(eq(doctor.getId()), eq(slotTime.toLocalDate()), eq(slotTime.toLocalTime())))
                .thenReturn(List.of(workSlot(doctor, slotTime.toLocalDate())));
        when(appointmentRepository.existsByDoctorAndSlotDateTimeAndNotCanceled(doctor, slotTime)).thenReturn(false);
        when(statusRepository.findByName("booked")).thenReturn(Optional.of(booked));
        when(appointmentRepository.saveAndFlush(any(Appointment.class)))
                .thenThrow(new DataIntegrityViolationException("active appointment for this doctor slot"));

        SlotNotAvailableException ex = assertThrows(
                SlotNotAvailableException.class,
                () -> appointmentService.createAppointment(request)
        );

        assertEquals("Этот слот уже занят", ex.getMessage());
    }

    @Test
    void updateAppointment_translates_db_uniqueness_conflict_to_slot_not_available() {
        LocalDateTime newSlotTime = LocalDateTime.now().plusDays(1).withHour(11).withMinute(0).withSecond(0).withNano(0);
        User doctor = userWithId(5L);

        Appointment appointment = new Appointment();
        appointment.setId(9L);
        appointment.setDoctor(doctor);
        appointment.setStatus(status("booked"));

        UpdateAppointmentRequestDto request = UpdateAppointmentRequestDto.builder()
                .slotDatetime(newSlotTime.toString())
                .build();

        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
        when(workSlotRepository.findContainingTime(eq(doctor.getId()), eq(newSlotTime.toLocalDate()), eq(newSlotTime.toLocalTime())))
                .thenReturn(List.of(workSlot(doctor, newSlotTime.toLocalDate())));
        when(appointmentRepository.existsActiveAppointment(doctor, newSlotTime, appointment.getId())).thenReturn(false);
        when(appointmentRepository.saveAndFlush(any(Appointment.class)))
                .thenThrow(new DataIntegrityViolationException("active appointment for this doctor slot"));

        SlotNotAvailableException ex = assertThrows(
                SlotNotAvailableException.class,
                () -> appointmentService.updateAppointment(appointment.getId(), request)
        );

        assertEquals("Этот слот уже занят другим пациентом", ex.getMessage());
    }

    private static User userWithId(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private static DictStatus status(String name) {
        DictStatus status = new DictStatus();
        status.setName(name);
        return status;
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
}


