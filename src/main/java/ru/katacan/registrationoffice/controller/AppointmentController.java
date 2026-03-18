package ru.katacan.registrationoffice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.katacan.registrationoffice.dto.AppointmentDetailDto;
import ru.katacan.registrationoffice.dto.AppointmentShortDto;
import ru.katacan.registrationoffice.dto.CreateAppointmentRequestDto;
import ru.katacan.registrationoffice.dto.UpdateAppointmentRequestDto;
import ru.katacan.registrationoffice.service.AppointmentService;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/create")
    public ResponseEntity<?> createAppointment(
            @RequestBody CreateAppointmentRequestDto appointmentRequestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createAppointment(appointmentRequestDto));
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long appointmentId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(appointmentService.cancelAppointment(appointmentId));
    }

    @PatchMapping("/{appointmentId}")
    public ResponseEntity<UpdateAppointmentRequestDto> updateAppointment(
            @RequestBody UpdateAppointmentRequestDto request,
            @PathVariable Long appointmentId) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(appointmentService.updateAppointment(appointmentId, request));
    }

    @GetMapping("/detail/{appointmentId}")
    public ResponseEntity<AppointmentDetailDto> getDetailAppointment(
            @PathVariable Long appointmentId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(appointmentService.getDetailAppointmentById(appointmentId));
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentShortDto> getShortAppointment(
            @PathVariable Long appointmentId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(appointmentService.getShortAppointmentById(appointmentId));
    }
}