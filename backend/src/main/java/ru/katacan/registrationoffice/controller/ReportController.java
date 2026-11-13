package ru.katacan.registrationoffice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.katacan.registrationoffice.dto.UtilizationReportDto;
import ru.katacan.registrationoffice.service.UtilizationService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final UtilizationService utilizationService;

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("/utilization")
    public ResponseEntity<UtilizationReportDto> utilization(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String speciality
    ) {
        return ResponseEntity.ok(utilizationService.buildReport(from, to, doctorId, speciality));
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("/utilization/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String speciality
    ) {
        String csv = utilizationService.buildCsv(from, to, doctorId, speciality);
        String fileName = "utilization_" + (from != null ? from : "period")
                + "_" + (to != null ? to : "period") + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}
