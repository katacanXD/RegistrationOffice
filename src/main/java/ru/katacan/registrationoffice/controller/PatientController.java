package ru.katacan.registrationoffice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.katacan.registrationoffice.dto.PatientDashboardDto;
import ru.katacan.registrationoffice.dto.PatientListDto;
import ru.katacan.registrationoffice.dto.PolicyRequestDto;
import ru.katacan.registrationoffice.mapper.AppointmentMapper;
import ru.katacan.registrationoffice.mapper.UserMapper;
import ru.katacan.registrationoffice.repository.UserRepository;
import ru.katacan.registrationoffice.service.UserService;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final UserService userService;

    @GetMapping("/dashboard")
    public ResponseEntity<PatientDashboardDto> getDashboard(
            @RequestParam Long patientId
    ) {
        return ResponseEntity.ok(
                userService.getDashboard(patientId));
    }

    @PatchMapping("/policy")
    public ResponseEntity<?> updatePolicy(
            @RequestParam Long patientId,
            @RequestBody PolicyRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.updatePolicy(patientId, request));
    }

    @GetMapping
    public ResponseEntity<PatientListDto> getAllPatients(
            @RequestParam(required = false) String search,
            Pageable pageable) { //TODO добавить плагинацию
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.searchPatients(search, pageable));
    }
}