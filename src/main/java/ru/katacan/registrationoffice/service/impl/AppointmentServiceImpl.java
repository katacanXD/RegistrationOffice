package ru.katacan.registrationoffice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import ru.katacan.registrationoffice.dto.*;
import ru.katacan.registrationoffice.entity.Appointment;
import ru.katacan.registrationoffice.entity.DictStatus;
import ru.katacan.registrationoffice.entity.User;
import ru.katacan.registrationoffice.exception.*;
import ru.katacan.registrationoffice.mapper.AppointmentMapper;
import ru.katacan.registrationoffice.repository.AppointmentRepository;
import ru.katacan.registrationoffice.repository.DictStatusRepository;
import ru.katacan.registrationoffice.repository.UserRepository;
import ru.katacan.registrationoffice.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.katacan.registrationoffice.util.LoginNormalizer;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DictStatusRepository statusRepository;

    private final AppointmentMapper appointmentMapper;

    @Override
    public CreateAppointmentResponseDto createAppointment(CreateAppointmentRequestDto request) {
        return createAppointmentInternal(request);
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

        return createAppointmentInternal(normalizedRequest);
    }

    private CreateAppointmentResponseDto createAppointmentInternal(CreateAppointmentRequestDto request) {
        // Валидация входных данных
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

        if (appointmentRepository.existsByDoctorAndSlotDateTimeAndNotCanceled(doctor, slotTime)) {
            throw new SlotNotAvailableException("Этот слот уже занят");
        }

        DictStatus status = statusRepository.findByName("booked")
                .orElseThrow(() -> new SystemConfigurationException("Статус booked не найден в системе"));

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setSlotDateTime(slotTime);
        appointment.setStatus(status);

        appointmentRepository.save(appointment);

        return appointmentMapper.toCreateResponse(appointment, doctor);
    }

    @Override
    @Transactional
    public UpdateAppointmentRequestDto updateAppointment(Long appointmentId, UpdateAppointmentRequestDto request) {
        // 1. Находим существующую запись
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Запись с id " + appointmentId + " не найдена"));

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

            // Проверяем доступность слота, исключая текущую запись из проверки
            if (appointmentRepository.existsActiveAppointment(
                    appointment.getDoctor(), newSlotTime, appointmentId)) {
                throw new SlotNotAvailableException("Этот слот уже занят другим пациентом");
            }
            appointment.setSlotDateTime(newSlotTime);
        }

        appointmentRepository.save(appointment);

        return appointmentMapper.toUpdateResponse(appointment);
    }

    @Override
    public CancelAppointmentResponseDto cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Запись с id " + appointmentId + " не найдена"));

        // Проверка, что запись еще не отменена
        if ("canceled".equals(appointment.getStatus().getName())) {
            throw new BadRequestException("Запись уже отменена");
        }

        DictStatus canceledStatus = statusRepository.findByName("canceled")
                .orElseThrow(() -> new SystemConfigurationException("Статус canceled не найден в системе"));

        appointment.setStatus(canceledStatus);
        appointmentRepository.save(appointment);

        return appointmentMapper.toCancelResponse(appointment, appointment.getDoctor().getSpeciality());
    }

    @Override
    public Optional<Appointment> getAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId);
    }

    @Override
    public AppointmentDetailDto getDetailAppointmentById(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));

        return appointmentMapper.toAppointmentDetail(appointment);
    }

    @Override
    public AppointmentShortDto getShortAppointmentById(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));

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

        return !appointmentRepository.existsByDoctorAndSlotDateTimeAndNotCanceled(doctor, slotTime);
    }
}