package com.hyperformancelabs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDTO {

    private Integer productDetailId;

    private Integer productId;

    private String detailName;

    private String detailValue;

    private String note;
}
