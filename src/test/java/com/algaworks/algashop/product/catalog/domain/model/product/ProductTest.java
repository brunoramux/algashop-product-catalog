package com.algaworks.algashop.product.catalog.domain.model.product;

import com.algaworks.algashop.product.catalog.domain.model.DomainException;
import com.algaworks.algashop.product.catalog.domain.model.category.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Product domain model")
class ProductTest {

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category("Electronics", true);
    }

    private Product buildProduct() {
        return Product.builder()
                .name("Notebook X11")
                .brand("TechBrand")
                .description("A great notebook")
                .regularPrice(new BigDecimal("1500.00"))
                .salePrice(new BigDecimal("1200.00"))
                .enabled(true)
                .category(category)
                .build();
    }

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should register ProductAddedEvent when created")
        void shouldRegisterProductAddedEventWhenCreated() {
            Product product = buildProduct();
            assertThat(product.getPendingEvents())
                    .hasSize(1)
                    .first().isInstanceOf(ProductAddedEvent.class);
        }

        @Test
        @DisplayName("should generate slug from name")
        void shouldGenerateSlugFromName() {
            Product product = buildProduct();
            assertThat(product.getSlug()).isEqualTo("notebook-x11");
        }

        @Test
        @DisplayName("should initialize with zero quantity in stock")
        void shouldInitializeWithZeroQuantityInStock() {
            Product product = buildProduct();
            assertThat(product.getQuantityInStock()).isZero();
            assertThat(product.isInStock()).isFalse();
        }

        @Test
        @DisplayName("should throw when name is blank")
        void shouldThrowWhenNameIsBlank() {
            assertThatThrownBy(() -> Product.builder()
                    .name("   ")
                    .brand("Brand")
                    .regularPrice(BigDecimal.TEN)
                    .salePrice(BigDecimal.TEN)
                    .enabled(true)
                    .category(category)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw when category is null")
        void shouldThrowWhenCategoryIsNull() {
            assertThatThrownBy(() -> Product.builder()
                    .name("Product")
                    .brand("Brand")
                    .regularPrice(BigDecimal.TEN)
                    .salePrice(BigDecimal.TEN)
                    .enabled(true)
                    .category(null)
                    .build())
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should calculate discount percentage on creation")
        void shouldCalculateDiscountPercentage() {
            Product product = buildProduct(); // regular=1500, sale=1200 → 20%
            assertThat(product.getDiscountPercentageRounded()).isEqualTo(20);
            assertThat(product.getHasDiscount()).isTrue();
        }

        @Test
        @DisplayName("should have no discount when prices are equal")
        void shouldHaveNoDiscountWhenPricesAreEqual() {
            Product product = Product.builder()
                    .name("Product")
                    .brand("Brand")
                    .regularPrice(new BigDecimal("100.00"))
                    .salePrice(new BigDecimal("100.00"))
                    .enabled(true)
                    .category(category)
                    .build();
            assertThat(product.getHasDiscount()).isFalse();
            assertThat(product.getDiscountPercentageRounded()).isZero();
        }
    }

    @Nested
    @DisplayName("Name and slug")
    class NameAndSlug {

        @Test
        @DisplayName("should update slug when name changes")
        void shouldUpdateSlugWhenNameChanges() {
            Product product = buildProduct();
            product.setName("New Notebook Y22");
            assertThat(product.getName()).isEqualTo("New Notebook Y22");
            assertThat(product.getSlug()).isEqualTo("new-notebook-y22");
        }

        @Test
        @DisplayName("should throw when setting blank name")
        void shouldThrowWhenSettingBlankName() {
            Product product = buildProduct();
            assertThatThrownBy(() -> product.setName("  "))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Enable/Disable")
    class EnableDisable {

        @Test
        @DisplayName("should fire ProductDelistedEvent when disabled from enabled state")
        void shouldFireProductDelistedEventWhenDisabled() {
            Product product = buildProduct();
            product.resetEvents();

            product.disable();

            assertThat(product.getPendingEvents())
                    .anyMatch(e -> e instanceof ProductDelistedEvent);
        }

        @Test
        @DisplayName("should fire ProductListedEvent when enabled from disabled state")
        void shouldFireProductListedEventWhenEnabled() {
            Product product = buildProduct();
            product.resetEvents();
            product.disable();
            product.resetEvents();

            product.enable();

            assertThat(product.getPendingEvents())
                    .anyMatch(e -> e instanceof ProductListedEvent);
        }

        @Test
        @DisplayName("should not fire event when enabled state does not change")
        void shouldNotFireEventWhenStateUnchanged() {
            Product product = buildProduct(); // already enabled
            product.resetEvents();

            product.setEnabled(true);

            assertThat(product.getPendingEvents()).isEmpty();
        }

        @Test
        @DisplayName("should throw when enabled is null")
        void shouldThrowWhenEnabledIsNull() {
            Product product = buildProduct();
            assertThatThrownBy(() -> product.setEnabled(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Price management")
    class PriceManagement {

        @Test
        @DisplayName("should throw when sale price is greater than regular price")
        void shouldThrowWhenSalePriceGreaterThanRegular() {
            Product product = buildProduct();
            assertThatThrownBy(() -> product.changePrice(
                    new BigDecimal("100.00"),
                    new BigDecimal("200.00")))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Sale price cannot be greater than regular price");
        }

        @Test
        @DisplayName("should fire ProductPriceChangedEvent when prices change")
        void shouldFirePriceChangedEvent() {
            Product product = buildProduct();
            product.resetEvents();

            product.changePrice(new BigDecimal("2000.00"), new BigDecimal("1800.00"));

            assertThat(product.getPendingEvents())
                    .anyMatch(e -> e instanceof ProductPriceChangedEvent);
        }

        @Test
        @DisplayName("should fire ProductPlacedOnSaleEvent when newly on sale")
        void shouldFirePlacedOnSaleEventWhenNewlyOnSale() {
            Product product = Product.builder()
                    .name("Product")
                    .brand("Brand")
                    .regularPrice(new BigDecimal("100.00"))
                    .salePrice(new BigDecimal("100.00"))
                    .enabled(true)
                    .category(category)
                    .build();
            product.resetEvents();

            product.changePrice(new BigDecimal("100.00"), new BigDecimal("80.00"));

            assertThat(product.getPendingEvents())
                    .anyMatch(e -> e instanceof ProductPlacedOnSaleEvent);
        }

        @Test
        @DisplayName("should not fire event when prices did not change")
        void shouldNotFireEventWhenPricesUnchanged() {
            Product product = buildProduct();
            product.resetEvents();

            product.changePrice(new BigDecimal("1500.00"), new BigDecimal("1200.00"));

            assertThat(product.getPendingEvents()).isEmpty();
        }

        @Test
        @DisplayName("should throw when regular price is negative")
        void shouldThrowWhenRegularPriceIsNegative() {
            Product product = buildProduct();
            assertThatThrownBy(() -> product.changePrice(
                    new BigDecimal("-1.00"),
                    new BigDecimal("-1.00")))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Image management")
    class ImageManagement {

        @Test
        @DisplayName("should add image and set as main image if first")
        void shouldAddImageAndSetAsMainImageIfFirst() {
            Product product = buildProduct();
            assertThat(product.getMainImage()).isNull();

            UUID imageId = product.addImage("image.jpg");

            assertThat(imageId).isNotNull();
            assertThat(product.getMainImage()).isNotNull();
            assertThat(product.getMainImage().getName()).isEqualTo("image.jpg");
            assertThat(product.getImages()).hasSize(1);
        }

        @Test
        @DisplayName("should not change main image when adding subsequent images")
        void shouldNotChangeMainImageWhenAddingSubsequentImages() {
            Product product = buildProduct();
            UUID firstId = product.addImage("first.jpg");
            product.addImage("second.jpg");

            assertThat(product.getMainImage().getId()).isEqualTo(firstId);
            assertThat(product.getImages()).hasSize(2);
        }

        @Test
        @DisplayName("should remove image")
        void shouldRemoveImage() {
            Product product = buildProduct();
            UUID imageId = product.addImage("image.jpg");

            product.removeImage(imageId);

            assertThat(product.getImages()).isEmpty();
            assertThat(product.getMainImage()).isNull();
        }

        @Test
        @DisplayName("should set next image as main when main is removed")
        void shouldSetNextImageAsMainWhenMainIsRemoved() {
            Product product = buildProduct();
            UUID firstId = product.addImage("first.jpg");
            UUID secondId = product.addImage("second.jpg");

            product.removeImage(firstId);

            assertThat(product.getMainImage().getId()).isEqualTo(secondId);
        }

        @Test
        @DisplayName("should change main image")
        void shouldChangeMainImage() {
            Product product = buildProduct();
            product.addImage("first.jpg");
            UUID secondId = product.addImage("second.jpg");

            product.changeMainImage(secondId);

            assertThat(product.getMainImage().getId()).isEqualTo(secondId);
        }

        @Test
        @DisplayName("should throw when removing non-existent image")
        void shouldThrowWhenRemovingNonExistentImage() {
            Product product = buildProduct();
            assertThatThrownBy(() -> product.removeImage(UUID.randomUUID()))
                    .isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("should throw when changing to non-existent main image")
        void shouldThrowWhenChangingToNonExistentMainImage() {
            Product product = buildProduct();
            assertThatThrownBy(() -> product.changeMainImage(UUID.randomUUID()))
                    .isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("should find image by id")
        void shouldFindImageById() {
            Product product = buildProduct();
            UUID imageId = product.addImage("image.jpg");

            assertThat(product.getImage(imageId)).isPresent();
        }

        @Test
        @DisplayName("should return empty when image not found by id")
        void shouldReturnEmptyWhenImageNotFoundById() {
            Product product = buildProduct();
            assertThat(product.getImage(UUID.randomUUID())).isEmpty();
        }
    }
}

