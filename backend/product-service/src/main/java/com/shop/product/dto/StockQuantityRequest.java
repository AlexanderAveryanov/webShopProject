package com.shop.product.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса на обновление остатка товара.
 * Клиент отправляет эту форму на PATCH /api/products/{id}/stock.
 * Значение абсолютное: оно заменяет текущий остаток, а не изменяет его на дельту.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockQuantityRequest {
    @NotNull(message = "Необходимо заполнить обязательный атрибут 'Количество на складе'")
    @PositiveOrZero(message = "Количество не может быть отрицательным")
    private Integer stockQuantity;
}
