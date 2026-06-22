package com.algaworks.algashop.product.catalog.presentation;

import com.algaworks.algashop.product.catalog.application.product.management.ImageInput;
import com.algaworks.algashop.product.catalog.application.product.management.ProductImageApplicationService;
import com.algaworks.algashop.product.catalog.application.product.query.ImageOutput;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageApplicationService productImageApplicationService;

    /**
     * Registra uma imagem que já foi enviada ao S3 via presigned URL.
     * O campo remoteFileName deve ser o nome do arquivo retornado pelo endpoint /upload-requests.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ImageOutput addImage(@PathVariable UUID productId,
                                @RequestBody @Valid ImageInput input) {
        return productImageApplicationService.addImage(productId, input);
    }

    /**
     * Faz o upload direto da imagem para o S3 e já associa ao produto.
     * Aceita multipart/form-data com o campo "file".
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImageOutput uploadImage(@PathVariable UUID productId,
                                   @RequestParam("file") MultipartFile file) throws IOException {
        return productImageApplicationService.uploadAndAddImage(
                productId,
                file.getOriginalFilename(),
                file.getInputStream(),
                file.getSize(),
                file.getContentType()
        );
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

