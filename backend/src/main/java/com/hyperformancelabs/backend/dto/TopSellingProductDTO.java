package com.hyperformancelabs.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class TopSellingProductDTO {
    // Getters and Setters
    private Integer productId;
    private String productName;
    private String brandName;
    private Integer volume;
    private BigDecimal price;
    private String imageUrl;
    private Integer totalQuantitySold;

    public TopSellingProductDTO(Integer productId, String productName, String brandName,
                                Integer volume, BigDecimal price, String imageUrl,
                                Integer totalQuantitySold) {
        this.productId = productId;
        this.productName = productName;
        this.brandName = brandName;
        this.volume = volume;
        this.price = price;
        this.imageUrl = imageUrl;
        this.totalQuantitySold = totalQuantitySold;
    }

}
