package com.hyperformancelabs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSuccessDTO {

    private Integer orderId;
    private LocalDateTime orderDate;
    private String paymentMethod;

    private String shippingAddress;
    private String customerName;
    private String customerPhone;

    private List<OrderSuccessItemDTO> items;

    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal total;
}
