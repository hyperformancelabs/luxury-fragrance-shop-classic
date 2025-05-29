package com.hyperformancelabs.backend.repository;

import com.hyperformancelabs.backend.model.OrderPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderPromotionRepository extends JpaRepository<OrderPromotion, Integer> {
}
