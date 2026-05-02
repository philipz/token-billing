package org.tw.token_billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tw.token_billing.entity.Subscription;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository for Subscription entity operations.
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, java.util.UUID> {

    /**
     * Find active subscription for a customer on a given date per SRS-F-7.
     */
    @Query("SELECT s FROM Subscription s JOIN FETCH s.plan p " +
           "WHERE s.customerId = :customerId " +
           "AND s.effectiveFrom <= :today " +
           "AND (s.effectiveTo IS NULL OR s.effectiveTo >= :today)")
    Optional<Subscription> findActiveSubscription(
            @Param("customerId") String customerId,
            @Param("today") LocalDate today);
}