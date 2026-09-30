package com.shop.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса на создание или обновление категории.
 * Клиент отправляет эту форму на POST /api/categories и PUT /api/categories/{id}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    // @NotBlank - в отличие от @NotNull запрещает создание пустой строки ""
    // max = 100 - соответствует VARCHAR(100) в таблице categories, иначе БД отклонит значение позже, чем валидация
    @NotBlank(message = "Необходимо заполнить обязательный атрибут 'Наименование'")
    @Size(max = 100, message = "Наименование категории не должно превышать 100 символов")
    private String name;
    private String description;
}
