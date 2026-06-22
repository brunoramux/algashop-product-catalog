package com.algaworks.algashop.product.catalog.infrastructure.persistence.category;

import com.algaworks.algashop.product.catalog.application.PageModel;
import com.algaworks.algashop.product.catalog.application.ResourceNotFoundException;
import com.algaworks.algashop.product.catalog.application.category.query.CategoryDetailOutput;
import com.algaworks.algashop.product.catalog.application.category.query.CategoryFilter;
import com.algaworks.algashop.product.catalog.application.category.query.CategoryQueryService;
import com.algaworks.algashop.product.catalog.application.utility.Mapper;
import com.algaworks.algashop.product.catalog.domain.model.category.Category;
import com.algaworks.algashop.product.catalog.domain.model.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryQueryServiceImpl implements CategoryQueryService {

    private final CategoryRepository categoryRepository;
    private final Mapper mapper;
    private final MongoOperations mongoOperations;

    @Override
    public PageModel<CategoryDetailOutput> filter(CategoryFilter filter) {
        Query query = buildQuery(filter);

        Sort sort = Sort.by(filter.getSortDirectionOrDefault(),
                filter.getSortByPropertyOrDefault().getPropertyName());
        PageRequest pageRequest = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        long totalElements = mongoOperations.count(query, Category.class);

        query.with(pageRequest);
        List<Category> categories = mongoOperations.find(query, Category.class);

        List<CategoryDetailOutput> content = categories.stream()
                .map(c -> mapper.convert(c, CategoryDetailOutput.class))
                .toList();

        return PageModel.<CategoryDetailOutput>builder()
                .content(content)
                .number(filter.getPage())
                .size(filter.getSize())
                .totalElements(totalElements)
                .totalPages((int) Math.ceil((double) totalElements / filter.getSize()))
                .build();
    }

    private Query buildQuery(CategoryFilter filter) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (filter.getEnabled() != null) {
            criteriaList.add(Criteria.where("enabled").is(filter.getEnabled()));
        }

        if (StringUtils.isNotBlank(filter.getName())) {
            criteriaList.add(Criteria.where("name").regex(filter.getName(), "i"));
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        return query;
    }

    @Override
    public CategoryDetailOutput findById(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(ResourceNotFoundException::new);
        return mapper.convert(category, CategoryDetailOutput.class);
    }
}