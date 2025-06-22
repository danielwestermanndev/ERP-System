package com.dwestermann.erp.product.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BulkOperationResponse {
    private int totalRequested;
    private int successfulOperations;
    private int failedOperations;
    private List<String> errors;
    private String operationType;
    private String message;
}