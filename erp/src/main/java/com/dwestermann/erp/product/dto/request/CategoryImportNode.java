package com.dwestermann.erp.product.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CategoryImportNode {
    private String name;
    private String description;
    private String notes;
    private List<CategoryImportNode> children;
}
