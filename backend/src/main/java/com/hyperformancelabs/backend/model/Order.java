package com.hyperformancelabs.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "[Order]") // SQL reserved keyword, using square brackets to escape it
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "order_date", insertable = false, updatable = false)
    private LocalDateTime orderDate;

    @DecimalMin(value = "0.0", inclusive = true, message = "Total amount cannot be negative")
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Shipping fee cannot be negative")
    @Column(name = "shipping_fee", precision = 10, scale = 2)
    private BigDecimal shippingFee;

    @NotBlank(message = "Order status cannot be empty")
    @Pattern(regexp = "pending|processing|shipping|delivered|cancelled", message = "Invalid order status")
    @Column(name = "order_status", nullable = false, length = 20)
    private String orderStatus;

    @NotBlank(message = "Shipping address cannot be empty")
    @Column(name = "shipping_address", nullable = false, length = 255)
    private String shippingAddress;

    @NotBlank(message = "Shipping option cannot be empty")
    @Column(name = "shipping_option", nullable = false, length = 50)
    private String shippingOption;

    @Column(name = "note", columnDefinition = "NVARCHAR(MAX)")
    private String note;

    @Future(message = "Estimated delivery date must be in the future")
    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;
}
