package com.algaworks.algashop.product.catalog.infrastructure.persistence.product;

import com.algaworks.algashop.product.catalog.application.ResourceNotFoundException;
import com.algaworks.algashop.product.catalog.domain.model.DomainException;
import com.algaworks.algashop.product.catalog.domain.model.product.Product;
import com.algaworks.algashop.product.catalog.domain.model.product.QuantityInStockAdjustment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuantityInStockAdjustmentImpl implements QuantityInStockAdjustment {

    private final MongoOperations mongoOperations;

    @Override
    public Result increase(UUID productId, int quantity) {
        Product before = findAndModify(productId, quantity);
        int previousQuantity = before.getQuantityInStock();
        int newQuantity = previousQuantity + quantity;
        return new Result(productId, previousQuantity, newQuantity);
    }

    @Override
    public Result decrease(UUID productId, int quantity) {
        Query query = Query.query(
                Criteria.where("_id").is(productId)
                        .and("quantityInStock").gte(quantity)
        );
        Product before = mongoOperations.findAndModify(
                query,
                new Update().inc("quantityInStock", -quantity),
                FindAndModifyOptions.options().returnNew(false),
                Product.class
        );
        if (before == null) {
            Product product = mongoOperations.findById(productId, Product.class);
            if (product == null) {
                throw new ResourceNotFoundException();
            }
            throw new DomainException(
                    String.format("Insufficient stock for product %s. Available: %d, requested: %d",
                            productId, product.getQuantityInStock(), quantity)
            );
        }
        int previousQuantity = before.getQuantityInStock();
        int newQuantity = previousQuantity - quantity;
        return new Result(productId, previousQuantity, newQuantity);
    }

    private Product findAndModify(UUID productId, int delta) {
        Product before = mongoOperations.findAndModify(
                Query.query(Criteria.where("_id").is(productId)),
                new Update().inc("quantityInStock", delta),
                FindAndModifyOptions.options().returnNew(false),
                Product.class
        );
        if (before == null) {
            throw new ResourceNotFoundException();
        }
        return before;
    }
}

