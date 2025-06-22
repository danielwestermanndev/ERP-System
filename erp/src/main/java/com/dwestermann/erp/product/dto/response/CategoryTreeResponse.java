package com.dwestermann.erp.product.dto.response;

import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class CategoryTreeResponse {

    private List<CategoryResponse> rootCategories;
    private Long totalCategories;
    private Integer maxDepth;
}
