package com.dwestermann.erp.customer.exception;

import java.util.UUID;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(UUID customerId) {
        super(String.format("Customer with ID %s not found", customerId));
    }

    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    // Static factory methods for better API clarity
    public static CustomerNotFoundException forId(UUID customerId) {
        return new CustomerNotFoundException(customerId);
    }

    public static CustomerNotFoundException forCustomerNumber(String customerNumber) {
        return new CustomerNotFoundException(
                String.format("Customer with number %s not found", customerNumber), null);
    }

    public static CustomerNotFoundException withMessage(String message) {
        return new CustomerNotFoundException(message, null);
    }
}