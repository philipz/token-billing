package org.tw.token_billing.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Bill entity representing bills table.
 */
@Entity
@Table(name = "bills")
public class Bill {
    @Id
    private String id;
    private String customerId;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private Integer includedTokensUsed;
    private Integer overageTokens;
    private BigDecimal totalCharge;
    private Instant calculatedAt;

    public Bill() {}

    public Bill(String id, String customerId, Integer promptTokens, Integer completionTokens,
              Integer totalTokens, Integer includedTokensUsed, Integer overageTokens,
              BigDecimal totalCharge, Instant calculatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
        this.includedTokensUsed = includedTokensUsed;
        this.overageTokens = overageTokens;
        this.totalCharge = totalCharge;
        this.calculatedAt = calculatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Integer getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(Integer promptTokens) {
        this.promptTokens = promptTokens;
    }

    public Integer getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(Integer completionTokens) {
        this.completionTokens = completionTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(Integer totalTokens) {
        this.totalTokens = totalTokens;
    }

    public Integer getIncludedTokensUsed() {
        return includedTokensUsed;
    }

    public void setIncludedTokensUsed(Integer includedTokensUsed) {
        this.includedTokensUsed = includedTokensUsed;
    }

    public Integer getOverageTokens() {
        return overageTokens;
    }

    public void setOverageTokens(Integer overageTokens) {
        this.overageTokens = overageTokens;
    }

    public BigDecimal getTotalCharge() {
        return totalCharge;
    }

    public void setTotalCharge(BigDecimal totalCharge) {
        this.totalCharge = totalCharge;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(Instant calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
}