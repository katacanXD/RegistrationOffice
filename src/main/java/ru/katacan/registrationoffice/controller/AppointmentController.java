package ru.katacan.registrationoffice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.katacan.registrationoffice.dto.AppointmentDetailDto;
import ru.katacan.registrationoffice.dto.AppointmentShortDto;
import ru.katacan.registrationoffice.dto.CreateAppointmentRequestDto;
import ru.katacan.registrationoffice.dto.CreateOwnAppointmentRequestDto;
import ru.katacan.registrationoffice.dto.UpdateAppointmentRequestDto;
import ru.katacan.registrationoffice.service.AppointmentService;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PreAuthorize("hasAnyAuthority('admin', 'registrar')")
    @PostMapping
    public ResponseEntity<?> createAppointment(
            @RequestBody CreateAppointmentRequestDto appointmentRequestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createAppointment(appointmentRequestDto));
    }

    @PreAuthorize("hasAuthority('patient')")
    @PostMapping("/me")
    public ResponseEntity<?> createOwnAppointment(
            Authentication authentication,
            @RequestBody CreateOwnAppointmentRequestDto appointmentRequestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createOwnAppointment(authentication.getName(), appointmentRequestDto));
    }

    @PreAuthorize("hasAnyAuthority('admin', 'registrar') or (hasAuthority('patient') and @appointmentAccess.isPatientOwnerOfAppointment(#appointmentId, authentication))")
    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long appointmentId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(appointmentService.cancelAppointment(appointmentId));
    }

    @PreAuthorize("hasAnyAuthority('admin', 'registrar') or (hasAuthority('patient') and @appointmentAccess.isPatientOwnerOfAppointment(#appointmentId, authentication))")
    @PatchMapping("/{appointmentId}")
    public ResponseEntity<UpdateAppointmentRequestDto> updateAppointment(
            @RequestBody UpdateAppointmentRequestDto request,
            @PathVariable Long appointmentId) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(appointmentService.updateAppointment(appointmentId, request));
    }

    @PreAuthorize("hasAnyAuthority('admin', 'registrar') or (hasAuthority('patient') and @appointmentAccess.isPatientOwnerOfAppointment(#appointmentId, authentication))")
    @GetMapping("/detail/{appointmentId}")
    public ResponseEntity<AppointmentDetailDto> getDetailAppointment(
            @PathVariable Long appointmentId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(appointmentService.getDetailAppointmentById(appointmentId));
    }

    @PreAuthorize("hasAnyAuthority('admin', 'registrar') or (hasAuthority('patient') and @appointmentAccess.isPatientOwnerOfAppointment(#appointmentId, authentication))")
    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentShortDto> getShortAppointment(
            @PathVariable Long appointmentId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(appointmentService.getShortAppointmentById(appointmentId));
    }
}