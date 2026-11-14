package ru.katacan.registrationoffice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.katacan.registrationoffice.entity.AppointmentOperation;

import java.util.List;

@Repository
public interface AppointmentOperationRepository extends JpaRepository<AppointmentOperation, Long> {

    @Query("SELECT o FROM AppointmentOperation o " +
            "WHERE o.appointment.id = :appointmentId " +
            "ORDER BY o.occurredAt ASC, o.id ASC")
    List<AppointmentOperation> findByAppointmentIdOrderByOccurredAtAsc(@Param("appointmentId") Long appointmentId);
}
