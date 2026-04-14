package ru.katacan.registrationoffice.service.impl;

import ru.katacan.registrationoffice.dto.DoctorCalendarDto;
import ru.katacan.registrationoffice.dto.DoctorScheduleDto;
import ru.katacan.registrationoffice.dto.DoctorSlotsDto;
import ru.katacan.registrationoffice.entity.Appointment;
import ru.katacan.registrationoffice.entity.User;
import ru.katacan.registrationoffice.entity.WorkSlot;
import ru.katacan.registrationoffice.exception.ResourceNotFoundException;
import ru.katacan.registrationoffice.mapper.WorkSlotMapper;
import ru.katacan.registrationoffice.repository.AppointmentRepository;
import ru.katacan.registrationoffice.repository.UserRepository;
import ru.katacan.registrationoffice.repository.WorkSlotRepository;
import ru.katacan.registrationoffice.service.WorkSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkSlotServiceImpl implements WorkSlotService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final WorkSlotRepository workSlotRepository;
    private final UserRepository userRepository;
    private final WorkSlotMapper workSlotMapper;
    private final AppointmentRepository appointmentRepository;

    @Override
    public DoctorSlotsDto getDoctorSlots(Long doctorId, LocalDate date) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        List<WorkSlot> workSlots = ensureWorkSlotsForDate(doctor, date);
        DoctorSlotsDto.BreakDto breakDto = getBreakForDoctor(workSlots);

        // Получаем все записи на этот день, которые не отменены
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndDate(doctorId, date);
        Set<LocalTime> bookedTimes = appointments.stream()
                .filter(a -> !"canceled".equalsIgnoreCase(a.getStatus().getName()))
                .map(a -> a.getSlotDateTime().toLocalTime())
                .collect(Collectors.toSet());

        return workSlotMapper.toDoctorSlotsWithAvailability(doctorId, date.toString(), workSlots, breakDto, bookedTimes);
    }

    @Override
    public DoctorCalendarDto getDoctorSlotsBetweenDates(Long doctorId, LocalDate startDate, LocalDate endDate) {
        User doctor = userRepository.getReferenceById(doctorId);
        ensureWorkSlotsForRange(doctor, startDate, endDate);
        List<WorkSlot> workSlots = workSlotRepository.findByDoctorAndDateBetween(doctor, startDate, endDate);

        // Получаем все записи за период
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        List<Appointment> appointments = appointmentRepository
                .findByDoctorAndSlotDateTimeBetween(doctor, startDateTime, endDateTime);

        // Считаем активные записи по датам (не canceled)
        Map<LocalDate, Integer> bookedCountByDate = appointments.stream()
                .filter(a -> !"canceled".equalsIgnoreCase(a.getStatus().getName()))
                .collect(Collectors.groupingBy(
                        a -> a.getSlotDateTime().toLocalDate(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        return workSlotMapper.toDoctorCalendarWithBookings(workSlots, bookedCountByDate);
    }

    private DoctorSlotsDto.BreakDto getBreakForDoctor(List<WorkSlot> workSlots) {
        return workSlots.stream()
                .filter(slot -> slot.getBreakStart() != null && slot.getBreakEnd() != null)
                .findFirst()
                .map(slot -> DoctorSlotsDto.BreakDto.builder()
                        .start(slot.getBreakStart().format(TIME_FORMATTER))
                        .end(slot.getBreakEnd().format(TIME_FORMATTER))
                        .build())
                .orElse(null);
    }

    @Override
    public DoctorScheduleDto getDoctorSchedule(Long doctorId, LocalDate date) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        List<WorkSlot> workSlots = ensureWorkSlotsForDate(doctor, date);
        DoctorSlotsDto.BreakDto breakDto = getBreakForDoctor(workSlots);

        // Получаем все записи на этот день для детального расписания
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndDate(doctorId, date);
        Map<LocalTime, Appointment> appointmentsByTime = appointments.stream()
                .filter(a -> !"canceled".equalsIgnoreCase(a.getStatus().getName()))
                .collect(Collectors.toMap(
                        a -> a.getSlotDateTime().toLocalTime(),
                        a -> a,
                        (existing, duplicate) -> existing
                ));

        DoctorScheduleDto.BreakDto scheduleBreak = breakDto != null ?
                DoctorScheduleDto.BreakDto.builder()
                        .start(breakDto.getStart())
                        .end(breakDto.getEnd())
                        .build() : null;

        return workSlotMapper.toDoctorScheduleWithBookings(date.toString(), workSlots, scheduleBreak, appointmentsByTime);
    }

    private List<WorkSlot> ensureWorkSlotsForDate(User doctor, LocalDate date) {
        List<WorkSlot> existing = workSlotRepository.findByDoctorAndDate(doctor, date);
        if (!existing.isEmpty()) {
            return existing;
        }

        List<WorkSlot> generated = createDefaultDaySlots(doctor, date);
        return workSlotRepository.saveAll(generated);
    }

    private void ensureWorkSlotsForRange(User doctor, LocalDate startDate, LocalDate endDate) {
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            ensureWorkSlotsForDate(doctor, date);
            date = date.plusDays(1);
        }
    }

    private List<WorkSlot> createDefaultDaySlots(User doctor, LocalDate date) {
        WorkSlot morningSlot = new WorkSlot();
        morningSlot.setDoctor(doctor);
        morningSlot.setDate(date);
        morningSlot.setStartTime(LocalTime.of(9, 0));
        morningSlot.setEndTime(LocalTime.of(13, 0));
        morningSlot.setSlotMinutes(60);
        morningSlot.setBreakStart(LocalTime.of(11, 0));
        morningSlot.setBreakEnd(LocalTime.of(11, 15));

        WorkSlot eveningSlot = new WorkSlot();
        eveningSlot.setDoctor(doctor);
        eveningSlot.setDate(date);
        eveningSlot.setStartTime(LocalTime.of(14, 0));
        eveningSlot.setEndTime(LocalTime.of(18, 0));
        eveningSlot.setSlotMinutes(60);
        eveningSlot.setBreakStart(LocalTime.of(16, 0));
        eveningSlot.setBreakEnd(LocalTime.of(16, 15));

        return List.of(morningSlot, eveningSlot);
    }
}