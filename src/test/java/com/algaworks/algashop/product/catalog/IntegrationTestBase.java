package com.algaworks.algashop.product.catalog;

import com.algaworks.algashop.product.catalog.application.upload.UploadResponseOutput;
import com.algaworks.algashop.product.catalog.domain.model.category.Category;
import com.algaworks.algashop.product.catalog.domain.model.category.CategoryRepository;
import com.algaworks.algashop.product.catalog.domain.model.product.Product;
import com.algaworks.algashop.product.catalog.domain.model.product.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public abstract class IntegrationTestBase {

    @Container
    @ServiceConnection
    static final MongoDBContainer mongoDBContainer =
            new MongoDBContainer("mongo:7.0").withReuse(true);

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @MockitoBean
    protected S3Client s3Client;

    @MockitoBean
    protected S3Presigner s3Presigner;

    @MockitoBean
    protected ProductImageStorageService productImageStorageService;

    @AfterEach
    void cleanUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    protected Category createCategory(String name) {
        Category category = new Category(name, true);
        return categoryRepository.save(category);
    }

    protected Product createProduct(Category category) {
        Product product = Product.builder()
                .name("Notebook X11")
                .brand("TechBrand")
                .description("A great notebook")
                .regularPrice(new BigDecimal("1500.00"))
                .salePrice(new BigDecimal("1200.00"))
                .enabled(true)
                .category(category)
                .build();
        return productRepository.save(product);
    }

    protected void mockStorageService() {
        when(productImageStorageService.buildImageUrl(anyString()))
                .thenAnswer(inv -> "http://localhost:4566/test-bucket/" + inv.getArgument(0));
        when(productImageStorageService.createUploadRequest(anyString(), anyLong()))
                .thenReturn(UploadResponseOutput.builder()
                        .remoteFileName("uuid.jpg")
                        .contentLength(1024L)
                        .contentType("image/jpeg")
                        .uploadSignedUrl("http://s3/signed-url")
                        .build());
        when(productImageStorageService.uploadImage(anyString(), any(), anyLong(), anyString()))
                .thenReturn("uploaded-uuid.jpg");
    }
}
