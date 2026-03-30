package ru.katacan.registrationoffice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "t_dict_role")
@Setter
@Getter
@NoArgsConstructor
public class DictRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @Column(name = "f_role_id")
    private Long id;

    @JsonValue
    @Column(name = "f_role_name", nullable = false, unique = true, length = 50)
    private String name;
}

