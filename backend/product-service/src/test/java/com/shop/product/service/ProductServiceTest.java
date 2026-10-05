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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты для {@link ProductService}.
 * <p>
 * Бизнес-логика проверяется в изоляции: репозитории товаров и категорий подменяются
 * моками Mockito, реальная БД и Spring-контекст не поднимаются. Так тесты быстрые
 * и не зависят от окружения.
 * <p>
 * {@link MockitoExtension} сам инициализирует {@code @Mock}-поля и внедряет их
 * в тестируемый объект, помеченный {@code @InjectMocks}.
 * Тест может быть выполнен, не выполнен, с ошибкой (если не смог выполниться из-за неожиданного исключения)
 */
// @ExtendWith(MockitoExtension.class) - входная точка для Mockito в JUnit 6. Без неё @Mock и @InjectMocks не будут работать.
// Что делает:
//  - Перед каждым тестом сканирует класс на поля с @Mock и создает для них загрушки.
//  - Находит поле с @InjectMocks и внедряет в него созданные моки.
//  - После каждого теста автоматически сбрасывает состояние моков (не нужно руками вызывать Mockito.reset())
//  - Включает строгий контроль: если заглушка объявлена через when(...), но не была использована,
//    тест падает с UnnecessaryStubbingException. Это защищает от «мёртвых» заглушек,
//    которые намекали на проверку, но ничего не проверяли.
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    /** Мок репозитория товаров */
    @Mock // Создает фейковый объект (заглушку). Все методы этого объекта по умолчанию возвращают: null/0/false/пустую коллекцию
    private ProductRepository productRepository;

    /** Мок репозитория категорий. Нужен, чтобы не ходить в БД при проверке существования категории. */
    @Mock
    private CategoryRepository categoryRepository;

    /** Тестируемый сервис; зависимости-моки внедряются Mockito автоматически. */
    @InjectMocks // Создает экземпляр через конструктор и передает ему созданные моки (объекты с аннотацией @Mock)
    private ProductService productService;

    /**
     * Фабрика товара для тестовых данных.
     * <p>
     * Заполняет только те поля, которые нужны конкретной проверке; остальное
     * заполняется «правдоподобными» значениями, чтобы DTO ответа был непустым.
     * Отдельная сущность нужна товару, потому что в {@code ProductResponse} наружу
     * отдаётся идентификатор категории, а не сама категория.
     */
    private Product createProduct(Long id, String name, Long categoryId, int stock) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription("desc");
        product.setPrice(BigDecimal.TEN);
        product.setImageUrl("img.png");
        product.setStockQuantity(stock);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        Category category = new Category();
        category.setId(categoryId);
        product.setCategory(category);
        return product;
    }

    /** Фабрика категории: сервису достаточно самого факта существования категории по id. */
    private Category createCategory(Long id) {
        Category category = new Category();
        category.setId(id);
        category.setName("Cat");
        return category;
    }

    /**
     * Получение всех товаров без фильтра по категории.
     * <p>
     * <b>Что проверяем:</b>
     * <ul>
     *   <li>при {@code categoryId == null} сервис обращается к {@code findAll()}, а не к фильтру;</li>
     *   <li>сущности корректно мапятся в {@link ProductResponse};</li>
     *   <li>порядок товаров сохраняется — так как возвращается поток из репозитория без переупорядочивания.</li>
     * </ul>
     * <p>
     * <b>Ожидаемый результат:</b> список из двух товаров с перенесёнными полями.
     */
    // Наименование методов принято: метод_ожидаемоеПоведение_условие
    @Test // помечает метод как тестовый. JUnit 6 запустит его при прогоне
    void getAll_shouldReturnAllWhenCategoryIdNull() {
        // Подготовка данных
        // Создаем два товара одной категории - так видно, что фильтр по категории не применялся
        Product p1 = createProduct(1L, "P1", 10L, 5);
        Product p2 = createProduct(2L, "P2", 10L, 3);
        // Заглушка: когда сервис вызовет findAll() без аргументов, вернем наш список
        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        // Действие: вызываем тестируемый метод
        List<ProductResponse> result = productService.getAll(null); // null - фильтр не задан, значит возвращаем все товары

        // Проверка результата: состав списка и перенос полей в DTO
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("P1");
        assertThat(result.get(0).getCategoryId()).isEqualTo(10L); // наружу отдается только id категории, сама категория не сериализуется
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    /**
     * Получение товаров одной категории.
     * <p>
     * <b>Что проверяем:</b>
     * <ul>
     *   <li>при заданном {@code categoryId} сервис вызывает {@code findByCategoryId(...)}, а не {@code findAll()};</li>
     *   <li>в ответе остаются только товары запрошенной категории.</li>
     * </ul>
     * <p>
     * <b>Ожидаемый результат:</b> один товар с категорией 20.
     */
    @Test
    void getAll_shouldReturnByCategoryWhenCategoryIdProvided() {
        // Подготовка данных: товар принадлежит категории 20
        Product p1 = createProduct(1L, "P1", 20L, 1);
        // Заглушка именно на findByCategoryId(20L): если сервис вызовет findAll(), вернется пустой список и тест упадет
        when(productRepository.findByCategoryId(20L)).thenReturn(List.of(p1));

        // Действие: запрашиваем товары конкретной категории
        List<ProductResponse> result = productService.getAll(20L);

        // Проверка результата
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoryId()).isEqualTo(20L);
    }

    /**
     * Пустой каталог.
     * <p>
     * <b>Что проверяем:</b> при отсутствии товаров сервис возвращает пустой список,
     * а не {@code null} — иначе контроллер отдал бы клиенту ошибку вместо пустого массива.
     * <p>
     * <b>Ожидаемый результат:</b> пустой список.
     */
    @Test
    void getAll_shouldReturnEmptyListWhenNoProducts() {
        when(productRepository.findAll()).thenReturn(List.of()); // репозиторий не нашел ни одного товара

        List<ProductResponse> result = productService.getAll(null);

        assertThat(result).isEmpty();
    }

    /**
     * Получение товара по идентификатору.
     * <p>
     * <b>Что проверяем:</b>
     * <ul>
     *   <li>найденная сущность полностью переносится в {@link ProductResponse};</li>
     *   <li>в ответе присутствуют остаток и идентификатор категории.</li>
     * </ul>
     * <p>
     * <b>Ожидаемый результат:</b> DTO с данными товара 1.
     */
    @Test
    void getById_shouldReturnProductResponseWhenFound() {
        Product product = createProduct(1L, "P1", 10L, 5);
        // findById у Spring Data возвращает Optional: возвращаем заполненный - товар существует
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("P1");
        assertThat(response.getCategoryId()).isEqualTo(10L);
        assertThat(response.getStockQuantity()).isEqualTo(5);
    }

    /**
     * Запрос несуществующего товара.
     * <p>
     * <b>Что проверяем:</b> сервис превращает пустой {@code Optional} в {@link ProductNotFoundException},
     * а не возвращает {@code null}: контроллер должен отдать клиенту 404 с понятным кодом ошибки.
     * <p>
     * <b>Ожидаемый результат:</b> выброшено исключение {@link ProductNotFoundException}.
     */
    @Test
    void getById_shouldThrowWhenNotFound() {
        // Пустой Optional - так репозиторий сообщает, что товара с таким id нет
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // assertThatThrownBy принимает лямбду (передаваемый метод) и если лямбда выбросила исключение, то оно ловится и оборачивается в объект ThrowableAssert
        // Далее у этого объекта мы проверяем тип ошибки.
        // Если лямбда не выбросила исключение, то assertThatThrownBy падает с AssertionError "Ожидалось, что код выбросит исключение, но он не выбросил". Тест заканчивается с ошибкой.
        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(ProductNotFoundException.class); // Если тип совпадает, то тест проходит, иначе нет. Учитываются совместимости по типу.
    }

    /**
     * Создание товара в существующей категории.
     * <p>
     * <b>Что проверяем:</b>
     * <ul>
     *   <li>сервис проверяет существование категории перед сохранением;</li>
     *   <li>данные из {@link ProductRequest} переносятся в ответ клиенту.</li>
     * </ul>
     * <p>
     * <b>Ожидаемый результат:</b> созданный товар с id, присвоенным репозиторием.
     */
    @Test
    void create_shouldCreateProductWhenCategoryExists() {
        // Подготовка данных: эмулируем DTO, пришедший от клиента
        ProductRequest request = new ProductRequest();
        request.setName("New");
        request.setDescription("d");
        request.setPrice(BigDecimal.valueOf(100));
        request.setImageUrl("img.png");
        request.setStockQuantity(10);
        request.setCategoryId(5L);

        // Категория существует - проверка существования пройдет
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(createCategory(5L)));

        // Репозиторий присваивает id и возвращает сохраненный товар
        Product saved = createProduct(1L, "New", 5L, 10);
        // any(Product.class) - любой объект типа Product, нам не важен конкретный экземпляр,
        // потому что id присваивает сама БД, а ее тут нет
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        // Действие
        ProductResponse response = productService.create(request);

        // Проверка результата
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("New");
        assertThat(response.getCategoryId()).isEqualTo(5L);
        assertThat(response.getStockQuantity()).isEqualTo(10);
    }

    /**
     * Создание товара в несуществующей категории.
     * <p>
     * <b>Что проверяем:</b> сервис выбрасывает {@link CategoryNotFoundException} - понятную 404,
     * вместо того чтобы упасть позже на ограничении внешнего ключа с невнятной ошибкой БД.
     * Категория проверяется до сохранения, поэтому {@code save()} не вызывается вовсе.
     * <p>
     * <b>Ожидаемый результат:</b> исключение выброшено, товар не сохранён.
     */
    @Test
    void create_shouldThrowWhenCategoryNotFound() {
        ProductRequest request = new ProductRequest();
        request.setCategoryId(999L);

        // Категории с таким id нет
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Сервис должен выйти с ошибкой 404 на этапе проверки категории
        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    /**
     * Обновление существующего товара.
     * <p>
     * <b>Что проверяем:</b>
     * <ul>
     *   <li>товар сначала найден по id — без этого обновлять нечего;</li>
     *   <li>категория переехала с 10 на 20, то есть переносится в том числе смена категории;</li>
     *   <li>остаток обновлён на значение из запроса.</li>
     * </ul>
     * <p>
     * <b>Ожидаемый результат:</b> обновлённый товар.
     */
    @Test
    void update_shouldUpdateProductWhenFound() {
        // Подготовка данных: товар существует и изначально в другой категории с меньшим остатком
        Product existing = createProduct(1L, "Old", 10L, 2);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        // Новая категория тоже должна существовать
        when(categoryRepository.findById(20L)).thenReturn(Optional.of(createCategory(20L)));

        ProductRequest request = new ProductRequest();
        request.setName("Updated");
        request.setDescription("nd");
        request.setPrice(BigDecimal.valueOf(50));
        request.setImageUrl("u.png");
        request.setStockQuantity(7);
        request.setCategoryId(20L);

        Product saved = createProduct(1L, "Updated", 20L, 7);
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        // Действие: меняем данные существующего товара
        ProductResponse response = productService.update(1L, request);

        // Проверка результата
        assertThat(response.getName()).isEqualTo("Updated");
        assertThat(response.getCategoryId()).isEqualTo(20L);
        assertThat(response.getStockQuantity()).isEqualTo(7);
    }

    /**
     * Обновление несуществующего товара.
     * <p>
     * <b>Что проверяем:</b> сервис выбрасывает {@link ProductNotFoundException} и выходит
     * раньше проверки категории — товара нет, обновлять нечего.
     * <p>
     * <b>Ожидаемый результат:</b> выброшено исключение {@link ProductNotFoundException}.
     */
    @Test
    void update_shouldThrowWhenProductNotFound() {
        ProductRequest request = new ProductRequest();
        request.setCategoryId(1L);
        // Товара с таким id нет
        when(productRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(123L, request))
                .isInstanceOf(ProductNotFoundException.class);
    }

    /**
     * Обновление товара с несуществующей категорией.
     * <p>
     * <b>Что проверяем:</b>
     * <ul>
     *   <li>товар найден, но проверка категории не пройдена;</li>
     *   <li>выброс {@link CategoryNotFoundException} — товар не должен «записаться» с битой ссылкой на категорию.</li>
     * </ul>
     * <p>
     * <b>Ожидаемый результат:</b> выброшено исключение {@link CategoryNotFoundException}.
     */
    @Test
    void update_shouldThrowWhenCategoryNotFound() {
        // Товар существует, а вот категория - нет
        Product existing = createProduct(1L, "Old", 10L, 2);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        ProductRequest request = new ProductRequest();
        request.setCategoryId(999L);

        assertThatThrownBy(() -> productService.update(1L, request))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    /**
     * Удаление существующего товара.
     * <p>
     * <b>Что проверяем:</b>
     * <ul>
     *   <li>перед удалением сервис убеждается, что товар существует — {@code deleteById()} вслепую
     *       вернул бы успех даже для несуществующего id;</li>
     *   <li>в репозиторий уходит именно найденный объект, а не просто id.</li>
     * </ul>
     * <p>
     * <b>Ожидаемый результат:</b> {@code delete()} вызван один раз с найденным товаром.
     */
    @Test
    void delete_shouldDeleteWhenProductFound() {
        Product product = createProduct(1L, "P", 10L, 1);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Действие: метод ничего не возвращает, поэтому проверять нечего - проверяем эффект
        productService.delete(1L);

        // verify(...) без times() - проверка ровно одного вызова с ровно этим аргументом
        verify(productRepository).delete(product);
    }

    /**
     * Удаление несуществующего товара.
     * <p>
     * <b>Что проверяем:</b> сервис выбрасывает {@link ProductNotFoundException} и не вызывает {@code delete()}.
     * <p>
     * <b>Ожидаемый результат:</b> выброшено исключение {@link ProductNotFoundException}.
     */
    @Test
    void delete_shouldThrowWhenProductNotFound() {
        when(productRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(5L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    /**
     * Установка остатка на складе.
     * <p>
     * <b>Что проверяем:</b>
     * <ul>
     *   <li>остаток трактуется как абсолютное значение, а не как прибавка: было 5, стало 42;</li>
     *   <li>остальные поля товара не затрагиваются.</li>
     * </ul>
     * <p>
     * <b>Ожидаемый результат:</b> ответ с остатком 42.
     */
    @Test
    void updateStock_shouldUpdateStockWhenProductFound() {
        Product product = createProduct(1L, "P", 10L, 5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // DTO с новым остатком
        StockQuantityRequest request = new StockQuantityRequest();
        request.setStockQuantity(42);

        Product saved = createProduct(1L, "P", 10L, 42);
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        // Действие: сервис правит остаток у найденного товара и сохраняет его
        ProductResponse response = productService.updateStock(1L, request);

        // Проверка результата
        assertThat(response.getStockQuantity()).isEqualTo(42);
    }

    /**
     * Установка остатка для несуществующего товара.
     * <p>
     * <b>Что проверяем:</b> сервис выбрасывает {@link ProductNotFoundException};
     * репозиторий категорий тут не нужен — при смене остатка категория не меняется.
     * <p>
     * <b>Ожидаемый результат:</b> выброшено исключение {@link ProductNotFoundException}.
     */
    @Test
    void updateStock_shouldThrowWhenProductNotFound() {
        StockQuantityRequest request = new StockQuantityRequest();
        request.setStockQuantity(10);
        when(productRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateStock(77L, request))
                .isInstanceOf(ProductNotFoundException.class);
    }
}