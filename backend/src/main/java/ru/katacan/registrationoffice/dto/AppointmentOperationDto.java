package ru.katacan.registrationoffice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
public class AppointmentOperationDto {
    private Long operationId;
    private Long appointmentId;
    private String operationType;
    private String actorLogin;
    private String actorFio;
    private Instant occurredAt;
    private String reason;
    private LocalDateTime oldSlotDatetime;
    private LocalDateTime newSlotDatetime;
    private String oldStatus;
    private String newStatus;
}
