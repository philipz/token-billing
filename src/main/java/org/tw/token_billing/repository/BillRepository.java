package org.tw.token_billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tw.token_billing.entity.Bill;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository for Bill entity operations.
 */
@Repository
public interface BillRepository extends JpaRepository<Bill, String> {

    /**
     * Get current month usage for a customer per SRS-F-8.
     * Returns Long because Hibernate 6 SUM returns Long.
     */
    @Query("SELECT COALESCE(SUM(b.totalTokens), 0) FROM Bill b " +
           "WHERE b.customerId = :customerId " +
           "AND b.calculatedAt >= :monthStart " +
           "AND b.calculatedAt < :monthEnd")
    Long getCurrentMonthUsage(
            @Param("customerId") String customerId,
            @Param("monthStart") Instant monthStart,
            @Param("monthEnd") Instant monthEnd);
}