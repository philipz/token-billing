package org.tw.token_billing.exception;

/**
 * Exception thrown when customer has no active subscription.
 */
public class NoActiveSubscriptionException extends RuntimeException {

    public NoActiveSubscriptionException(String customerId) {
        super("Customer '" + customerId + "' has no active subscription");
    }
}