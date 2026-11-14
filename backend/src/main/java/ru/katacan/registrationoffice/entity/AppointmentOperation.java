package ru.katacan.registrationoffice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Журнал операций с записью на приём.
 * Хранит, кто и когда создал, перенёс или отменил запись, а также причину операции.
 */
@Entity
@Table(name = "t_appointment_operations")
@Setter
@Getter
@NoArgsConstructor
public class AppointmentOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_operation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "f_appointment_id", nullable = false)
    private Appointment appointment;

    @Column(name = "f_operation_type", nullable = false, length = 32)
    private String operationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "f_actor_id")
    private User actor;

    @Column(name = "f_occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(name = "f_reason", length = 255)
    private String reason;

    @Column(name = "f_old_slot_datetime")
    private LocalDateTime oldSlotDateTime;

    @Column(name = "f_new_slot_datetime")
    private LocalDateTime newSlotDateTime;

    @Column(name = "f_old_status", length = 50)
    private String oldStatus;

    @Column(name = "f_new_status", length = 50)
    private String newStatus;

    @PrePersist
    public void prePersist() {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }
}
