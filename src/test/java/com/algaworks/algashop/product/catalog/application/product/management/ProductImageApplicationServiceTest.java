package com.algaworks.algashop.product.catalog.application.product.management;

import com.algaworks.algashop.product.catalog.application.ResourceNotFoundException;
import com.algaworks.algashop.product.catalog.application.product.query.ImageOutput;
import com.algaworks.algashop.product.catalog.application.upload.ProductImageStorageService;
import com.algaworks.algashop.product.catalog.domain.model.category.Category;
import com.algaworks.algashop.product.catalog.domain.model.product.Product;
import com.algaworks.algashop.product.catalog.domain.model.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductImageApplicationService")
class ProductImageApplicationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageStorageService productImageStorageService;

    @InjectMocks
    private ProductImageApplicationService service;

    private UUID productId;
    private Product product;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        Category category = new Category("Electronics", true);
        product = Product.builder()
                .name("Notebook")
                .brand("Brand")
                .regularPrice(new BigDecimal("1000.00"))
                .salePrice(new BigDecimal("900.00"))
                .enabled(true)
                .category(category)
                .build();
    }

    @Test
    @DisplayName("should add image to product and return ImageOutput")
    void shouldAddImage() {
        ImageInput input = ImageInput.builder().remoteFileName("photo.jpg").build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productImageStorageService.buildImageUrl("photo.jpg"))
                .thenReturn("http://localhost/photo.jpg");

        ImageOutput result = service.addImage(productId, input);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getUrl()).isEqualTo("http://localhost/photo.jpg");
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("should upload image to S3 and add to product")
    void shouldUploadAndAddImage() throws Exception {
        InputStream content = new ByteArrayInputStream("image-bytes".getBytes());
        String remoteFileName = "uuid.jpg";

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productImageStorageService.uploadImage("photo.jpg", content, 100L, "image/jpeg"))
                .thenReturn(remoteFileName);
        when(productImageStorageService.buildImageUrl(remoteFileName))
                .thenReturn("http://localhost/" + remoteFileName);

        ImageOutput result = service.uploadAndAddImage(productId, "photo.jpg", content, 100L, "image/jpeg");

        assertThat(result.getId()).isNotNull();
        assertThat(result.getUrl()).isEqualTo("http://localhost/" + remoteFileName);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("should list all images with resolved URLs")
    void shouldListImages() {
        product.addImage("img1.jpg");
        product.addImage("img2.jpg");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productImageStorageService.buildImageUrl(anyString()))
                .thenAnswer(inv -> "http://localhost/" + inv.getArgument(0));

        List<ImageOutput> images = service.listImages(productId);

        assertThat(images).hasSize(2);
        assertThat(images).allMatch(img -> img.getUrl().startsWith("http://localhost/"));
    }

    @Test
    @DisplayName("should find specific image")
    void shouldFindImage() {
        UUID imageId = product.addImage("specific.jpg");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productImageStorageService.buildImageUrl("specific.jpg"))
                .thenReturn("http://localhost/specific.jpg");

        ImageOutput result = service.findImage(productId, imageId);

        assertThat(result.getId()).isEqualTo(imageId);
        assertThat(result.getUrl()).isEqualTo("http://localhost/specific.jpg");
    }

    @Test
    @DisplayName("should throw when finding non-existent image")
    void shouldThrowWhenImageNotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.findImage(productId, UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("should remove image from product")
    void shouldRemoveImage() {
        UUID imageId = product.addImage("to-remove.jpg");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        service.removeImage(productId, imageId);

        assertThat(product.getImages()).isEmpty();
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("should set main image")
    void shouldSetMainImage() {
        product.addImage("first.jpg");
        UUID secondId = product.addImage("second.jpg");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        service.setMainImage(productId, secondId);

        assertThat(product.getMainImage().getId()).isEqualTo(secondId);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("should throw when product not found")
    void shouldThrowWhenProductNotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addImage(productId,
                ImageInput.builder().remoteFileName("img.jpg").build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

