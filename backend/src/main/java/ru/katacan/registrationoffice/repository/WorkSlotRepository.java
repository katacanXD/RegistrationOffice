package ru.katacan.registrationoffice.repository;

import ru.katacan.registrationoffice.entity.WorkSlot;
import ru.katacan.registrationoffice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkSlotRepository extends JpaRepository<WorkSlot, Long> {

    List<WorkSlot> findByDoctorAndDate(User doctor, LocalDate date);

    List<WorkSlot> findByDoctorAndDateBetween(User doctor, LocalDate startDate, LocalDate endDate);

    Optional<WorkSlot> findByDoctorAndDateAndStartTime(User doctor, LocalDate date, LocalTime startTime);

    @Query("SELECT DISTINCT w.date FROM WorkSlot w WHERE w.doctor.id = :doctorId " +
            "AND w.date BETWEEN :startDate AND :endDate ORDER BY w.date")
    List<LocalDate> findDistinctDatesByDoctorAndDateRange(
            @Param("doctorId") Long doctorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT w FROM WorkSlot w WHERE w.doctor.id = :doctorId " +
            "AND w.date = :date AND :time BETWEEN w.startTime AND w.endTime")
    List<WorkSlot> findContainingTime(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("time") LocalTime time);
}