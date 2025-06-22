package com.dwestermann.erp.product.exception;

/**
 * Exception thrown when a category operation would create a circular reference in the hierarchy
 */
public class CircularReferenceException extends CategoryException {
    public CircularReferenceException(String message) {
        super(message);
    }

    public CircularReferenceException(Long categoryId, Long parentCategoryId) {
        super("Moving category " + categoryId + " to parent " + parentCategoryId + " would create a circular reference");
    }
}