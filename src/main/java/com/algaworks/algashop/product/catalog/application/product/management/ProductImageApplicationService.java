package com.algaworks.algashop.product.catalog.application.product.management;

import com.algaworks.algashop.product.catalog.application.ResourceNotFoundException;
import com.algaworks.algashop.product.catalog.application.product.query.ImageOutput;
import com.algaworks.algashop.product.catalog.application.upload.ProductImageStorageService;
import com.algaworks.algashop.product.catalog.domain.model.product.Image;
import com.algaworks.algashop.product.catalog.domain.model.product.Product;
import com.algaworks.algashop.product.catalog.domain.model.product.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductImageApplicationService {

    private final ProductRepository productRepository;
    private final ProductImageStorageService productImageStorageService;

    public ImageOutput addImage(UUID productId, @Valid ImageInput input) {
        Product product = findProduct(productId);
        UUID imageId = product.addImage(input.getRemoteFileName());
        productRepository.save(product);
        return buildImageOutput(imageId, input.getRemoteFileName());
    }

    public ImageOutput uploadAndAddImage(UUID productId, String originalFileName,
                                         InputStream content, long contentLength, String contentType) {
        Product product = findProduct(productId);
        String remoteFileName = productImageStorageService.uploadImage(
                originalFileName, content, contentLength, contentType);
        UUID imageId = product.addImage(remoteFileName);
        productRepository.save(product);
        return buildImageOutput(imageId, remoteFileName);
    }

    public List<ImageOutput> listImages(UUID productId) {
        Product product = findProduct(productId);
        return product.getImages().stream()
                .map(image -> buildImageOutput(image.getId(), image.getName()))
                .toList();
    }

    public ImageOutput findImage(UUID productId, UUID imageId) {
        Product product = findProduct(productId);
        Image image = product.getImage(imageId)
                .orElseThrow(ResourceNotFoundException::new);
        return buildImageOutput(image.getId(), image.getName());
    }

    public void removeImage(UUID productId, UUID imageId) {
        Product product = findProduct(productId);
        product.removeImage(imageId);
        productRepository.save(product);
    }

    public void setMainImage(UUID productId, UUID imageId) {
        Product product = findProduct(productId);
        product.changeMainImage(imageId);
        productRepository.save(product);
    }

    private Product findProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    private ImageOutput buildImageOutput(UUID imageId, String imageName) {
        String url = productImageStorageService.buildImageUrl(imageName);
        return ImageOutput.builder()
                .id(imageId)
                .url(url)
                .build();
    }
}
