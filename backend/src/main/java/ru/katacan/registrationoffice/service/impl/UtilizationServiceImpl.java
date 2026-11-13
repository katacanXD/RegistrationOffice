package ru.katacan.registrationoffice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.katacan.registrationoffice.dto.UtilizationReportDto;
import ru.katacan.registrationoffice.dto.UtilizationRowDto;
import ru.katacan.registrationoffice.entity.Appointment;
import ru.katacan.registrationoffice.entity.User;
import ru.katacan.registrationoffice.entity.WorkSlot;
import ru.katacan.registrationoffice.exception.BadRequestException;
import ru.katacan.registrationoffice.exception.ResourceNotFoundException;
import ru.katacan.registrationoffice.repository.AppointmentRepository;
import ru.katacan.registrationoffice.repository.UserRepository;
import ru.katacan.registrationoffice.repository.WorkSlotRepository;
import ru.katacan.registrationoffice.service.UtilizationService;
import ru.katacan.registrationoffice.util.SlotGenerator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UtilizationServiceImpl implements UtilizationService {

    private static final int MAX_REPORT_DAYS = 31;

    private final UserRepository userRepository;
    private final WorkSlotRepository workSlotRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    public UtilizationReportDto buildReport(LocalDate from, LocalDate to, Long doctorId, String speciality) {
        DateRange range = normalizeRange(from, to);
        List<User> doctors = resolveDoctors(doctorId, speciality);

        List<UtilizationRowDto> rows = new ArrayList<>();
        int totalSlots = 0;
        int totalBooked = 0;

        for (User doctor : doctors) {
            Map<LocalDate, Set<LocalTime>> slotsByDate = buildSlotsByDate(doctor, range);

            for (Map.Entry<LocalDate, Set<LocalTime>> entry : slotsByDate.entrySet()) {
                LocalDate date = entry.getKey();
                Set<LocalTime> slotTimes = entry.getValue();
                if (slotTimes.isEmpty()) {
                    continue;
                }

                int booked = countBooked(doctor, date, slotTimes);
                int slots = slotTimes.size();
                totalSlots += slots;
                totalBooked += booked;

                rows.add(UtilizationRowDto.builder()
                        .doctorId(doctor.getId())
                        .doctorFio(doctor.getFullName())
                        .speciality(doctor.getSpeciality())
                        .date(date)
                        .slotsTotal(slots)
                        .slotsBooked(booked)
                        .utilization(ratio(booked, slots))
                        .build());
            }
        }

        rows.sort(Comparator
                .comparing(UtilizationRowDto::getDate)
                .thenComparing(UtilizationRowDto::getDoctorFio, Comparator.nullsLast(String::compareTo)));

        return UtilizationReportDto.builder()
                .from(range.from())
                .to(range.to())
                .rows(rows)
                .totalSlots(totalSlots)
                .totalBooked(totalBooked)
                .totalUtilization(ratio(totalBooked, totalSlots))
                .build();
    }

    @Override
    public String buildCsv(LocalDate from, LocalDate to, Long doctorId, String speciality) {
        UtilizationReportDto report = buildReport(from, to, doctorId, speciality);
        StringBuilder csv = new StringBuilder();
        csv.append("doctor_id,date,slots_total,slots_booked,utilization\n");
        for (UtilizationRowDto row : report.getRows()) {
            csv.append(row.getDoctorId()).append(',')
                    .append(row.getDate()).append(',')
                    .append(row.getSlotsTotal()).append(',')
                    .append(row.getSlotsBooked()).append(',')
                    .append(String.format(Locale.ROOT, "%.4f", row.getUtilization()))
                    .append('\n');
        }
        return csv.toString();
    }

    private DateRange normalizeRange(LocalDate from, LocalDate to) {
        LocalDate start = from;
        LocalDate end = to;

        if (start == null && end == null) {
            start = LocalDate.now();
            end = start.plusDays(6);
        }
        if (start == null || end == null) {
            throw new BadRequestException("Параметры from и to должны быть указаны вместе");
        }
        if (start.isAfter(end)) {
            throw new BadRequestException("Дата начала периода не может быть позже даты окончания");
        }
        if (ChronoUnit.DAYS.between(start, end) + 1 > MAX_REPORT_DAYS) {
            throw new BadRequestException("Период отчёта не должен превышать " + MAX_REPORT_DAYS + " дней");
        }
        return new DateRange(start, end);
    }

    private List<User> resolveDoctors(Long doctorId, String speciality) {
        if (doctorId != null) {
            User doctor = userRepository.findById(doctorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Врач с id " + doctorId + " не найден"));
            if (doctor.getRole() == null || !"doctor".equals(doctor.getRole().getName())) {
                throw new ResourceNotFoundException("Пользователь с id " + doctorId + " не является врачом");
            }
            return List.of(doctor);
        }

        String normalizedSpeciality = speciality != null && !speciality.isBlank()
                ? speciality.trim()
                : null;
        return userRepository.findDoctorsForReport(normalizedSpeciality);
    }

    private Map<LocalDate, Set<LocalTime>> buildSlotsByDate(User doctor, DateRange range) {
        List<WorkSlot> workSlots = workSlotRepository.findByDoctorAndDateBetween(
                doctor, range.from(), range.to());

        Map<LocalDate, Set<LocalTime>> result = new LinkedHashMap<>();
        for (WorkSlot workSlot : workSlots) {
            Set<LocalTime> times = result.computeIfAbsent(workSlot.getDate(), ignored -> new TreeSet<>());
            times.addAll(SlotGenerator.generateSlots(
                    workSlot.getStartTime(),
                    workSlot.getEndTime(),
                    workSlot.getSlotMinutes(),
                    workSlot.getBreakStart(),
                    workSlot.getBreakEnd()));
        }
        return result;
    }

    private int countBooked(User doctor, LocalDate date, Set<LocalTime> slotTimes) {
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndDate(doctor.getId(), date);
        return (int) appointments.stream()
                .filter(appointment -> appointment.getStatus() == null
                        || !"canceled".equals(appointment.getStatus().getName()))
                .filter(appointment -> appointment.getSlotDateTime() != null
                        && date.equals(appointment.getSlotDateTime().toLocalDate())
                        && slotTimes.contains(appointment.getSlotDateTime().toLocalTime()))
                .count();
    }

    private double ratio(int booked, int total) {
        if (total <= 0) {
            return 0.0;
        }
        return Math.round(((double) booked / total) * 10000.0) / 10000.0;
    }

    private record DateRange(LocalDate from, LocalDate to) {
    }
}
