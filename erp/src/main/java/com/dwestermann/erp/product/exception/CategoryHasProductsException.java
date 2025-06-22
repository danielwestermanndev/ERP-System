package com.dwestermann.erp.product.exception;

import lombok.Getter;

/**
 * Exception thrown when trying to delete a category that contains products
 */
@Getter
public class CategoryHasProductsException extends CategoryException {
    private final Long categoryId;
    private final Long productCount;

    public CategoryHasProductsException(Long categoryId, Long productCount) {
        super("Cannot delete category " + categoryId + " because it contains " + productCount + " products");
        this.categoryId = categoryId;
        this.productCount = productCount;
    }

}