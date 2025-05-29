package com.hyperformancelabs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecentOrderDTO {
    Integer orderId;
    Integer customerId;
    LocalDateTime orderDate;
    String orderStatus;
    BigDecimal totalAmount;
}
