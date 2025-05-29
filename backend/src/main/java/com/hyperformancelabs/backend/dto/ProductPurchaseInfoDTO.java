package com.hyperformancelabs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPurchaseInfoDTO {
    private Integer productId;
    private String productName;
    private String imageUrl;
    private Integer volume;
    private BigDecimal price;
}
