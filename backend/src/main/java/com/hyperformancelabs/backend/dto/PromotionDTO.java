package com.hyperformancelabs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDTO {

    private Integer promotionId;

    private String promotionName;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    private String discountType;

    private BigDecimal discountValue;

    private String status;

    private Integer usageLimit;
}
