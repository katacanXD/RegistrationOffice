package ru.katacan.registrationoffice.service.impl;

import ru.katacan.registrationoffice.dto.DoctorCalendarDto;
import ru.katacan.registrationoffice.dto.DoctorScheduleDto;
import ru.katacan.registrationoffice.dto.DoctorSlotsDto;
import ru.katacan.registrationoffice.entity.User;
import ru.katacan.registrationoffice.entity.WorkSlot;
import ru.katacan.registrationoffice.exception.ResourceNotFoundException;
import ru.katacan.registrationoffice.mapper.WorkSlotMapper;
import ru.katacan.registrationoffice.repository.UserRepository;
import ru.katacan.registrationoffice.repository.WorkSlotRepository;
import ru.katacan.registrationoffice.service.WorkSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkSlotServiceImpl implements WorkSlotService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final WorkSlotRepository workSlotRepository;
    private final UserRepository userRepository;
    private final WorkSlotMapper workSlotMapper;
    // private final AppointmentRepository appointmentRepository;

    @Override
    public DoctorSlotsDto getDoctorSlots(Long doctorId, LocalDate date) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        List<WorkSlot> workSlots = workSlotRepository.findByDoctorAndDate(doctor, date);
        DoctorSlotsDto.BreakDto breakDto = getBreakForDoctor(workSlots);

        return workSlotMapper.toDoctorSlots(doctorId, date.toString(), workSlots, breakDto);

        /*
        // Собираем занятые времена (не canceled) для пометки isAvailable = false
        Set<LocalTime> bookedTimes = appointmentRepository.findByDoctorIdAndDate(doctorId, date)
                .stream()
                .filter(a -> !"canceled".equalsIgnoreCase(a.getStatus().getName()))
                .map(a -> a.getSlotDateTime().toLocalTime())
                .collect(Collectors.toSet());

        return workSlotMapper.toDoctorSlots(doctorId, date.toString(), workSlots, breakDto, bookedTimes);
        */
    }

    @Override
    public DoctorCalendarDto getDoctorSlotsBetweenDates(Long doctorId, LocalDate startDate, LocalDate endDate) {
        User doctor = userRepository.getReferenceById(doctorId);
        List<WorkSlot> workSlots = workSlotRepository.findByDoctorAndDateBetween(doctor, startDate, endDate);

        return workSlotMapper.toDoctorCalendar(workSlots);

        /*
        // Получаем все записи за период одним запросом
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

        return workSlotMapper.toDoctorCalendar(workSlots, bookedCountByDate);
        */
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

        List<WorkSlot> workSlots = workSlotRepository.findByDoctorAndDate(doctor, date);
        DoctorSlotsDto.BreakDto breakDto = getBreakForDoctor(workSlots);

        DoctorScheduleDto.BreakDto scheduleBreak = breakDto != null ?
                DoctorScheduleDto.BreakDto.builder()
                        .start(breakDto.getStart())
                        .end(breakDto.getEnd())
                        .build() : null;
        return workSlotMapper.toDoctorSchedule(date.toString(), workSlots, scheduleBreak);
    }
}