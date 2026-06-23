package com.algaworks.algashop.product.catalog.application.product.management;

import com.algaworks.algashop.product.catalog.application.ResourceNotFoundException;
import com.algaworks.algashop.product.catalog.domain.model.DomainEventPublisher;
import com.algaworks.algashop.product.catalog.domain.model.category.Category;
import com.algaworks.algashop.product.catalog.domain.model.product.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockManagementApplicationService")
class StockManagementApplicationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private QuantityInStockAdjustment quantityInStockAdjustment;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private StockManagementApplicationService service;

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
    @DisplayName("should restock product and save stock movement")
    void shouldRestockProduct() {
        StockInput input = StockInput.builder().quantity(10).build();
        QuantityInStockAdjustment.Result result =
                new QuantityInStockAdjustment.Result(productId, 0, 10);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(quantityInStockAdjustment.increase(productId, 10)).thenReturn(result);

        StockMovement movement = service.restock(productId, input);

        assertThat(movement).isNotNull();
        assertThat(movement.getMovementQuantity()).isEqualTo(10);
        assertThat(movement.getPreviousQuantity()).isEqualTo(0);
        assertThat(movement.getNewQuantity()).isEqualTo(10);
        assertThat(movement.getType()).isEqualTo(StockMovement.MovementType.STOCK_IN);

        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("should publish ProductRestockedEvent when restocking from zero")
    void shouldPublishProductRestockedEventWhenRestockingFromZero() {
        StockInput input = StockInput.builder().quantity(5).build();
        QuantityInStockAdjustment.Result result =
                new QuantityInStockAdjustment.Result(productId, 0, 5);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(quantityInStockAdjustment.increase(productId, 5)).thenReturn(result);

        service.restock(productId, input);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(domainEventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(ProductRestockedEvent.class);
    }

    @Test
    @DisplayName("should not publish ProductRestockedEvent when already in stock")
    void shouldNotPublishRestockedEventWhenAlreadyInStock() {
        StockInput input = StockInput.builder().quantity(5).build();
        QuantityInStockAdjustment.Result result =
                new QuantityInStockAdjustment.Result(productId, 3, 8); // was already in stock

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(quantityInStockAdjustment.increase(productId, 5)).thenReturn(result);

        service.restock(productId, input);

        verify(domainEventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("should withdraw from stock and save movement")
    void shouldWithdrawFromStock() {
        StockInput input = StockInput.builder().quantity(3).build();
        QuantityInStockAdjustment.Result result =
                new QuantityInStockAdjustment.Result(productId, 10, 7);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(quantityInStockAdjustment.decrease(productId, 3)).thenReturn(result);

        StockMovement movement = service.withdraw(productId, input);

        assertThat(movement.getMovementQuantity()).isEqualTo(3);
        assertThat(movement.getPreviousQuantity()).isEqualTo(10);
        assertThat(movement.getNewQuantity()).isEqualTo(7);
        assertThat(movement.getType()).isEqualTo(StockMovement.MovementType.STOCK_OUT);

        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("should publish ProductSoldOutEvent when stock reaches zero")
    void shouldPublishSoldOutEventWhenStockReachesZero() {
        StockInput input = StockInput.builder().quantity(5).build();
        QuantityInStockAdjustment.Result result =
                new QuantityInStockAdjustment.Result(productId, 5, 0);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(quantityInStockAdjustment.decrease(productId, 5)).thenReturn(result);

        service.withdraw(productId, input);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(domainEventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(ProductSoldOutEvent.class);
    }

    @Test
    @DisplayName("should throw when product not found on restock")
    void shouldThrowWhenProductNotFoundOnRestock() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.restock(productId, StockInput.builder().quantity(1).build()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(quantityInStockAdjustment, never()).increase(any(), anyInt());
    }

    @Test
    @DisplayName("should throw when product not found on withdraw")
    void shouldThrowWhenProductNotFoundOnWithdraw() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.withdraw(productId, StockInput.builder().quantity(1).build()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(quantityInStockAdjustment, never()).decrease(any(), anyInt());
    }
}

