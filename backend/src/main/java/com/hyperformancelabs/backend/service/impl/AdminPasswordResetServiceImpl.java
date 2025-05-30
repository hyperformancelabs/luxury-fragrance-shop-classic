package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.model.AdminPasswordResetToken;
import com.hyperformancelabs.backend.model.Employee;
import com.hyperformancelabs.backend.repository.AdminPasswordResetTokenRepository;
import com.hyperformancelabs.backend.repository.EmployeeRepository;
import com.hyperformancelabs.backend.service.AdminPasswordResetService;
import com.hyperformancelabs.backend.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AdminPasswordResetServiceImpl implements AdminPasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(AdminPasswordResetServiceImpl.class);
    private static final int TOKEN_EXPIRY_MINUTES = 15;
    private static final int MAX_ACTIVE_TOKENS_PER_EMPLOYEE = 3;

    @Autowired
    private AdminPasswordResetTokenRepository adminPasswordResetTokenRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public boolean createPasswordResetToken(String email) {
        logger.info("Creating password reset token for admin email: {}", email);

        try {
            // Find employee by email
            Optional<Employee> employeeOpt = employeeRepository.findByEmail(email);
            
            if (employeeOpt.isEmpty()) {
                logger.warn("Admin employee not found for email: {}", email);
                // For security, we return true even if email doesn't exist
                // This prevents email enumeration attacks
                return true;
            }

            Employee employee = employeeOpt.get();
            
            // Check if employee is active
            if (!"active".equals(employee.getStatus())) {
                logger.warn("Inactive admin employee attempted password reset: {}", email);
                return true; // Still return true for security
            }

            // Check if employee has too many active tokens
            long activeTokenCount = adminPasswordResetTokenRepository.countActiveTokensForEmployee(employee, LocalDateTime.now());
            if (activeTokenCount >= MAX_ACTIVE_TOKENS_PER_EMPLOYEE) {
                logger.warn("Too many active tokens for admin employee: {}", email);
                return false;
            }

            // Generate secure 6-digit token
            String token = generateSecureToken();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES);

            // Create and save token
            AdminPasswordResetToken resetToken = new AdminPasswordResetToken();
            resetToken.setEmployee(employee);
            resetToken.setToken(token);
            resetToken.setExpiresAt(expiresAt);
            resetToken.setCreatedAt(LocalDateTime.now());
            resetToken.setIsUsed(false);

            adminPasswordResetTokenRepository.save(resetToken);

            // Send email
            emailService.sendAdminPasswordResetEmail(employee.getEmail(), employee.getFullName(), token);

            logger.info("Password reset token created and email sent for admin: {}", email);
            return true;

        } catch (Exception e) {
            logger.error("Error creating password reset token for admin: {}", email, e);
            return false;
        }
    }

    @Override
    public Employee validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        try {
            Optional<AdminPasswordResetToken> tokenOpt = adminPasswordResetTokenRepository
                    .findValidToken(token.trim(), LocalDateTime.now());

            if (tokenOpt.isPresent()) {
                AdminPasswordResetToken resetToken = tokenOpt.get();
                logger.info("Valid admin password reset token found for employee: {}", 
                           resetToken.getEmployee().getEmail());
                return resetToken.getEmployee();
            }

            logger.warn("Invalid or expired admin password reset token: {}", token);
            return null;

        } catch (Exception e) {
            logger.error("Error validating admin password reset token", e);
            return null;
        }
    }

    @Override
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        if (token == null || newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }

        try {
            Optional<AdminPasswordResetToken> tokenOpt = adminPasswordResetTokenRepository
                    .findValidToken(token.trim(), LocalDateTime.now());

            if (tokenOpt.isEmpty()) {
                logger.warn("Invalid token used for admin password reset: {}", token);
                return false;
            }

            AdminPasswordResetToken resetToken = tokenOpt.get();
            Employee employee = resetToken.getEmployee();

            // Refresh employee entity to ensure we have latest data
            Employee refreshedEmployee = employeeRepository.findById(employee.getEmployeeId()).orElse(null);
            if (refreshedEmployee == null) {
                logger.error("Employee not found during password reset: {}", employee.getEmployeeId());
                return false;
            }

            // Encode new password
            String encodedPassword = passwordEncoder.encode(newPassword);

            // Update employee password
            refreshedEmployee.setPassword(encodedPassword);
            employeeRepository.saveAndFlush(refreshedEmployee);

            // Mark token as used
            resetToken.setIsUsed(true);
            resetToken.setUsedAt(LocalDateTime.now());
            adminPasswordResetTokenRepository.save(resetToken);

            // Invalidate all other tokens for this employee
            invalidateAllTokensForEmployee(refreshedEmployee);

            logger.info("Password successfully reset for admin employee: {}", refreshedEmployee.getEmail());
            return true;

        } catch (Exception e) {
            logger.error("Error resetting admin password", e);
            return false;
        }
    }

    @Override
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            adminPasswordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
            logger.info("Cleaned up expired admin password reset tokens");
        } catch (Exception e) {
            logger.error("Error cleaning up expired admin password reset tokens", e);
        }
    }

    @Override
    @Transactional
    public void invalidateAllTokensForEmployee(Employee employee) {
        try {
            adminPasswordResetTokenRepository.invalidateAllTokensForEmployee(employee, LocalDateTime.now());
            logger.info("Invalidated all tokens for admin employee: {}", employee.getEmail());
        } catch (Exception e) {
            logger.error("Error invalidating tokens for admin employee: {}", employee.getEmail(), e);
        }
    }

    /**
     * Generate a secure 6-digit numeric token
     */
    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        int token = 100000 + random.nextInt(900000); // Ensures 6 digits
        return String.valueOf(token);
    }
} 