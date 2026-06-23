package com.algaworks.algashop.product.catalog.application.product.management;

import com.algaworks.algashop.product.catalog.application.ResourceNotFoundException;
import com.algaworks.algashop.product.catalog.application.product.query.ProductDetailOutput;
import com.algaworks.algashop.product.catalog.application.utility.Mapper;
import com.algaworks.algashop.product.catalog.domain.model.category.Category;
import com.algaworks.algashop.product.catalog.domain.model.category.CategoryRepository;
import com.algaworks.algashop.product.catalog.domain.model.product.Product;
import com.algaworks.algashop.product.catalog.domain.model.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductManagementApplicationService")
class ProductManagementApplicationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private Mapper mapper;

    @InjectMocks
    private ProductManagementApplicationService service;

    private Category category;
    private UUID categoryId;
    private ProductInput validInput;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        category = new Category("Electronics", true);

        validInput = ProductInput.builder()
                .name("Notebook X11")
                .brand("TechBrand")
                .description("A great notebook")
                .regularPrice(new BigDecimal("1500.00"))
                .salePrice(new BigDecimal("1200.00"))
                .enabled(true)
                .categoryId(categoryId)
                .build();
    }

    @Test
    @DisplayName("should create product and return output")
    void shouldCreateProduct() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(mapper.convert(any(Product.class), eq(ProductDetailOutput.class)))
                .thenReturn(new ProductDetailOutput());

        ProductDetailOutput result = service.create(validInput);

        verify(productRepository).save(any(Product.class));
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("should throw when category not found on create")
    void shouldThrowWhenCategoryNotFoundOnCreate() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(validInput))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("should update product fields")
    void shouldUpdateProduct() {
        UUID productId = UUID.randomUUID();
        Product product = buildProduct();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(mapper.convert(any(Product.class), eq(ProductDetailOutput.class)))
                .thenReturn(new ProductDetailOutput());

        service.update(productId, validInput);

        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("should throw when product not found on update")
    void shouldThrowWhenProductNotFoundOnUpdate() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(productId, validInput))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("should disable product")
    void shouldDisableProduct() {
        UUID productId = UUID.randomUUID();
        Product product = buildProduct();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        service.disable(productId);

        assertThat(product.getEnabled()).isFalse();
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("should enable product")
    void shouldEnableProduct() {
        UUID productId = UUID.randomUUID();
        Product product = buildProduct();
        product.disable();
        product.resetEvents();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        service.enable(productId);

        assertThat(product.getEnabled()).isTrue();
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("should throw when product not found on disable")
    void shouldThrowWhenProductNotFoundOnDisable() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.disable(productId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("should update product name and slug on update")
    void shouldUpdateProductNameAndSlug() {
        UUID productId = UUID.randomUUID();
        Product product = buildProduct();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(mapper.convert(any(Product.class), eq(ProductDetailOutput.class)))
                .thenReturn(new ProductDetailOutput());

        ProductInput updateInput = ProductInput.builder()
                .name("Updated Notebook")
                .brand("BrandX")
                .regularPrice(new BigDecimal("2000.00"))
                .salePrice(new BigDecimal("1800.00"))
                .enabled(true)
                .categoryId(categoryId)
                .build();

        service.update(productId, updateInput);

        assertThat(product.getName()).isEqualTo("Updated Notebook");
        assertThat(product.getSlug()).isEqualTo("updated-notebook");
    }

    private Product buildProduct() {
        return Product.builder()
                .name("Notebook X11")
                .brand("TechBrand")
                .description("A great notebook")
                .regularPrice(new BigDecimal("1500.00"))
                .salePrice(new BigDecimal("1200.00"))
                .enabled(true)
                .category(category)
                .build();
    }
}

