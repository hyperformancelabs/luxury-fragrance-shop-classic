package com.hyperformancelabs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleProductDTO {
    private Integer productId;
    private String productName;

    private Integer productPromotionId;
    private BigDecimal maxDiscountAmount;
    private String conditionJson;

    private String promotionName;
    private String discountType; // e.g., 'percent' or 'fixed'
    private BigDecimal discountValue;
    private Integer usageLimit;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
}

