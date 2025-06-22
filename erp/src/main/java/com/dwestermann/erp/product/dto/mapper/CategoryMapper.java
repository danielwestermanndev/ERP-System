package com.dwestermann.erp.product.dto.mapper;

import com.dwestermann.erp.product.domain.ProductCategory;
import com.dwestermann.erp.product.dto.request.CreateCategoryRequest;
import com.dwestermann.erp.product.dto.request.UpdateCategoryRequest;
import com.dwestermann.erp.product.dto.response.CategoryResponse;
import com.dwestermann.erp.product.dto.response.CategoryTreeResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    // ==================== ENTITY TO RESPONSE MAPPING ====================

    public CategoryResponse toResponse(ProductCategory category) {
        if (category == null) {
            return null;
        }

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .fullPath(buildFullPath(category))
                .parentCategory(category.getParentCategory() != null ? toResponseWithoutSubcategories(category.getParentCategory()) : null)
                .subcategories(category.getSubcategories() != null ? toResponseList(category.getSubcategories()) : null)
                .productCount(category.getProductCount() != null ? category.getProductCount() : 0L)
                .notes(category.getNotes())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .createdBy(category.getCreatedBy())
                .updatedBy(category.getUpdatedBy())
                .version(category.getVersion())
                .build();
    }

    public CategoryResponse toResponseWithoutSubcategories(ProductCategory category) {
        if (category == null) {
            return null;
        }

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .fullPath(buildFullPath(category))
                .parentCategory(category.getParentCategory() != null ?
                        toResponseWithoutSubcategories(category.getParentCategory()) : null)
                .productCount(category.getProductCount() != null ? category.getProductCount() : 0L)
                .notes(category.getNotes())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .createdBy(category.getCreatedBy())
                .updatedBy(category.getUpdatedBy())
                .version(category.getVersion())
                .build();
    }

    // ==================== LIST MAPPING ====================

    public List<CategoryResponse> toResponseList(List<ProductCategory> categories) {
        if (categories == null) {
            return null;
        }
        return categories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CategoryTreeResponse toTreeResponse(List<ProductCategory> rootCategories) {
        return CategoryTreeResponse.builder()
                .rootCategories(toResponseList(rootCategories))
                .totalCategories(countTotalCategories(rootCategories))
                .maxDepth(calculateMaxDepth(rootCategories))
                .build();
    }

    // ==================== REQUEST TO ENTITY MAPPING ====================

    public ProductCategory toEntity(CreateCategoryRequest request, ProductCategory parentCategory) {
        ProductCategory category = new ProductCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setParentCategory(parentCategory);
        category.setNotes(request.getNotes());
        return category;
    }

    public void updateEntity(ProductCategory category, UpdateCategoryRequest request, ProductCategory parentCategory) {
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setParentCategory(parentCategory);
        category.setNotes(request.getNotes());
    }

    // ==================== UTILITY METHODS ====================

    private String buildFullPath(ProductCategory category) {
        if (category == null) {
            return "";
        }

        StringBuilder path = new StringBuilder();
        ProductCategory current = category;

        // Build path from current to root
        while (current != null) {
            if (path.length() > 0) {
                path.insert(0, " > ");
            }
            path.insert(0, current.getName());
            current = current.getParentCategory();
        }

        return path.toString();
    }

    private Long countTotalCategories(List<ProductCategory> categories) {
        if (categories == null) {
            return 0L;
        }

        long count = categories.size();
        for (ProductCategory category : categories) {
            if (category.getSubcategories() != null) {
                count += countTotalCategories(category.getSubcategories());
            }
        }
        return count;
    }

    private Integer calculateMaxDepth(List<ProductCategory> categories) {
        if (categories == null || categories.isEmpty()) {
            return 0;
        }

        int maxDepth = 1;
        for (ProductCategory category : categories) {
            if (category.getSubcategories() != null && !category.getSubcategories().isEmpty()) {
                int childDepth = 1 + calculateMaxDepth(category.getSubcategories());
                maxDepth = Math.max(maxDepth, childDepth);
            }
        }
        return maxDepth;
    }
}