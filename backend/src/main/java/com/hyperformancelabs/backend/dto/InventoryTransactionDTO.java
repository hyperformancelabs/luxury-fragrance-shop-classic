package com.hyperformancelabs.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionDTO {

    private Integer inventoryTransactionId;

    private Integer productVariantId; // Chỉ cần ID của productVariant

    private Integer performedByEmployeeId; // Chỉ cần ID của Employee

    private String transactionType;

    private LocalDateTime transactionDate;

    private Integer beforeQuantity;

    private Integer quantity;

    private Integer afterQuantity;

    private String reason;

    private String note;

    private BigDecimal costPrice;
}
