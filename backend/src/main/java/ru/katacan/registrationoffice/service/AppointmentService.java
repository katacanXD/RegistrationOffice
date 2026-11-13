package ru.katacan.registrationoffice.service;

import ru.katacan.registrationoffice.dto.*;
import ru.katacan.registrationoffice.entity.Appointment;

import java.util.List;
import java.util.Optional;

public interface AppointmentService {
    CreateAppointmentResponseDto createAppointment(CreateAppointmentRequestDto request);

    CreateAppointmentResponseDto createAppointment(CreateAppointmentRequestDto request, String actorLogin);

    CreateAppointmentResponseDto createOwnAppointment(String login, CreateOwnAppointmentRequestDto request);

    UpdateAppointmentResponseDto updateAppointment(Long appointmentId,
                                                   UpdateAppointmentRequestDto request);

    UpdateAppointmentResponseDto updateAppointment(Long appointmentId,
                                                   UpdateAppointmentRequestDto request,
                                                   String actorLogin);

    CancelAppointmentResponseDto cancelAppointment(Long appointmentId);

    CancelAppointmentResponseDto cancelAppointment(Long appointmentId,
                                                   CancelAppointmentRequestDto request,
                                                   String actorLogin);

    List<AppointmentOperationDto> getAppointmentHistory(Long appointmentId);

    Optional<Appointment> getAppointmentById(Long appointmentId);
    AppointmentDetailDto getDetailAppointmentById(Long appointmentId);
    AppointmentShortDto getShortAppointmentById(Long appointmentId);
    List<Appointment> getPatientAppointments(Long patientId, List<String> statuses);
    boolean isSlotAvailable(Long doctorId, String slotDateTime);
}