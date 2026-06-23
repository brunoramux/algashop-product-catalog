package com.algaworks.algashop.product.catalog.presentation;

import com.algaworks.algashop.product.catalog.IntegrationTestBase;
import com.algaworks.algashop.product.catalog.domain.model.category.Category;
import com.algaworks.algashop.product.catalog.domain.model.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("ProductController Integration Tests")
class ProductControllerIT extends IntegrationTestBase {

    private Category category;

    @BeforeEach
    void setup() {
        category = createCategory("Electronics");
        mockStorageService();
    }

    @Test
    @DisplayName("POST /api/v1/products should create product and return 201")
    void shouldCreateProduct() throws Exception {
        String body = """
                {
                  "name": "Notebook X11",
                  "brand": "TechBrand",
                  "description": "A great notebook",
                  "regularPrice": 1500.00,
                  "salePrice": 1200.00,
                  "enabled": true,
                  "categoryId": "%s"
                }
                """.formatted(category.getId());

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Notebook X11"))
                .andExpect(jsonPath("$.brand").value("TechBrand"))
                .andExpect(jsonPath("$.slug").value("notebook-x11"))
                .andExpect(jsonPath("$.regularPrice").value(1500.00))
                .andExpect(jsonPath("$.salePrice").value(1200.00))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.discountPercentageRounded").value(20))
                .andExpect(jsonPath("$.category.id").value(category.getId().toString()));
    }

    @Test
    @DisplayName("POST /api/v1/products should return 400 when name is blank")
    void shouldReturn400WhenNameIsBlank() throws Exception {
        String body = """
                {
                  "name": "",
                  "brand": "TechBrand",
                  "regularPrice": 1500.00,
                  "salePrice": 1200.00,
                  "enabled": true,
                  "categoryId": "%s"
                }
                """.formatted(category.getId());

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid fields"));
    }

    @Test
    @DisplayName("POST /api/v1/products should return 404 when category not found")
    void shouldReturn404WhenCategoryNotFound() throws Exception {
        String body = """
                {
                  "name": "Notebook",
                  "brand": "Brand",
                  "regularPrice": 1500.00,
                  "salePrice": 1200.00,
                  "enabled": true,
                  "categoryId": "%s"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} should return product")
    void shouldReturnProductById() throws Exception {
        Product product = createProduct(category);

        mockMvc.perform(get("/api/v1/products/{id}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId().toString()))
                .andExpect(jsonPath("$.name").value("Notebook X11"))
                .andExpect(jsonPath("$.category.id").value(category.getId().toString()));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} should return 404 when not found")
    void shouldReturn404WhenProductNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not found"));
    }

    @Test
    @DisplayName("GET /api/v1/products should return paginated list")
    void shouldReturnPaginatedProducts() throws Exception {
        createProduct(category);
        createProduct(category);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/products should filter by enabled")
    void shouldFilterProductsByEnabled() throws Exception {
        createProduct(category);
        Product disabled = Product.builder()
                .name("Disabled Product")
                .brand("Brand")
                .regularPrice(java.math.BigDecimal.TEN)
                .salePrice(java.math.BigDecimal.TEN)
                .enabled(false)
                .category(category)
                .build();
        productRepository.save(disabled);

        mockMvc.perform(get("/api/v1/products").param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("PUT /api/v1/products/{id} should update product")
    void shouldUpdateProduct() throws Exception {
        Product product = createProduct(category);
        String body = """
                {
                  "name": "Updated Notebook",
                  "brand": "NewBrand",
                  "description": "Updated description",
                  "regularPrice": 2000.00,
                  "salePrice": 1800.00,
                  "enabled": true,
                  "categoryId": "%s"
                }
                """.formatted(category.getId());

        mockMvc.perform(put("/api/v1/products/{id}", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Notebook"))
                .andExpect(jsonPath("$.slug").value("updated-notebook"))
                .andExpect(jsonPath("$.brand").value("NewBrand"));
    }

    @Test
    @DisplayName("PUT /api/v1/products/{id}/enable should enable product")
    void shouldEnableProduct() throws Exception {
        Product product = Product.builder()
                .name("Disabled Product")
                .brand("Brand")
                .regularPrice(java.math.BigDecimal.TEN)
                .salePrice(java.math.BigDecimal.TEN)
                .enabled(false)
                .category(category)
                .build();
        productRepository.save(product);

        mockMvc.perform(put("/api/v1/products/{id}/enable", product.getId()))
                .andExpect(status().isNoContent());

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updated.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id}/enable should disable product")
    void shouldDisableProduct() throws Exception {
        Product product = createProduct(category);

        mockMvc.perform(delete("/api/v1/products/{id}/enable", product.getId()))
                .andExpect(status().isNoContent());

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updated.getEnabled()).isFalse();
    }

    @Test
    @DisplayName("POST /api/v1/products/{id}/restock should increase stock")
    void shouldRestockProduct() throws Exception {
        Product product = createProduct(category);
        String body = """
                {"quantity": 10}
                """;

        mockMvc.perform(post("/api/v1/products/{id}/restock", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updated.getQuantityInStock()).isEqualTo(10);
    }

    @Test
    @DisplayName("POST /api/v1/products/{id}/withdraw should decrease stock")
    void shouldWithdrawFromStock() throws Exception {
        Product product = createProduct(category);
        // First restock
        mockMvc.perform(post("/api/v1/products/{id}/restock", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 20}"))
                .andExpect(status().isNoContent());

        // Then withdraw
        mockMvc.perform(post("/api/v1/products/{id}/withdraw", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 5}"))
                .andExpect(status().isNoContent());

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updated.getQuantityInStock()).isEqualTo(15);
    }

    @Test
    @DisplayName("POST /api/v1/products/{id}/restock should return 400 for quantity less than 1")
    void shouldReturn400ForInvalidRestockQuantity() throws Exception {
        Product product = createProduct(category);

        mockMvc.perform(post("/api/v1/products/{id}/restock", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/products/{id}/withdraw should return 422 when insufficient stock")
    void shouldReturn422WhenInsufficientStock() throws Exception {
        Product product = createProduct(category); // stock = 0

        mockMvc.perform(post("/api/v1/products/{id}/withdraw", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 5}"))
                .andExpect(status().isUnprocessableEntity());
    }
}

