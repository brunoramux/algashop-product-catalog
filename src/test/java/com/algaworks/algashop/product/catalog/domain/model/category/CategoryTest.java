package com.algaworks.algashop.product.catalog.domain.model.category;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Category domain model")
class CategoryTest {

    @Test
    @DisplayName("should create category with name and enabled status")
    void shouldCreateCategoryWithNameAndEnabled() {
        Category category = new Category("Electronics", true);

        assertThat(category.getId()).isNotNull();
        assertThat(category.getName()).isEqualTo("Electronics");
        assertThat(category.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("should create disabled category")
    void shouldCreateDisabledCategory() {
        Category category = new Category("Electronics", false);
        assertThat(category.getEnabled()).isFalse();
    }

    @Test
    @DisplayName("should throw when name is blank")
    void shouldThrowWhenNameIsBlank() {
        assertThatThrownBy(() -> new Category("  ", true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should throw when name is null")
    void shouldThrowWhenNameIsNull() {
        assertThatThrownBy(() -> new Category(null, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should throw when enabled is null")
    void shouldThrowWhenEnabledIsNull() {
        assertThatThrownBy(() -> new Category("Electronics", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should update name")
    void shouldUpdateName() {
        Category category = new Category("Electronics", true);
        category.setName("Computers");
        assertThat(category.getName()).isEqualTo("Computers");
    }

    @Test
    @DisplayName("should throw when updating to blank name")
    void shouldThrowWhenUpdatingToBlankName() {
        Category category = new Category("Electronics", true);
        assertThatThrownBy(() -> category.setName(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should update enabled status")
    void shouldUpdateEnabledStatus() {
        Category category = new Category("Electronics", true);
        category.setEnabled(false);
        assertThat(category.getEnabled()).isFalse();
    }

    @Test
    @DisplayName("should generate unique ids for different categories")
    void shouldGenerateUniqueIds() {
        Category c1 = new Category("Cat1", true);
        Category c2 = new Category("Cat2", true);
        assertThat(c1.getId()).isNotEqualTo(c2.getId());
    }
}

