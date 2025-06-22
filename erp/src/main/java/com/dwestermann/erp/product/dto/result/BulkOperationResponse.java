package com.dwestermann.erp.product.dto.result;

import com.dwestermann.erp.product.dto.response.ProductSummaryResponse;
import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class BulkOperationResponse {

    private Integer totalRequested;
    private Integer successfulOperations;
    private Integer failedOperations;
    private List<String> errors;
    private List<ProductSummaryResponse> processedProducts;
    private String operationType;  // UPDATE_STOCK, UPDATE_STATUS, etc.
}