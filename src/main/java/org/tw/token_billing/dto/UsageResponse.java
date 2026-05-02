package org.tw.token_billing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for successful token usage billing.
 * Follows SRS-F-4 success response schema.
 */
public record UsageResponse(
    @JsonProperty("billId")
    String billId,

    @JsonProperty("customerId")
    String customerId,

    @JsonProperty("promptTokens")
    Integer promptTokens,

    @JsonProperty("completionTokens")
    Integer completionTokens,

    @JsonProperty("totalTokens")
    Integer totalTokens,

    @JsonProperty("tokensFromQuota")
    Integer tokensFromQuota,

    @JsonProperty("overageTokens")
    Integer overageTokens,

    @JsonProperty("totalCharge")
    BigDecimal totalCharge,

    @JsonProperty("currency")
    String currency,

    @JsonProperty("calculatedAt")
    Instant calculatedAt
) {}