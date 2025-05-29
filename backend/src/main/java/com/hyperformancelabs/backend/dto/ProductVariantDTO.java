package com.hyperformancelabs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDTO {

    private Integer productVariantId;

    private Integer productId;

    private Integer volume;

    private BigDecimal price;

    private BigDecimal discountPrice;

    private Integer quantityInStock;

    private Integer reorderLevel;
}
