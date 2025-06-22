package com.dwestermann.erp.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private UUID parentCategoryId; // Changed from Long to UUID

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
}