package com.shop.product.service;

import com.shop.product.dto.ProductRequest;
import com.shop.product.dto.ProductResponse;
import com.shop.product.dto.StockQuantityRequest;
import com.shop.product.entity.Category;
import com.shop.product.entity.Product;
import com.shop.product.exception.CategoryNotFoundException;
import com.shop.product.exception.ProductNotFoundException;
import com.shop.product.repository.CategoryRepository;
import com.shop.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис управления товарами.
 * <p>
 * Содержит бизнес-логику:
 * - чтение товаров (все, по категории или по id)
 * - создание и обновление товара с проверкой существования категории
 * - изменение остатка товара на складе
 * - удаление товара
 * - преобразование сущности Product в DTO ответа
 */
@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param productRepository  репозиторий для работы с товарами
     * @param categoryRepository репозиторий для работы с категориями
     */
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Получение всех товаров или товаров одной категории.
     *
     * @param categoryId идентификатор категории для фильтрации; если null - все товары.
     *                   Категория, в которой нет товаров (в том числе несуществующая),
     *                   дает пустой список: фильтр - это удобство выборки, а не проверка существования
     * @return список товаров
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll(Long categoryId) {
        List<Product> products = categoryId == null
                ? productRepository.findAll()
                : productRepository.findByCategoryId(categoryId);

        return products.stream()
                .map(this::mapToProductResponse)
                .toList();
    }

    /**
     * Получение товара по id.
     *
     * @param id идентификатор товара
     * @return товар
     * @throws ProductNotFoundException если товар с таким id не найден
     */
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return mapToProductResponse(findProduct(id));
    }

    /**
     * Создание нового товара.
     *
     * @param request DTO с данными товара
     * @return созданный товар
     * @throws CategoryNotFoundException если указанная категория не найдена
     */
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        applyRequest(product, request);

        return mapToProductResponse(productRepository.save(product));
    }

    /**
     * Обновление существующего товара.
     *
     * @param id      идентификатор товара
     * @param request DTO с новыми данными товара
     * @return обновленный товар
     * @throws ProductNotFoundException  если товар с таким id не найден
     * @throws CategoryNotFoundException если указанная категория не найдена
     */
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findProduct(id);
        applyRequest(product, request);

        return mapToProductResponse(productRepository.save(product));
    }

    /**
     * Удаление товара.
     *
     * @param id идентификатор товара
     * @throws ProductNotFoundException если товар с таким id не найден
     */
    public void delete(Long id) {
        Product product = findProduct(id);
        productRepository.delete(product);
    }

    /**
     * Установка остатка товара на складе.
     * <p>
     * Значение абсолютное: переданное количество заменяет текущее,
     * поэтому два параллельных запроса с одинаковым остатком дают одинаковый результат.
     *
     * @param id      идентификатор товара
     * @param request DTO с новым остатком
     * @return обновленный товар
     * @throws ProductNotFoundException если товар с таким id не найден
     */
    public ProductResponse updateStock(Long id, StockQuantityRequest request) {
        Product product = findProduct(id);
        product.setStockQuantity(request.getStockQuantity());

        return mapToProductResponse(productRepository.save(product));
    }

    /**
     * Переносит данные из DTO в сущность товара.
     *
     * @param product сущность товара
     * @param request DTO с данными товара
     * @throws CategoryNotFoundException если указанная категория не найдена
     */
    private void applyRequest(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setStockQuantity(request.getStockQuantity());
        // Категорию ищем заранее, чтобы несуществующая давала понятную ошибку 404,
        // а не ошибку целостности от БД при попытке сохранить ссылку
        product.setCategory(findCategory(request.getCategoryId()));
    }

    /**
     * Поиск товара по id или выброс исключения.
     *
     * @param id идентификатор товара
     * @return найденный товар
     * @throws ProductNotFoundException если товар с таким id не найден
     */
    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    /**
     * Поиск категории по id или выброс исключения.
     *
     * @param id идентификатор категории
     * @return найденная категория
     * @throws CategoryNotFoundException если категория с таким id не найдена
     */
    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    /**
     * Преобразует сущность Product в ProductResponse DTO.
     * <p>
     * Отдается только идентификатор категории, а не сама категория.
     * Запроса к категории при этом не будет: у ленивого прокси идентификатор известен заранее,
     * поэтому getId() возвращает его, не обращаясь к БД. Если бы понадобились поля самой
     * категории, вот тогда прокси инициализировался бы - и запрос был бы.
     *
     * @param product сущность товара
     * @return DTO для ответа клиенту
     */
    private ProductResponse mapToProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setCategoryId(product.getCategory().getId());
        response.setImageUrl(product.getImageUrl());
        response.setStockQuantity(product.getStockQuantity());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }
}
