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
    // findByCategoryId - производный запрос Spring Data: имя метода разбирается как category.id,
    // то есть идентификатор связанной категории
    List<Product> findByCategoryId(Long categoryId);

    /**
     * Удаляет все товары категории одним запросом.
     * <p>
     * Нужен для удаления категории вместе с её товарами: сначала уходят товары,
     * затем сама категория, и всё это в одной транзакции.
     * <p>
     * Удаление остаётся явным bulk-запросом, а не производным deleteAllByCategoryId:
     * производный метод сначала выбирает товары, а затем удаляет их по одному,
     * то есть одним запросом он не является.
     *
     * @param categoryId идентификатор категории
     */
    @Modifying
    @Query("delete from Product p where p.category.id = :categoryId")
    void deleteAllByCategoryId(@Param("categoryId") Long categoryId);
}
