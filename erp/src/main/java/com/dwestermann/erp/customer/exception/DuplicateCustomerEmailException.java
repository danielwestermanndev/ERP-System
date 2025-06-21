package com.dwestermann.erp.customer.exception;

public class DuplicateCustomerEmailException extends RuntimeException {

    public DuplicateCustomerEmailException(String email) {
        super(String.format("Customer with email %s already exists in this tenant", email));
    }

    public DuplicateCustomerEmailException(String email, String tenantId) {
        super(String.format("Customer with email %s already exists in tenant %s", email, tenantId));
    }

    public DuplicateCustomerEmailException(String message, Throwable cause) {
        super(message, cause);
    }

    // Static factory methods for better API
    public static DuplicateCustomerEmailException forEmail(String email) {
        return new DuplicateCustomerEmailException(email);
    }

    public static DuplicateCustomerEmailException forEmailAndTenant(String email, String tenantId) {
        return new DuplicateCustomerEmailException(email, tenantId);
    }
}