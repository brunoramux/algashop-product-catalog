package com.algaworks.algashop.product.catalog.application.product.management;

import com.algaworks.algashop.product.catalog.application.ResourceNotFoundException;
import com.algaworks.algashop.product.catalog.domain.model.category.Category;
import com.algaworks.algashop.product.catalog.domain.model.category.CategoryRepository;
import com.algaworks.algashop.product.catalog.domain.model.product.Product;
import com.algaworks.algashop.product.catalog.domain.model.product.ProductRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductManagementApplicationService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public UUID create(ProductInput input) {
        Product product = mapToProduct(input);
        productRepository.save(product);
        return product.getId();
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

    public void update(UUID productId, ProductInput input) {
        Product product = findProduct(productId);
        Category category = findCategory(input.getCategoryId());

        updateProduct(input, product);

        productRepository.save(product);

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