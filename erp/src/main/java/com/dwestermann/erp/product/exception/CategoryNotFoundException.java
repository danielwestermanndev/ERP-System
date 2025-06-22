package com.dwestermann.erp.product.exception;

/**
 * Exception thrown when a requested category cannot be found
 */
public class CategoryNotFoundException extends CategoryException {
    public CategoryNotFoundException(String message) {
        super(message);
    }

    public CategoryNotFoundException(Long categoryId) {
        super("Category not found with ID: " + categoryId);
    }
}