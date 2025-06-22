package com.dwestermann.erp.product.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class CategoryHierarchyData {
    private List<CategoryImportNode> rootNodes;
}

