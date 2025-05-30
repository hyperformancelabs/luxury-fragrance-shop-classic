package com.hyperformancelabs.backend.config;

import com.hyperformancelabs.backend.service.AdminPasswordResetService;
import com.hyperformancelabs.backend.service.PasswordResetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private AdminPasswordResetService adminPasswordResetService;

    /**
     * Clean up expired password reset tokens every hour
     * Initial delay of 5 minutes to ensure application startup is complete
     */
    @Scheduled(fixedRate = 3600000, initialDelay = 300000) // Run every hour with 5 min initial delay
    public void cleanupExpiredTokens() {
        logger.info("Starting cleanup of expired password reset tokens");
        try {
            // Cleanup customer tokens
            passwordResetService.cleanupExpiredTokens();
            logger.info("Successfully cleaned up expired customer password reset tokens");
            
            // Cleanup admin tokens
            adminPasswordResetService.cleanupExpiredTokens();
            logger.info("Successfully cleaned up expired admin password reset tokens");
            
        } catch (Exception e) {
            // Check if it's a table not found error (during startup/migration)
            if (e.getMessage() != null && (e.getMessage().contains("PasswordResetToken") || 
                                         e.getMessage().contains("AdminPasswordResetToken"))) {
                logger.warn("Password reset token tables not found - migration may still be running. Skipping cleanup.");
            } else {
                logger.error("Error during cleanup of expired password reset tokens", e);
            }
        }
    }
} 