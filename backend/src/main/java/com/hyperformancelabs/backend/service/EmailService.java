package com.hyperformancelabs.backend.service;

public interface EmailService {
    
    /**
     * Send password reset email with verification code
     * @param toEmail recipient email address
     * @param customerName customer name for personalization
     * @param verificationCode 6-digit verification code
     */
    void sendPasswordResetEmail(String toEmail, String customerName, String verificationCode);
    
    /**
     * Send password reset email for admin employees
     * @param toEmail recipient email address
     * @param employeeName employee name for personalization
     * @param verificationCode 6-digit verification code
     */
    void sendAdminPasswordResetEmail(String toEmail, String employeeName, String verificationCode);
    
    /**
     * Send email verification code for registration
     * @param toEmail recipient email address
     * @param customerName customer name for personalization
     * @param verificationCode 6-digit verification code
     */
    void sendEmailVerificationCode(String toEmail, String customerName, String verificationCode);
} 