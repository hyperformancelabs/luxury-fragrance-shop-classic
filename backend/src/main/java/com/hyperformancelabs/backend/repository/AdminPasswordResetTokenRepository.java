package com.hyperformancelabs.backend.repository;

import com.hyperformancelabs.backend.model.AdminPasswordResetToken;
import com.hyperformancelabs.backend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminPasswordResetTokenRepository extends JpaRepository<AdminPasswordResetToken, Integer> {

    /**
     * Find a valid token by token string
     */
    @Query("SELECT t FROM AdminPasswordResetToken t WHERE t.token = :token AND t.isUsed = false AND t.expiresAt > :now")
    Optional<AdminPasswordResetToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    /**
     * Find tokens by employee
     */
    List<AdminPasswordResetToken> findByEmployeeOrderByCreatedAtDesc(Employee employee);

    /**
     * Find all valid tokens for an employee
     */
    @Query("SELECT t FROM AdminPasswordResetToken t WHERE t.employee = :employee AND t.isUsed = false AND t.expiresAt > :now")
    List<AdminPasswordResetToken> findValidTokensByEmployee(@Param("employee") Employee employee, @Param("now") LocalDateTime now);

    /**
     * Invalidate all tokens for an employee
     */
    @Modifying
    @Transactional
    @Query("UPDATE AdminPasswordResetToken t SET t.isUsed = true, t.usedAt = :usedAt WHERE t.employee = :employee AND t.isUsed = false")
    void invalidateAllTokensForEmployee(@Param("employee") Employee employee, @Param("usedAt") LocalDateTime usedAt);

    /**
     * Delete expired tokens
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM AdminPasswordResetToken t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Count active tokens for an employee
     */
    @Query("SELECT COUNT(t) FROM AdminPasswordResetToken t WHERE t.employee = :employee AND t.isUsed = false AND t.expiresAt > :now")
    long countActiveTokensForEmployee(@Param("employee") Employee employee, @Param("now") LocalDateTime now);
} 