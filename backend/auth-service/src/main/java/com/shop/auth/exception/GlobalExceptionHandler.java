package com.shop.auth.exception;

import com.shop.auth.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений для REST API.
 * <p>
 * Перехватывает исключения и возвращает понятный JSON-ответ.
 */
// @RestControllerAdvice = @ControllerAdvice + @ResponseBody
// @ControllerAdvice - позволяет глобально обрабатывать исключения для всех контроллеров
// @ResponseBody - Автоматически преобразует возвращаемый объект в JSON
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Обработка ошибок валидации (@Valid).
     * Возвращает 400 Bad Request с подробностями, какое поле не прошло валидацию.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class) // Ловит ошибки валидации (@Valid)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation failed")
                .message("Проверьте правильность заполнения полей")
                .details(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Обработка бизнес-ошибок (например, "Email уже используется").
     * Возвращает 409 Conflict.
     */
    @ExceptionHandler(RuntimeException.class) // Ловит все бизнес-ошибки (RuntimeException)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        HttpStatus status;

        if (ex.getMessage().contains("Email уже используется")) {
            status = HttpStatus.CONFLICT; // 409
        } else if (ex.getMessage().contains("Не найден пользователь")) {
            status = HttpStatus.NOT_FOUND; // 404
        } else if (ex.getMessage().contains("Неверный пароль")) {
            status = HttpStatus.UNAUTHORIZED; // 401
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }
}