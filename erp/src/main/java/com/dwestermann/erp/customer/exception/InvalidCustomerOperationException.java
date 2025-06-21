package com.dwestermann.erp.customer.exception;

public class InvalidCustomerOperationException extends RuntimeException {

    public InvalidCustomerOperationException(String message) {
        super(message);
    }

    public InvalidCustomerOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static InvalidCustomerOperationException cannotDeleteCustomerWithInvoices(String customerName) {
        return new InvalidCustomerOperationException(
                String.format("Cannot delete customer '%s' because they have associated invoices", customerName));
    }

    public static InvalidCustomerOperationException cannotArchiveActiveCustomer(String customerName) {
        return new InvalidCustomerOperationException(
                String.format("Cannot archive customer '%s' while they are still active", customerName));
    }

    public static InvalidCustomerOperationException primaryContactRequired() {
        return new InvalidCustomerOperationException(
                "Customer must have at least one primary contact person");
    }

    public static InvalidCustomerOperationException onlyOnePrimaryContactAllowed() {
        return new InvalidCustomerOperationException(
                "Customer can have only one primary contact person");
    }

    public static InvalidCustomerOperationException withMessage(String message) {
        return new InvalidCustomerOperationException(message);
    }
}
