package com.hyperformancelabs.backend.service;

import com.hyperformancelabs.backend.model.Customer;

public interface PasswordResetService {
    
    /**
     * Request password reset - generate token and send email
     * @param email customer email
     * @return success message or throws exception
     */
    String requestPasswordReset(String email);
    
    /**
     * Verify reset token and return customer if valid
     * @param token reset token
     * @return customer if token is valid
     */
    Customer verifyResetToken(String token);
    
    /**
     * Reset password using valid token
     * @param token reset token
     * @param newPassword new password (plain text, will be encoded)
     * @return success message or throws exception
     */
    String resetPassword(String token, String newPassword);
    
    /**
     * Clean up expired tokens (called by scheduled task)
     */
    void cleanupExpiredTokens();
} 