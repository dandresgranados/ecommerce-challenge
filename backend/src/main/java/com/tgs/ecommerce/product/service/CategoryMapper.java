package com.tgs.ecommerce.product.service;

import com.tgs.ecommerce.product.domain.Category;
import com.tgs.ecommerce.product.dto.CategoryResponse;

/**
 * Conversiones Category ↔ CategoryResponse.
 */
public final class CategoryMapper {

    private CategoryMapper() {}

    public static CategoryResponse toResponse(Category c) {
        return new CategoryResponse(
            c.getId(),
            c.getName(),
            c.getDescription(),
            c.getCreatedAt(),
            c.getUpdatedAt()
        );
    }
}
