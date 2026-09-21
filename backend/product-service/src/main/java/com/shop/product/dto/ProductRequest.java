package com.shop.product.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Необходимо заполнить обязательный атрибут 'Наименование'")
    @Size(max = 100, message = "Наименование товара не должно превышать 100 символов")
    private String name;

    private String description;

    @NotNull(message = "Необходимо заполнить обязательный атрибут 'Цена'")
    @Positive(message = "Цена должна быть больше 0")
    private BigDecimal price;

    @NotNull(message = "Необходимо заполнить обязательный атрибут 'Идентификатор категории'")
    private Long category;

    private String imageUrl;

    @Builder.Default
    @NotNull(message = "Необходимо заполнить обязательный атрибут 'Количество на складе'")
    @PositiveOrZero(message = "Количество не может быть отрицательным")
    private Integer stockQuantity = 0;
}
