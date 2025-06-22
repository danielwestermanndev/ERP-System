package com.dwestermann.erp.product.exception;

/**
 * Base exception for all product-related errors
 */
public class ProductException extends RuntimeException {
    public ProductException(String message) {
        super(message);
    }

    public ProductException(String message, Throwable cause) {
        super(message, cause);
    }
}