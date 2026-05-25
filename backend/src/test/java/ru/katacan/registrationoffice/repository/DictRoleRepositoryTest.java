package ru.katacan.registrationoffice.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.katacan.registrationoffice.entity.DictRole;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictRoleRepositoryTest {

    @Mock
    private DictRoleRepository dictRoleRepository;

    @Test
    void findByName_and_exists() {
        DictRole role = new DictRole();
        role.setId(1L);
        role.setName("patient");

        when(dictRoleRepository.findByName("patient")).thenReturn(Optional.of(role));
        when(dictRoleRepository.existsByName("patient")).thenReturn(true);

        Optional<DictRole> found = dictRoleRepository.findByName("patient");

        assertThat(found).isPresent();
        assertThat(dictRoleRepository.existsByName("patient")).isTrue();
    }
}
