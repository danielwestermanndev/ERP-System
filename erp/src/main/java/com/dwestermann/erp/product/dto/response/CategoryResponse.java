package com.dwestermann.erp.product.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CategoryResponse {

    private UUID id; // Changed from Long to UUID
    private String name;
    private String description;
    private String fullPath;
    private CategoryResponse parentCategory;
    private List<CategoryResponse> subcategories;
    private Long productCount;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
}