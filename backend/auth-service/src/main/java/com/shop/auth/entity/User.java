package com.shop.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity // Указывает на соответствие класса таблице в БД
@Table(name = "users") // Если не указать имя таблицы, Hibernate используем имя класса
@Getter @Setter // Гереация геттеров и сеттеров (Lombok)
public class User {
    @Id // Первичный ключ
    // Значение генерируется БД автоматически (соотв. BIGSERIAL в PostgreSQL)
    // IDENTITY - нельзя использовать массовую вставку
    // т.е. Hibernate не управляет генерацией ID. Вставляет null, БД сама заполняет ID через BIGSERIAL
    // SEQUENCE - позволяет массовую вставку, более гибкий, но сложнее настройка и лишний запрос к БД.
    // т.е. Hibernate заранее запрашивает у БД следующий ID, сохраняет у себя, потом вставляет
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name") // Указывает на имя колонки в БД (т.к. в Java - camelCase, а в SQL - snake_case)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING) // Тип элемента enum, который будет сохранен в БД
    private Role role = Role.USER;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist // Перед первым сохранением объекта в БД
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate // Перед обновлением существующего объекта в БД
    protected void onUpdate() { // protected достаточно, т.к. вызываются Hibernate через рефлексию
        updatedAt = LocalDateTime.now();
    }
}
