package ru.katacan.registrationoffice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.katacan.registrationoffice.dto.CsvExportResultDto;
import ru.katacan.registrationoffice.dto.CsvImportErrorDto;
import ru.katacan.registrationoffice.dto.CsvImportResultDto;
import ru.katacan.registrationoffice.entity.*;
import ru.katacan.registrationoffice.exception.CsvProcessingException;
import ru.katacan.registrationoffice.exception.CsvValidationException;
import ru.katacan.registrationoffice.exception.ResourceNotFoundException;
import ru.katacan.registrationoffice.repository.*;
import ru.katacan.registrationoffice.service.CsvService;
import ru.katacan.registrationoffice.util.LoginNormalizer;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CsvServiceImpl implements CsvService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final UserRepository userRepository;
    private final WorkSlotRepository workSlotRepository;
    private final AppointmentRepository appointmentRepository;
    private final DictStatusRepository statusRepository;
    private final DictRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // ==================== EXPORT METHODS ====================

    @Override
    public CsvExportResultDto exportDoctors() {
        log.info("Starting export of doctors");
        try {
            List<User> doctors = userRepository.findAll().stream()
                    .filter(u -> u.getRole() != null && "doctor".equals(u.getRole().getName()))
                    .toList();

            StringWriter sw = new StringWriter();
            try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT
                    .withHeader("login", "fullName", "speciality"))) {
                for (User doctor : doctors) {
                    printer.printRecord(
                            doctor.getLogin(),
                            doctor.getFullName(),
                            doctor.getSpeciality() != null ? doctor.getSpeciality() : ""
                    );
                }
                printer.flush();
            }

            log.info("Successfully exported {} doctors", doctors.size());
            return CsvExportResultDto.builder()
                    .fileName("doctors.csv")
                    .rowsExported(doctors.size())
                    .timestamp(LocalDateTime.now())
                    .status("SUCCESS")
                    .message("Успешно экспортировано врачей: " + doctors.size())
                    .csvContent(sw.toString())
                    .build();
        } catch (IOException e) {
            log.error("Error exporting doctors", e);
            throw new CsvProcessingException("Ошибка при экспорте врачей: " + e.getMessage(), e);
        }
    }

    @Override
    public CsvExportResultDto exportPatients() {
        log.info("Starting export of patients");
        try {
            List<User> patients = userRepository.findAll().stream()
                    .filter(u -> u.getRole() != null && "patient".equals(u.getRole().getName()))
                    .toList();

            StringWriter sw = new StringWriter();
            try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT
                    .withHeader("login", "fullName", "policyNumber"))) {
                for (User patient : patients) {
                    printer.printRecord(
                            patient.getLogin(),
                            patient.getFullName(),
                            patient.getPolicyNumber() != null ? patient.getPolicyNumber() : ""
                    );
                }
                printer.flush();
            }

            log.info("Successfully exported {} patients", patients.size());
            return CsvExportResultDto.builder()
                    .fileName("patients.csv")
                    .rowsExported(patients.size())
                    .timestamp(LocalDateTime.now())
                    .status("SUCCESS")
                    .message("Успешно экспортировано пациентов: " + patients.size())
                    .csvContent(sw.toString())
                    .build();
        } catch (IOException e) {
            log.error("Error exporting patients", e);
            throw new CsvProcessingException("Ошибка при экспорте пациентов: " + e.getMessage(), e);
        }
    }

    @Override
    public CsvExportResultDto exportAppointments() {
        log.info("Starting export of appointments");
        try {
            List<Appointment> appointments = appointmentRepository.findAll();

            StringWriter sw = new StringWriter();
            try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT
                    .withHeader("patientLogin", "doctorLogin", "slotDateTime", "status"))) {
                for (Appointment appointment : appointments) {
                    printer.printRecord(
                            appointment.getPatient().getLogin(),
                            appointment.getDoctor().getLogin(),
                            appointment.getSlotDateTime().format(DATE_TIME_FORMATTER),
                            appointment.getStatus().getName()
                    );
                }
                printer.flush();
            }

            log.info("Successfully exported {} appointments", appointments.size());
            return CsvExportResultDto.builder()
                    .fileName("appointments.csv")
                    .rowsExported(appointments.size())
                    .timestamp(LocalDateTime.now())
                    .status("SUCCESS")
                    .message("Успешно экспортировано записей: " + appointments.size())
                    .csvContent(sw.toString())
                    .build();
        } catch (IOException e) {
            log.error("Error exporting appointments", e);
            throw new CsvProcessingException("Ошибка при экспорте записей: " + e.getMessage(), e);
        }
    }

    @Override
    public CsvExportResultDto exportWorkSlots() {
        log.info("Starting export of work slots");
        try {
            List<WorkSlot> workSlots = workSlotRepository.findAll();

            StringWriter sw = new StringWriter();
            try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT
                    .withHeader("doctorLogin", "date", "startTime", "endTime", "slotMinutes", "breakStart", "breakEnd"))) {
                for (WorkSlot slot : workSlots) {
                    printer.printRecord(
                            slot.getDoctor().getLogin(),
                            slot.getDate().format(DATE_FORMATTER),
                            slot.getStartTime().format(TIME_FORMATTER),
                            slot.getEndTime().format(TIME_FORMATTER),
                            slot.getSlotMinutes(),
                            slot.getBreakStart() != null ? slot.getBreakStart().format(TIME_FORMATTER) : "",
                            slot.getBreakEnd() != null ? slot.getBreakEnd().format(TIME_FORMATTER) : ""
                    );
                }
                printer.flush();
            }

            log.info("Successfully exported {} work slots", workSlots.size());
            return CsvExportResultDto.builder()
                    .fileName("work_slots.csv")
                    .rowsExported(workSlots.size())
                    .timestamp(LocalDateTime.now())
                    .status("SUCCESS")
                    .message("Успешно экспортировано расписаний: " + workSlots.size())
                    .csvContent(sw.toString())
                    .build();
        } catch (IOException e) {
            log.error("Error exporting work slots", e);
            throw new CsvProcessingException("Ошибка при экспорте расписания: " + e.getMessage(), e);
        }
    }

    // ==================== IMPORT METHODS ====================

    @Override
    public CsvImportResultDto importDoctors(String csvContent) {
        log.info("Starting import of doctors from CSV");
        List<CsvImportErrorDto> errors = new ArrayList<>();
        int successCount = 0;
        int processedCount = 0;

        try {
            StringReader sr = new StringReader(csvContent);
            try (CSVParser parser = new CSVParser(sr, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
                DictRole doctorRole = roleRepository.findByName("doctor")
                        .orElseThrow(() -> new ResourceNotFoundException("Роль 'doctor' не найдена"));

                for (var record : parser) {
                    processedCount++;
                    try {
                        String login = record.get("login");
                        String fullName = record.get("fullName");
                        String speciality = record.get("speciality");

                        if (login == null || login.trim().isEmpty()) {
                            throw new CsvValidationException("Поле 'login' обязательно", processedCount, record.toString());
                        }
                        if (fullName == null || fullName.trim().isEmpty()) {
                            throw new CsvValidationException("Поле 'fullName' обязательно", processedCount, record.toString());
                        }

                        String normalizedLogin = LoginNormalizer.normalize(login);

                        if (userRepository.existsByLoginIgnoreCase(normalizedLogin)) {
                            throw new CsvValidationException("Пользователь с логином '" + login + "' уже существует",
                                    processedCount, record.toString());
                        }

                        User doctor = new User();
                        doctor.setLogin(normalizedLogin);
                        doctor.setFullName(fullName.trim());
                        doctor.setSpeciality(speciality != null ? speciality.trim() : null);
                        doctor.setRole(doctorRole);
                        String password = generateTempPassword(login);
                        doctor.setPassword(passwordEncoder.encode(password));

                        userRepository.save(doctor);
                        successCount++;
                        log.debug("Successfully imported doctor: {}", login);

                    } catch (CsvValidationException e) {
                        errors.add(CsvImportErrorDto.builder()
                                .rowNumber(e.getRowNumber())
                                .errorMessage(e.getMessage())
                                .data(e.getRowData())
                                .build());
                        log.warn("Validation error at row {}: {}", processedCount, e.getMessage());
                    } catch (Exception e) {
                        errors.add(CsvImportErrorDto.builder()
                                .rowNumber(processedCount)
                                .errorMessage("Ошибка обработки: " + e.getMessage())
                                .data(record.toString())
                                .build());
                        log.warn("Error processing doctor at row {}: {}", processedCount, e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error parsing CSV content", e);
            throw new CsvProcessingException("Ошибка при парсинге CSV: " + e.getMessage(), e);
        }

        String status = errors.isEmpty() ? "SUCCESS" : (successCount > 0 ? "PARTIAL" : "FAILED");
        return CsvImportResultDto.builder()
                .rowsProcessed(processedCount)
                .rowsSuccessful(successCount)
                .rowsFailed(errors.size())
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .status(status)
                .message(String.format("Обработано: %d, успешно: %d, ошибок: %d", processedCount, successCount, errors.size()))
                .build();
    }

    @Override
    public CsvImportResultDto importPatients(String csvContent) {
        log.info("Starting import of patients from CSV");
        List<CsvImportErrorDto> errors = new ArrayList<>();
        int successCount = 0;
        int processedCount = 0;

        try {
            StringReader sr = new StringReader(csvContent);
            try (CSVParser parser = new CSVParser(sr, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
                DictRole patientRole = roleRepository.findByName("patient")
                        .orElseThrow(() -> new ResourceNotFoundException("Роль 'patient' не найдена"));

                for (var record : parser) {
                    processedCount++;
                    try {
                        String login = record.get("login");
                        String fullName = record.get("fullName");
                        String policyNumber = record.get("policyNumber");

                        if (login == null || login.trim().isEmpty()) {
                            throw new CsvValidationException("Поле 'login' обязательно", processedCount, record.toString());
                        }
                        if (fullName == null || fullName.trim().isEmpty()) {
                            throw new CsvValidationException("Поле 'fullName' обязательно", processedCount, record.toString());
                        }

                        String normalizedLogin = LoginNormalizer.normalize(login);

                        if (userRepository.existsByLoginIgnoreCase(normalizedLogin)) {
                            throw new CsvValidationException("Пользователь с логином '" + login + "' уже существует",
                                    processedCount, record.toString());
                        }

                        User patient = new User();
                        patient.setLogin(normalizedLogin);
                        patient.setFullName(fullName.trim());
                        patient.setPolicyNumber(policyNumber != null ? policyNumber.trim() : null);
                        patient.setRole(patientRole);
                        String password = generateTempPassword(login);
                        patient.setPassword(passwordEncoder.encode(password));

                        userRepository.save(patient);
                        successCount++;
                        log.debug("Successfully imported patient: {}", login);

                    } catch (CsvValidationException e) {
                        errors.add(CsvImportErrorDto.builder()
                                .rowNumber(e.getRowNumber())
                                .errorMessage(e.getMessage())
                                .data(e.getRowData())
                                .build());
                        log.warn("Validation error at row {}: {}", processedCount, e.getMessage());
                    } catch (Exception e) {
                        errors.add(CsvImportErrorDto.builder()
                                .rowNumber(processedCount)
                                .errorMessage("Ошибка обработки: " + e.getMessage())
                                .data(record.toString())
                                .build());
                        log.warn("Error processing patient at row {}: {}", processedCount, e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error parsing CSV content", e);
            throw new CsvProcessingException("Ошибка при парсинге CSV: " + e.getMessage(), e);
        }

        String status = errors.isEmpty() ? "SUCCESS" : (successCount > 0 ? "PARTIAL" : "FAILED");
        return CsvImportResultDto.builder()
                .rowsProcessed(processedCount)
                .rowsSuccessful(successCount)
                .rowsFailed(errors.size())
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .status(status)
                .message(String.format("Обработано: %d, успешно: %d, ошибок: %d", processedCount, successCount, errors.size()))
                .build();
    }

    @Override
    public CsvImportResultDto importAppointments(String csvContent) {
        log.info("Starting import of appointments from CSV");
        List<CsvImportErrorDto> errors = new ArrayList<>();
        int successCount = 0;
        int processedCount = 0;

        try {
            StringReader sr = new StringReader(csvContent);
            try (CSVParser parser = new CSVParser(sr, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

                for (var record : parser) {
                    processedCount++;
                    try {
                        String patientLogin = record.get("patientLogin");
                        String doctorLogin = record.get("doctorLogin");
                        String slotDatetime = record.get("slotDateTime");
                        String statusName = record.get("status");

                        if (patientLogin == null || patientLogin.trim().isEmpty()) {
                            throw new CsvValidationException("Поле 'patientLogin' обязательно", processedCount, record.toString());
                        }
                        if (doctorLogin == null || doctorLogin.trim().isEmpty()) {
                            throw new CsvValidationException("Поле 'doctorLogin' обязательно", processedCount, record.toString());
                        }
                        if (slotDatetime == null || slotDatetime.trim().isEmpty()) {
                            throw new CsvValidationException("Поле 'slotDateTime' обязательно", processedCount, record.toString());
                        }
                        if (statusName == null || statusName.trim().isEmpty()) {
                            throw new CsvValidationException("Поле 'status' обязательно", processedCount, record.toString());
                        }

                        String normalizedPatientLogin = LoginNormalizer.normalize(patientLogin);
                        String normalizedDoctorLogin = LoginNormalizer.normalize(doctorLogin);

                        User patient = userRepository.findByLoginIgnoreCase(normalizedPatientLogin)
                                .orElseThrow(() -> new CsvValidationException("Пациент с логином '" + patientLogin + "' не найден",
                                        processedCount, record.toString()));

                        User doctor = userRepository.findByLoginIgnoreCase(normalizedDoctorLogin)
                                .orElseThrow(() -> new CsvValidationException("Врач с логином '" + doctorLogin + "' не найден",
                                        processedCount, record.toString()));

                        LocalDateTime appointmentTime;
                        try {
                            appointmentTime = LocalDateTime.parse(slotDatetime, DATE_TIME_FORMATTER);
                        } catch (DateTimeParseException e) {
                            throw new CsvValidationException("Неверный формат даты и времени: " + slotDatetime,
                                    processedCount, record.toString());
                        }

                        DictStatus status = statusRepository.findByName(statusName.trim())
                                .orElseThrow(() -> new CsvValidationException("Статус '" + statusName + "' не найден",
                                        processedCount, record.toString()));

                        if (appointmentRepository.existsByDoctorAndSlotDateTimeAndNotCanceled(doctor, appointmentTime)) {
                            throw new CsvValidationException("На этот слот уже есть активная запись",
                                    processedCount, record.toString());
                        }

                        Appointment appointment = new Appointment();
                        appointment.setPatient(patient);
                        appointment.setDoctor(doctor);
                        appointment.setSlotDateTime(appointmentTime);
                        appointment.setStatus(status);

                        appointmentRepository.save(appointment);
                        successCount++;
                        log.debug("Successfully imported appointment for patient: {} with doctor: {}", patientLogin, doctorLogin);

                    } catch (CsvValidationException e) {
                        errors.add(CsvImportErrorDto.builder()
                                .rowNumber(e.getRowNumber())
                                .errorMessage(e.getMessage())
                                .data(e.getRowData())
                                .build());
                        log.warn("Validation error at row {}: {}", processedCount, e.getMessage());
                    } catch (Exception e) {
                        errors.add(CsvImportErrorDto.builder()
                                .rowNumber(processedCount)
                                .errorMessage("Ошибка обработки: " + e.getMessage())
                                .data(record.toString())
                                .build());
                        log.warn("Error processing appointment at row {}: {}", processedCount, e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error parsing CSV content", e);
            throw new CsvProcessingException("Ошибка при парсинге CSV: " + e.getMessage(), e);
        }

        String status = errors.isEmpty() ? "SUCCESS" : (successCount > 0 ? "PARTIAL" : "FAILED");
        return CsvImportResultDto.builder()
                .rowsProcessed(processedCount)
                .rowsSuccessful(successCount)
                .rowsFailed(errors.size())
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .status(status)
                .message(String.format("Обработано: %d, успешно: %d, ошибок: %d", processedCount, successCount, errors.size()))
                .build();
    }

    @Override
    public CsvImportResultDto importWorkSlots(String csvContent) {
        log.info("Starting import of work slots from CSV");
        List<CsvImportErrorDto> errors = new ArrayList<>();
        int successCount = 0;
        int processedCount = 0;

        try {
            StringReader sr = new StringReader(csvContent);
            try (CSVParser parser = new CSVParser(sr, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

                for (var record : parser) {
                    processedCount++;
                    try {
                        String doctorLogin = record.get("doctorLogin");
                        String date = record.get("date");
                        String startTime = record.get("startTime");
                        String endTime = record.get("endTime");
                        String slotMinutes = record.get("slotMinutes");
                        String breakStart = record.get("breakStart");
                        String breakEnd = record.get("breakEnd");

                        if (doctorLogin == null || doctorLogin.trim().isEmpty()) {
                            throw new CsvValidationException("Поле 'doctorLogin' обязательно", processedCount, record.toString());
                        }
                        if (date == null || date.trim().isEmpty()) {
                            throw new CsvValidationException("Поле 'date' обязательно", processedCount, record.toString());
                        }
                        if (startTime == null || startTime.trim().isEmpty()) {
                            throw new CsvValidationException("Поле 'startTime' обязательно", processedCount, record.toString());
                        }
                        if (endTime == null || endTime.trim().isEmpty()) {
                            throw new CsvValidationException("Поле 'endTime' обязательно", processedCount, record.toString());
                        }
                        if (slotMinutes == null || slotMinutes.trim().isEmpty()) {
                            throw new CsvValidationException("Поле 'slotMinutes' обязательно", processedCount, record.toString());
                        }

                        String normalizedDoctorLogin = LoginNormalizer.normalize(doctorLogin);

                        User doctor = userRepository.findByLoginIgnoreCase(normalizedDoctorLogin)
                                .orElseThrow(() -> new CsvValidationException("Врач с логином '" + doctorLogin + "' не найден",
                                        processedCount, record.toString()));

                        LocalDate slotDate;
                        LocalTime slotStartTime;
                        LocalTime slotEndTime;
                        LocalTime slotBreakStart = null;
                        LocalTime slotBreakEnd = null;
                        Integer minutes;

                        try {
                            slotDate = LocalDate.parse(date.trim(), DATE_FORMATTER);
                        } catch (DateTimeParseException e) {
                            throw new CsvValidationException("Неверный формат даты: " + date, processedCount, record.toString());
                        }

                        try {
                            slotStartTime = LocalTime.parse(startTime.trim(), TIME_FORMATTER);
                        } catch (DateTimeParseException e) {
                            throw new CsvValidationException("Неверный формат начального времени: " + startTime,
                                    processedCount, record.toString());
                        }

                        try {
                            slotEndTime = LocalTime.parse(endTime.trim(), TIME_FORMATTER);
                        } catch (DateTimeParseException e) {
                            throw new CsvValidationException("Неверный формат конечного времени: " + endTime,
                                    processedCount, record.toString());
                        }

                        try {
                            minutes = Integer.parseInt(slotMinutes.trim());
                            if (minutes <= 0) {
                                throw new NumberFormatException("Значение должно быть больше 0");
                            }
                        } catch (NumberFormatException e) {
                            throw new CsvValidationException("Неверное значение для слотов (в минутах): " + slotMinutes,
                                    processedCount, record.toString());
                        }

                        if (breakStart != null && !breakStart.trim().isEmpty()) {
                            try {
                                slotBreakStart = LocalTime.parse(breakStart.trim(), TIME_FORMATTER);
                            } catch (DateTimeParseException e) {
                                throw new CsvValidationException("Неверный формат времени начала перерыва: " + breakStart,
                                        processedCount, record.toString());
                            }
                        }

                        if (breakEnd != null && !breakEnd.trim().isEmpty()) {
                            try {
                                slotBreakEnd = LocalTime.parse(breakEnd.trim(), TIME_FORMATTER);
                            } catch (DateTimeParseException e) {
                                throw new CsvValidationException("Неверный формат времени конца перерыва: " + breakEnd,
                                        processedCount, record.toString());
                            }
                        }

                        if (slotStartTime.isAfter(slotEndTime) || slotStartTime.equals(slotEndTime)) {
                            throw new CsvValidationException("Время начала должно быть раньше времени конца",
                                    processedCount, record.toString());
                        }

                        if (slotBreakStart != null && slotBreakEnd != null) {
                            if (slotBreakStart.isAfter(slotBreakEnd) || slotBreakStart.equals(slotBreakEnd)) {
                                throw new CsvValidationException("Время начала перерыва должно быть раньше времени конца перерыва",
                                        processedCount, record.toString());
                            }
                            if (slotBreakStart.isBefore(slotStartTime) || slotBreakEnd.isAfter(slotEndTime)) {
                                throw new CsvValidationException("Перерыв должен быть в пределах рабочего времени",
                                        processedCount, record.toString());
                            }
                        }

                        if (workSlotRepository.findByDoctorAndDateAndStartTime(doctor, slotDate, slotStartTime).isPresent()) {
                            throw new CsvValidationException("Расписание на этот день и время для врача уже существует",
                                    processedCount, record.toString());
                        }

                        WorkSlot workSlot = new WorkSlot();
                        workSlot.setDoctor(doctor);
                        workSlot.setDate(slotDate);
                        workSlot.setStartTime(slotStartTime);
                        workSlot.setEndTime(slotEndTime);
                        workSlot.setSlotMinutes(minutes);
                        workSlot.setBreakStart(slotBreakStart);
                        workSlot.setBreakEnd(slotBreakEnd);

                        workSlotRepository.save(workSlot);
                        successCount++;
                        log.debug("Successfully imported work slot for doctor: {} on date: {}", doctorLogin, date);

                    } catch (CsvValidationException e) {
                        errors.add(CsvImportErrorDto.builder()
                                .rowNumber(e.getRowNumber())
                                .errorMessage(e.getMessage())
                                .data(e.getRowData())
                                .build());
                        log.warn("Validation error at row {}: {}", processedCount, e.getMessage());
                    } catch (Exception e) {
                        errors.add(CsvImportErrorDto.builder()
                                .rowNumber(processedCount)
                                .errorMessage("Ошибка обработки: " + e.getMessage())
                                .data(record.toString())
                                .build());
                        log.warn("Error processing work slot at row {}: {}", processedCount, e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error parsing CSV content", e);
            throw new CsvProcessingException("Ошибка при парсинге CSV: " + e.getMessage(), e);
        }

        String status = errors.isEmpty() ? "SUCCESS" : (successCount > 0 ? "PARTIAL" : "FAILED");
        return CsvImportResultDto.builder()
                .rowsProcessed(processedCount)
                .rowsSuccessful(successCount)
                .rowsFailed(errors.size())
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .status(status)
                .message(String.format("Обработано: %d, успешно: %d, ошибок: %d", processedCount, successCount, errors.size()))
                .build();
    }

    private String generateTempPassword(String login) {
        return login.substring(0, Math.min(1, login.length())).toUpperCase() +
               login.substring(0, Math.min(2, login.length())) +
               "123";
    }
}

