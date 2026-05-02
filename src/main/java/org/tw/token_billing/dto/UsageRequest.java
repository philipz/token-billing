package org.tw.token_billing.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Request DTO for token usage billing endpoint.
 * Follows SRS-F-2 request body schema specification.
 */
public record UsageRequest(
    @NotBlank(message = "Customer ID is required")
    @Pattern(
        regexp = "^[A-Za-z0-9_\\-]{1,50}$",
        message = "Invalid customer ID format"
    )
    String customerId,

    @NotNull(message = "Prompt tokens is required")
    @PositiveOrZero(message = "Token count cannot be negative")
    @Max(value = 2_000_000_000, message = "Token count exceeds maximum allowed value")
    Integer promptTokens,

    @NotNull(message = "Completion tokens is required")
    @PositiveOrZero(message = "Token count cannot be negative")
    @Max(value = 2_000_000_000, message = "Token count exceeds maximum allowed value")
    Integer completionTokens
) {}