package ru.katacan.registrationoffice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.katacan.registrationoffice.dto.AppointmentOperationDto;
import ru.katacan.registrationoffice.dto.CancelAppointmentRequestDto;
import ru.katacan.registrationoffice.dto.CancelAppointmentResponseDto;
import ru.katacan.registrationoffice.dto.CreateAppointmentRequestDto;
import ru.katacan.registrationoffice.dto.CreateAppointmentResponseDto;
import ru.katacan.registrationoffice.dto.UpdateAppointmentRequestDto;
import ru.katacan.registrationoffice.entity.Appointment;
import ru.katacan.registrationoffice.entity.AppointmentOperation;
import ru.katacan.registrationoffice.entity.DictRole;
import ru.katacan.registrationoffice.entity.DictStatus;
import ru.katacan.registrationoffice.entity.User;
import ru.katacan.registrationoffice.entity.WorkSlot;
import ru.katacan.registrationoffice.exception.PatientScheduleConflictException;
import ru.katacan.registrationoffice.exception.SlotNotAvailableException;
import ru.katacan.registrationoffice.mapper.AppointmentMapper;
import ru.katacan.registrationoffice.repository.AppointmentOperationRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    private AppointmentOperationRepository operationRepository;

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
                operationRepository,
                appointmentMapper
        );
    }

    @Test
    void createAppointment_translates_db_uniqueness_conflict_to_slot_not_available() {
        LocalDateTime slotTime = futureSlot(10);
        User doctor = userWithId(1L, "doctor");
        User patient = userWithId(2L, "patient");
        DictStatus booked = status("booked");

        CreateAppointmentRequestDto request = request(doctor.getId(), patient.getId(), slotTime);

        when(userRepository.findById(doctor.getId())).thenReturn(Optional.of(doctor));
        when(userRepository.findById(patient.getId())).thenReturn(Optional.of(patient));
        when(workSlotRepository.findContainingTime(eq(doctor.getId()), eq(slotTime.toLocalDate()), eq(slotTime.toLocalTime())))
                .thenReturn(List.of(workSlot(doctor, slotTime.toLocalDate())));
        when(appointmentRepository.findActiveByDoctorAndSlot(doctor, slotTime)).thenReturn(List.of());
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
    void createAppointment_returns_existing_appointment_for_same_patient() {
        LocalDateTime slotTime = futureSlot(10);
        User doctor = userWithId(1L, "doctor");
        User patient = userWithId(2L, "patient");
        Appointment existing = appointment(7L, doctor, patient, slotTime, "booked");

        CreateAppointmentRequestDto request = request(doctor.getId(), patient.getId(), slotTime);
        CreateAppointmentResponseDto expected = CreateAppointmentResponseDto.builder()
                .appointmentId(7L)
                .message("Запись уже существует")
                .replayed(true)
                .build();

        when(userRepository.findById(doctor.getId())).thenReturn(Optional.of(doctor));
        when(userRepository.findById(patient.getId())).thenReturn(Optional.of(patient));
        when(workSlotRepository.findContainingTime(eq(doctor.getId()), eq(slotTime.toLocalDate()), eq(slotTime.toLocalTime())))
                .thenReturn(List.of(workSlot(doctor, slotTime.toLocalDate())));
        when(appointmentRepository.findActiveByDoctorAndSlot(doctor, slotTime)).thenReturn(List.of(existing));
        when(appointmentMapper.toCreateResponse(existing, doctor)).thenReturn(expected);

        CreateAppointmentResponseDto result = appointmentService.createAppointment(request);

        assertEquals(7L, result.getAppointmentId());
        assertTrue(Boolean.TRUE.equals(result.getReplayed()));
        verify(appointmentRepository, never()).saveAndFlush(any(Appointment.class));
        verify(operationRepository, never()).save(any(AppointmentOperation.class));
    }

    @Test
    void createAppointment_rejects_patient_schedule_conflict() {
        LocalDateTime slotTime = futureSlot(10);
        User doctor = userWithId(1L, "doctor");
        User patient = userWithId(2L, "patient");

        CreateAppointmentRequestDto request = request(doctor.getId(), patient.getId(), slotTime);

        when(userRepository.findById(doctor.getId())).thenReturn(Optional.of(doctor));
        when(userRepository.findById(patient.getId())).thenReturn(Optional.of(patient));
        when(workSlotRepository.findContainingTime(eq(doctor.getId()), eq(slotTime.toLocalDate()), eq(slotTime.toLocalTime())))
                .thenReturn(List.of(workSlot(doctor, slotTime.toLocalDate())));
        when(appointmentRepository.findActiveByDoctorAndSlot(doctor, slotTime)).thenReturn(List.of());
        when(appointmentRepository.existsActiveAppointmentForPatient(patient, slotTime, -1L)).thenReturn(true);

        PatientScheduleConflictException ex = assertThrows(
                PatientScheduleConflictException.class,
                () -> appointmentService.createAppointment(request)
        );

        assertEquals("У пациента уже есть запись на это время", ex.getMessage());
        verify(appointmentRepository, never()).saveAndFlush(any(Appointment.class));
    }

    @Test
    void createAppointment_records_created_operation() {
        LocalDateTime slotTime = futureSlot(10);
        User doctor = userWithId(1L, "doctor");
        User patient = userWithId(2L, "patient");
        User registrar = userWithId(3L, "registrar");
        DictStatus booked = status("booked");

        CreateAppointmentRequestDto request = request(doctor.getId(), patient.getId(), slotTime);
        CreateAppointmentResponseDto expected = CreateAppointmentResponseDto.builder()
                .appointmentId(11L)
                .replayed(false)
                .build();

        when(userRepository.findById(doctor.getId())).thenReturn(Optional.of(doctor));
        when(userRepository.findById(patient.getId())).thenReturn(Optional.of(patient));
        when(userRepository.findByLoginIgnoreCase("registrar")).thenReturn(Optional.of(registrar));
        when(workSlotRepository.findContainingTime(eq(doctor.getId()), eq(slotTime.toLocalDate()), eq(slotTime.toLocalTime())))
                .thenReturn(List.of(workSlot(doctor, slotTime.toLocalDate())));
        when(appointmentRepository.findActiveByDoctorAndSlot(doctor, slotTime)).thenReturn(List.of());
        when(statusRepository.findByName("booked")).thenReturn(Optional.of(booked));
        when(appointmentRepository.saveAndFlush(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment saved = invocation.getArgument(0);
            saved.setId(11L);
            return saved;
        });
        when(appointmentMapper.toCreateResponse(any(Appointment.class), eq(doctor))).thenReturn(expected);

        CreateAppointmentResponseDto result = appointmentService.createAppointment(request, "registrar");

        assertNotNull(result);
        assertEquals(11L, result.getAppointmentId());

        ArgumentCaptor<AppointmentOperation> captor = ArgumentCaptor.forClass(AppointmentOperation.class);
        verify(operationRepository).save(captor.capture());
        AppointmentOperation operation = captor.getValue();
        assertEquals("created", operation.getOperationType());
        assertEquals(registrar, operation.getActor());
        assertEquals(slotTime, operation.getNewSlotDateTime());
        assertEquals("booked", operation.getNewStatus());
    }

    @Test
    void updateAppointment_translates_db_uniqueness_conflict_to_slot_not_available() {
        LocalDateTime newSlotTime = futureSlot(11);
        User doctor = userWithId(5L, "doctor");
        User patient = userWithId(6L, "patient");

        Appointment appointment = appointment(9L, doctor, patient, futureSlot(9), "booked");

        UpdateAppointmentRequestDto request = new UpdateAppointmentRequestDto();
        request.setSlotDatetime(newSlotTime.toString());

        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
        when(workSlotRepository.findContainingTime(eq(doctor.getId()), eq(newSlotTime.toLocalDate()), eq(newSlotTime.toLocalTime())))
                .thenReturn(List.of(workSlot(doctor, newSlotTime.toLocalDate())));
        when(appointmentRepository.existsActiveAppointment(doctor, newSlotTime, appointment.getId())).thenReturn(false);
        when(appointmentRepository.existsActiveAppointmentForPatient(patient, newSlotTime, appointment.getId())).thenReturn(false);
        when(appointmentRepository.saveAndFlush(any(Appointment.class)))
                .thenThrow(new DataIntegrityViolationException("active appointment for this doctor slot"));

        SlotNotAvailableException ex = assertThrows(
                SlotNotAvailableException.class,
                () -> appointmentService.updateAppointment(appointment.getId(), request)
        );

        assertEquals("Этот слот уже занят другим пациентом", ex.getMessage());
    }

    @Test
    void updateAppointment_rejects_patient_schedule_conflict() {
        LocalDateTime newSlotTime = futureSlot(11);
        User doctor = userWithId(5L, "doctor");
        User patient = userWithId(6L, "patient");
        Appointment appointment = appointment(9L, doctor, patient, futureSlot(9), "booked");

        UpdateAppointmentRequestDto request = new UpdateAppointmentRequestDto();
        request.setSlotDatetime(newSlotTime.toString());

        when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
        when(workSlotRepository.findContainingTime(eq(doctor.getId()), eq(newSlotTime.toLocalDate()), eq(newSlotTime.toLocalTime())))
                .thenReturn(List.of(workSlot(doctor, newSlotTime.toLocalDate())));
        when(appointmentRepository.existsActiveAppointment(doctor, newSlotTime, appointment.getId())).thenReturn(false);
        when(appointmentRepository.existsActiveAppointmentForPatient(patient, newSlotTime, appointment.getId())).thenReturn(true);

        PatientScheduleConflictException ex = assertThrows(
                PatientScheduleConflictException.class,
                () -> appointmentService.updateAppointment(appointment.getId(), request)
        );

        assertEquals("У пациента уже есть запись на это время", ex.getMessage());
        verify(appointmentRepository, never()).saveAndFlush(any(Appointment.class));
    }

    @Test
    void cancelAppointment_records_reason_and_actor() {
        User doctor = userWithId(5L, "doctor");
        User patient = userWithId(6L, "patient");
        User registrar = userWithId(7L, "registrar");
        Appointment appointment = appointment(9L, doctor, patient, futureSlot(9), "booked");
        DictStatus canceled = status("canceled");

        CancelAppointmentRequestDto request = new CancelAppointmentRequestDto();
        request.setReason("Пациент заболел");
        CancelAppointmentResponseDto expected = CancelAppointmentResponseDto.builder()
                .appointmentId(9L)
                .status("canceled")
                .build();

        when(appointmentRepository.findById(9L)).thenReturn(Optional.of(appointment));
        when(statusRepository.findByName("canceled")).thenReturn(Optional.of(canceled));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        when(userRepository.findByLoginIgnoreCase("registrar")).thenReturn(Optional.of(registrar));
        when(appointmentMapper.toCancelResponse(appointment, doctor.getSpeciality())).thenReturn(expected);

        appointmentService.cancelAppointment(9L, request, "registrar");

        ArgumentCaptor<AppointmentOperation> captor = ArgumentCaptor.forClass(AppointmentOperation.class);
        verify(operationRepository).save(captor.capture());
        AppointmentOperation operation = captor.getValue();
        assertEquals("canceled", operation.getOperationType());
        assertEquals("Пациент заболел", operation.getReason());
        assertEquals(registrar, operation.getActor());
        assertEquals("booked", operation.getOldStatus());
        assertEquals("canceled", operation.getNewStatus());
    }

    @Test
    void getAppointmentHistory_maps_operations() {
        User doctor = userWithId(5L, "doctor");
        User patient = userWithId(6L, "patient");
        Appointment appointment = appointment(9L, doctor, patient, futureSlot(9), "canceled");

        AppointmentOperation operation = new AppointmentOperation();
        operation.setId(1L);
        operation.setAppointment(appointment);
        operation.setOperationType("canceled");
        operation.setActor(patient);
        operation.setReason("Не приду");

        when(appointmentRepository.existsById(9L)).thenReturn(true);
        when(operationRepository.findByAppointmentIdOrderByOccurredAtAsc(9L)).thenReturn(List.of(operation));

        List<AppointmentOperationDto> history = appointmentService.getAppointmentHistory(9L);

        assertEquals(1, history.size());
        AppointmentOperationDto dto = history.get(0);
        assertEquals(9L, dto.getAppointmentId());
        assertEquals("canceled", dto.getOperationType());
        assertEquals("patient6", dto.getActorLogin());
        assertEquals("Не приду", dto.getReason());
    }

    private static CreateAppointmentRequestDto request(Long doctorId, Long patientId, LocalDateTime slotTime) {
        CreateAppointmentRequestDto request = new CreateAppointmentRequestDto();
        request.setDoctorId(doctorId);
        request.setPatientId(patientId);
        request.setSlotDatetime(slotTime.toString());
        return request;
    }

    private static LocalDateTime futureSlot(int hour) {
        return LocalDateTime.now().plusDays(1).withHour(hour).withMinute(0).withSecond(0).withNano(0);
    }

    private static User userWithId(Long id, String roleName) {
        User user = new User();
        user.setId(id);
        user.setLogin(roleName + id);
        user.setFullName("Пользователь " + id);
        DictRole role = new DictRole();
        role.setName(roleName);
        user.setRole(role);
        if ("doctor".equals(roleName)) {
            user.setSpeciality("Терапевт");
        }
        return user;
    }

    private static DictStatus status(String name) {
        DictStatus status = new DictStatus();
        status.setName(name);
        return status;
    }

    private static Appointment appointment(Long id, User doctor, User patient,
                                           LocalDateTime slotTime, String statusName) {
        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setSlotDateTime(slotTime);
        appointment.setStatus(status(statusName));
        return appointment;
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
