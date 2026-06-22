package com.algaworks.algashop.product.catalog.infrastructure.persistence.product;

import com.algaworks.algashop.product.catalog.application.PageModel;
import com.algaworks.algashop.product.catalog.application.ResourceNotFoundException;
import com.algaworks.algashop.product.catalog.application.product.query.ImageOutput;
import com.algaworks.algashop.product.catalog.application.product.query.ProductDetailOutput;
import com.algaworks.algashop.product.catalog.application.product.query.ProductFilter;
import com.algaworks.algashop.product.catalog.application.product.query.ProductQueryService;
import com.algaworks.algashop.product.catalog.application.upload.ProductImageStorageService;
import com.algaworks.algashop.product.catalog.application.utility.Mapper;
import com.algaworks.algashop.product.catalog.domain.model.product.Image;
import com.algaworks.algashop.product.catalog.domain.model.product.Product;
import com.algaworks.algashop.product.catalog.domain.model.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductRepository productRepository;
    private final Mapper mapper;
    private final MongoOperations mongoOperations;
    private final ProductImageStorageService productImageStorageService;

    private static final String findWordRegex = "(?i)%s";

    @Override
    public ProductDetailOutput findById(UUID productId) {
        Product product = productRepository.findById(productId).orElseThrow(ResourceNotFoundException::new);
        ProductDetailOutput output = mapper.convert(product, ProductDetailOutput.class);
        resolveImageUrls(output, product);
        return output;
    }

    @Override
    public PageModel<ProductDetailOutput> filter(ProductFilter filter) {
        Optional<Criteria> criteria = buildCriteria(filter);
        Optional<TextCriteria> textCriteria = buildTextCriteria(filter);

        Query query = new Query();
        textCriteria.ifPresent(query::addCriteria);
        criteria.ifPresent(query::addCriteria);

        long totalElements = mongoOperations.count(query, Product.class);

        List<AggregationOperation> operations = new ArrayList<>();

        textCriteria.ifPresent(c -> operations.add(Aggregation.match(c)));
        criteria.ifPresent(c -> operations.add(Aggregation.match(c)));

        PageRequest pageRequest = PageRequest.of(filter.getPage(), filter.getSize());

        operations.addAll(Arrays.asList(
                Aggregation.lookup("categories", "categoryId", "_id", "category"),
                Aggregation.unwind("$category"),
                Aggregation.sort(sortWith(filter)),
                projectionForSummary(),
                Aggregation.skip(pageRequest.getOffset()),
                Aggregation.limit(filter.getSize())
        ));

        Aggregation aggregation = Aggregation.newAggregation(operations);

        List<ProductDetailOutput> productSummaryOutputs = mongoOperations
                .aggregate(aggregation, Product.class, ProductDetailOutput.class)
                .getMappedResults();

        return PageModel.<ProductDetailOutput>builder()
                .content(productSummaryOutputs)
                .number(filter.getPage())
                .size(filter.getSize())
                .totalElements(totalElements)
                .totalPages((int) Math.ceil((double) totalElements / filter.getSize()))
                .build();

    }

    private void resolveImageUrls(ProductDetailOutput output, Product product) {
        if (product.getMainImage() != null) {
            output.setMainImage(toImageOutput(product.getMainImage()));
        }
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            output.setImages(product.getImages().stream()
                    .map(this::toImageOutput)
                    .collect(Collectors.toSet()));
        }
    }

    private ImageOutput toImageOutput(Image image) {
        return ImageOutput.builder()
                .id(image.getId())
                .url(productImageStorageService.buildImageUrl(image.getName()))
                .build();
    }

    private ProjectionOperation projectionForSummary() {
        return Aggregation.project()
                .and("_id").as("_id")
                .and("addedAt").as("addedAt")
                .and("name").as("name")
                .and("brand").as("brand")
                .and("regularPrice").as("regularPrice")
                .and("salePrice").as("salePrice")
                .and("enabled").as("enabled")
                .and("quantityInStock").as("quantityInStock")
                .and("discountPercentageRounded").as("discountPercentageRounded")
                .and("score").as("score")
                .and("category._id").as("category._id")
                .and("category.name").as("category.name")
                .and("description").as("description")
                .and("slug").as("slug")
                .and("version").as("version")
                .and("updatedAt").as("updatedAt")
                .andExpression("salePrice < regularPrice").as("hasDiscount")
                .andExpression("quantityInStock > 0").as("inStock");

    }

    private Optional<TextCriteria> buildTextCriteria(ProductFilter filter) {
        if (StringUtils.isNotBlank(filter.getTerm())) {
            return Optional.of(TextCriteria.forDefaultLanguage().matching(filter.getTerm()));
        }
        return Optional.empty();
    }

    private Optional<Criteria> buildCriteria(ProductFilter filter) {
        List<CriteriaDefinition> criteriaList = new ArrayList<>();

        if (filter.getEnabled() != null) {
            criteriaList.add(Criteria.where("enabled").is(filter.getEnabled()));
        }

        if(filter.getAddedAtFrom()  != null && filter.getAddedAtTo() != null) {
            criteriaList.add(Criteria.where("addedAt")
                    .gte(filter.getAddedAtFrom())
                    .lte(filter.getAddedAtTo())
            );
        } else {
            if(filter.getAddedAtTo()  != null) {
                criteriaList.add(Criteria.where("addedAt").lte(filter.getAddedAtTo()));
            } else if(filter.getAddedAtFrom() != null) {
                criteriaList.add(Criteria.where("addedAt")
                        .gte(filter.getAddedAtFrom())
                );
            }
        }

        if(filter.getPriceFrom() != null && filter.getPriceTo() != null) {
            criteriaList.add(Criteria.where("salePrice")
                    .gte(filter.getPriceFrom())
                    .lte(filter.getPriceTo())
            );
        } else {
            if(filter.getPriceFrom() != null) {
                criteriaList.add(Criteria.where("salePrice")
                        .gte(filter.getPriceFrom()));
            } else if(filter.getPriceTo() != null) {
                criteriaList.add(Criteria.where("salePrice")
                        .lte(filter.getPriceTo())
                );
            }
        }

        if(filter.getHasDiscount() != null) {
            if(filter.getHasDiscount()) {
                criteriaList.add(AggregationExpressionCriteria.whereExpr(
                        ComparisonOperators.valueOf("$salePrice")
                                .lessThan("$regularPrice")
                ));
            } else {
                criteriaList.add(AggregationExpressionCriteria.whereExpr(
                        ComparisonOperators.valueOf("$salePrice")
                                .equalTo("$regularPrice")
                ));
            }
        }

        if (filter.getInStock() != null) {
            if (filter.getInStock()) {
                criteriaList.add(Criteria.where("quantityInStock").gt(0));
            } else {
                criteriaList.add(Criteria.where("quantityInStock").is(0));
            }
        }

        if (filter.getCategoriesId() != null && filter.getCategoriesId().length > 0) {
            criteriaList.add(Criteria.where("categoryId").in(
                    (Object[]) filter.getCategoriesId()
            ));
        }

        if(criteriaList.isEmpty()){
            return Optional.empty();
        }

        return Optional.of(
                new Criteria().andOperator(criteriaList.toArray(new Criteria[0]))
        );
    }

    private Sort sortWith(ProductFilter filter) {
        // CASO FILTRO TEXTUAL, UTILIZA SCORE PARA CALCULAR PESOS DE CADA UM DOS CAMPO DE FILTRO
        if(StringUtils.isNotBlank(filter.getTerm())){
            return Sort.by("score");
        }
        return Sort.by(filter.getSortDirectionOrDefault(),
                filter.getSortByPropertyOrDefault().getPropertyName());
    }
}
