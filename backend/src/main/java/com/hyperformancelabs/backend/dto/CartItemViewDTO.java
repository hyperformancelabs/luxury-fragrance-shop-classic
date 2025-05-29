package com.hyperformancelabs.backend.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CartItemViewDTO extends CartItemDTO {
    private String name;
    private String imageUrl;
    private String size;
    private BigDecimal price;

    public CartItemViewDTO(Integer cartItemId, Integer cartId, Integer productVariantId, Integer quantity,
                          BigDecimal unitPrice, String note, Boolean isSelected,
                          String name, String imageUrl, String size, BigDecimal price) {
        super(cartItemId, cartId, productVariantId, quantity, unitPrice, note, isSelected);
        this.name = name;
        this.imageUrl = imageUrl;
        this.size = size;
        this.price = price;
    }
}
