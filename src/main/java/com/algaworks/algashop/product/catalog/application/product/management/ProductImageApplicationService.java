package com.algaworks.algashop.product.catalog.application.product.management;

import com.algaworks.algashop.product.catalog.application.ResourceNotFoundException;
import com.algaworks.algashop.product.catalog.application.product.query.ImageOutput;
import com.algaworks.algashop.product.catalog.application.storage.StorageProvider;
import com.algaworks.algashop.product.catalog.application.utility.Mapper;
import com.algaworks.algashop.product.catalog.domain.model.DomainException;
import com.algaworks.algashop.product.catalog.domain.model.product.Image;
import com.algaworks.algashop.product.catalog.domain.model.product.Product;
import com.algaworks.algashop.product.catalog.domain.model.product.ProductNotFoundException;
import com.algaworks.algashop.product.catalog.domain.model.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductImageApplicationService {

    private final ProductRepository productRepository;
    private final StorageProvider storageProvider;
    private final Mapper mapper;

    public ImageOutput create(UUID productId, ImageInput input){
        Objects.requireNonNull(productId);
        Objects.requireNonNull(input);

        Product product = productRepository.findById(productId)
                .orElseThrow(ResourceNotFoundException::new);

        if(!storageProvider.fileExists(input.getRemoteFileName())){
            throw new DomainException("File not found in storage: " + input.getRemoteFileName());
        }

        if(productRepository.existsByImagesName(input.getRemoteFileName())){
            throw new DomainException("Image already exists: " + input.getRemoteFileName());
        }

        UUID imageId = product.addImage(input.getRemoteFileName());
        productRepository.save(product);

        Image image = product.getImage(imageId).orElseThrow();

        return mapper.convert(image, ImageOutput.class);
    }

    public void delete(UUID productId, UUID imageId){
        Objects.requireNonNull(productId);
        Objects.requireNonNull(imageId);

        Product product = productRepository.findById(productId)
                .orElseThrow(ResourceNotFoundException::new);

        Image image = product.getImage(imageId)
                .orElseThrow(ResourceNotFoundException::new);

        product.removeImage(imageId);
        productRepository.save(product);

        storageProvider.deleteFile(image.getName());
    }

    public void primary(UUID productId, UUID imageId) {
        Objects.requireNonNull(productId);
        Objects.requireNonNull(imageId);

        Product product = findById(productId);
        product.changeMainImage(imageId);
        productRepository.save(product);
    }

    private Product findById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private Image findImage(Product product, UUID imageId) {
        return product.getImage(imageId)
                .orElseThrow(() ->
                        new DomainException(String.format("Image of id %s was not found on product %s",
                                product.getId(), imageId)));
    }
}
