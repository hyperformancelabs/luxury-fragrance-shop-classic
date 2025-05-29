package com.hyperformancelabs.backend.repository;

import com.hyperformancelabs.backend.model.ProductPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductPromotionRepository extends JpaRepository<ProductPromotion, Integer> {
}
