package com.shop.product.repository;

import com.shop.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// JpaRepository<User, Long> - Наследуем готовые методы для работы с сущностью Product, где ID типа Long
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Генерация тела метода Spring Data JPA (SQL) по ключевым словам.
    Optional<Product> findByName(String name);
    boolean existsByName(String name);
    List<Product> findByCategoryId(Long categoryId);
    boolean existsByCategoryId(Long categoryId);
    long countByCategoryId(Long categoryId);
}
