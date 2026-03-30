package ru.katacan.registrationoffice.repository;

import ru.katacan.registrationoffice.entity.Appointment;
import ru.katacan.registrationoffice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Appointment a " +
            "WHERE a.doctor = :doctor " +
            "AND a.slotDateTime = :slotDateTime " +
            "AND a.status.name <> 'canceled' " +
            "AND a.id <> :id")
    boolean existsActiveAppointment(@Param("doctor") User doctor,
                                    @Param("slotDateTime") LocalDateTime slotDateTime,
                                    @Param("id") Long id);

    List<Appointment> findByPatientOrderBySlotDateTimeDesc(User patient);

    List<Appointment> findByPatientAndStatus_NameIn(User patient, List<String> statuses);

    List<Appointment> findByDoctorAndSlotDateTimeBetween(User doctor, LocalDateTime start, LocalDateTime end);

    Optional<Appointment> findByDoctorAndSlotDateTime(User doctor, LocalDateTime slotDateTime);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a " +
            "WHERE a.doctor = :doctor AND a.slotDateTime = :slotDateTime " +
            "AND a.status.name != 'canceled'")
    boolean existsByDoctorAndSlotDateTimeAndNotCanceled(
            @Param("doctor") User doctor,
            @Param("slotDateTime") LocalDateTime slotDateTime);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
            "AND DATE(a.slotDateTime) = :date")
    List<Appointment> findByDoctorIdAndDate(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId " +
            "AND DATE(a.slotDateTime) = :date AND a.status.name != 'canceled'")
    int countBookedByDoctorAndDate(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date);
}