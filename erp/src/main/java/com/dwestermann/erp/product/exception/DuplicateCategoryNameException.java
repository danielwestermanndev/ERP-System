package com.dwestermann.erp.product.exception;

/**
 * Exception thrown when trying to create a category with a name that already exists at the same level
 */
public class DuplicateCategoryNameException extends CategoryException {
    public DuplicateCategoryNameException(String message) {
        super(message);
    }

    public DuplicateCategoryNameException(String categoryName, Long parentCategoryId) {
        super("Category name '" + categoryName + "' already exists" +
                (parentCategoryId != null ? " under parent category ID: " + parentCategoryId : " at root level"));
    }
}