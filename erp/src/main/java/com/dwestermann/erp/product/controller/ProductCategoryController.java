package com.dwestermann.erp.product.controller;

import com.dwestermann.erp.product.dto.request.CategoryHierarchyData;
import com.dwestermann.erp.product.dto.request.CreateCategoryRequest;
import com.dwestermann.erp.product.dto.request.UpdateCategoryRequest;
import com.dwestermann.erp.product.dto.response.*;
import com.dwestermann.erp.product.dto.result.*;
import com.dwestermann.erp.product.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Product Category Management", description = "APIs for managing product categories and hierarchy")
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    // ==================== CRUD OPERATIONS ====================

    @PostMapping
    @PreAuthorize("hasPermission('product', 'write')")
    @Operation(summary = "Create a new category", description = "Creates a new product category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "409", description = "Category name already exists at this level")
    })
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        log.debug("Creating new category: {}", request.getName());

        CategoryResponse response = categoryService.createCategory(request);

        log.info("Created category: {} (ID: {})", response.getName(), response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('product', 'read')")
    @Operation(summary = "Get category by ID", description = "Retrieves a category by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable UUID id) {
        log.debug("Retrieving category with ID: {}", id);

        CategoryResponse response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission('product', 'write')")
    @Operation(summary = "Update category", description = "Updates an existing category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "409", description = "Category name already exists or circular reference")
    })
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "Category ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        log.debug("Updating category ID: {} with new data", id);

        CategoryResponse response = categoryService.updateCategory(id, request);

        log.info("Updated category: {} (ID: {})", response.getName(), response.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission('product', 'delete')")
    @Operation(summary = "Delete category", description = "Deletes a category by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "409", description = "Category has products or subcategories")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable UUID id) {
        log.debug("Deleting category with ID: {}", id);

        categoryService.deleteCategory(id);

        log.info("Deleted category with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    // ==================== LISTING AND SEARCH ====================

    @GetMapping
    @PreAuthorize("hasPermission('product', 'read')")
    @Operation(summary = "Get all categories", description = "Retrieves a paginated list of all categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String sortDirection) {

        log.debug("Retrieving categories - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, sortDirection);

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<CategoryResponse> response = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/root")
    @PreAuthorize("hasPermission('product', 'read')")
    @Operation(summary = "Get root categories", description = "Retrieves all root categories with their hierarchy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Root categories retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        log.debug("Retrieving root categories");

        List<CategoryResponse> response = categoryService.getRootCategories();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tree")
    @PreAuthorize("hasPermission('product', 'read')")
    @Operation(summary = "Get category tree", description = "Retrieves complete category tree with statistics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category tree retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<CategoryTreeResponse> getCategoryTree() {
        log.debug("Retrieving complete category tree");

        CategoryTreeResponse response = categoryService.getCategoryTree();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/subcategories")
    @PreAuthorize("hasPermission('product', 'read')")
    @Operation(summary = "Get subcategories", description = "Retrieves subcategories of a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subcategories retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Parent category not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<CategoryResponse>> getSubcategories(
            @Parameter(description = "Parent category ID") @PathVariable UUID id) {
        log.debug("Retrieving subcategories for category ID: {}", id);

        List<CategoryResponse> response = categoryService.getSubcategories(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @PreAuthorize("hasPermission('product', 'read')")
    @Operation(summary = "Search categories", description = "Searches categories by name or description")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<Page<CategoryResponse>> searchCategories(
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.debug("Searching categories with term: {}", searchTerm);

        Pageable pageable = PageRequest.of(page, size);
        Page<CategoryResponse> response = categoryService.searchCategories(searchTerm, pageable);
        return ResponseEntity.ok(response);
    }

    // ==================== HIERARCHY OPERATIONS ====================

    @PutMapping("/{id}/move")
    @PreAuthorize("hasPermission('product', 'write')")
    @Operation(summary = "Move category", description = "Moves a category to a new parent")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category moved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid move operation"),
            @ApiResponse(responseCode = "404", description = "Category or new parent not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "409", description = "Move would create circular reference")
    })
    public ResponseEntity<CategoryResponse> moveCategory(
            @Parameter(description = "Category ID to move") @PathVariable UUID id,
            @Parameter(description = "New parent category ID") @RequestParam(required = false) UUID newParentId) {
        log.debug("Moving category ID: {} to new parent ID: {}", id, newParentId);

        CategoryResponse response = categoryService.moveCategory(id, newParentId);

        log.info("Moved category: {} (ID: {}) to new parent: {}",
                response.getName(), response.getId(), newParentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/path")
    @PreAuthorize("hasPermission('product', 'read')")
    @Operation(summary = "Get category path", description = "Gets the path from root to the specified category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category path retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<CategoryResponse>> getCategoryPath(
            @Parameter(description = "Category ID") @PathVariable UUID id) {
        log.debug("Retrieving path for category ID: {}", id);

        List<CategoryResponse> response = categoryService.getCategoryPath(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/descendants")
    @PreAuthorize("hasPermission('product', 'read')")
    @Operation(summary = "Get all descendants", description = "Gets all descendant categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Descendants retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<CategoryResponse>> getAllDescendants(
            @Parameter(description = "Category ID") @PathVariable UUID id,
            @Parameter(description = "Include product counts") @RequestParam(defaultValue = "false") boolean includeProducts) {
        log.debug("Retrieving descendants for category ID: {}", id);

        List<CategoryResponse> response = categoryService.getAllDescendants(id, includeProducts);
        return ResponseEntity.ok(response);
    }

    // ==================== VALIDATION ====================

    @PostMapping("/{id}/validate-deletion")
    @PreAuthorize("hasPermission('product', 'read')")
    @Operation(summary = "Validate category deletion", description = "Validates if a category can be deleted")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation completed"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<CategoryValidationResult> validateCategoryDeletion(
            @Parameter(description = "Category ID") @PathVariable UUID id) {
        log.debug("Validating deletion for category ID: {}", id);

        CategoryValidationResult response = categoryService.validateCategoryDeletion(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/validate-move")
    @PreAuthorize("hasPermission('product', 'read')")
    @Operation(summary = "Validate category move", description = "Validates if a category can be moved")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation completed"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<CategoryValidationResult> validateCategoryMove(
            @Parameter(description = "Category ID") @PathVariable UUID id,
            @Parameter(description = "New parent category ID") @RequestParam(required = false) UUID newParentId) {
        log.debug("Validating move for category ID: {} to parent: {}", id, newParentId);

        CategoryValidationResult response = categoryService.validateCategoryMove(id, newParentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name-available")
    @PreAuthorize("hasPermission('product', 'read')")
    @Operation(summary = "Check category name availability", description = "Checks if a category name is available")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Name availability checked"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<Boolean> isCategoryNameAvailable(
            @Parameter(description = "Category name to check") @RequestParam String categoryName,
            @Parameter(description = "Parent category ID") @RequestParam(required = false) UUID parentCategoryId,
            @Parameter(description = "Category ID to exclude") @RequestParam(required = false) UUID excludeCategoryId) {
        log.debug("Checking name availability: {} under parent: {}", categoryName, parentCategoryId);

        boolean available = categoryService.isCategoryNameAvailable(categoryName, parentCategoryId, excludeCategoryId);
        return ResponseEntity.ok(available);
    }

    // ==================== STATISTICS AND ANALYTICS ====================

    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasPermission('product', 'read')")
    @Operation(summary = "Get category statistics", description = "Gets detailed statistics for a category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<CategoryStatistics> getCategoryStatistics(
            @Parameter(description = "Category ID") @PathVariable UUID id) {
        log.debug("Retrieving statistics for category ID: {}", id);

        CategoryStatistics response = categoryService.getCategoryStatistics(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics/system")
    @PreAuthorize("hasPermission('product', 'read')")
    @Operation(summary = "Get system statistics", description = "Gets overall category system statistics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System statistics retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<CategorySystemStatistics> getSystemStatistics() {
        log.debug("Retrieving category system statistics");

        CategorySystemStatistics response = categoryService.getSystemStatistics();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-counts")
    @PreAuthorize("hasPermission('product', 'write')")
    @Operation(summary = "Refresh product counts", description = "Refreshes product counts for all categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product counts refreshed successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<Void> refreshAllProductCounts() {
        log.debug("Refreshing product counts for all categories");

        categoryService.refreshAllProductCounts();

        log.info("Refreshed product counts for all categories");
        return ResponseEntity.noContent().build();
    }

    // ==================== BULK OPERATIONS ====================

    @PostMapping("/bulk/create")
    @PreAuthorize("hasPermission('product', 'write')")
    @Operation(summary = "Bulk create categories", description = "Creates multiple categories in batch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bulk operation completed"),
            @ApiResponse(responseCode = "400", description = "Invalid bulk operation data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<BulkCategoryOperationResult> createCategoriesBatch(
            @Valid @RequestBody List<CreateCategoryRequest> requests) {
        log.debug("Bulk creating {} categories", requests.size());

        BulkCategoryOperationResult response = categoryService.createCategoriesBatch(requests);

        log.info("Bulk category creation completed: {} successful, {} failed",
                response.getSuccessfulOperations(), response.getFailedOperations());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/import")
    @PreAuthorize("hasPermission('product', 'write')")
    @Operation(summary = "Import category hierarchy", description = "Imports a complete category hierarchy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Import completed"),
            @ApiResponse(responseCode = "400", description = "Invalid import data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<CategoryImportResult> importCategoryHierarchy(
            @Valid @RequestBody CategoryHierarchyData hierarchyData) {
        log.debug("Importing category hierarchy with {} root nodes", hierarchyData.getRootNodes().size());

        CategoryImportResult response = categoryService.importCategoryHierarchy(hierarchyData);

        log.info("Category hierarchy import completed: {} successful, {} failed",
                response.getSuccessfulImports(), response.getFailedImports());
        return ResponseEntity.ok(response);
    }

    // ==================== ADMIN OPERATIONS ====================

    @PostMapping("/{id}/force-delete")
    @PreAuthorize("hasPermission('product', 'admin')")
    @Operation(summary = "Force delete category", description = "Force deletes a category (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category force deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<Void> forceDeleteCategory(
            @Parameter(description = "Category ID") @PathVariable UUID id,
            @Parameter(description = "Move products to category") @RequestParam(required = false) UUID moveProductsToCategoryId,
            @Parameter(description = "Move subcategories to parent") @RequestParam(defaultValue = "true") boolean moveSubcategoriesToParent) {
        log.warn("Force deleting category ID: {} with products moved to: {} and subcategories moved to parent: {}",
                id, moveProductsToCategoryId, moveSubcategoriesToParent);

        // TODO: Implement force delete logic in service
        // This would:
        // 1. Move all products to specified category or root
        // 2. Move subcategories to parent or root
        // 3. Delete the category

        log.info("Force deleted category with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}