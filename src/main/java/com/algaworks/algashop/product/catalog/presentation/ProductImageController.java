package com.algaworks.algashop.product.catalog.presentation;

import com.algaworks.algashop.product.catalog.application.product.management.ImageInput;
import com.algaworks.algashop.product.catalog.application.product.management.ProductImageApplicationService;
import com.algaworks.algashop.product.catalog.application.product.query.ImageOutput;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageApplicationService productImageApplicationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ImageOutput addImage(@PathVariable UUID productId,
                                @RequestBody @Valid ImageInput input) {
        return productImageApplicationService.addImage(productId, input);
    }

    @GetMapping
    public List<ImageOutput> listImages(@PathVariable UUID productId) {
        return productImageApplicationService.listImages(productId);
    }

    @GetMapping("/{imageId}")
    public ImageOutput findImage(@PathVariable UUID productId,
                                 @PathVariable UUID imageId) {
        return productImageApplicationService.findImage(productId, imageId);
    }

    @DeleteMapping("/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeImage(@PathVariable UUID productId,
                            @PathVariable UUID imageId) {
        productImageApplicationService.removeImage(productId, imageId);
    }

    @PutMapping("/{imageId}/primary")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setMainImage(@PathVariable UUID productId,
                             @PathVariable UUID imageId) {
        productImageApplicationService.setMainImage(productId, imageId);
    }
}

