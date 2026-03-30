package ru.katacan.registrationoffice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "t_users")
@Setter
@Getter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_users_id")
    private Long id;

    @Column(name = "f_login", nullable = false, unique = true)
    private String login;

    @Column(name = "f_fio", nullable = false)
    private String fullName;

    @Column(name = "f_password", nullable = false)
    private String password;

    @Column(name = "f_policy_number", length = 30)
    private String policyNumber;

    @ManyToOne
    @JoinColumn(name = "f_role_id", nullable = false)
    private DictRole role;

    @Column(name = "f_speciality_name")
    private String speciality;
}