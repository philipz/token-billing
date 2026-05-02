package org.tw.token_billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tw.token_billing.entity.Customer;

/**
 * Repository for Customer entity operations.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
}