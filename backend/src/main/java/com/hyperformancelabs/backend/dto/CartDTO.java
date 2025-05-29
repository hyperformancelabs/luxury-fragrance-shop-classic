package com.hyperformancelabs.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {

    private Integer cartId;

    @NotNull(message = "Customer ID is required")
    private Integer customerId;

    @NotBlank(message = "Status is required")
    @Size(max = 20, message = "Status must be at most 20 characters")
    private String status;

    @DecimalMin(value = "0.00", inclusive = true, message = "Total amount cannot be negative")
    private BigDecimal totalAmount;

    @Size(max = 50, message = "Session ID must be at most 50 characters")
    private String sessionId;
}
