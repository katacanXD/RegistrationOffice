package ru.katacan.registrationoffice.service;

import ru.katacan.registrationoffice.dto.*;
import ru.katacan.registrationoffice.entity.Appointment;

import java.util.List;
import java.util.Optional;

public interface AppointmentService {
    CreateAppointmentResponseDto createAppointment(CreateAppointmentRequestDto request);
    CreateAppointmentResponseDto createOwnAppointment(String login, CreateOwnAppointmentRequestDto request);
    UpdateAppointmentRequestDto updateAppointment(Long appointmentId,
                                                  UpdateAppointmentRequestDto request);
    CancelAppointmentResponseDto cancelAppointment(Long appointmentId);
    Optional<Appointment> getAppointmentById(Long appointmentId);
    AppointmentDetailDto getDetailAppointmentById(Long appointmentId);
    AppointmentShortDto getShortAppointmentById(Long appointmentId);
    List<Appointment> getPatientAppointments(Long patientId, List<String> statuses);
    boolean isSlotAvailable(Long doctorId, String slotDateTime);
}