package com.algaworks.algashop.product.catalog.application.category.management;

import com.algaworks.algashop.product.catalog.application.ResourceNotFoundException;
import com.algaworks.algashop.product.catalog.domain.model.category.Category;
import com.algaworks.algashop.product.catalog.domain.model.category.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryManagementApplicationService")
class CategoryManagementApplicationServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryManagementApplicationService service;

    private CategoryInput validInput;

    @BeforeEach
    void setUp() {
        validInput = CategoryInput.builder()
                .name("Electronics")
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("should create category and return UUID")
    void shouldCreateCategoryAndReturnUUID() {
        UUID id = service.create(validInput);

        assertThat(id).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("should update category name and enabled status")
    void shouldUpdateCategory() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category("Old Name", true);
        CategoryInput updateInput = CategoryInput.builder()
                .name("New Name")
                .enabled(false)
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        service.update(categoryId, updateInput);

        assertThat(category.getName()).isEqualTo("New Name");
        assertThat(category.getEnabled()).isFalse();
        verify(categoryRepository).save(category);
    }

    @Test
    @DisplayName("should throw when category not found on update")
    void shouldThrowWhenCategoryNotFoundOnUpdate() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(categoryId, validInput))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("should disable category")
    void shouldDisableCategory() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category("Electronics", true);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        service.disable(categoryId);

        assertThat(category.getEnabled()).isFalse();
        verify(categoryRepository).save(category);
    }

    @Test
    @DisplayName("should throw when category not found on disable")
    void shouldThrowWhenCategoryNotFoundOnDisable() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.disable(categoryId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, never()).save(any());
    }
}

