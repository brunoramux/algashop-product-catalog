package com.algaworks.algashop.product.catalog.presentation;

import com.algaworks.algashop.product.catalog.IntegrationTestBase;
import com.algaworks.algashop.product.catalog.domain.model.category.Category;
import com.algaworks.algashop.product.catalog.domain.model.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("ProductImageController Integration Tests")
class ProductImageControllerIT extends IntegrationTestBase {

    private Product product;

    @BeforeEach
    void setup() {
        Category category = createCategory("Electronics");
        product = createProduct(category);
        mockStorageService();
    }

    @Test
    @DisplayName("POST /images should add image by remoteFileName and return 201")
    void shouldAddImageByRemoteFileName() throws Exception {
        String body = """
                {"remoteFileName": "photo-uuid.jpg"}
                """;

        mockMvc.perform(post("/api/v1/products/{id}/images", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.url").value("http://localhost:4566/test-bucket/photo-uuid.jpg"));
    }

    @Test
    @DisplayName("POST /images should return 400 when remoteFileName is blank")
    void shouldReturn400WhenRemoteFileNameIsBlank() throws Exception {
        String body = """
                {"remoteFileName": ""}
                """;

        mockMvc.perform(post("/api/v1/products/{id}/images", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /images/upload should upload file and add image")
    void shouldUploadFileAndAddImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/products/{id}/images/upload", product.getId())
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.url").value("http://localhost:4566/test-bucket/uploaded-uuid.jpg"));
    }

    @Test
    @DisplayName("GET /images should list all product images")
    void shouldListImages() throws Exception {
        product.addImage("img1.jpg");
        product.addImage("img2.jpg");
        productRepository.save(product);

        mockMvc.perform(get("/api/v1/products/{id}/images", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /images/{imageId} should return specific image")
    void shouldReturnImageById() throws Exception {
        UUID imageId = product.addImage("specific.jpg");
        productRepository.save(product);

        mockMvc.perform(get("/api/v1/products/{productId}/images/{imageId}",
                        product.getId(), imageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(imageId.toString()))
                .andExpect(jsonPath("$.url").value("http://localhost:4566/test-bucket/specific.jpg"));
    }

    @Test
    @DisplayName("GET /images/{imageId} should return 404 when image not found")
    void shouldReturn404WhenImageNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/products/{productId}/images/{imageId}",
                        product.getId(), UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /images/{imageId} should remove image and return 204")
    void shouldRemoveImage() throws Exception {
        UUID imageId = product.addImage("to-remove.jpg");
        productRepository.save(product);

        mockMvc.perform(delete("/api/v1/products/{productId}/images/{imageId}",
                        product.getId(), imageId))
                .andExpect(status().isNoContent());

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updated.getImages()).isEmpty();
    }

    @Test
    @DisplayName("PUT /images/{imageId}/primary should set image as main")
    void shouldSetMainImage() throws Exception {
        product.addImage("first.jpg");
        UUID secondId = product.addImage("second.jpg");
        productRepository.save(product);

        mockMvc.perform(put("/api/v1/products/{productId}/images/{imageId}/primary",
                        product.getId(), secondId))
                .andExpect(status().isNoContent());

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updated.getMainImage().getId()).isEqualTo(secondId);
    }

    @Test
    @DisplayName("DELETE /images/{imageId} should return 404 when product not found")
    void shouldReturn404WhenProductNotFoundOnRemove() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{productId}/images/{imageId}",
                        UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}

