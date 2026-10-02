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

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

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

    private Category createCategory(Long id) {
        Category category = new Category();
        category.setId(id);
        category.setName("Cat");
        return category;
    }

    @Test
    void getAll_shouldReturnAllWhenCategoryIdNull() {
        Product p1 = createProduct(1L, "P1", 10L, 5);
        Product p2 = createProduct(2L, "P2", 10L, 3);
        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        List<ProductResponse> result = productService.getAll(null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("P1");
        assertThat(result.get(0).getCategoryId()).isEqualTo(10L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void getAll_shouldReturnByCategoryWhenCategoryIdProvided() {
        Product p1 = createProduct(1L, "P1", 20L, 1);
        when(productRepository.findByCategoryId(20L)).thenReturn(List.of(p1));

        List<ProductResponse> result = productService.getAll(20L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoryId()).isEqualTo(20L);
    }

    @Test
    void getAll_shouldReturnEmptyListWhenNoProducts() {
        when(productRepository.findAll()).thenReturn(List.of());

        List<ProductResponse> result = productService.getAll(null);

        assertThat(result).isEmpty();
    }

    @Test
    void getById_shouldReturnProductResponseWhenFound() {
        Product product = createProduct(1L, "P1", 10L, 5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("P1");
        assertThat(response.getCategoryId()).isEqualTo(10L);
        assertThat(response.getStockQuantity()).isEqualTo(5);
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void create_shouldCreateProductWhenCategoryExists() {
        ProductRequest request = new ProductRequest();
        request.setName("New");
        request.setDescription("d");
        request.setPrice(BigDecimal.valueOf(100));
        request.setImageUrl("img.png");
        request.setStockQuantity(10);
        request.setCategoryId(5L);

        when(categoryRepository.findById(5L)).thenReturn(Optional.of(createCategory(5L)));

        Product saved = createProduct(1L, "New", 5L, 10);
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = productService.create(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("New");
        assertThat(response.getCategoryId()).isEqualTo(5L);
        assertThat(response.getStockQuantity()).isEqualTo(10);
    }

    @Test
    void create_shouldThrowWhenCategoryNotFound() {
        ProductRequest request = new ProductRequest();
        request.setCategoryId(999L);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void update_shouldUpdateProductWhenFound() {
        Product existing = createProduct(1L, "Old", 10L, 2);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
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

        ProductResponse response = productService.update(1L, request);

        assertThat(response.getName()).isEqualTo("Updated");
        assertThat(response.getCategoryId()).isEqualTo(20L);
        assertThat(response.getStockQuantity()).isEqualTo(7);
    }

    @Test
    void update_shouldThrowWhenProductNotFound() {
        ProductRequest request = new ProductRequest();
        request.setCategoryId(1L);
        when(productRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(123L, request))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void update_shouldThrowWhenCategoryNotFound() {
        Product existing = createProduct(1L, "Old", 10L, 2);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        ProductRequest request = new ProductRequest();
        request.setCategoryId(999L);

        assertThatThrownBy(() -> productService.update(1L, request))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void delete_shouldDeleteWhenProductFound() {
        Product product = createProduct(1L, "P", 10L, 1);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.delete(1L);

        verify(productRepository).delete(product);
    }

    @Test
    void delete_shouldThrowWhenProductNotFound() {
        when(productRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(5L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void updateStock_shouldUpdateStockWhenProductFound() {
        Product product = createProduct(1L, "P", 10L, 5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        StockQuantityRequest request = new StockQuantityRequest();
        request.setStockQuantity(42);

        Product saved = createProduct(1L, "P", 10L, 42);
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = productService.updateStock(1L, request);

        assertThat(response.getStockQuantity()).isEqualTo(42);
    }

    @Test
    void updateStock_shouldThrowWhenProductNotFound() {
        StockQuantityRequest request = new StockQuantityRequest();
        request.setStockQuantity(10);
        when(productRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateStock(77L, request))
                .isInstanceOf(ProductNotFoundException.class);
    }
}
