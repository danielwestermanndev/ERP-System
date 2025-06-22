package com.dwestermann.erp.product.dto.result;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CategoryImportResult {

    private Integer totalNodes;
    private Integer successfulImports;
    private Integer failedImports;
    private Integer maxDepthCreated;
    private List<String> errors;
    private List<UUID> createdCategoryIds; // Changed from Long to UUID
}