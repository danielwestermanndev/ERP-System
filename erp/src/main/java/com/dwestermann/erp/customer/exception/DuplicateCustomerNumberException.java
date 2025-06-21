package com.dwestermann.erp.customer.exception;

public class DuplicateCustomerNumberException extends RuntimeException {

    public DuplicateCustomerNumberException(String customerNumber) {
        super(String.format("Customer with number %s already exists in this tenant", customerNumber));
    }

    public DuplicateCustomerNumberException(String customerNumber, String tenantId) {
        super(String.format("Customer with number %s already exists in tenant %s", customerNumber, tenantId));
    }

    public DuplicateCustomerNumberException(String message, Throwable cause) {
        super(message, cause);
    }

    // Static factory methods for better API
    public static DuplicateCustomerNumberException forNumber(String customerNumber) {
        return new DuplicateCustomerNumberException(customerNumber);
    }

    public static DuplicateCustomerNumberException forNumberAndTenant(String customerNumber, String tenantId) {
        return new DuplicateCustomerNumberException(customerNumber, tenantId);
    }
}