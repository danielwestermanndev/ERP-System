package com.dwestermann.erp.product.service;

import com.dwestermann.erp.product.domain.ProductCategory;
import com.dwestermann.erp.product.dto.request.CreateCategoryRequest;
import com.dwestermann.erp.product.dto.request.UpdateCategoryRequest;
import com.dwestermann.erp.product.dto.response.CategoryResponse;
import com.dwestermann.erp.product.dto.response.CategoryStatistics;
import com.dwestermann.erp.product.dto.response.CategorySystemStatistics;
import com.dwestermann.erp.product.dto.response.CategoryTreeResponse;
import com.dwestermann.erp.product.dto.result.BulkCategoryOperationResult;
import com.dwestermann.erp.product.dto.request.CategoryHierarchyData;
import com.dwestermann.erp.product.dto.result.CategoryImportResult;
import com.dwestermann.erp.product.dto.result.CategoryValidationResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing product categories.
 * Provides operations for CRUD, hierarchy management, and category analytics.
 */
public interface ProductCategoryService {

    // ==================== CRUD OPERATIONS ====================

    /**
     * Creates a new product category
     * @param request Category creation request
     * @return Created category response
     */
    CategoryResponse createCategory(CreateCategoryRequest request);

    /**
     * Updates an existing product category
     * @param categoryId Category UUID to update
     * @param request Category update request
     * @return Updated category response
     */
    CategoryResponse updateCategory(UUID categoryId, UpdateCategoryRequest request);

    /**
     * Deletes a product category
     * @param categoryId Category UUID to delete
     */
    void deleteCategory(UUID categoryId);

    /**
     * Retrieves a category by ID
     * @param categoryId Category UUID
     * @return Category response
     */
    CategoryResponse getCategoryById(UUID categoryId);

    /**
     * Retrieves a category entity by ID (for internal use)
     * @param categoryId Category UUID
     * @return Category entity
     */
    ProductCategory getCategoryEntityById(UUID categoryId);

    // ==================== LISTING AND SEARCH ====================

    /**
     * Retrieves all categories with pagination
     * @param pageable Pagination parameters
     * @return Page of categories
     */
    Page<CategoryResponse> getAllCategories(Pageable pageable);

    /**
     * Retrieves all root categories (categories without parent)
     * @return List of root categories with their complete hierarchy
     */
    List<CategoryResponse> getRootCategories();

    /**
     * Retrieves complete category tree
     * @return Category tree response with statistics
     */
    CategoryTreeResponse getCategoryTree();

    /**
     * Retrieves subcategories of a specific category
     * @param parentCategoryId Parent category UUID
     * @return List of subcategories
     */
    List<CategoryResponse> getSubcategories(UUID parentCategoryId);

    /**
     * Searches categories by name
     * @param searchTerm Search term
     * @param pageable Pagination parameters
     * @return Page of matching categories
     */
    Page<CategoryResponse> searchCategories(String searchTerm, Pageable pageable);

    // ==================== HIERARCHY OPERATIONS ====================

    /**
     * Moves a category to a new parent
     * @param categoryId Category UUID to move
     * @param newParentId New parent category UUID (null for root level)
     * @return Updated category response
     */
    CategoryResponse moveCategory(UUID categoryId, UUID newParentId);

    /**
     * Gets all ancestors of a category (path to root)
     * @param categoryId Category UUID
     * @return List of ancestor categories
     */
    List<CategoryResponse> getCategoryPath(UUID categoryId);

    /**
     * Gets all descendants of a category (complete subtree)
     * @param categoryId Category UUID
     * @param includeProducts Whether to include product counts
     * @return List of all descendant categories
     */
    List<CategoryResponse> getAllDescendants(UUID categoryId, boolean includeProducts);

    // ==================== VALIDATION AND BUSINESS LOGIC ====================

    /**
     * Validates if a category can be deleted
     * @param categoryId Category UUID
     * @return Validation result with details
     */
    CategoryValidationResult validateCategoryDeletion(UUID categoryId);

    /**
     * Validates if a category can be moved to a new parent
     * @param categoryId Category UUID to move
     * @param newParentId New parent category UUID
     * @return Validation result with details
     */
    CategoryValidationResult validateCategoryMove(UUID categoryId, UUID newParentId);

    /**
     * Checks if a category name is available at the specified level
     * @param categoryName Category name to check
     * @param parentCategoryId Parent category UUID (null for root level)
     * @param excludeCategoryId Category UUID to exclude from check (for updates)
     * @return true if name is available
     */
    boolean isCategoryNameAvailable(String categoryName, UUID parentCategoryId, UUID excludeCategoryId);

    // ==================== STATISTICS AND ANALYTICS ====================

    /**
     * Gets category statistics
     * @param categoryId Category UUID
     * @return Category statistics
     */
    CategoryStatistics getCategoryStatistics(UUID categoryId);

    /**
     * Gets overall category system statistics
     * @return Overall statistics
     */
    CategorySystemStatistics getSystemStatistics();

    /**
     * Refreshes product counts for all categories
     * (useful after bulk product operations)
     */
    void refreshAllProductCounts();

    // ==================== BULK OPERATIONS ====================

    /**
     * Creates multiple categories in batch
     * @param requests List of category creation requests
     * @return Bulk operation result
     */
    BulkCategoryOperationResult createCategoriesBatch(List<CreateCategoryRequest> requests);

    /**
     * Imports category hierarchy from structured data
     * @param categoryHierarchyData Structured category data
     * @return Import result with statistics
     */
    CategoryImportResult importCategoryHierarchy(CategoryHierarchyData categoryHierarchyData);
}