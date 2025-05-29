package com.hyperformancelabs.backend.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPromotionDTO {

    private Integer orderPromotionId;

    private Integer orderId;

    private Integer promotionId;

    private BigDecimal discountAmount;

    private String note;
}
