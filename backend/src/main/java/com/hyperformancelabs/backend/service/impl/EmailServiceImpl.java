package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.model.Order;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

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
    
    @Override
    public void sendOrderConfirmationEmail(String toEmail, 
                                         String customerName, 
                                         Order order,
                                         List<Map<String, Object>> orderItems,
                                         int subtotal,
                                         int shipping,
                                         int total) {
        try {
            if (toEmail == null || toEmail.trim().isEmpty()) {
                logger.warn("Email address is empty, skipping order confirmation email");
                return;
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Shop Nước Hoa Xa Xỉ");
            helper.setTo(toEmail);
            helper.setSubject("Xác nhận đơn hàng #" + order.getOrderId() + " - Shop Nước Hoa Xa Xỉ");

            // Format date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String orderDate = LocalDateTime.now().format(formatter);
            
            // Create Thymeleaf context
            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("orderId", order.getOrderId());
            context.setVariable("orderDate", orderDate);
            context.setVariable("paymentMethod", "Thanh toán khi nhận hàng (COD)");
            context.setVariable("shippingAddress", order.getShippingAddress());
            context.setVariable("orderItems", orderItems);
            context.setVariable("subtotal", subtotal);
            context.setVariable("shipping", shipping);
            context.setVariable("total", total);
            context.setVariable("trackingUrl", baseUrl + "/order/track?orderId=" + order.getOrderId());
            context.setVariable("shopUrl", baseUrl);

            // Process template
            String htmlContent = templateEngine.process("shared/email/order-confirmation", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Order confirmation email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send order confirmation email to: " + toEmail, e);
            // Don't throw exception here to prevent order processing failure
            logger.error("Error details:", e);
        } catch (Exception e) {
            logger.error("Unexpected error while sending order confirmation email to: " + toEmail, e);
            // Don't throw exception here to prevent order processing failure
            logger.error("Error details:", e);
        }
    }
} 