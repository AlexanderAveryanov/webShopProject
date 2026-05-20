package com.shop.auth.repository;

import com.shop.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository<User, Long> - Наследуем готовые методы для работы с сущностью User, где ID типа Long
public interface UserRepository extends JpaRepository<User, Long> {
    // Генерация тела метода Spring Data JPA (SQL) по ключевым словам.
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
