package com.dwestermann.erp.product.dto.response;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;

@Data
@Builder
public class CategoryStatisticsResponse {

    private Long categoryId;
    private String categoryName;
    private Long productCount;
    private BigDecimal totalValue;
    private String totalValueFormatted;
    private Long lowStockCount;
    private Long outOfStockCount;
}
