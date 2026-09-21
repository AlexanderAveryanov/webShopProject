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

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Цена товара

    @ManyToOne(fetch = FetchType.LAZY) // Тип связи (у многих товаров может быть одиа категория). Загружаем лениво, только при вызове getCategory()
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "image_url")
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
