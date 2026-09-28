package com.shop.product.exception;

/**
 * Исключение, выбрасываемое при обращении к несуществующему товару (404).
 */
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long productId) {
        super("Не найден товар с id: " + productId);
    }
}
