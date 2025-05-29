package com.hyperformancelabs.backend.dto.user.response;

// import com.hyperformancelabs.backend.dto.CartItemDTO; // Old import - will be replaced by the new one if needed, or remove if same package
import com.hyperformancelabs.backend.dto.user.response.CartItemDTO; // Corrected import
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