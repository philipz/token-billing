package org.tw.token_billing.repository;

import jakarta.persistence.Lock;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import jakarta.persistence.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tw.token_billing.entity.Customer;

import java.util.Optional;

/**
 * Repository for Customer entity operations.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {

    /**
     * Find customer by ID with pessimistic write lock per SRS-F-11.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")})
    @Query("SELECT c FROM Customer c WHERE c.id = :id")
    Optional<Customer> findByIdForUpdate(@Param("id") String id);
}