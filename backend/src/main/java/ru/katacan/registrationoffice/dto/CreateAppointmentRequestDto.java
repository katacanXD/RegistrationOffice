package ru.katacan.registrationoffice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateAppointmentRequestDto {
    @NotNull(message = "doctorId обязателен")
    @Positive(message = "doctorId должен быть положительным")
    private Long doctorId;

    @NotNull(message = "patientId обязателен")
    @Positive(message = "patientId должен быть положительным")
    private Long patientId;

    @NotBlank(message = "slotDatetime обязателен")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2})?$", message = "slotDatetime должен быть в формате yyyy-MM-dd'T'HH:mm[:ss]")
    private String slotDatetime; // "2026-03-24T13:15"
}
