package com.dwestermann.erp.product.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginationResponse {
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean isFirst;
    private boolean isLast;
}