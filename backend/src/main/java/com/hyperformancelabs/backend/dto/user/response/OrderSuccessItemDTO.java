package com.hyperformancelabs.backend.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSuccessItemDTO {
    private String productName;
    private String volume;
    private int quantity;
    private BigDecimal unitPrice;
    private String imageUrl;
} 