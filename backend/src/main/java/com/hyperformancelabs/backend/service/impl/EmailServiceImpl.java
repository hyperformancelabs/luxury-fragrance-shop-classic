package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendPasswordResetEmail(String toEmail, String customerName, String verificationCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Shop Nước Hoa Xa Xỉ");
            helper.setTo(toEmail);
            helper.setSubject("Đặt lại mật khẩu - Shop Nước Hoa Xa Xỉ");

            // Create Thymeleaf context
            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("code", verificationCode);
            context.setVariable("expiresAt", LocalDateTime.now().plusMinutes(15));

            // Process template
            String htmlContent = templateEngine.process("shared/email/password-reset", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Password reset email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send password reset email to: " + toEmail, e);
            throw new RuntimeException("Không thể gửi email đặt lại mật khẩu", e);
        } catch (Exception e) {
            logger.error("Unexpected error while sending password reset email to: " + toEmail, e);
            throw new RuntimeException("Lỗi hệ thống khi gửi email", e);
        }
    }

    @Override
    public void sendAdminPasswordResetEmail(String toEmail, String employeeName, String verificationCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Hệ thống Quản trị - Shop Nước Hoa Xa Xỉ");
            helper.setTo(toEmail);
            helper.setSubject("Đặt lại mật khẩu Admin - Shop Nước Hoa Xa Xỉ");

            // Create Thymeleaf context
            Context context = new Context();
            context.setVariable("employeeName", employeeName);
            context.setVariable("code", verificationCode);
            context.setVariable("expiresAt", LocalDateTime.now().plusMinutes(15));

            // Process template
            String htmlContent = templateEngine.process("shared/email/admin-password-reset", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Admin password reset email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send admin password reset email to: " + toEmail, e);
            throw new RuntimeException("Không thể gửi email đặt lại mật khẩu admin", e);
        } catch (Exception e) {
            logger.error("Unexpected error while sending admin password reset email to: " + toEmail, e);
            throw new RuntimeException("Lỗi hệ thống khi gửi email", e);
        }
    }

    @Override
    public void sendEmailVerificationCode(String toEmail, String customerName, String verificationCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Shop Nước Hoa Xa Xỉ");
            helper.setTo(toEmail);
            helper.setSubject("Xác thực email - Shop Nước Hoa Xa Xỉ");

            // Create Thymeleaf context
            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("code", verificationCode);
            context.setVariable("expiresAt", LocalDateTime.now().plusMinutes(15));

            // Process template
            String htmlContent = templateEngine.process("shared/email/email-verification", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Email verification code sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send email verification code to: " + toEmail, e);
            throw new RuntimeException("Không thể gửi mã xác thực email", e);
        } catch (Exception e) {
            logger.error("Unexpected error while sending email verification code to: " + toEmail, e);
            throw new RuntimeException("Lỗi hệ thống khi gửi email", e);
        }
    }
} 