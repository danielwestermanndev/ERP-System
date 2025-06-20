// Custom Exceptions
package com.dwestermann.erp.security.exception;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}