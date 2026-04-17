package ru.katacan.registrationoffice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.katacan.registrationoffice.dto.CsvExportResultDto;
import ru.katacan.registrationoffice.dto.CsvImportResultDto;
import ru.katacan.registrationoffice.service.CsvService;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/admins")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('admin')")
public class AdminController {

    private final CsvService csvService;

    /**
     * Экспортирует всех врачей в CSV формате
     */
    @GetMapping("/export/doctors")
    public ResponseEntity<CsvExportResultDto> exportDoctors() {
        log.info("Received request to export doctors");
        CsvExportResultDto result = csvService.exportDoctors();
        return ResponseEntity.ok(result);
    }

    /**
     * Экспортирует всех пациентов в CSV формате
     */
    @GetMapping("/export/patients")
    public ResponseEntity<CsvExportResultDto> exportPatients() {
        log.info("Received request to export patients");
        CsvExportResultDto result = csvService.exportPatients();
        return ResponseEntity.ok(result);
    }

    /**
     * Экспортирует все записи в CSV формате
     */
    @GetMapping("/export/appointments")
    public ResponseEntity<CsvExportResultDto> exportAppointments() {
        log.info("Received request to export appointments");
        CsvExportResultDto result = csvService.exportAppointments();
        return ResponseEntity.ok(result);
    }

    /**
     * Экспортирует расписание врачей в CSV формате
     */
    @GetMapping("/export/work-slots")
    public ResponseEntity<CsvExportResultDto> exportWorkSlots() {
        log.info("Received request to export work slots");
        CsvExportResultDto result = csvService.exportWorkSlots();
        return ResponseEntity.ok(result);
    }

    /**
     * Импортирует врачей из CSV файла
     * @param file CSV файл с колонками: login, fullName, speciality
     */
    @PostMapping("/import/doctors")
    public ResponseEntity<CsvImportResultDto> importDoctors(@RequestParam("file") MultipartFile file) {
        log.info("Received request to import doctors from file: {}", file.getOriginalFilename());
        try {
            String csvContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            CsvImportResultDto result = csvService.importDoctors(csvContent);
            
            HttpStatus status = "SUCCESS".equals(result.getStatus()) ? HttpStatus.OK : 
                               "PARTIAL".equals(result.getStatus()) ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(result, status);
        } catch (Exception e) {
            log.error("Error importing doctors", e);
            CsvImportResultDto errorResult = CsvImportResultDto.builder()
                    .rowsProcessed(0)
                    .rowsSuccessful(0)
                    .rowsFailed(0)
                    .status("FAILED")
                    .message(resolveErrorMessage(e))
                    .build();
            return ResponseEntity.badRequest().body(errorResult);
        }
    }

    /**
     * Импортирует пациентов из CSV файла
     * @param file CSV файл с колонками: login, fullName, policyNumber
     */
    @PostMapping("/import/patients")
    public ResponseEntity<CsvImportResultDto> importPatients(@RequestParam("file") MultipartFile file) {
        log.info("Received request to import patients from file: {}", file.getOriginalFilename());
        try {
            String csvContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            CsvImportResultDto result = csvService.importPatients(csvContent);
            
            HttpStatus status = "SUCCESS".equals(result.getStatus()) ? HttpStatus.OK : 
                               "PARTIAL".equals(result.getStatus()) ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(result, status);
        } catch (Exception e) {
            log.error("Error importing patients", e);
            CsvImportResultDto errorResult = CsvImportResultDto.builder()
                    .rowsProcessed(0)
                    .rowsSuccessful(0)
                    .rowsFailed(0)
                    .status("FAILED")
                    .message(resolveErrorMessage(e))
                    .build();
            return ResponseEntity.badRequest().body(errorResult);
        }
    }

    /**
     * Импортирует записи из CSV файла
     * @param file CSV файл с колонками: patientLogin, doctorLogin, slotDateTime, status
     */
    @PostMapping("/import/appointments")
    public ResponseEntity<CsvImportResultDto> importAppointments(@RequestParam("file") MultipartFile file) {
        log.info("Received request to import appointments from file: {}", file.getOriginalFilename());
        try {
            String csvContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            CsvImportResultDto result = csvService.importAppointments(csvContent);
            
            HttpStatus status = "SUCCESS".equals(result.getStatus()) ? HttpStatus.OK : 
                               "PARTIAL".equals(result.getStatus()) ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(result, status);
        } catch (Exception e) {
            log.error("Error importing appointments", e);
            CsvImportResultDto errorResult = CsvImportResultDto.builder()
                    .rowsProcessed(0)
                    .rowsSuccessful(0)
                    .rowsFailed(0)
                    .status("FAILED")
                    .message(resolveErrorMessage(e))
                    .build();
            return ResponseEntity.badRequest().body(errorResult);
        }
    }

    /**
     * Импортирует расписание врачей из CSV файла
     * @param file CSV файл с колонками: doctorLogin, date, startTime, endTime, slotMinutes, breakStart, breakEnd
     */
    @PostMapping("/import/work-slots")
    public ResponseEntity<CsvImportResultDto> importWorkSlots(@RequestParam("file") MultipartFile file) {
        log.info("Received request to import work slots from file: {}", file.getOriginalFilename());
        try {
            String csvContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            CsvImportResultDto result = csvService.importWorkSlots(csvContent);
            
            HttpStatus status = "SUCCESS".equals(result.getStatus()) ? HttpStatus.OK : 
                               "PARTIAL".equals(result.getStatus()) ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(result, status);
        } catch (Exception e) {
            log.error("Error importing work slots", e);
            CsvImportResultDto errorResult = CsvImportResultDto.builder()
                    .rowsProcessed(0)
                    .rowsSuccessful(0)
                    .rowsFailed(0)
                    .status("FAILED")
                    .message(resolveErrorMessage(e))
                    .build();
            return ResponseEntity.badRequest().body(errorResult);
        }
    }

    private String resolveErrorMessage(Exception e) {
        return e.getMessage() != null && !e.getMessage().isBlank()
                ? e.getMessage()
                : "Ошибка при обработке файла";
    }
}
