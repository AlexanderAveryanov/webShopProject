package com.shop.product.exception;

import com.shop.product.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений для REST API.
 * <p>
 * Перехватывает исключения и возвращает понятный JSON-ответ.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Универсальный метод для формирования ответа с ошибкой
     *
     * @param status           - HTTP статус
     * @param messageException - Текст сообщения ошибки
     * @param details          - Детали ошибки по полям
     * @return ResponseEntity с ErrorResponse в теле
     */
    private ResponseEntity<ErrorResponse> handleException(
            HttpStatus status,
            String messageException,
            Map<String, String> details
    ) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(messageException)
                .details(details)
                .build();
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Исключение, выбрасываемое при обращении к несуществующей категории (404)
     */
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFoundException(CategoryNotFoundException ex) {
        return handleException(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    /**
     * Исключение, выбрасываемое при обращении к несуществующему товару (404)
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(ProductNotFoundException ex) {
        return handleException(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    /**
     * Исключение, выбрасываемое при занятом имени категории (409)
     */
    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCategoryAlreadyExistsException(CategoryAlreadyExistsException ex) {
        return handleException(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    /**
     * Неверный формат параметра пути или запроса (400)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return handleException(HttpStatus.BAD_REQUEST, "Некорректный формат параметра: " + ex.getName(), null);
    }

    /**
     * Некорректное тело запроса, не являющееся валидным JSON (400)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return handleException(HttpStatus.BAD_REQUEST, "Некорректное тело запроса", null);
    }

    /**
     * Нарушение ограничений целостности БД, не перехваченное на уровне сервиса (409).
     * Например, гонка двух запросов на создание категории с одинаковым именем:
     * сервис проверил уникальность, но вторая транзакция успела вставить раньше.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return handleException(HttpStatus.CONFLICT, "Нарушение ограничений базы данных", null);
    }

    /**
     * Отказ в доступе из-за недостаточной роли (403).
     * <p>
     * Отказ приходит сюда, потому что проверка {@code @PreAuthorize} выполняется внутри контроллера.
     * Без отдельного обработчика исключение попало бы в общий обработчик RuntimeException
     * и клиент получил бы 500 вместо 403.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return handleException(HttpStatus.FORBIDDEN, "Недостаточно прав для выполнения операции", null);
    }

    /**
     * Небработанные исключения (500).
     * Ошибка логируется, клиенту отдаётся общий текст без внутренних деталей.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("Необработанная ошибка при обработке запроса", ex);
        return handleException(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", null);
    }

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
        return handleException(HttpStatus.BAD_REQUEST, "Проверьте правильность заполнения полей", errors);
    }
}
