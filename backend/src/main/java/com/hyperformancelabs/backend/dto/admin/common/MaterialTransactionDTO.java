package com.hyperformancelabs.backend.dto.admin.common;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialTransactionDTO {

    private Integer materialTransactionId;

    private Integer materialId;

    private Integer performedById; // Should be employeeId if performed by an employee

    private LocalDateTime transactionDate;

    private Integer beforeQuantity;

    private Integer quantity;

    private Integer afterQuantity;

    private String transactionType;

    private String reason;

    private String note;

    private BigDecimal costPrice;
} 