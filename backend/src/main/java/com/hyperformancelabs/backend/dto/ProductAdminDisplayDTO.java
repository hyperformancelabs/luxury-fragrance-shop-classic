package com.hyperformancelabs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductAdminDisplayDTO {
    private Integer productId;
    private Integer brandId;
    private String imageUrl;
    private String productName;
    private String description;
    private Integer volume;
    private BigDecimal price;
    private Integer quantityInStock;
    private Integer reorderLevel;
    private BigDecimal discountPrice;
    private String categoryName;
}
