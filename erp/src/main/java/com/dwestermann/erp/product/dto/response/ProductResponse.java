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
public class ProductResponse {
    private UUID id; // Geändert von Long zu UUID
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private String currency;
    private String formattedPrice;
    private Unit unit;
    private String unitDisplayName;
    private Integer stockQuantity;
    private Integer minimumStockLevel;
    private ProductStatus status;
    private String statusDisplayName;
    private CategoryResponse category;
    private String notes;
    private Boolean isLowStock;
    private Boolean isOutOfStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
}