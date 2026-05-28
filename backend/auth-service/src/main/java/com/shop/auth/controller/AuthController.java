package com.shop.auth.controller;

import com.shop.auth.dto.AuthResponse;
import com.shop.auth.dto.LoginRequest;
import com.shop.auth.dto.RegisterRequest;
import com.shop.auth.dto.UserResponse;
import com.shop.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для аутентификации и регистрации пользователей.
 * <p>
 * Эндпоинты:
 * - POST /api/auth/register — регистрация нового пользователя
 * - POST /api/auth/login — вход пользователя (выдача JWT)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    /**
     * Конструктор для внедрения зависимости AuthService.
     *
     * @param authService сервис с бизнес-логикой регистрации и аутентификации
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Регистрация нового пользователя.
     *
     * @param request DTO с данными для регистрации (email, password, firstName, lastName)
     * @return DTO с данными созданного пользователя
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Вход пользователя (логин).
     *
     * @param request DTO с email и паролем
     * @return DTO с JWT-токеном и данными пользователя
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}