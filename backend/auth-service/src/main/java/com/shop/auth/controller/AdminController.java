package com.shop.auth.controller;

import com.shop.auth.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для обработки эндпоинтов роли ADMIN
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Все методы данного контроллера требуют роль ADMIN
public class AdminController {
    private final UserRepository userRepository;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param userRepository репозиторий для работы с пользователями
     */
    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

}
