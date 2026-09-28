package com.shop.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter @Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Размер поля в соответствии с миграцией Flyway.
    @Column(nullable = false, length = 255)
    private String name;

    @Column
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Цена товара

    // Ссылка на категорию товара. Поле названо categoryId ради единообразия с запросом и
    // колонкой category_id, хотя тип здесь - объект Category, а не число
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category categoryId;

    // Размер поля в соответствии с миграцией Flyway.
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0; // Количество товара на складе

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
