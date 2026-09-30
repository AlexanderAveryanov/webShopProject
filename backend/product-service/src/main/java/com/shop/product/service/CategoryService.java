package com.shop.product.service;

import com.shop.product.dto.CategoryRequest;
import com.shop.product.dto.CategoryResponse;
import com.shop.product.entity.Category;
import com.shop.product.exception.CategoryAlreadyExistsException;
import com.shop.product.exception.CategoryNotFoundException;
import com.shop.product.repository.CategoryRepository;
import com.shop.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис управления категориями товаров.
 * <p>
 * Содержит бизнес-логику:
 * - чтение категорий (все или по id)
 * - создание и обновление категории с проверкой уникальности имени
 * - удаление категории вместе с присвоенными ей товарами одной транзакцией
 * - преобразование сущности Category в DTO ответа
 */
@Service
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param categoryRepository репозиторий для работы с категориями
     * @param productRepository репозиторий для работы с товарами (проверка, что категория пуста)
     */
    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    /**
     * Получение всех категорий.
     *
     * @return список категорий
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(this::mapToCategoryResponse)
                .toList();
    }

    /**
     * Получение категории по id.
     *
     * @param id идентификатор категории
     * @return категория
     * @throws CategoryNotFoundException если категория с таким id не найдена
     */
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return mapToCategoryResponse(findCategory(id));
    }

    /**
     * Создание новой категории.
     *
     * @param request DTO с именем и описанием категории
     * @return созданная категория
     * @throws CategoryAlreadyExistsException если имя уже занято другой категорией (без учета регистра)
     */
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new CategoryAlreadyExistsException(request.getName());
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        return mapToCategoryResponse(categoryRepository.save(category));
    }

    /**
     * Обновление существующей категории.
     * <p>
     * Имя проверяется на занятость без учета регистра, кроме самой изменяемой категории,
     * поэтому переименование с сохранением своего же имени не считается конфликтом.
     *
     * @param id      идентификатор категории
     * @param request DTO с новым именем и описанием категории
     * @return обновленная категория
     * @throws CategoryNotFoundException      если категория с таким id не найдена
     * @throws CategoryAlreadyExistsException если новое имя уже занято другой категорией
     */
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findCategory(id);

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new CategoryAlreadyExistsException(request.getName());
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        return mapToCategoryResponse(categoryRepository.save(category));
    }

    /**
     * Удаление категории вместе с её товарами.
     * <p>
     * Каскадного удаления на уровне БД нет: товары удаляются явным запросом
     * {@code deleteAllByCategoryId}, и только потом удаляется сама категория. Обе операции
     * идут в одной транзакции, поэтому при ошибке не остаётся ни товаров без категории,
     * ни категории с товарами.
     * <p>
     * О том, что удалятся товары, предупреждает клиент до отправки запроса: сервер отвечает
     * без дополнительного подтверждения.
     *
     * @param id идентификатор категории
     * @throws CategoryNotFoundException если категория с таким id не найдена
     */
    public void delete(Long id) {
        Category category = findCategory(id);
        productRepository.deleteAllByCategoryId(id);
        categoryRepository.delete(category);
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
     * Преобразует сущность Category в CategoryResponse DTO.
     * <p>
     * Список товаров категории намеренно не отдается: он не нужен клиенту в карточке категории
     * и требует дополнительного запроса к ленивой связи.
     *
     * @param category сущность категории
     * @return DTO для ответа клиенту
     */
    private CategoryResponse mapToCategoryResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        return response;
    }
}
