package ru.katacan.registrationoffice.repository;

import ru.katacan.registrationoffice.entity.DictStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DictStatusRepository extends JpaRepository<DictStatus, Long> {

    Optional<DictStatus> findByName(String name); // name = "booked", "canceled", "completed"
}