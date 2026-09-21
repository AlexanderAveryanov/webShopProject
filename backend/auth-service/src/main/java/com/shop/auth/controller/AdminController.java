package com.shop.auth.controller;

import com.shop.auth.dto.UserResponse;
import com.shop.auth.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер для обработки эндпоинтов роли ADMIN.
 * <p>
 * Эндпоинты:
 * - GET /api/admin/users — список всех пользователей
 * - GET /api/admin/users/{id} — получение пользователя по id
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Все методы данного контроллера требуют роль ADMIN
public class AdminController {
    private final AdminService adminService;

    /**
     * Конструктор внедрения зависимостей.
     *
     * @param adminService сервис для администрирования пользователей
     */
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Получение информации о всех пользователях
     *
     * @return Последовательность DTO с данными пользователей
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /**
     * Получение пользователя по id
     *
     * @param userId id пользователя
     * @return DTO с данными пользователя
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long userId) {
        return ResponseEntity.ok(adminService.getUserById(userId));
    }
}