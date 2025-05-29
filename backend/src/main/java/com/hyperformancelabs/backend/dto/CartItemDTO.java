package com.hyperformancelabs.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {

    private Integer cartItemId;

    @NotNull(message = "Cart ID is required")
    private Integer cartId;

    @NotNull(message = "Product variant ID is required")
    private Integer productVariantId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Unit price cannot be negative")
    private BigDecimal unitPrice;

    @Size(max = 255, message = "Note must be at most 255 characters")
    private String note;

    @NotNull(message = "Selection status is required")
    private Boolean isSelected;
}
