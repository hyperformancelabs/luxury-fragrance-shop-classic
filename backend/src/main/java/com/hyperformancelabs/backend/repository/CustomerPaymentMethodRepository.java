package com.hyperformancelabs.backend.repository;

import com.hyperformancelabs.backend.model.CustomerPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerPaymentMethodRepository extends JpaRepository<CustomerPaymentMethod, Integer> {
}
