package com.algaworks.algashop.product.catalog.application.category.query;

import com.algaworks.algashop.product.catalog.application.utility.SortablePageFilter;
import lombok.*;
import org.springframework.data.domain.Sort;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CategoryFilter extends SortablePageFilter<CategoryFilter.SortType> {

    private String name;
    private Boolean enabled;

    @Override
    public SortType getSortByPropertyOrDefault() {
        return SortType.NAME;
    }

    @Override
    public Sort.Direction getSortDirectionOrDefault() {
        return Sort.Direction.ASC;
    }

    @Getter
    @RequiredArgsConstructor
    public enum SortType {
        NAME("name"),
        CREATED_AT("createdAt");

        private final String propertyName;
    }
}

