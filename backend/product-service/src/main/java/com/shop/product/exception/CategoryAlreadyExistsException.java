package com.shop.product.exception;

/**
 * Исключение, выбрасываемое при создании или переименовании категории
 * в имя, которое уже занято другой категорией без учета регистра (409).
 */
public class CategoryAlreadyExistsException extends RuntimeException {
    public CategoryAlreadyExistsException(String name) {
        super("Категория с наименованием '" + name + "' уже существует");
    }
}
