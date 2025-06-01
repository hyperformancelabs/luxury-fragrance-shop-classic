package com.hyperformancelabs.backend.service;

import com.hyperformancelabs.backend.model.Order;

import java.util.List;
import java.util.Map;

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
    
    /**
     * Send order confirmation email
     * @param toEmail recipient email address
     * @param customerName customer name for personalization
     * @param order order information
     * @param orderItems list of items in the order
     * @param subtotal subtotal amount
     * @param shipping shipping cost
     * @param total total order amount
     */
    void sendOrderConfirmationEmail(String toEmail, 
                                   String customerName, 
                                   Order order,
                                   List<Map<String, Object>> orderItems,
                                   int subtotal,
                                   int shipping,
                                   int total);
} 