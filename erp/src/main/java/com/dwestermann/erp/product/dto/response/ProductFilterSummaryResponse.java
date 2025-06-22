package com.dwestermann.erp.product.dto.response;

import com.dwestermann.erp.product.domain.ProductStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductFilterSummaryResponse {

    private Long totalResults;
    private String appliedSearchTerm;
    private List<ProductStatus> appliedStatuses;
    private List<String> appliedUnits;
}