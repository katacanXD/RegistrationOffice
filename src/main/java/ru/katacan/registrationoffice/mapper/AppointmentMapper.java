package ru.katacan.registrationoffice.mapper;

import org.springframework.stereotype.Component;
import ru.katacan.registrationoffice.dto.*;
import ru.katacan.registrationoffice.entity.Appointment;
import ru.katacan.registrationoffice.entity.User;

import java.time.format.DateTimeFormatter;

@Component
public class AppointmentMapper {

    private final String CLINIC_ADDRESS = "г. Москва, ул. Менделеева, д. 15с2";
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public AppointmentShortDto toAppointmentShort(Appointment appointment) {
        return AppointmentShortDto.builder()
                .appointmentId(appointment.getId())
                .speciality(appointment.getDoctor() != null ?
                        appointment.getDoctor().getSpeciality() : null)
                .doctorFio(appointment.getDoctor() != null ?
                        appointment.getDoctor().getFullName() : null)
                .datetime(appointment.getSlotDateTime() != null ?
                        appointment.getSlotDateTime().format(dateTimeFormatter) : null)
                .address(CLINIC_ADDRESS)
                .status(appointment.getStatus() != null ?
                        appointment.getStatus().getName() : null)
                .build();
    }

    public AppointmentDetailDto toAppointmentDetail(Appointment appointment) {
        return AppointmentDetailDto.builder()
                .appointmentId(appointment.getId())
                .datetime(appointment.getSlotDateTime() != null ?
                        appointment.getSlotDateTime().format(dateTimeFormatter) : null)
                .address(CLINIC_ADDRESS)
                // Маппинг врача
                .doctor(appointment.getDoctor() != null ?
                        AppointmentDetailDto.DoctorInfoDto.builder()
                                .fio(appointment.getDoctor().getFullName())
                                .build() : null)
                // Маппинг пациента
                .patient(appointment.getPatient() != null ?
                        AppointmentDetailDto.PatientInfoDto.builder()
                                .fio(appointment.getPatient().getFullName())
                                .policyNumber(appointment.getPatient().getPolicyNumber())
                                .build() : null)
                .build();
    }

    public CreateAppointmentResponseDto toCreateResponse(Appointment appointment, User doctor) {
        return CreateAppointmentResponseDto.builder()
                .appointmentId(appointment.getId())
                .message(String.format("Запись успешно создана на %s",
                        appointment.getSlotDateTime().format(DateTimeFormatter.ofPattern("d MMMM, HH:mm"))))
                .details(CreateAppointmentResponseDto.AppointmentDetailsDto.builder()
                        .doctorFio(doctor != null ? doctor.getFullName() : null)
                        .speciality(doctor != null ? doctor.getSpeciality() : null)
                        .address(CLINIC_ADDRESS)
                        .build())
                .build();
    }

    public CancelAppointmentResponseDto toCancelResponse(Appointment appointment, String speciality) {
        return CancelAppointmentResponseDto.builder()
                .appointmentId(appointment.getId())
                .status(appointment.getStatus() != null ? appointment.getStatus().getName() : null)
                .message(String.format("Запись к врачу (%s) отменена", speciality))
                .build();
    }

    public UpdateAppointmentRequestDto toUpdateResponse(Appointment appointment) {
        return UpdateAppointmentRequestDto.builder()
                .doctorId(appointment.getDoctor().getId())
                .slotDatetime(appointment.getSlotDateTime().format(DateTimeFormatter.ofPattern("d MMMM, HH:mm")))
                .status(appointment.getStatus() != null ? appointment.getStatus().getName() : null)
                .message(String.format("Запись к врачу (%s) отменена",
                        appointment.getSlotDateTime().format(DateTimeFormatter.ofPattern("d MMMM, HH:mm"))))
                .build();
    }
}