package com.shop.auth.exception;

/**
 * Исключение, выбрасываемое при попытке регистрации с уже существующим email
 * <p>
 * HTTP статус: 409 Conflict
 */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("Email уже используется: " + email);
    }

    public EmailAlreadyExistsException(String email, Throwable cause) {
        super("Email уже используется: " + email, cause);
    }
}
