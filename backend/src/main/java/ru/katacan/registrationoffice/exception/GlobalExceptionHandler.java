package ru.katacan.registrationoffice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.katacan.registrationoffice.dto.ErrorResponseDto;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Некорректные данные запроса");

        log.debug("Validation error: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDto.builder().message(message).build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleMalformedJson(HttpMessageNotReadableException e) {
        Throwable rootCause = getRootCause(e);
        String message = "Некорректный JSON в теле запроса";
        String exceptionName = rootCause.getClass().getSimpleName();

        if ("UnrecognizedPropertyException".equals(exceptionName)) {
            String propertyName = extractPropertyName(rootCause);
            message = propertyName != null
                    ? "JSON содержит недопустимое поле: " + propertyName
                    : "JSON содержит недопустимые поля";
        } else if ("InvalidFormatException".equals(exceptionName)) {
            message = "JSON содержит значение неверного типа";
        } else if ("MismatchedInputException".equals(exceptionName)) {
            message = "JSON имеет некорректную структуру";
        } else if ("JsonParseException".equals(exceptionName)) {
            message = "JSON синтаксически некорректен";
        }

        log.debug("Malformed JSON: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDto.builder().message(message).build());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(ResourceNotFoundException e) {
        log.debug("Resource not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseDto.builder().message(e.getMessage()).build());
    }

    @ExceptionHandler(SlotNotAvailableException.class)
    public ResponseEntity<ErrorResponseDto> handleSlotNotAvailable(SlotNotAvailableException e) {
        log.debug("Slot not available: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponseDto.builder().message(e.getMessage()).build());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequest(BadRequestException e) {
        log.debug("Bad request: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDto.builder().message(e.getMessage()).build());
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccountLocked(AccountLockedException e) {
        log.debug("Account temporarily locked: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ErrorResponseDto.builder().message(e.getMessage()).build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(BadCredentialsException e) {
        log.debug("Bad credentials: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponseDto.builder().message("Требуется авторизация").build());
    }

    @ExceptionHandler(SystemConfigurationException.class)
    public ResponseEntity<ErrorResponseDto> handleSystemConfig(SystemConfigurationException e) {
        log.error("System configuration error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponseDto.builder().message("Ошибка конфигурации системы").build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneric(Exception e) {
        log.error("Unexpected error occurred: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponseDto.builder().message("Внутренняя ошибка сервера").build());
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private String extractPropertyName(Throwable throwable) {
        try {
            Object value = throwable.getClass().getMethod("getPropertyName").invoke(throwable);
            return value != null ? value.toString() : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
//ПИРИВЕТ