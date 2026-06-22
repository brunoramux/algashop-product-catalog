package com.algaworks.algashop.product.catalog.application.product.management;

import com.algaworks.algashop.product.catalog.application.ResourceNotFoundException;
import com.algaworks.algashop.product.catalog.application.product.query.ProductDetailOutput;
import com.algaworks.algashop.product.catalog.application.utility.Mapper;
import com.algaworks.algashop.product.catalog.domain.model.category.Category;
import com.algaworks.algashop.product.catalog.domain.model.category.CategoryRepository;
import com.algaworks.algashop.product.catalog.domain.model.product.Product;
import com.algaworks.algashop.product.catalog.domain.model.product.ProductRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductManagementApplicationService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final Mapper mapper;

    @CachePut(cacheNames = "algashop:products:v1", key = "#result.id")
    public ProductDetailOutput create(ProductInput input) {
        Product product = mapToProduct(input);
        productRepository.save(product);
        return mapper.convert(product, ProductDetailOutput.class);
    }

    private Product mapToProduct(ProductInput input) {
        Category category = findCategory(input.getCategoryId());
        return Product.builder()
                .name(input.getName())
                .brand(input.getBrand())
                .description(input.getDescription())
                .regularPrice(input.getRegularPrice())
                .salePrice(input.getSalePrice())
                .enabled(input.getEnabled())
                .category(category)
                .build();
    }

    @CachePut(cacheNames = "algashop:products:v1", key = "#productId")
    public ProductDetailOutput update(UUID productId, ProductInput input) {
        Product product = findProduct(productId);
        Category category = findCategory(input.getCategoryId());

        updateProduct(input, product);
        product.setCategory(category);

        productRepository.save(product);

        return mapper.convert(product, ProductDetailOutput.class);

    }

    public void disable(UUID productId) {
        Product product = findProduct(productId);
        product.setEnabled(false);

        productRepository.save(product);
    }

    public void enable(UUID productId) {
        Product product = findProduct(productId);
        product.setEnabled(true);

        productRepository.save(product);
    }

    private static void updateProduct(ProductInput input, Product product) {
        product.setName(input.getName());
        product.setBrand(input.getBrand());
        product.setDescription(input.getDescription());
        product.changePrice(input.getRegularPrice(), input.getSalePrice());
        product.setEnabled(input.getEnabled());
    }

    private Product findProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    private Category findCategory(@NotNull UUID categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(ResourceNotFoundException::new);
    }
}