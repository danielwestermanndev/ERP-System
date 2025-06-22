package com.dwestermann.erp.product.dto.request;

import com.dwestermann.erp.product.domain.ProductStatus;
import com.dwestermann.erp.product.domain.Unit;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UpdateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    @Digits(integer = 15, fraction = 4, message = "Price format is invalid")
    private BigDecimal price;

    @Size(max = 3, message = "Currency code must be 3 characters")
    private String currency = "EUR";

    @NotNull(message = "Unit is required")
    private Unit unit;

    @Min(value = 0, message = "Minimum stock level cannot be negative")
    private Integer minimumStockLevel = 0;

    @NotNull(message = "Status is required")
    private ProductStatus status;

    private UUID categoryId;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @Size(max = 100, message = "Barcode must not exceed 100 characters")
    private String barcode;

    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    private BigDecimal weight;

    @Size(max = 500, message = "Supplier info must not exceed 500 characters")
    private String supplierInfo;
}