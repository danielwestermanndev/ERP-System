package com.dwestermann.erp.product.exception;

/**
 * Base exception for all category-related errors
 */
public class CategoryException extends RuntimeException {
    public CategoryException(String message) {
        super(message);
    }

    public CategoryException(String message, Throwable cause) {
        super(message, cause);
    }
}