package com.shop.product.repository;

import com.shop.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository<Category, Long> - Наследуем готовые методы для работы с сущностью Category, где ID типа Long
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Генерация тела метода Spring Data JPA (SQL) по ключевым словам.
    // Сравнение без учета регистра - так же, как работает уникальный индекс по LOWER(name) в БД
    boolean existsByNameIgnoreCase(String name);
    // Проверка занятости имени при переименовании: категория с таким именем есть, и это не она сама
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
