package com.dwestermann.erp.product.dto.request;

import com.dwestermann.erp.product.domain.ProductStatus;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductSearchRequest {

    private String searchTerm;
    private UUID categoryId;
    private ProductStatus status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean lowStockOnly;
    private Boolean outOfStockOnly;

    // Pagination
    private int page = 0;
    private int size = 20;
    private String sortBy = "name";
    private String sortDirection = "ASC";

    public Pageable toPageable() {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}