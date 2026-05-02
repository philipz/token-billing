package org.tw.token_billing.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * PricingPlan entity representing pricing_plans table.
 */
@Entity
@Table(name = "pricing_plans")
public class PricingPlan {
    @Id
    private String id;
    private String name;
    private Integer monthlyQuota;
    private BigDecimal overageRatePer1k;

    public PricingPlan() {}

    public PricingPlan(String id, String name, Integer monthlyQuota, BigDecimal overageRatePer1k) {
        this.id = id;
        this.name = name;
        this.monthlyQuota = monthlyQuota;
        this.overageRatePer1k = overageRatePer1k;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMonthlyQuota() {
        return monthlyQuota;
    }

    public void setMonthlyQuota(Integer monthlyQuota) {
        this.monthlyQuota = monthlyQuota;
    }

    public BigDecimal getOverageRatePer1k() {
        return overageRatePer1k;
    }

    public void setOverageRatePer1k(BigDecimal overageRatePer1k) {
        this.overageRatePer1k = overageRatePer1k;
    }
}