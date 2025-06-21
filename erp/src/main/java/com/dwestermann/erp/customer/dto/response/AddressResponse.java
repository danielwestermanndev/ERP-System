package com.dwestermann.erp.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private String street;
    private String city;
    private String postalCode;
    private String country;
    private String fullAddress;
    private boolean isGermanAddress;
}