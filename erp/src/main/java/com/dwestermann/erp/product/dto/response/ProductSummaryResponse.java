package com.dwestermann.erp.product.dto.response;

import com.dwestermann.erp.product.domain.ProductStatus;
import com.dwestermann.erp.product.domain.Unit;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProductSummaryResponse {
    private UUID id; // Geändert von Long zu UUID
    private String name;
    private String sku;
    private BigDecimal price;
    private String currency;
    private String formattedPrice;
    private Unit unit;
    private Integer stockQuantity;
    private ProductStatus status;
    private String statusDisplayName;
    private String categoryName;
    private Boolean isLowStock;
    private Boolean isOutOfStock;
    private LocalDateTime updatedAt;
}