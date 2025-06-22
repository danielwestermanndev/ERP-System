package com.dwestermann.erp.product.dto.response;

import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class CategorySystemStatistics {
    private Long totalCategories;
    private Long rootCategories;
    private Integer maxHierarchyDepth;
    private Long categoriesWithProducts;
    private Long emptyCategoriesCount;
    private List<CategoryStatistics> topCategoriesByProductCount;
    private List<CategoryStatistics> topCategoriesByValue;
}