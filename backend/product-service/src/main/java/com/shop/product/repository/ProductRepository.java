package com.shop.product.repository;

import com.shop.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// JpaRepository<Product, Long> - Наследуем готовые методы для работы с сущностью Product, где ID типа Long
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Методов поиска по name здесь нет: имя товара не уникально, поэтому existsByName вводил бы в заблуждение.
    // Запросы к категории написаны через @Query, потому что в Product поле называется categoryId,
    // а хранит объект Category - до id в нём ещё один шаг: categoryId.id
    @Query("select p from Product p where p.categoryId.id = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Удаляет все товары категории одним запросом.
     * <p>
     * Нужен для удаления категории вместе с её товарами: сначала уходят товары,
     * затем сама категория, и всё это в одной транзакции.
     *
     * @param categoryId идентификатор категории
     */
    @Modifying
    @Query("delete from Product p where p.categoryId.id = :categoryId")
    void deleteAllByCategoryId(@Param("categoryId") Long categoryId);
}
