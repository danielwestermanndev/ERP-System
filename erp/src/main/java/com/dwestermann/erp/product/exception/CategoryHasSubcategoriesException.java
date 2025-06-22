package com.dwestermann.erp.product.exception;

import lombok.Getter;

/**
 * Exception thrown when trying to delete a category that has subcategories
 */
@Getter
public class CategoryHasSubcategoriesException extends CategoryException {
    private final Long categoryId;
    private final Long subcategoryCount;

    public CategoryHasSubcategoriesException(Long categoryId, Long subcategoryCount) {
        super("Cannot delete category " + categoryId + " because it has " + subcategoryCount + " subcategories");
        this.categoryId = categoryId;
        this.subcategoryCount = subcategoryCount;
    }

}