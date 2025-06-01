package com.hyperformancelabs.backend.repository;

import com.hyperformancelabs.backend.model.TermsAndConditions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TermsAndConditionsRepository extends JpaRepository<TermsAndConditions, Long> {
    // Find the most recent terms and conditions
    TermsAndConditions findFirstByOrderByLastUpdatedDesc();
} 