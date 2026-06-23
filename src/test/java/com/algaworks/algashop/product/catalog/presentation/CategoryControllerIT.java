package com.algaworks.algashop.product.catalog.presentation;

import com.algaworks.algashop.product.catalog.IntegrationTestBase;
import com.algaworks.algashop.product.catalog.domain.model.category.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("CategoryController Integration Tests")
class CategoryControllerIT extends IntegrationTestBase {

    @Test
    @DisplayName("POST /api/v1/categories should create category and return 201")
    void shouldCreateCategory() throws Exception {
        String body = """
                {"name": "Electronics", "enabled": true}
                """;

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/categories should return 400 when name is blank")
    void shouldReturn400WhenNameIsBlank() throws Exception {
        String body = """
                {"name": "", "enabled": true}
                """;

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid fields"));
    }

    @Test
    @DisplayName("POST /api/v1/categories should return 400 when enabled is null")
    void shouldReturn400WhenEnabledIsNull() throws Exception {
        String body = """
                {"name": "Electronics"}
                """;

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/categories should return paginated list")
    void shouldReturnPaginatedCategories() throws Exception {
        createCategory("Electronics");
        createCategory("Computers");

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].name").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/categories should filter by name")
    void shouldFilterByName() throws Exception {
        createCategory("Electronics");
        createCategory("Furniture");

        mockMvc.perform(get("/api/v1/categories").param("name", "Electr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Electronics"));
    }

    @Test
    @DisplayName("GET /api/v1/categories should filter by enabled")
    void shouldFilterByEnabled() throws Exception {
        createCategory("Active Cat");
        Category disabled = new Category("Disabled Cat", false);
        categoryRepository.save(disabled);

        mockMvc.perform(get("/api/v1/categories").param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Active Cat"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} should return category")
    void shouldReturnCategoryById() throws Exception {
        Category category = createCategory("Electronics");

        mockMvc.perform(get("/api/v1/categories/{id}", category.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId().toString()))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} should return 404 when not found")
    void shouldReturn404WhenCategoryNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/categories/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not found"));
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} should update category")
    void shouldUpdateCategory() throws Exception {
        Category category = createCategory("Old Name");
        String body = """
                {"name": "New Name", "enabled": false}
                """;

        mockMvc.perform(put("/api/v1/categories/{id}", category.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} should disable category")
    void shouldDisableCategory() throws Exception {
        Category category = createCategory("Electronics");

        mockMvc.perform(delete("/api/v1/categories/{id}", category.getId()))
                .andExpect(status().isNoContent());

        Category updated = categoryRepository.findById(category.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(updated.getEnabled()).isFalse();
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} should return 404 when not found")
    void shouldReturn404WhenDisablingNonExistent() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}

