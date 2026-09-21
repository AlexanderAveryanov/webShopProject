package com.shop.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    // @NotBlank - в отличие от @NotNull запрещает создание пустой строки ""
    @NotBlank(message = "Необходимо заполнить обязательный атрибут 'Наименование'")
    @Size(max = 50, message = "Наименование категории не должно превышать 50 символов")
    private String name;
    private String description;
}
