package com.hyperformancelabs.backend.repository;

import com.hyperformancelabs.backend.model.PasswordResetToken;
import com.hyperformancelabs.backend.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    
    Optional<PasswordResetToken> findByTokenAndIsUsedFalse(String token);
    
    @Query("SELECT p FROM PasswordResetToken p WHERE p.customer.customerId = :customerId AND p.isUsed = false AND p.expiresAt > :now")
    Optional<PasswordResetToken> findValidTokenByCustomerId(@Param("customerId") Integer customerId, @Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.isUsed = true WHERE p.customer.customerId = :customerId AND p.isUsed = false")
    void invalidateAllTokensByCustomerId(@Param("customerId") Integer customerId);
    
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
} 