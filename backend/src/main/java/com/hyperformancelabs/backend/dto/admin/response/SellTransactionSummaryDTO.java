package com.hyperformancelabs.backend.dto.admin.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate; // This import seems unused, can be removed if not needed later

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellTransactionSummaryDTO {
    private int totalSellTransactions;
    private BigDecimal totalCostPrice;
} 