package com.dwestermann.erp.customer.dto.request;

import com.dwestermann.erp.customer.domain.CustomerStatus;
import com.dwestermann.erp.customer.domain.CustomerType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomerRequest {

    @NotNull(message = "Customer ID is required")
    private UUID id;

    @NotBlank(message = "Customer name is required")
    @Size(max = 255, message = "Customer name must not exceed 255 characters")
    private String name;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Size(max = 50, message = "Phone must not exceed 50 characters")
    private String phone;

    @Size(max = 20, message = "Customer number must not exceed 20 characters")
    private String customerNumber;

    private CustomerStatus status = CustomerStatus.ACTIVE;

    private CustomerType type = CustomerType.B2B;

    @Valid
    private AddressRequest primaryAddress;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}