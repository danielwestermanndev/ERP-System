package com.dwestermann.erp.product.dto.response;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;

@Data
@Builder
public class CategoryStatistics {
    private String categoryId;      // String to handle both Long and UUID display
    private String categoryName;
    private Long directProductCount;
    private Long totalProductCount; // Including subcategories
    private Long subcategoryCount;
    private Integer hierarchyDepth;
    private BigDecimal totalValue;
    private String totalValueFormatted;
    private Long lowStockProductCount;
    private Long outOfStockProductCount;
}