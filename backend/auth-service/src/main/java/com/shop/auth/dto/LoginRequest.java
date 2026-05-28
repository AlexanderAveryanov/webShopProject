package com.shop.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

/**
 * DTO для запроса на авторизацию.
 * Клиент отправляет эту форму на POST /api/auth/login
 */
public class LoginRequest {

    @NotNull(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotNull(message = "Пароль обязателен")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
