package com.dwestermann.erp.product.dto.result;

import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class CategoryValidationResult {
    private boolean valid;
    private List<String> errors;
    private List<String> warnings;
    private String operation;
}