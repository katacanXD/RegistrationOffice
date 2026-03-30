package ru.katacan.registrationoffice.service;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import ru.katacan.registrationoffice.dto.DoctorCalendarDto;
import ru.katacan.registrationoffice.dto.DoctorScheduleDto;
import ru.katacan.registrationoffice.dto.DoctorSlotsDto;
import ru.katacan.registrationoffice.entity.WorkSlot;

import java.time.LocalDate;
import java.util.List;

public interface WorkSlotService {
    DoctorSlotsDto getDoctorSlots(Long doctorId, LocalDate date);
    DoctorCalendarDto getDoctorSlotsBetweenDates(Long doctorId, LocalDate startDate, LocalDate endDate);
    DoctorScheduleDto getDoctorSchedule(Long doctorId, LocalDate date);
}
