package com.algaworks.algashop.product.catalog.presentation;

import com.algaworks.algashop.product.catalog.application.category.management.CategoryManagementApplicationService;
import com.algaworks.algashop.product.catalog.application.category.query.CategoryDetailOutput;
import com.algaworks.algashop.product.catalog.application.category.query.CategoryFilter;
import com.algaworks.algashop.product.catalog.application.category.query.CategoryQueryService;
import com.algaworks.algashop.product.catalog.application.PageModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@DisplayName("CategoryController Web Layer Tests")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryQueryService categoryQueryService;

    @MockitoBean
    private CategoryManagementApplicationService categoryManagementApplicationService;

    @Test
    @DisplayName("POST /api/v1/categories should create category and return 201")
    void shouldCreateCategory() throws Exception {
        UUID categoryId = UUID.randomUUID();
        CategoryDetailOutput output = CategoryDetailOutput.builder()
                .id(categoryId).name("Electronics").enabled(true).build();

        when(categoryManagementApplicationService.create(any())).thenReturn(categoryId);
        when(categoryQueryService.findById(categoryId)).thenReturn(output);

        String body = """
                {"name": "Electronics", "enabled": true}
                """;

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/categories should return 400 when name is blank")
    void shouldReturn400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\", \"enabled\": true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid fields"));
    }

    @Test
    @DisplayName("POST /api/v1/categories should return 400 when enabled is null")
    void shouldReturn400WhenEnabledIsNull() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Electronics\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/categories should return paginated list")
    void shouldReturnPaginatedCategories() throws Exception {
        CategoryDetailOutput cat = CategoryDetailOutput.builder()
                .id(UUID.randomUUID()).name("Electronics").enabled(true).build();

        PageModel<CategoryDetailOutput> page = PageModel.<CategoryDetailOutput>builder()
                .content(List.of(cat)).number(0).size(15).totalElements(1).totalPages(1).build();

        when(categoryQueryService.filter(any(CategoryFilter.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Electronics"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} should return category")
    void shouldReturnCategoryById() throws Exception {
        UUID id = UUID.randomUUID();
        CategoryDetailOutput output = CategoryDetailOutput.builder()
                .id(id).name("Electronics").enabled(true).build();

        when(categoryQueryService.findById(id)).thenReturn(output);

        mockMvc.perform(get("/api/v1/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} should return 404 when not found")
    void shouldReturn404WhenCategoryNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(categoryQueryService.findById(id))
                .thenThrow(new com.algaworks.algashop.product.catalog.application.ResourceNotFoundException());

        mockMvc.perform(get("/api/v1/categories/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not found"));
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} should update category")
    void shouldUpdateCategory() throws Exception {
        UUID id = UUID.randomUUID();
        CategoryDetailOutput updated = CategoryDetailOutput.builder()
                .id(id).name("New Name").enabled(false).build();

        doNothing().when(categoryManagementApplicationService).update(eq(id), any());
        when(categoryQueryService.findById(id)).thenReturn(updated);

        mockMvc.perform(put("/api/v1/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"New Name\", \"enabled\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} should disable category")
    void shouldDisableCategory() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(categoryManagementApplicationService).disable(id);

        mockMvc.perform(delete("/api/v1/categories/{id}", id))
                .andExpect(status().isNoContent());

        verify(categoryManagementApplicationService).disable(id);
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} should return 404 when not found")
    void shouldReturn404WhenDisablingNonExistent() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new com.algaworks.algashop.product.catalog.application.ResourceNotFoundException())
                .when(categoryManagementApplicationService).disable(id);

        mockMvc.perform(delete("/api/v1/categories/{id}", id))
                .andExpect(status().isNotFound());
    }
}

