package com.shop.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса на регистрацию нового пользователя.
 * Клиент отправляет эту форму на POST /api/auth/register
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Необходимо заполнить обязательный атрибут 'Email'")
    @Size(max = 255, message = "Email не должен превышать 255 символов")
    @Email(message = "Некорректный формат Email")
    private String email;

    @NotBlank(message = "Необходимо заполнить обязательный атрибут 'Пароль'")
    @Size(min = 6, max = 72, message = "Пароль должен содержать от 6 до 72 символов")
    private String password;

    @Size(max = 100, message = "Имя не должно превышать 100 символов")
    private String firstName;

    @Size(max = 100, message = "Фамилия не должна превышать 100 символов")
    private String lastName;

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }
}
