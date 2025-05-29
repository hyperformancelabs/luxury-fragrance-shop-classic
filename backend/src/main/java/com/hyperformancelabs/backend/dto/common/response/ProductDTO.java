package com.hyperformancelabs.backend.dto.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Integer productId;

    private String brandName;

    private String productName;

    private String description;

    private String imageUrl;

    private Double averageRating;

    private Integer totalReviews;
} 