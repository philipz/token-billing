package org.tw.token_billing.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * Subscription entity representing customer_subscriptions table.
 */
@Entity
@Table(name = "customer_subscriptions")
public class Subscription {
    @Id
    private java.util.UUID id;

    private String customerId;

    @OneToOne
    @JoinColumn(name = "plan_id")
    private PricingPlan plan;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    public Subscription() {}

    public java.util.UUID getId() {
        return id;
    }

    public void setId(java.util.UUID id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public PricingPlan getPlan() {
        return plan;
    }

    public void setPlan(PricingPlan plan) {
        this.plan = plan;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }
}