package ru.katacan.registrationoffice.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.katacan.registrationoffice.entity.DictRole;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DictRoleRepositoryTest {

    @Autowired
    private DictRoleRepository dictRoleRepository;

    @Test
    void findByName_and_exists() {
        DictRole role = new DictRole();
        role.setName("patient");
        dictRoleRepository.save(role);

        Optional<DictRole> found = dictRoleRepository.findByName("patient");

        assertThat(found).isPresent();
        assertThat(dictRoleRepository.existsByName("patient")).isTrue();
    }
}
