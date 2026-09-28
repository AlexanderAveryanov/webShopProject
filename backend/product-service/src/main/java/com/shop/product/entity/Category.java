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

    // Размер поля в соответствии с миграцией Flyway. Уникальность поля обеспечивается индексом БД.
    @Column(nullable = false, length = 100)
    private String name;

    @Column
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Связь с товарами. Каскада нет намеренно: удаление категории сервис делает сам,
    // сначала удаляя товары запросом deleteAllByCategoryId, а уже потом саму категорию.
    // Так поведение видно в коде, а не спрятано в БД, и его легко покрыть проверкой
    @OneToMany(mappedBy = "categoryId", fetch = FetchType.LAZY)
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
