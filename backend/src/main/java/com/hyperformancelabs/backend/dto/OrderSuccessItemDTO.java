package com.hyperformancelabs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSuccessItemDTO {

    private String name;
    private String size;
    private Integer quantity;
    private BigDecimal price;
    private String imageUrl;
}
