package com.dwestermann.erp.product.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductValidationResponse {
    private boolean valid;
    private List<String> errors;
    private List<String> warnings;
    private String message;
}