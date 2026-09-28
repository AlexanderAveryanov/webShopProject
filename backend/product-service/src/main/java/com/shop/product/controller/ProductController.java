package com.shop.product.controller;

import com.shop.product.dto.ProductRequest;
import com.shop.product.dto.ProductResponse;
import com.shop.product.dto.StockQuantityRequest;
import com.shop.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер для операций с товарами.
 * <p>
 * Чтение доступно пользователям с ролью USER и ADMIN,
 * изменение - только с ролью ADMIN.
 * <p>
 * Эндпоинты:
 * - GET /api/products — список товаров, с фильтрацией по categoryId
 * - GET /api/products/{id} — получение товара по id
 * - POST /api/products — создание товара
 * - PUT /api/products/{id} — обновление товара
 * - DELETE /api/products/{id} — удаление товара
 * - PATCH /api/products/{id}/stock — установка остатка товара на складе
 */
@RestController
@RequestMapping("/api/products")
@PreAuthorize("hasAnyRole('USER','ADMIN')") // Чтение доступно пользователям и администраторам
public class ProductController {
    private final ProductService productService;

    /**
     * Конструктор внедрения зависимостей.
     *
     * @param productService сервис для работы с товарами
     */
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Получение списка товаров. Можно отфильтровать товары по категории.
     *
     * @param categoryId идентификатор категории; если не передан - возвращаются все товары
     * @return список товаров
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts(@RequestParam(value = "categoryId", required = false) Long categoryId) {
        return ResponseEntity.ok(productService.getAll(categoryId));
    }

    /**
     * Получение информации о товаре по id.
     *
     * @param id идентификатор товара
     * @return данные товара
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    /**
     * Создание нового товара.
     *
     * @param request DTO с данными товара
     * @return созданный товар
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Изменение каталога доступно только администраторам
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    /**
     * Обновление существующего товара.
     *
     * @param id      идентификатор товара
     * @param request DTO с новыми данными товара
     * @return обновленный товар
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable("id") Long id,
                                                         @RequestBody @Valid ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    /**
     * Удаление товара.
     *
     * @param id идентификатор товара
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Установка остатка товара на складе.
     * <p>
     * В теле запроса передается итоговое количество товара, а не изменение на дельту.
     *
     * @param id      идентификатор товара
     * @param request DTO с новым остатком
     * @return обновленный товар
     */
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateStock(@PathVariable("id") Long id,
                                                       @RequestBody @Valid StockQuantityRequest request) {
        return ResponseEntity.ok(productService.updateStock(id, request));
    }
}
