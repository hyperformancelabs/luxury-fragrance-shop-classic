package com.hyperformancelabs.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

    private Integer paymentId;

    private Integer orderId;

    private LocalDateTime paymentDate;

    private BigDecimal amount;

    private String paymentStatus;

    private String transactionId;

    private String note;

    private String currency;
}
