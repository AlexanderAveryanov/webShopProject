package com.shop.auth.dto;

import com.shop.auth.entity.Role;

/**
 * DTO для ответа сервера после успешного входа пользователя.
 * Включает JWT-токен и основные данные пользователя.
 */
public class AuthResponse {
    public static final String BEARER = "Bearer";

    private String token;
    /** тип токена (Доступ предоставляется просто фактом предъявления валидного токена)
     * Клиент должен передавать токен в заголовке: Authorization: Bearer <token> */
    private String type = BEARER;
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
