package ru.katacan.registrationoffice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.katacan.registrationoffice.dto.PatientDashboardDto;
import ru.katacan.registrationoffice.dto.PatientListDto;
import ru.katacan.registrationoffice.dto.PolicyRequestDto;
import ru.katacan.registrationoffice.service.UserService;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final UserService userService;

    @PreAuthorize("hasAnyAuthority('admin', 'patient')")
    @GetMapping("/me/dashboard")
    public ResponseEntity<PatientDashboardDto> getDashboard(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                userService.getDashboardByLogin(authentication.getName()));
    }

    @PreAuthorize("hasAnyAuthority('admin', 'patient')")
    @PatchMapping("/me/policy")
    public ResponseEntity<?> updatePolicy(
            Authentication authentication,
            @Valid @RequestBody PolicyRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.updatePolicyByLogin(authentication.getName(), request));
    }

    @PreAuthorize("hasAnyAuthority('admin', 'registrar')")
    @GetMapping
    public ResponseEntity<PatientListDto> getAllPatients(
            @RequestParam(required = false) String search,
            Pageable pageable) { //TODO добавить плагинацию
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.searchPatients(search, pageable));
    }
}