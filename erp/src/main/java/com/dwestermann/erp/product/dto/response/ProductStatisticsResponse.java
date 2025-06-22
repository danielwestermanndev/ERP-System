package com.dwestermann.erp.product.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ProductStatisticsResponse {

    // Basic Counts
    private Long totalProducts;
    private Long activeProducts;
    private Long draftProducts;
    private Long discontinuedProducts;

    // Stock Information
    private Long lowStockProducts;
    private Long outOfStockProducts;
    private List<ProductSummaryResponse> lowStockProductsList;
    private List<ProductSummaryResponse> outOfStockProductsList;

    // Financial Information
    private BigDecimal totalInventoryValue;
    private String totalInventoryValueFormatted;
    private BigDecimal averageProductPrice;
    private String averageProductPriceFormatted;

    // Category Information
    private Long categoriesWithProducts;
    private Long totalCategories;

    // Recent Activity
    private List<ProductSummaryResponse> recentlyAddedProducts;
    private List<ProductSummaryResponse> recentlyUpdatedProducts;
}