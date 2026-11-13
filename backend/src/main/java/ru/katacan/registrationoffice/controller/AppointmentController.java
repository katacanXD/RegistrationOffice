package ru.katacan.registrationoffice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.katacan.registrationoffice.dto.AppointmentDetailDto;
import ru.katacan.registrationoffice.dto.AppointmentOperationDto;
import ru.katacan.registrationoffice.dto.AppointmentShortDto;
import ru.katacan.registrationoffice.dto.CancelAppointmentRequestDto;
import ru.katacan.registrationoffice.dto.CreateAppointmentRequestDto;
import ru.katacan.registrationoffice.dto.CreateAppointmentResponseDto;
import ru.katacan.registrationoffice.dto.CreateOwnAppointmentRequestDto;
import ru.katacan.registrationoffice.dto.UpdateAppointmentRequestDto;
import ru.katacan.registrationoffice.dto.UpdateAppointmentResponseDto;
import ru.katacan.registrationoffice.service.AppointmentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PreAuthorize("hasAnyAuthority('admin', 'registrar')")
    @PostMapping
    public ResponseEntity<?> createAppointment(
            @Valid @RequestBody CreateAppointmentRequestDto appointmentRequestDto,
            Authentication authentication
    ) {
        CreateAppointmentResponseDto response = appointmentService.createAppointment(
                appointmentRequestDto,
                authentication != null ? authentication.getName() : null);
        HttpStatus status = Boolean.TRUE.equals(response.getReplayed())
                ? HttpStatus.OK
                : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(response);
    }

    @PreAuthorize("hasAnyAuthority('admin', 'patient')")
    @PostMapping("/me")
    public ResponseEntity<?> createOwnAppointment(
            Authentication authentication,
            @Valid @RequestBody CreateOwnAppointmentRequestDto appointmentRequestDto
    ) {
        CreateAppointmentResponseDto response = appointmentService.createOwnAppointment(
                authentication.getName(), appointmentRequestDto);
        HttpStatus status = Boolean.TRUE.equals(response.getReplayed())
                ? HttpStatus.OK
                : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(response);
    }

    @PreAuthorize("hasAnyAuthority('admin', 'registrar') or (hasAuthority('patient') and @appointmentAccess.isPatientOwnerOfAppointment(#appointmentId, authentication))")
    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long appointmentId,
            @RequestBody(required = false) CancelAppointmentRequestDto request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(appointmentService.cancelAppointment(
                        appointmentId,
                        request,
                        authentication != null ? authentication.getName() : null));
    }

    @PreAuthorize("hasAnyAuthority('admin', 'registrar') or (hasAuthority('patient') and @appointmentAccess.isPatientOwnerOfAppointment(#appointmentId, authentication))")
    @PatchMapping("/{appointmentId}")
    public ResponseEntity<UpdateAppointmentResponseDto> updateAppointment(
            @Valid @RequestBody UpdateAppointmentRequestDto request,
            @PathVariable Long appointmentId,
            Authentication authentication) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(appointmentService.updateAppointment(
                        appointmentId,
                        request,
                        authentication != null ? authentication.getName() : null));
    }

    @PreAuthorize("hasAnyAuthority('admin', 'registrar', 'doctor') or (hasAuthority('patient') and @appointmentAccess.isPatientOwnerOfAppointment(#appointmentId, authentication))")
    @GetMapping("/{appointmentId}/history")
    public ResponseEntity<List<AppointmentOperationDto>> getAppointmentHistory(
            @PathVariable Long appointmentId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(appointmentService.getAppointmentHistory(appointmentId));
    }

    @PreAuthorize("hasAnyAuthority('admin', 'registrar', 'doctor') or (hasAuthority('patient') and @appointmentAccess.isPatientOwnerOfAppointment(#appointmentId, authentication))")
    @GetMapping("/detail/{appointmentId}")
    public ResponseEntity<AppointmentDetailDto> getDetailAppointment(
            @PathVariable Long appointmentId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(appointmentService.getDetailAppointmentById(appointmentId));
    }

    @PreAuthorize("hasAnyAuthority('admin', 'registrar', 'doctor') or (hasAuthority('patient') and @appointmentAccess.isPatientOwnerOfAppointment(#appointmentId, authentication))")
    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentShortDto> getShortAppointment(
            @PathVariable Long appointmentId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(appointmentService.getShortAppointmentById(appointmentId));
    }
}