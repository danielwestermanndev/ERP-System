package com.dwestermann.erp.product.exception;

import lombok.Getter;

import java.util.List;

/**
 * Exception thrown during category hierarchy import operations
 */
@Getter
public class CategoryImportException extends CategoryException {
    private final List<String> importErrors;
    private final Integer successfulImports;
    private final Integer failedImports;

    public CategoryImportException(String message, List<String> importErrors,
                                   Integer successfulImports, Integer failedImports) {
        super(message);
        this.importErrors = importErrors;
        this.successfulImports = successfulImports;
        this.failedImports = failedImports;
    }

}