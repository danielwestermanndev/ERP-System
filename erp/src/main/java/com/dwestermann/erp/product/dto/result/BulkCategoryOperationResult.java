package com.dwestermann.erp.product.dto.result;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BulkCategoryOperationResult {

    private Integer totalRequested;
    private Integer successfulOperations;
    private Integer failedOperations;
    private List<String> errors;
    private List<UUID> createdCategoryIds; // Changed from Long to UUID
    private String operationType;
}