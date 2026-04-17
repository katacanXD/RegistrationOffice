package ru.katacan.registrationoffice.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.katacan.registrationoffice.dto.ErrorResponseDto;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String VALIDATION_MESSAGE = "Некорректные данные запроса";
    private static final String MALFORMED_JSON_MESSAGE = "Некорректный JSON в теле запроса";
    private static final String NOT_FOUND_MESSAGE = "Запрошенный ресурс не найден";
    private static final String BAD_REQUEST_MESSAGE = "Некорректный запрос";
    private static final String AUTHENTICATION_MESSAGE = "Требуется авторизация";
    private static final String ACCESS_DENIED_MESSAGE = "Недостаточно прав для выполнения операции";
    private static final String CONFLICT_MESSAGE = "Запрос не может быть выполнен из-за конфликта данных";
    private static final String INTERNAL_ERROR_MESSAGE = "Внутренняя ошибка сервера";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage() != null && !error.getDefaultMessage().isBlank()
                        ? error.getDefaultMessage()
                        : "Поле '" + error.getField() + "' заполнено некорректно")
                .distinct()
                .collect(Collectors.joining("; "));

        if (message.isBlank()) {
            message = VALIDATION_MESSAGE;
        }

        log.debug("Validation error: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDto.builder().message(message).build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleMalformedJson(HttpMessageNotReadableException e) {
        Throwable rootCause = getRootCause(e);
        String message = MALFORMED_JSON_MESSAGE;
        String exceptionName = rootCause.getClass().getSimpleName();

        switch (exceptionName) {
            case "UnrecognizedPropertyException" -> {
                String propertyName = extractPropertyName(rootCause);
                message = propertyName != null
                        ? "JSON содержит недопустимое поле: " + propertyName
                        : "JSON содержит недопустимые поля";
            }
            case "InvalidFormatException" -> message = "JSON содержит значение неверного типа";
            case "MismatchedInputException" -> message = "JSON имеет некорректную структуру";
            case "JsonParseException" -> message = "JSON синтаксически некорректен";
            default -> {
            }
        }

        log.debug("Malformed JSON: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDto.builder().message(message).build());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingParameter(MissingServletRequestParameterException e) {
        String message = "Не передан обязательный параметр '" + e.getParameterName() + "'";
        log.debug("Missing request parameter: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDto.builder().message(message).build());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingPart(MissingServletRequestPartException e) {
        String message = "Не передана обязательная часть запроса '" + e.getRequestPartName() + "'";
        log.debug("Missing request part: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDto.builder().message(message).build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = "Параметр '" + e.getName() + "' имеет неверный формат";
        log.debug("Type mismatch: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDto.builder().message(message).build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getMessage() != null && !violation.getMessage().isBlank()
                        ? violation.getMessage()
                        : "Некорректное значение параметра")
                .distinct()
                .collect(Collectors.joining("; "));

        if (message.isBlank()) {
            message = VALIDATION_MESSAGE;
        }

        log.debug("Constraint violation: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDto.builder().message(message).build());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        String message = "Метод " + e.getMethod() + " не поддерживается для этого запроса";
        log.debug("Method not supported: {}", message);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ErrorResponseDto.builder().message(message).build());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        String message = "Тип содержимого '" + e.getContentType() + "' не поддерживается";
        log.debug("Media type not supported: {}", message);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ErrorResponseDto.builder().message(message).build());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(ResourceNotFoundException e) {
        String message = resolveMessage(e.getMessage(), NOT_FOUND_MESSAGE);
        log.debug("Resource not found: {}", message);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseDto.builder().message(message).build());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNoResourceFound(NoResourceFoundException e) {
        String message = "Ресурс '" + e.getResourcePath() + "' не найден";
        log.debug("No resource found: {}", message);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseDto.builder().message(message).build());
    }

    @ExceptionHandler(SlotNotAvailableException.class)
    public ResponseEntity<ErrorResponseDto> handleSlotNotAvailable(SlotNotAvailableException e) {
        String message = resolveMessage(e.getMessage(), "Выбранное время недоступно");
        log.debug("Slot not available: {}", message);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponseDto.builder().message(message).build());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequest(BadRequestException e) {
        String message = resolveMessage(e.getMessage(), BAD_REQUEST_MESSAGE);
        log.debug("Bad request: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDto.builder().message(message).build());
    }

    @ExceptionHandler(CsvValidationException.class)
    public ResponseEntity<ErrorResponseDto> handleCsvValidation(CsvValidationException e) {
        String message = resolveMessage(e.getMessage(), "Ошибка проверки CSV-файла");
        if (e.getRowNumber() > 0) {
            message = "Строка " + e.getRowNumber() + ": " + message;
        }

        log.debug("CSV validation error: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDto.builder().message(message).build());
    }

    @ExceptionHandler(CsvProcessingException.class)
    public ResponseEntity<ErrorResponseDto> handleCsvProcessing(CsvProcessingException e) {
        String message = resolveMessage(e.getMessage(), "Ошибка обработки CSV-файла");
        log.error("CSV processing error: {}", message, e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDto.builder().message(message).build());
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccountLocked(AccountLockedException e) {
        String message = resolveMessage(e.getMessage(), "Аккаунт временно заблокирован");
        log.debug("Account temporarily locked: {}", message);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ErrorResponseDto.builder().message(message).build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(BadCredentialsException e) {
        log.debug("Bad credentials: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponseDto.builder().message(AUTHENTICATION_MESSAGE).build());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> handleSpringAuthentication(AuthenticationException e) {
        log.debug("Authentication error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponseDto.builder().message(AUTHENTICATION_MESSAGE).build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException e) {
        log.debug("Access denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponseDto.builder().message(ACCESS_DENIED_MESSAGE).build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrity(DataIntegrityViolationException e) {
        log.debug("Data integrity violation: {}", e.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponseDto.builder().message(CONFLICT_MESSAGE).build());
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
                .body(ErrorResponseDto.builder().message(INTERNAL_ERROR_MESSAGE).build());
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private String resolveMessage(String message, String fallback) {
        return message != null && !message.isBlank() ? message : fallback;
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

