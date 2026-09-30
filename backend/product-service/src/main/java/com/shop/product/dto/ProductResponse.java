package com.shop.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для ответа с данными товара.
 * Возвращается сервером на GET /api/products, GET /api/products/{id},
 * POST /api/products, PUT /api/products/{id} и PATCH /api/products/{id}/stock.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Long categoryId; /** Идентификатор категории товара. Сама категория товару не отдается, чтобы не тянуть ее данные в ответ */
    private String imageUrl;
    @Builder.Default
    private Integer stockQuantity = 0;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
