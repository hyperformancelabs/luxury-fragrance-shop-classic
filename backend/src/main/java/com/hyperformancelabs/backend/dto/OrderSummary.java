package com.hyperformancelabs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummary {
    private Integer orderId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String shippingOption;
}

