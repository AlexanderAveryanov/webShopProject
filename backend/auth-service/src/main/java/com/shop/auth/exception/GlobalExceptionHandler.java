package com.shop.auth.exception;

import com.shop.auth.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
        return ResponseEntity.status(status).body(errorResponse); // Spring сам вызывает .value у status
    }

    /**
     * Исключение, выбрасываемое при попытке регистрации с уже существующим email (409)
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsExceptions(EmailAlreadyExistsException ex) {
        return handleException(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    /**
     * Исключение, выбрасываемое при неверном пароле (401)
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordException(InvalidPasswordException ex) {
        return handleException(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    /**
     * Исключение, выбрасываемое когда пользователь не найден по email (404)
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        return handleException(HttpStatus.NOT_FOUND, ex.getMessage(), null);
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
     * Нарушение ограничений целостности БД, не перехваченное на уровне сервиса (409)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return handleException(HttpStatus.CONFLICT, "Нарушение ограничений базы данных", null);
    }

    /**
     * Необработанные исключения (500).
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