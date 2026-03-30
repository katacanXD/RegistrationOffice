package ru.katacan.registrationoffice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateAppointmentRequestDto {
    private Long doctorId;        // если null, не меняем врача
    private String slotDatetime;  // если null, не меняем время
    private String status;        // можно добавить смену статуса
    private String message;
}