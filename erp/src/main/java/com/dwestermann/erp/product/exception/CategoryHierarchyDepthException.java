package com.dwestermann.erp.product.exception;

import lombok.Getter;

/**
 * Exception thrown when category hierarchy exceeds maximum allowed depth
 */
@Getter
public class CategoryHierarchyDepthException extends CategoryException {
    private final Integer currentDepth;
    private final Integer maxAllowedDepth;

    public CategoryHierarchyDepthException(Integer currentDepth, Integer maxAllowedDepth) {
        super("Category hierarchy depth " + currentDepth + " exceeds maximum allowed depth of " + maxAllowedDepth);
        this.currentDepth = currentDepth;
        this.maxAllowedDepth = maxAllowedDepth;
    }

}