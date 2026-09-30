package com.shop.product.exception;

/**
 * Исключение, выбрасываемое при обращении к несуществующей категории (404).
 */
public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(Long categoryId) {
        super("Не найдена категория с id: " + categoryId);
    }
}
