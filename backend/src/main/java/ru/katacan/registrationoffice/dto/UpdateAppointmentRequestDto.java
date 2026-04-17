package ru.katacan.registrationoffice.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateAppointmentRequestDto {
    @Positive(message = "doctorId должен быть положительным")
    private Long doctorId;        // если null, не меняем врача

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2})?$", message = "slotDatetime должен быть в формате yyyy-MM-dd'T'HH:mm[:ss]")
    private String slotDatetime;  // если null, не меняем время

    @Pattern(regexp = "^(booked|canceled|completed)$", message = "Статус должен быть одним из: booked, canceled, completed")
    private String status;        // можно добавить смену статуса
    private String message;
}