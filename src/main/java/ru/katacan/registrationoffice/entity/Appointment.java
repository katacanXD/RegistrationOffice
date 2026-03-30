package ru.katacan.registrationoffice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_appointments")
@Setter
@Getter
@NoArgsConstructor
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_appoint_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "f_doctor_id", nullable = false)
    private User doctor;

    @Column(name = "f_slot_datetime", nullable = false)
    private LocalDateTime slotDateTime;

    @ManyToOne
    @JoinColumn(name = "f_status_id", nullable = false)
    private DictStatus status;

    @ManyToOne
    @JoinColumn(name = "f_patient_id", nullable = false)
    private User patient;
}
