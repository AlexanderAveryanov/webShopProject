package com.shop.auth.controller;

import com.shop.auth.dto.AuthResponse;
import com.shop.auth.dto.LoginRequest;
import com.shop.auth.dto.RegisterRequest;
import com.shop.auth.dto.UserResponse;
import com.shop.auth.entity.User;
import com.shop.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // ===== ПУБЛИЧНЫЕ ЭНДПОИНТЫ =====
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

    // ===== ЭНДПОИНТЫ ДЛЯ АВТОРИЗОВАННЫХ ПОЛЬЗОВАТЕЛЕЙ =====
    /**
     * Получение информации о текущем аутентифицированном пользователе
     *
     * @return DTO с данными пользователя
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(authService.mapToUserResponse(user));
    }

    // ===== ЭНДПОИНТЫ ТОЛЬКО ДЛЯ ADMIN =====
    /**
     * Получение информации о всех пользователях
     *
     * @return Последовательность DTO с данными пользователей
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    /**
     * Получение пользователя по id
     *
     * @param userId id пользователя
     * @return DTO с данными пользователя
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long userId) {
        return ResponseEntity.ok(authService.getUserById(userId));
    }
}