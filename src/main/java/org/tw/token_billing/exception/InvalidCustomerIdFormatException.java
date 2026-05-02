package org.tw.token_billing.exception;

/**
 * Exception thrown when customer ID format is invalid.
 */
public class InvalidCustomerIdFormatException extends RuntimeException {

    public InvalidCustomerIdFormatException() {
        super("Invalid customer ID format");
    }
}