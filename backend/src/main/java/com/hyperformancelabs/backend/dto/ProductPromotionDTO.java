package com.hyperformancelabs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPromotionDTO {

    private Integer productPromotionId;

    private Integer productId;

    private Integer promotionId;

    private String conditionJson;

    private BigDecimal maxDiscountAmount;

    private LocalDate startDate;

    private LocalDate endDate;

    private String status;
}
