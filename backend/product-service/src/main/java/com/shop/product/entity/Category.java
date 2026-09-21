package com.shop.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter @Setter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Описание связи таблицы categories - products для ORM (JPA), чтобы она могла строить запросы
    // mappedBy = "category" - указывает на имя поля в классе Product, которое владеет связью
    // cascade = CascadeType.ALL - При сохранении/удалении категории, операции применяются к товарам
    // fetch = FetchType.LAZY - Товары загружаются только когда вызывается getProducts() - лениво
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

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
