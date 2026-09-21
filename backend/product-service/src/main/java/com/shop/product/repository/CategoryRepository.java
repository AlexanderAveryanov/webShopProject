package com.shop.product.repository;

import com.shop.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository<User, Long> - Наследуем готовые методы для работы с сущностью Category, где ID типа Long
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Генерация тела метода Spring Data JPA (SQL) по ключевым словам.
    Optional<Category> findByName(String name);
    boolean existsByName(String name);
}
