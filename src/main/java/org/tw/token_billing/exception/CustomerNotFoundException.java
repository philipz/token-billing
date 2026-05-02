package org.tw.token_billing.exception;

/**
 * Exception thrown when a customer is not found in the system.
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String customerId) {
        super("Customer '" + customerId + "' does not exist");
    }
}