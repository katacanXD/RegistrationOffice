package ru.katacan.registrationoffice.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.katacan.registrationoffice.controller.AuthController;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ControllersContextTest {

    @Autowired
    private AuthController authController;

    @Test
    void authController_present_in_context() {
        assertThat(authController).isNotNull();
    }
}
