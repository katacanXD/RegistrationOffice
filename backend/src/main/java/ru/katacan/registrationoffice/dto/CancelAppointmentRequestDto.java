package ru.katacan.registrationoffice.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelAppointmentRequestDto {

    @Size(max = 255, message = "Причина отмены не должна превышать 255 символов")
    private String reason;
}
