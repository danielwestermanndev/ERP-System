package com.dwestermann.erp.product.service.impl;

import com.dwestermann.erp.product.domain.ProductCategory;
import com.dwestermann.erp.product.dto.mapper.CategoryMapper;
import com.dwestermann.erp.product.dto.request.CategoryImportNode;
import com.dwestermann.erp.product.dto.request.CreateCategoryRequest;
import com.dwestermann.erp.product.dto.request.UpdateCategoryRequest;
import com.dwestermann.erp.product.dto.request.CategoryHierarchyData;
import com.dwestermann.erp.product.dto.response.*;
import com.dwestermann.erp.product.dto.result.*;
import com.dwestermann.erp.product.exception.*;
import com.dwestermann.erp.product.repository.ProductCategoryRepository;
import com.dwestermann.erp.product.repository.ProductRepository;
import com.dwestermann.erp.product.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    // ==================== CRUD OPERATIONS ====================

    @Override
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.debug("Creating new category: {}", request.getName());

        // Validate parent category if specified
        ProductCategory parentCategory = null;
        if (request.getParentCategoryId() != null) {
            parentCategory = getCategoryEntityById(request.getParentCategoryId());
        }

        // Check for duplicate name at same level
        if (!isCategoryNameAvailable(request.getName(), request.getParentCategoryId(), null)) {
            throw new DuplicateCategoryNameException(
                    "Category name '" + request.getName() + "' already exists at this level"
            );
        }

        // Create and save category
        ProductCategory category = categoryMapper.toEntity(request, parentCategory);
        category = categoryRepository.save(category);

        log.info("Created category: {} (ID: {})", category.getName(), category.getId());
        return categoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponse updateCategory(UUID categoryId, UpdateCategoryRequest request) {
        log.debug("Updating category ID: {} with data: {}", categoryId, request.getName());

        ProductCategory category = getCategoryEntityById(categoryId);

        // Validate parent category if specified
        ProductCategory newParentCategory = null;
        if (request.getParentCategoryId() != null) {
            newParentCategory = getCategoryEntityById(request.getParentCategoryId());

            // Check for circular reference
            if (wouldCreateCircularReference(categoryId, request.getParentCategoryId())) {
                throw new CircularReferenceException(
                        "Moving category would create a circular reference"
                );
            }
        }

        // Check for duplicate name at new level
        if (!isCategoryNameAvailable(request.getName(), request.getParentCategoryId(), categoryId)) {
            throw new DuplicateCategoryNameException(
                    "Category name '" + request.getName() + "' already exists at this level"
            );
        }

        // Update category
        categoryMapper.updateEntity(category, request, newParentCategory);
        category = categoryRepository.save(category);

        log.info("Updated category: {} (ID: {})", category.getName(), category.getId());
        return categoryMapper.toResponse(category);
    }

    @Override
    public void deleteCategory(UUID categoryId) {
        log.debug("Deleting category ID: {}", categoryId);

        ProductCategory category = getCategoryEntityById(categoryId);

        // Validate deletion
        CategoryValidationResult validation = validateCategoryDeletion(categoryId);
        if (!validation.isValid()) {
            throw new CategoryValidationException(
                    "Category cannot be deleted: " + String.join(", ", validation.getErrors())
            );
        }

        categoryRepository.delete(category);
        log.info("Deleted category: {} (ID: {})", category.getName(), category.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID categoryId) {
        ProductCategory category = getCategoryEntityById(categoryId);
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductCategory getCategoryEntityById(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + categoryId));
    }

    // ==================== LISTING AND SEARCH ====================

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        Page<ProductCategory> categories = categoryRepository.findAllByOrderByNameAsc(pageable);
        return categories.map(categoryMapper::toResponseWithoutSubcategories);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        List<ProductCategory> rootCategories = categoryRepository.findRootCategoriesWithSubcategories();
        return categoryMapper.toResponseList(rootCategories);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryTreeResponse getCategoryTree() {
        List<ProductCategory> rootCategories = categoryRepository.findRootCategoriesWithSubcategories();
        return categoryMapper.toTreeResponse(rootCategories);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getSubcategories(UUID parentCategoryId) {
        ProductCategory parentCategory = getCategoryEntityById(parentCategoryId);
        List<ProductCategory> subcategories = categoryRepository.findByParentCategoryOrderByNameAsc(parentCategory);
        return categoryMapper.toResponseList(subcategories);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> searchCategories(String searchTerm, Pageable pageable) {
        Page<ProductCategory> categories = categoryRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                searchTerm, searchTerm, pageable);
        return categories.map(categoryMapper::toResponseWithoutSubcategories);
    }

    // ==================== HIERARCHY OPERATIONS ====================

    @Override
    public CategoryResponse moveCategory(UUID categoryId, UUID newParentId) {
        log.debug("Moving category ID: {} to new parent ID: {}", categoryId, newParentId);

        ProductCategory category = getCategoryEntityById(categoryId);
        ProductCategory newParent = null;

        if (newParentId != null) {
            newParent = getCategoryEntityById(newParentId);

            // Check for circular reference
            if (wouldCreateCircularReference(categoryId, newParentId)) {
                throw new CircularReferenceException(
                        "Moving category would create a circular reference"
                );
            }
        }

        category.setParentCategory(newParent);
        category = categoryRepository.save(category);

        log.info("Moved category: {} (ID: {}) to new parent: {}",
                category.getName(), category.getId(),
                newParent != null ? newParent.getName() : "ROOT");

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryPath(UUID categoryId) {
        ProductCategory category = getCategoryEntityById(categoryId);
        List<ProductCategory> path = new ArrayList<>();

        ProductCategory current = category.getParentCategory();
        while (current != null) {
            path.add(0, current); // Add to beginning for correct order
            current = current.getParentCategory();
        }

        return categoryMapper.toResponseList(path);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllDescendants(UUID categoryId, boolean includeProducts) {
        ProductCategory category = getCategoryEntityById(categoryId);
        List<ProductCategory> descendants = new ArrayList<>();
        collectDescendants(category, descendants);
        return categoryMapper.toResponseList(descendants);
    }

    // ==================== VALIDATION AND BUSINESS LOGIC ====================

    @Override
    @Transactional(readOnly = true)
    public CategoryValidationResult validateCategoryDeletion(UUID categoryId) {
        ProductCategory category = getCategoryEntityById(categoryId);

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Check for products in category
        Long productCount = productRepository.countByCategoryId(categoryId);
        if (productCount > 0) {
            errors.add("Category contains " + productCount + " products");
        }

        // Check for subcategories
        Long subcategoryCount = categoryRepository.countByParentCategoryId(categoryId);
        if (subcategoryCount > 0) {
            errors.add("Category has " + subcategoryCount + " subcategories");
        }

        return CategoryValidationResult.builder()
                .valid(errors.isEmpty())
                .errors(errors)
                .warnings(warnings)
                .operation("DELETE")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryValidationResult validateCategoryMove(UUID categoryId, UUID newParentId) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Check if source category exists
        if (!categoryRepository.existsById(categoryId)) {
            errors.add("Source category does not exist");
        }

        // Check if target parent exists (if not null)
        if (newParentId != null && !categoryRepository.existsById(newParentId)) {
            errors.add("Target parent category does not exist");
        }

        // Check for circular reference
        if (newParentId != null && wouldCreateCircularReference(categoryId, newParentId)) {
            errors.add("Move would create circular reference");
        }

        return CategoryValidationResult.builder()
                .valid(errors.isEmpty())
                .errors(errors)
                .warnings(warnings)
                .operation("MOVE")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCategoryNameAvailable(String categoryName, UUID parentCategoryId, UUID excludeCategoryId) {
        return categoryRepository.isCategoryNameAvailable(categoryName, parentCategoryId, excludeCategoryId);
    }

    // ==================== STATISTICS AND ANALYTICS ====================

    @Override
    @Transactional(readOnly = true)
    public CategoryStatistics getCategoryStatistics(UUID categoryId) {
        ProductCategory category = getCategoryEntityById(categoryId);

        Long directProductCount = productRepository.countByCategoryId(categoryId);
        Long totalProductCount = getTotalProductCountIncludingSubcategories(categoryId);
        Long subcategoryCount = categoryRepository.countByParentCategoryId(categoryId);
        Integer hierarchyDepth = calculateHierarchyDepth(category);

        // Calculate total value
        BigDecimal totalValue = productRepository.calculateTotalValueByCategoryId(categoryId);

        Long lowStockCount = productRepository.countLowStockProductsByCategoryId(categoryId);
        Long outOfStockCount = productRepository.countOutOfStockProductsByCategoryId(categoryId);

        return CategoryStatistics.builder()
                .categoryId(categoryId.toString())
                .categoryName(category.getName())
                .directProductCount(directProductCount)
                .totalProductCount(totalProductCount)
                .subcategoryCount(subcategoryCount)
                .hierarchyDepth(hierarchyDepth)
                .totalValue(totalValue)
                .totalValueFormatted(formatCurrency(totalValue, "EUR"))
                .lowStockProductCount(lowStockCount)
                .outOfStockProductCount(outOfStockCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CategorySystemStatistics getSystemStatistics() {
        Long totalCategories = categoryRepository.count();
        Long rootCategories = categoryRepository.countRootCategories();
        Integer maxDepth = categoryRepository.findMaxHierarchyDepth();
        Long categoriesWithProducts = categoryRepository.countCategoriesWithProducts();
        Long emptyCategoriesCount = totalCategories - categoriesWithProducts;

        List<CategoryStatistics> topByProductCount = categoryRepository.findTopCategoriesByProductCount(PageRequest.of(0, 5))
                .stream()
                .map(cat -> getCategoryStatistics(cat.getId()))
                .collect(Collectors.toList());

        List<CategoryStatistics> topByValue = categoryRepository.findTopCategoriesByValue(PageRequest.of(0, 5))
                .stream()
                .map(cat -> getCategoryStatistics(cat.getId()))
                .collect(Collectors.toList());

        return CategorySystemStatistics.builder()
                .totalCategories(totalCategories)
                .rootCategories(rootCategories)
                .maxHierarchyDepth(maxDepth)
                .categoriesWithProducts(categoriesWithProducts)
                .emptyCategoriesCount(emptyCategoriesCount)
                .topCategoriesByProductCount(topByProductCount)
                .topCategoriesByValue(topByValue)
                .build();
    }

    @Override
    public void refreshAllProductCounts() {
        log.info("Refreshing product counts for all categories");
        categoryRepository.refreshAllProductCounts();
    }

    // ==================== BULK OPERATIONS ====================

    @Override
    public BulkCategoryOperationResult createCategoriesBatch(List<CreateCategoryRequest> requests) {
        log.info("Creating {} categories in batch", requests.size());

        List<String> errors = new ArrayList<>();
        List<UUID> createdIds = new ArrayList<>();
        int successful = 0;

        for (CreateCategoryRequest request : requests) {
            try {
                CategoryResponse created = createCategory(request);
                createdIds.add(created.getId());
                successful++;
            } catch (Exception e) {
                errors.add("Failed to create category '" + request.getName() + "': " + e.getMessage());
            }
        }

        return BulkCategoryOperationResult.builder()
                .totalRequested(requests.size())
                .successfulOperations(successful)
                .failedOperations(requests.size() - successful)
                .errors(errors)
                .createdCategoryIds(createdIds)
                .operationType("CREATE_BATCH")
                .build();
    }

    @Override
    public CategoryImportResult importCategoryHierarchy(CategoryHierarchyData hierarchyData) {
        log.info("Importing category hierarchy with {} root nodes", hierarchyData.getRootNodes().size());

        List<String> errors = new ArrayList<>();
        List<UUID> createdIds = new ArrayList<>();
        int totalNodes = countTotalNodes(hierarchyData.getRootNodes());
        int successful = 0;
        int maxDepth = 0;

        for (CategoryImportNode rootNode : hierarchyData.getRootNodes()) {
            try {
                int depth = importCategoryNode(rootNode, null, createdIds, errors, 1);
                maxDepth = Math.max(maxDepth, depth);
                successful++;
            } catch (Exception e) {
                errors.add("Failed to import root category '" + rootNode.getName() + "': " + e.getMessage());
            }
        }

        return CategoryImportResult.builder()
                .totalNodes(totalNodes)
                .successfulImports(successful)
                .failedImports(totalNodes - successful)
                .maxDepthCreated(maxDepth)
                .errors(errors)
                .createdCategoryIds(createdIds)
                .build();
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private boolean wouldCreateCircularReference(UUID categoryId, UUID newParentId) {
        if (newParentId == null || categoryId.equals(newParentId)) {
            return categoryId.equals(newParentId);
        }

        ProductCategory potentialParent = categoryRepository.findById(newParentId).orElse(null);
        if (potentialParent == null) {
            return false;
        }

        // Check if categoryId is an ancestor of newParentId
        ProductCategory current = potentialParent.getParentCategory();
        while (current != null) {
            if (current.getId().equals(categoryId)) {
                return true;
            }
            current = current.getParentCategory();
        }

        return false;
    }

    private void collectDescendants(ProductCategory category, List<ProductCategory> descendants) {
        if (category.getSubcategories() != null) {
            for (ProductCategory subcategory : category.getSubcategories()) {
                descendants.add(subcategory);
                collectDescendants(subcategory, descendants);
            }
        }
    }

    private Long getTotalProductCountIncludingSubcategories(UUID categoryId) {
        ProductCategory category = getCategoryEntityById(categoryId);
        Long total = productRepository.countByCategoryId(categoryId);

        if (category.getSubcategories() != null) {
            for (ProductCategory subcategory : category.getSubcategories()) {
                total += getTotalProductCountIncludingSubcategories(subcategory.getId());
            }
        }

        return total;
    }

    private Integer calculateHierarchyDepth(ProductCategory category) {
        int depth = 0;
        ProductCategory current = category;

        while (current.getParentCategory() != null) {
            depth++;
            current = current.getParentCategory();
        }

        return depth;
    }

    private String formatCurrency(BigDecimal amount, String currency) {
        if (amount == null) {
            return currency + " 0,00";
        }
        return currency + " " + amount.toString();
    }

    private int countTotalNodes(List<CategoryImportNode> nodes) {
        int count = nodes.size();
        for (CategoryImportNode node : nodes) {
            if (node.getChildren() != null) {
                count += countTotalNodes(node.getChildren());
            }
        }
        return count;
    }

    private int importCategoryNode(CategoryImportNode node, UUID parentId,
                                   List<UUID> createdIds, List<String> errors, int currentDepth) {
        try {
            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName(node.getName());
            request.setDescription(node.getDescription());
            request.setNotes(node.getNotes());
            request.setParentCategoryId(parentId);

            CategoryResponse created = createCategory(request);
            createdIds.add(created.getId());

            int maxDepth = currentDepth;
            if (node.getChildren() != null) {
                for (CategoryImportNode child : node.getChildren()) {
                    int childDepth = importCategoryNode(child, created.getId(), createdIds, errors, currentDepth + 1);
                    maxDepth = Math.max(maxDepth, childDepth);
                }
            }

            return maxDepth;
        } catch (Exception e) {
            errors.add("Failed to import category '" + node.getName() + "': " + e.getMessage());
            return currentDepth;
        }
    }
}