package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.model.Customer;
import com.hyperformancelabs.backend.model.PasswordResetToken;
import com.hyperformancelabs.backend.repository.CustomerRepository;
import com.hyperformancelabs.backend.repository.PasswordResetTokenRepository;
import com.hyperformancelabs.backend.service.EmailService;
import com.hyperformancelabs.backend.service.PasswordResetService;
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
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetServiceImpl.class);
    private static final int TOKEN_EXPIRY_MINUTES = 15;
    private static final int TOKEN_LENGTH = 6;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String requestPasswordReset(String email) {
        logger.info("Password reset requested for email: {}", email);

        // Find customer by email
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);
        if (customerOpt.isEmpty()) {
            logger.warn("Password reset requested for non-existent email: {}", email);
            // Don't reveal if email exists or not for security
            return "Nếu email này tồn tại trong hệ thống, mã xác thực sẽ được gửi đến email của bạn.";
        }

        Customer customer = customerOpt.get();

        // Check if customer is active
        if (!"active".equalsIgnoreCase(customer.getStatus())) {
            logger.warn("Password reset requested for inactive customer: {}", email);
            throw new RuntimeException("Tài khoản của bạn đang bị khóa hoặc không hoạt động.");
        }

        // Invalidate all existing tokens for this customer
        tokenRepository.invalidateAllTokensByCustomerId(customer.getCustomerId());

        // Generate new token
        String token = generateSecureToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES);

        // Save token to database
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setCustomer(customer);
        resetToken.setToken(token);
        resetToken.setExpiresAt(expiresAt);
        resetToken.setCreatedAt(LocalDateTime.now());
        resetToken.setIsUsed(false);

        tokenRepository.save(resetToken);

        // Send email
        try {
            emailService.sendPasswordResetEmail(customer.getEmail(), customer.getName(), token);
            logger.info("Password reset email sent successfully to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", email, e);
            throw new RuntimeException("Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại sau.");
        }

        return "Mã xác thực đã được gửi đến email của bạn. Vui lòng kiểm tra email và làm theo hướng dẫn.";
    }

    @Override
    public Customer verifyResetToken(String token) {
        logger.info("Verifying reset token: {}", token.substring(0, Math.min(token.length(), 3)) + "***");

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByTokenAndIsUsedFalse(token);
        if (tokenOpt.isEmpty()) {
            logger.warn("Invalid or used reset token: {}", token);
            throw new RuntimeException("Mã xác thực không hợp lệ hoặc đã được sử dụng.");
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.isExpired()) {
            logger.warn("Expired reset token: {}", token);
            throw new RuntimeException("Mã xác thực đã hết hạn. Vui lòng yêu cầu mã mới.");
        }

        return resetToken.getCustomer();
    }

    @Override
    public String resetPassword(String token, String newPassword) {
        logger.info("Resetting password with token: {}", token.substring(0, Math.min(token.length(), 3)) + "***");

        // Verify token
        Customer customer = verifyResetToken(token);

        // Update password
        customer.setPassword(passwordEncoder.encode(newPassword));
        customer.setUpdateAt(LocalDateTime.now());
        customerRepository.save(customer);

        // Mark token as used
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByTokenAndIsUsedFalse(token);
        if (tokenOpt.isPresent()) {
            PasswordResetToken resetToken = tokenOpt.get();
            resetToken.setIsUsed(true);
            resetToken.setUsedAt(LocalDateTime.now());
            tokenRepository.save(resetToken);
        }

        logger.info("Password reset successfully for customer: {}", customer.getEmail());
        return "Mật khẩu đã được thay đổi thành công. Vui lòng đăng nhập bằng mật khẩu mới.";
    }

    @Override
    public void cleanupExpiredTokens() {
        logger.info("Cleaning up expired password reset tokens");
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    private String generateSecureToken() {
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(secureRandom.nextInt(10));
        }
        return token.toString();
    }
} 