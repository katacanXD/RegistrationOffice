package ru.katacan.registrationoffice.repository;

import ru.katacan.registrationoffice.entity.DictRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DictRoleRepository extends JpaRepository<DictRole, Long> {

    Optional<DictRole> findByName(String name);

    boolean existsByName(String name);
}
