package org.tw.token_billing.exception;

/**
 * Exception thrown when token count is negative.
 */
public class TokenCountNegativeException extends RuntimeException {

    public TokenCountNegativeException() {
        super("Token count cannot be negative");
    }
}