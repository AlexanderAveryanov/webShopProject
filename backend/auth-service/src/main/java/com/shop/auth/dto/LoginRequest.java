package com.shop.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса на авторизацию.
 * Клиент отправляет эту форму на POST /api/auth/login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Необходимо заполнить обязательный атрибут 'Email'")
    @Size(max = 255, message = "Email не должен превышать 255 символов")
    @Email(message = "Некорректный формат Email")
    private String email;

    @NotBlank(message = "Необходимо заполнить обязательный атрибут 'Пароль'")
    @Size(min = 6, max = 72, message = "Пароль должен содержать от 6 до 72 символов")
    private String password;

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }
}
