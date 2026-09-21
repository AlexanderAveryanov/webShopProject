package com.shop.auth.dto;

import com.shop.auth.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для ответа сервера после успешного входа пользователя.
 * Включает JWT-токен и основные данные пользователя.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    public static final String BEARER = "Bearer";

    private String token;
    /** тип токена (Доступ предоставляется просто фактом предъявления валидного токена)
     * Клиент должен передавать токен в заголовке: Authorization: Bearer <token> */
    @Builder.Default // Для того, чтобы билдер дефолтно инициализировал поле Типа
    private String type = BEARER;
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
}
