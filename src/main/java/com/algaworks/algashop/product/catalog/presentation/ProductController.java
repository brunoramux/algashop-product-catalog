package com.algaworks.algashop.product.catalog.presentation;

import com.algaworks.algashop.product.catalog.application.PageModel;
import com.algaworks.algashop.product.catalog.application.product.management.ProductInput;
import com.algaworks.algashop.product.catalog.application.product.management.ProductManagementApplicationService;
import com.algaworks.algashop.product.catalog.application.product.query.ProductDetailOutput;
import com.algaworks.algashop.product.catalog.application.product.query.ProductFilter;
import com.algaworks.algashop.product.catalog.application.product.query.ProductQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductManagementApplicationService productManagementApplicationService;
    private final ProductQueryService productQueryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDetailOutput create(@RequestBody ProductInput input) {

        UUID productId = productManagementApplicationService.create(input);
        return productQueryService.findById(productId);

    }

    @GetMapping("/{productId}")
    public ProductDetailOutput findById(@PathVariable UUID productId) {
        return productQueryService.findById(productId);
    }

    @PutMapping("/{productId}")
    public ProductDetailOutput update(@PathVariable UUID productId, @RequestBody ProductInput input) {
        productManagementApplicationService.update(productId, input);

        return productQueryService.findById(productId);
    }

    @PutMapping("/{productId}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable(@PathVariable UUID productId, @RequestBody ProductInput input) {
        productManagementApplicationService.disable(productId);
    }

    @DeleteMapping("/{productId}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable UUID productId) {
        productManagementApplicationService.disable(productId);
    }

    @GetMapping
    public PageModel<ProductDetailOutput> filter(ProductFilter filter) {
        return productQueryService.filter(filter);
    }
}
