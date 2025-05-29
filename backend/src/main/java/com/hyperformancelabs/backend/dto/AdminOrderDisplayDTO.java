package com.hyperformancelabs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminOrderDisplayDTO {
    private Integer orderId;
    private String customerName;
    private String email;
    private String phone;
    private BigDecimal totalAmount;
    private String orderStatus;
    private LocalDate orderDate;
    private Integer itemCount;
    private String paymentMethod;
    private String shippingAddress;
}
