package com.hyperformancelabs.backend.service;

import com.hyperformancelabs.backend.model.Employee;

public interface AdminPasswordResetService {

    /**
     * Create and send password reset token for admin employee
     * @param email employee email
     * @return true if email is sent (or would be sent), false for validation errors
     */
    boolean createPasswordResetToken(String email);

    /**
     * Validate token and return associated employee if valid
     * @param token the reset token
     * @return Employee if token is valid, null otherwise
     */
    Employee validateToken(String token);

    /**
     * Reset password using valid token
     * @param token the reset token
     * @param newPassword the new password
     * @return true if password was reset successfully
     */
    boolean resetPassword(String token, String newPassword);

    /**
     * Cleanup expired tokens (called by scheduled task)
     */
    void cleanupExpiredTokens();

    /**
     * Invalidate all tokens for an employee
     * @param employee the employee
     */
    void invalidateAllTokensForEmployee(Employee employee);
} 