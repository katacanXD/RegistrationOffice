package ru.katacan.registrationoffice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.katacan.registrationoffice.dto.DoctorCalendarDto;
import ru.katacan.registrationoffice.dto.DoctorListDto;
import ru.katacan.registrationoffice.dto.DoctorScheduleDto;
import ru.katacan.registrationoffice.dto.DoctorSlotsDto;
import ru.katacan.registrationoffice.mapper.UserMapper;
import ru.katacan.registrationoffice.mapper.WorkSlotMapper;
import ru.katacan.registrationoffice.service.UserService;
import ru.katacan.registrationoffice.service.WorkSlotService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final UserService userService;
    private final WorkSlotService workSlotService;

    @GetMapping()
    public ResponseEntity<DoctorListDto> getDoctors(
            @RequestParam(required = false) String search
    ) { //TODO добавить плагинацию
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.searchDoctors(search));
    }

    @GetMapping("/{doctorId}/slots")
    public ResponseEntity<DoctorSlotsDto> getDoctorSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(workSlotService.getDoctorSlots(doctorId, date));
    }

    @GetMapping("/{doctorId}/calendar")
    public ResponseEntity<DoctorCalendarDto> getDoctorCalendar(
            @PathVariable Long doctorId,
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        return ResponseEntity.status(HttpStatus.OK)
                .body(workSlotService.getDoctorSlotsBetweenDates(doctorId, startDate, endDate));
    }

    @GetMapping("/{doctorId}/schedule")
    public ResponseEntity<DoctorScheduleDto> getDoctorSchedule(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(workSlotService.getDoctorSchedule(doctorId, date));
    }
}