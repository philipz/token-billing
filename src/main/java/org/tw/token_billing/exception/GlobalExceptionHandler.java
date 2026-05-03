package org.tw.token_billing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.stream.Collectors;

import org.tw.token_billing.exception.CustomerNotFoundException;
import org.tw.token_billing.exception.InvalidCustomerIdFormatException;
import org.tw.token_billing.exception.NoActiveSubscriptionException;
import org.tw.token_billing.exception.TokenCountNegativeException;

/**
 * Global exception handler for RFC 7807 Problem Detail responses.
 * Handles all error responses with application/problem+json content type.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected org.springframework.http.ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            org.springframework.http.HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                detail.isEmpty() ? "Invalid request body" : detail
        );
        problemDetail.setTitle("Invalid request body");
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setInstance(URI.create(request.getDescription(false)));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public org.springframework.http.ResponseEntity<ProblemDetail> handleCustomerNotFound(
            CustomerNotFoundException ex,
            jakarta.servlet.http.HttpServletRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Customer not found");
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    @ExceptionHandler(InvalidCustomerIdFormatException.class)
    public org.springframework.http.ResponseEntity<ProblemDetail> handleInvalidCustomerId(
            InvalidCustomerIdFormatException ex,
            jakarta.servlet.http.HttpServletRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Invalid customer ID format");
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(TokenCountNegativeException.class)
    public org.springframework.http.ResponseEntity<ProblemDetail> handleTokenCountNegative(
            TokenCountNegativeException ex,
            jakarta.servlet.http.HttpServletRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Token count cannot be negative");
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(NoActiveSubscriptionException.class)
    public org.springframework.http.ResponseEntity<ProblemDetail> handleNoActiveSubscription(
            NoActiveSubscriptionException ex,
            jakarta.servlet.http.HttpServletRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problemDetail.setTitle("No active subscription");
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public org.springframework.http.ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex,
            jakarta.servlet.http.HttpServletRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
        problemDetail.setTitle("Internal server error");
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }
}