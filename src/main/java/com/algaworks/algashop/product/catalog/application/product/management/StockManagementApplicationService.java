package com.algaworks.algashop.product.catalog.application.product.management;

import com.algaworks.algashop.product.catalog.application.ResourceNotFoundException;
import com.algaworks.algashop.product.catalog.domain.model.DomainEventPublisher;
import com.algaworks.algashop.product.catalog.domain.model.product.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockManagementApplicationService {

    private final ProductRepository productRepository;
    private final QuantityInStockAdjustment quantityInStockAdjustment;
    private final StockMovementRepository stockMovementRepository;
    private final DomainEventPublisher domainEventPublisher;

    public StockMovement restock(@Valid UUID productId, @Valid StockInput input) {
        findProduct(productId);

        QuantityInStockAdjustment.Result result = quantityInStockAdjustment.increase(productId, input.getQuantity());

        if (result.inRestocked()) {
            domainEventPublisher.publish(
                    ProductRestockedEvent.builder().productId(productId).build()
            );
        }

        StockMovement movement = StockMovement.builder()
                .productId(productId)
                .movementQuantity(input.getQuantity())
                .previousQuantity(result.previousQuantity())
                .newQuantity(result.newQuantity())
                .type(StockMovement.MovementType.STOCK_IN)
                .build();

        stockMovementRepository.save(movement);
        return movement;
    }

    public StockMovement withdraw(UUID productId, @Valid StockInput input) {
        findProduct(productId);

        QuantityInStockAdjustment.Result result = quantityInStockAdjustment.decrease(productId, input.getQuantity());

        if (result.isOutOfStock()) {
            domainEventPublisher.publish(
                    ProductSoldOutEvent.builder().productId(productId).build()
            );
        }

        StockMovement movement = StockMovement.builder()
                .productId(productId)
                .movementQuantity(input.getQuantity())
                .previousQuantity(result.previousQuantity())
                .newQuantity(result.newQuantity())
                .type(StockMovement.MovementType.STOCK_OUT)
                .build();

        stockMovementRepository.save(movement);
        return movement;
    }

    private Product findProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(ResourceNotFoundException::new);
    }
}

