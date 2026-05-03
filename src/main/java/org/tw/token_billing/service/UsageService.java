package org.tw.token_billing.service;

import org.tw.token_billing.dto.UsageRequest;
import org.tw.token_billing.dto.UsageResponse;
import org.tw.token_billing.exception.CustomerNotFoundException;
import org.tw.token_billing.exception.InvalidCustomerIdFormatException;
import org.tw.token_billing.exception.NoActiveSubscriptionException;
import org.tw.token_billing.repository.CustomerRepository;
import org.tw.token_billing.repository.SubscriptionRepository;
import org.tw.token_billing.repository.BillRepository;
import org.tw.token_billing.entity.Subscription;
import org.tw.token_billing.entity.Bill;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for processing token usage billing.
 * Handles business logic per SRS-F-9 and SRS-F-10.
 */
@Service
@Transactional
public class UsageService {

    private final CustomerRepository customerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BillRepository billRepository;

    public UsageService(
            CustomerRepository customerRepository,
            SubscriptionRepository subscriptionRepository,
            BillRepository billRepository) {
        this.customerRepository = customerRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.billRepository = billRepository;
    }

    /**
     * Process token usage for a customer.
     * @param request the usage request
     * @return usage response with billing details
     */
    public UsageResponse processUsage(UsageRequest request) {
        String customerId = request.customerId();

        // Validate customer exists and acquire lock (SRS-F-6 step 4, SRS-F-11)
        customerRepository.findByIdForUpdate(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // Resolve active subscription (SRS-F-7)
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Subscription subscription = subscriptionRepository.findActiveSubscription(customerId, today)
                .orElseThrow(() -> new NoActiveSubscriptionException(customerId));

        // Calculate current month usage (SRS-F-8)
        Instant monthStart = today.withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant(null);
        Instant monthEnd = today.withDayOfMonth(1).plusMonths(1).atStartOfDay(ZoneOffset.UTC).toInstant(null);
        long currentMonthUsage = billRepository.getCurrentMonthUsage(customerId, monthStart, monthEnd);

        // Calculate token usage (SRS-F-9)
        int totalTokens = request.promptTokens() + request.completionTokens();
        int monthlyQuota = subscription.getPlan().getMonthlyQuota();
        int remainingQuota = Math.max(0, monthlyQuota - (int) currentMonthUsage);
        int tokensFromQuota = Math.min(totalTokens, remainingQuota);
        int overageTokens = totalTokens - tokensFromQuota;

        // Calculate charge (SRS-F-10)
        BigDecimal overageRate = subscription.getPlan().getOverageRatePer1k();
        BigDecimal totalCharge = calculateTotalCharge(
                BigDecimal.valueOf(overageTokens),
                overageRate
        );

        // Create and save bill
        Instant calculatedAt = Instant.now();
        Bill bill = new Bill(
                UUID.randomUUID().toString(),
                customerId,
                request.promptTokens(),
                request.completionTokens(),
                totalTokens,
                tokensFromQuota,
                overageTokens,
                totalCharge,
                calculatedAt
        );
        billRepository.save(bill);

        return new UsageResponse(
                bill.getId(),
                customerId,
                request.promptTokens(),
                request.completionTokens(),
                totalTokens,
                tokensFromQuota,
                overageTokens,
                totalCharge,
                "USD",
                calculatedAt
        );
    }

    /**
     * Calculate total charge using BigDecimal per SRS-F-10.
     */
    private BigDecimal calculateTotalCharge(BigDecimal overageTokens, BigDecimal ratePer1k) {
        MathContext mc = new MathContext(10);
        BigDecimal charge = overageTokens
                .divide(BigDecimal.valueOf(1000), mc)
                .multiply(ratePer1k, mc)
                .setScale(2, RoundingMode.HALF_EVEN);
        return charge;
    }
}