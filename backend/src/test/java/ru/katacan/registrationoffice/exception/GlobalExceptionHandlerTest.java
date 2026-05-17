package ru.katacan.registrationoffice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import ru.katacan.registrationoffice.dto.ErrorResponseDto;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void handleBadRequest_returns_400_and_message() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        BadRequestException ex = new BadRequestException("Некорректно");

        ResponseEntity<ErrorResponseDto> resp = handler.handleBadRequest(ex);

        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Некорректно");
    }
}
