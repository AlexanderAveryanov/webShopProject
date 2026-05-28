package com.shop.auth.exception;

/**
 * Исключение, выбрасываемое при неверном пароле
 * <p>
 * HTTP статус: 401 Unauthorized
 */
public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("Неверный пароль");
    }
}
