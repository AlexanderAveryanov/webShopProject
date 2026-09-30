package com.shop.product.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO для запроса на создание или обновление товара.
 * Клиент отправляет эту форму на POST /api/products и PUT /api/products/{id}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Необходимо заполнить обязательный атрибут 'Наименование'")
    // max = 255 - соответствует VARCHAR(255) в таблице products
    @Size(max = 255, message = "Наименование товара не должно превышать 255 символов")
    private String name;

    private String description;

    // @Digits(integer = 8, fraction = 2) - соответствует DECIMAL(10,2) в таблице products:
    // не более 8 цифр до запятой и не более 2 после, иначе БД округлит или отклонит значение позже валидации
    @NotNull(message = "Необходимо заполнить обязательный атрибут 'Цена'")
    @Positive(message = "Цена должна быть больше 0")
    @Digits(integer = 8, fraction = 2, message = "Цена должна содержать не более 8 цифр до запятой и не более 2 после")
    private BigDecimal price;

    // categoryId - идентификатор категории товара, чтобы клиенту не приходилось передавать вложенный объект категории
    @NotNull(message = "Необходимо заполнить обязательный атрибут 'Идентификатор категории'")
    private Long categoryId;

    // max = 500 - соответствует VARCHAR(500) в таблице products
    @Size(max = 500, message = "Ссылка на изображение не должна превышать 500 символов")
    private String imageUrl;

    @Builder.Default
    @NotNull(message = "Необходимо заполнить обязательный атрибут 'Количество на складе'")
    @PositiveOrZero(message = "Количество не может быть отрицательным")
    private Integer stockQuantity = 0;
}
