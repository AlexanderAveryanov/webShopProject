package com.shop.product.controller;

import com.shop.product.dto.CategoryRequest;
import com.shop.product.dto.CategoryResponse;
import com.shop.product.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер для операций с категориями товаров.
 * <p>
 * Чтение доступно пользователям с ролью USER и ADMIN,
 * изменение - только с ролью ADMIN.
 * <p>
 * Эндпоинты:
 * - GET /api/categories — список всех категорий
 * - GET /api/categories/{id} — получение категории по id
 * - POST /api/categories — создание категории
 * - PUT /api/categories/{id} — обновление категории
 * - DELETE /api/categories/{id} — удаление категории
 */
@RestController
@RequestMapping("/api/categories")
@PreAuthorize("hasAnyRole('USER','ADMIN')") // Чтение доступно пользователям и администраторам
public class CategoryController {
    private final CategoryService categoryService;

    /**
     * Конструктор внедрения зависимостей.
     *
     * @param categoryService сервис для работы с категориями
     */
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Получение списка всех категорий.
     *
     * @return список категорий
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    /**
     * Получение информации о категории по id.
     *
     * @param id идентификатор категории
     * @return данные категории
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    /**
     * Создание новой категории.
     *
     * @param request DTO с именем и описанием категории
     * @return созданная категория
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Изменение каталога доступно только администраторам
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody @Valid CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    /**
     * Обновление существующей категории.
     *
     * @param id      идентификатор категории
     * @param request DTO с новым именем и описанием категории
     * @return обновленная категория
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable("id") Long id,
                                                            @RequestBody @Valid CategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    /**
     * Удаление категории.
     * <p>
     * Вместе с категорией удаляются все товары, которые были ей присвоены, одной транзакцией.
     * Подтверждение этого последствия запрашивает клиент до отправки запроса.
     *
     * @param id идентификатор категории
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
