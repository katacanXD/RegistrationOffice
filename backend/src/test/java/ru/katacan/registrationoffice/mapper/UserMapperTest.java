package ru.katacan.registrationoffice.mapper;

import org.junit.jupiter.api.Test;
import ru.katacan.registrationoffice.entity.DictRole;
import ru.katacan.registrationoffice.entity.User;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void toRegistrationResponse_and_loginResponse() {
        DictRole role = new DictRole();
        role.setName("patient");

        User user = new User();
        user.setId(5L);
        user.setLogin("user5");
        user.setFullName("Иван Петров");
        user.setRole(role);

        var reg = mapper.toRegistrationResponse(user, "ok");
        assertThat(reg.getUserId()).isEqualTo(5L);
        assertThat(reg.getRole()).isEqualTo("patient");

        var login = mapper.toLoginResponse(user, "token");
        assertThat(login.getAccessToken()).isEqualTo("token");
        assertThat(login.getUser()).isNotNull();
    }
}
