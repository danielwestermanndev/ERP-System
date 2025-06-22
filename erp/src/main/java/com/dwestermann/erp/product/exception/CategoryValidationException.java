package com.dwestermann.erp.product.exception;

import lombok.Getter;

import java.util.List;

/**
 * Exception thrown when category validation fails
 */
@Getter
public class CategoryValidationException extends CategoryException {
    private final List<String> validationErrors;

    public CategoryValidationException(String message) {
        super(message);
        this.validationErrors = List.of(message);
    }

    public CategoryValidationException(String message, List<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

}