package ru.katacan.registrationoffice.service.impl;

import ru.katacan.registrationoffice.dto.AppointmentDetailDto;
import ru.katacan.registrationoffice.dto.AppointmentOperationDto;
import ru.katacan.registrationoffice.dto.AppointmentShortDto;
import ru.katacan.registrationoffice.dto.CancelAppointmentRequestDto;
import ru.katacan.registrationoffice.dto.CancelAppointmentResponseDto;
import ru.katacan.registrationoffice.dto.CreateAppointmentRequestDto;
import ru.katacan.registrationoffice.dto.CreateAppointmentResponseDto;
import ru.katacan.registrationoffice.dto.CreateOwnAppointmentRequestDto;
import ru.katacan.registrationoffice.dto.UpdateAppointmentRequestDto;
import ru.katacan.registrationoffice.dto.UpdateAppointmentResponseDto;
import ru.katacan.registrationoffice.entity.Appointment;
import ru.katacan.registrationoffice.entity.AppointmentOperation;
import ru.katacan.registrationoffice.entity.DictStatus;
import ru.katacan.registrationoffice.entity.User;
import ru.katacan.registrationoffice.exception.BadRequestException;
import ru.katacan.registrationoffice.exception.PatientScheduleConflictException;
import ru.katacan.registrationoffice.exception.ResourceNotFoundException;
import ru.katacan.registrationoffice.exception.SlotNotAvailableException;
import ru.katacan.registrationoffice.exception.SystemConfigurationException;
import ru.katacan.registrationoffice.mapper.AppointmentMapper;
import ru.katacan.registrationoffice.repository.AppointmentOperationRepository;
import ru.katacan.registrationoffice.repository.AppointmentRepository;
import ru.katacan.registrationoffice.repository.DictStatusRepository;
import ru.katacan.registrationoffice.repository.UserRepository;
import ru.katacan.registrationoffice.repository.WorkSlotRepository;
import ru.katacan.registrationoffice.service.AppointmentService;
import ru.katacan.registrationoffice.util.LoginNormalizer;
import ru.katacan.registrationoffice.util.SlotGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DictStatusRepository statusRepository;
    private final WorkSlotRepository workSlotRepository;
    private final AppointmentOperationRepository operationRepository;

    private final AppointmentMapper appointmentMapper;

    @Override
    public CreateAppointmentResponseDto createAppointment(CreateAppointmentRequestDto request) {
        return createAppointment(request, null);
    }

    @Override
    public CreateAppointmentResponseDto createAppointment(CreateAppointmentRequestDto request, String actorLogin) {
        return createAppointmentInternal(request, resolveActor(actorLogin));
    }

    @Override
    public CreateAppointmentResponseDto createOwnAppointment(String login, CreateOwnAppointmentRequestDto request) {
        if (request == null) {
            throw new BadRequestException("Не все обязательные поля заполнены");
        }

        String normalizedLogin = LoginNormalizer.normalize(login);
        User currentUser = userRepository.findByLoginIgnoreCase(normalizedLogin)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        if (currentUser.getRole() == null || !"patient".equals(currentUser.getRole().getName())) {
            throw new BadRequestException("Только пациент может создавать запись через /me");
        }

        CreateAppointmentRequestDto normalizedRequest = new CreateAppointmentRequestDto();
        normalizedRequest.setDoctorId(request.getDoctorId());
        normalizedRequest.setSlotDatetime(request.getSlotDatetime());
        normalizedRequest.setPatientId(currentUser.getId());

        return createAppointmentInternal(normalizedRequest, currentUser);
    }

    private CreateAppointmentResponseDto createAppointmentInternal(CreateAppointmentRequestDto request, User actor) {
        // Валидация входных данных
        if (request == null) {
            throw new BadRequestException("Тело запроса не должно быть пустым");
        }
        if (request.getDoctorId() == null || request.getPatientId() == null || request.getSlotDatetime() == null) {
            throw new BadRequestException("Не все обязательные поля заполнены");
        }

        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Врач с id " + request.getDoctorId() + " не найден"));

        User patient = userRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Пациент с id " + request.getPatientId() + " не найден"));

        LocalDateTime slotTime;
        try {
            slotTime = LocalDateTime.parse(request.getSlotDatetime());
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Неверный формат даты и времени");
        }

        // Проверка, что время записи в будущем
        if (slotTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Нельзя записаться на прошедшее время");
        }

        validateDoctorHasBookableSlot(doctor.getId(), slotTime);

        // Идемпотентность: повторный запрос с теми же данными возвращает существующую запись
        Optional<Appointment> existing = findActiveAppointment(doctor, slotTime);
        if (existing.isPresent()) {
            Appointment appointment = existing.get();
            if (isSamePatient(appointment, patient)) {
                return replayResponse(appointment, doctor);
            }
            throw new SlotNotAvailableException("Этот слот уже занят");
        }

        // У пациента не может быть второй активной записи на то же время
        if (appointmentRepository.existsActiveAppointmentForPatient(patient, slotTime, -1L)) {
            throw new PatientScheduleConflictException("У пациента уже есть запись на это время");
        }

        DictStatus status = statusRepository.findByName("booked")
                .orElseThrow(() -> new SystemConfigurationException("Статус booked не найден в системе"));

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setSlotDateTime(slotTime);
        appointment.setStatus(status);

        try {
            appointmentRepository.saveAndFlush(appointment);
        } catch (DataIntegrityViolationException e) {
            String rootMessage = rootMessage(e);

            if (rootMessage != null && rootMessage.contains("active appointment for this patient slot")) {
                throw new PatientScheduleConflictException("У пациента уже есть запись на это время");
            }

            if (rootMessage != null && rootMessage.contains("active appointment for this doctor slot")) {
                Optional<Appointment> concurrent = findActiveAppointment(doctor, slotTime);
                if (concurrent.isPresent() && isSamePatient(concurrent.get(), patient)) {
                    return replayResponse(concurrent.get(), doctor);
                }
            }

            throw new SlotNotAvailableException("Этот слот уже занят");
        }

        recordOperation(appointment, "created", actor, null,
                null, slotTime, null, statusName(appointment));

        return appointmentMapper.toCreateResponse(appointment, doctor);
    }

    @Override
    public UpdateAppointmentResponseDto updateAppointment(Long appointmentId, UpdateAppointmentRequestDto request) {
        return updateAppointment(appointmentId, request, null);
    }

    @Override
    @Transactional
    public UpdateAppointmentResponseDto updateAppointment(Long appointmentId,
                                                         UpdateAppointmentRequestDto request,
                                                         String actorLogin) {
        if (request == null) {
            throw new BadRequestException("Тело запроса не должно быть пустым");
        }

        // 1. Находим существующую запись
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Запись с id " + appointmentId + " не найдена"));

        User actor = resolveActor(actorLogin);
        LocalDateTime oldSlotTime = appointment.getSlotDateTime();
        String oldStatus = statusName(appointment);

        // Меняют время
        if (request.getSlotDatetime() != null) {
            LocalDateTime newSlotTime;
            try {
                newSlotTime = LocalDateTime.parse(request.getSlotDatetime());
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Неверный формат даты и времени");
            }

            if (newSlotTime.isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Нельзя перенести запись на прошедшее время");
            }

            validateDoctorHasBookableSlot(appointment.getDoctor().getId(), newSlotTime);

            // Проверяем доступность слота, исключая текущую запись из проверки
            if (appointmentRepository.existsActiveAppointment(
                    appointment.getDoctor(), newSlotTime, appointmentId)) {
                throw new SlotNotAvailableException("Этот слот уже занят другим пациентом");
            }

            if (appointmentRepository.existsActiveAppointmentForPatient(
                    appointment.getPatient(), newSlotTime, appointmentId)) {
                throw new PatientScheduleConflictException("У пациента уже есть запись на это время");
            }

            appointment.setSlotDateTime(newSlotTime);
        }

        try {
            appointmentRepository.saveAndFlush(appointment);
        } catch (DataIntegrityViolationException e) {
            String rootMessage = rootMessage(e);
            if (rootMessage != null && rootMessage.contains("active appointment for this patient slot")) {
                throw new PatientScheduleConflictException("У пациента уже есть запись на это время");
            }
            if (rootMessage != null && rootMessage.contains("active appointment for this doctor slot")) {
                throw new SlotNotAvailableException("Этот слот уже занят другим пациентом");
            }
            throw e;
        }

        if (!Objects.equals(oldSlotTime, appointment.getSlotDateTime())) {
            recordOperation(appointment, "rescheduled", actor, normalizeReason(request.getReason()),
                    oldSlotTime, appointment.getSlotDateTime(), oldStatus, statusName(appointment));
        }

        return appointmentMapper.toUpdateResponse(appointment);
    }

    @Override
    public CancelAppointmentResponseDto cancelAppointment(Long appointmentId) {
        return cancelAppointment(appointmentId, null, null);
    }

    @Override
    public CancelAppointmentResponseDto cancelAppointment(Long appointmentId,
                                                          CancelAppointmentRequestDto request,
                                                          String actorLogin) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Запись с id " + appointmentId + " не найдена"));

        // Проверка, что запись еще не отменена
        if ("canceled".equals(statusName(appointment))) {
            throw new BadRequestException("Запись уже отменена");
        }

        DictStatus canceledStatus = statusRepository.findByName("canceled")
                .orElseThrow(() -> new SystemConfigurationException("Статус canceled не найден в системе"));

        String oldStatus = statusName(appointment);
        appointment.setStatus(canceledStatus);
        appointmentRepository.save(appointment);

        String reason = request != null ? normalizeReason(request.getReason()) : null;
        recordOperation(appointment, "canceled", resolveActor(actorLogin), reason,
                appointment.getSlotDateTime(), appointment.getSlotDateTime(), oldStatus, "canceled");

        return appointmentMapper.toCancelResponse(appointment, appointment.getDoctor().getSpeciality());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentOperationDto> getAppointmentHistory(Long appointmentId) {
        if (!appointmentRepository.existsById(appointmentId)) {
            throw new ResourceNotFoundException("Запись с id " + appointmentId + " не найдена");
        }

        return operationRepository.findByAppointmentIdOrderByOccurredAtAsc(appointmentId).stream()
                .map(this::toOperationDto)
                .toList();
    }

    @Override
    public Optional<Appointment> getAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId);
    }

    @Override
    public AppointmentDetailDto getDetailAppointmentById(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Запись с id " + appointmentId + " не найдена"));

        return appointmentMapper.toAppointmentDetail(appointment);
    }

    @Override
    public AppointmentShortDto getShortAppointmentById(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Запись с id " + appointmentId + " не найдена"));

        return appointmentMapper.toAppointmentShort(appointment);
    }

    @Override
    public List<Appointment> getPatientAppointments(Long patientId, List<String> statuses) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Пациент с id " + patientId + " не найден"));
        return appointmentRepository.findByPatientAndStatus_NameIn(patient, statuses);
    }

    @Override
    public boolean isSlotAvailable(Long doctorId, String slotDateTime) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Врач с id " + doctorId + " не найден"));

        LocalDateTime slotTime;
        try {
            slotTime = LocalDateTime.parse(slotDateTime);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Неверный формат даты и времени");
        }

        if (!isBookableWorkSlot(doctorId, slotTime)) {
            return false;
        }

        return !appointmentRepository.existsByDoctorAndSlotDateTimeAndNotCanceled(doctor, slotTime);
    }

    private void validateDoctorHasBookableSlot(Long doctorId, LocalDateTime slotTime) {
        if (!isBookableWorkSlot(doctorId, slotTime)) {
            throw new BadRequestException("Выбранное время не входит в рабочие слоты врача");
        }
    }

    private boolean isBookableWorkSlot(Long doctorId, LocalDateTime slotTime) {
        LocalDate date = slotTime.toLocalDate();
        LocalTime time = slotTime.toLocalTime();

        return workSlotRepository.findContainingTime(doctorId, date, time).stream()
                .anyMatch(slot -> SlotGenerator.generateSlots(
                                slot.getStartTime(),
                                slot.getEndTime(),
                                slot.getSlotMinutes(),
                                slot.getBreakStart(),
                                slot.getBreakEnd())
                        .contains(time));
    }

    private Optional<Appointment> findActiveAppointment(User doctor, LocalDateTime slotTime) {
        return appointmentRepository.findActiveByDoctorAndSlot(doctor, slotTime).stream().findFirst();
    }

    private boolean isSamePatient(Appointment appointment, User patient) {
        return appointment.getPatient() != null
                && patient != null
                && Objects.equals(appointment.getPatient().getId(), patient.getId());
    }

    private CreateAppointmentResponseDto replayResponse(Appointment appointment, User doctor) {
        CreateAppointmentResponseDto response = appointmentMapper.toCreateResponse(appointment, doctor);
        response.setReplayed(true);
        response.setMessage("Запись уже существует");
        return response;
    }

    private User resolveActor(String actorLogin) {
        if (actorLogin == null || actorLogin.isBlank()) {
            return null;
        }
        return userRepository.findByLoginIgnoreCase(LoginNormalizer.normalize(actorLogin))
                .orElse(null);
    }

    private void recordOperation(Appointment appointment,
                                 String operationType,
                                 User actor,
                                 String reason,
                                 LocalDateTime oldSlotDateTime,
                                 LocalDateTime newSlotDateTime,
                                 String oldStatus,
                                 String newStatus) {
        AppointmentOperation operation = new AppointmentOperation();
        operation.setAppointment(appointment);
        operation.setOperationType(operationType);
        operation.setActor(actor);
        operation.setReason(reason);
        operation.setOldSlotDateTime(oldSlotDateTime);
        operation.setNewSlotDateTime(newSlotDateTime);
        operation.setOldStatus(oldStatus);
        operation.setNewStatus(newStatus);
        operationRepository.save(operation);
    }

    private AppointmentOperationDto toOperationDto(AppointmentOperation operation) {
        return AppointmentOperationDto.builder()
                .operationId(operation.getId())
                .appointmentId(operation.getAppointment() != null
                        ? operation.getAppointment().getId() : null)
                .operationType(operation.getOperationType())
                .actorLogin(operation.getActor() != null ? operation.getActor().getLogin() : null)
                .actorFio(operation.getActor() != null ? operation.getActor().getFullName() : null)
                .occurredAt(operation.getOccurredAt())
                .reason(operation.getReason())
                .oldSlotDatetime(operation.getOldSlotDateTime())
                .newSlotDatetime(operation.getNewSlotDateTime())
                .oldStatus(operation.getOldStatus())
                .newStatus(operation.getNewStatus())
                .build();
    }

    private String statusName(Appointment appointment) {
        return appointment.getStatus() != null ? appointment.getStatus().getName() : null;
    }

    private String normalizeReason(String reason) {
        if (reason == null) {
            return null;
        }
        String trimmed = reason.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String rootMessage(DataIntegrityViolationException e) {
        return e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : null;
    }
}
