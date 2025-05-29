package com.hyperformancelabs.backend.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    private Integer orderItemId;

    private Integer orderId;

    private Integer productVariantId;

    private Integer quantity;

    private BigDecimal unitPrice;

    private String note;
}
