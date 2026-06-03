package com.shop.auth.exception;

/**
 * Исключение, выбрасываемое когда пользователь не найден
 * <p>
 * HTTP статус: 404 Not Found
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("Не найден пользователь с Email: " + email);
    }

    public UserNotFoundException(Long userId) {
        super("Не найден пользователь с Id: " + userId);
    }
}
