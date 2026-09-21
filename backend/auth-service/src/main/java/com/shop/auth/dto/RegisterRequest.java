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
    @Email(message = "Некорректный формат Email")
    private String email;

    @NotBlank(message = "Необходимо заполнить обязательный атрибут 'Пароль'")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    private String password;

    private String firstName;
    private String lastName;
}
