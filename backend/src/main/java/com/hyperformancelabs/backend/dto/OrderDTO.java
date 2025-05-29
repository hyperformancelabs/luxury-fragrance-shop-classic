package com.hyperformancelabs.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Integer orderId;

    private Integer customerId;

    private Integer employeeId;

    private LocalDateTime orderDate;

    private BigDecimal totalAmount;

    private BigDecimal shippingFee;

    private String orderStatus;

    private String shippingAddress;

    private String shippingOption;

    private String note;

    private LocalDate estimatedDeliveryDate;
}
