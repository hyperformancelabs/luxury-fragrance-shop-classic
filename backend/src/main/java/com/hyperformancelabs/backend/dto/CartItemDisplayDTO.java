package com.hyperformancelabs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDisplayDTO {
    private Integer cartItemId;
    private Integer productId;
    private Integer productVariantId;
    private String productName;
    private String brandName;
    private String imageUrl;
    private Integer volume;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private Boolean isSelected;
    private String note;
}
