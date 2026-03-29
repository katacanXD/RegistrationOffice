package ru.katacan.registrationoffice.repository;

import ru.katacan.registrationoffice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.katacan.registrationoffice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLogin(String login);

    Optional<User> findByLoginIgnoreCase(String login);

    boolean existsByLogin(String login);

    boolean existsByLoginIgnoreCase(String login);

    Optional<User> findByFullName(String fullName);

    Optional<User> findByFullName(String fullName, String password);

    User findByPolicyNumber(String policyNumber);

    // --- МЕТОДЫ ДЛЯ ПАГИНАЦИИ ---

    // Поиск врачей по ФИО и Специальности
    @Query("SELECT u FROM User u WHERE u.role.name = 'doctor' " +
            "AND (:search IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:specialty IS NULL OR LOWER(u.speciality) LIKE LOWER(CONCAT('%', :specialty, '%')))")
    Page<User> searchDoctors(@Param("search") String search,
                             @Param("specialty") String specialty,
                             Pageable pageable);

    // Поиск пациентов по ФИО или Полису
    @Query("SELECT u FROM User u WHERE u.role.name = 'patient' " +
            "AND (:search IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR u.policyNumber LIKE CONCAT('%', :search, '%'))")
    Page<User> searchPatients(@Param("search") String search, Pageable pageable);

    // Общий поиск по всем пользователям
    @Query("SELECT u FROM User u WHERE " +
            ":search IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    // --- СТАРЫЕ МЕТОДЫ (можно оставить, если они нужны в других частях кода) ---

    @Query("SELECT u FROM User u WHERE u.role.name = 'doctor'")
    Page<User> findAllDoctors(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role.name = 'patient'")
    Page<User> findAllPatients(Pageable pageable);
}

