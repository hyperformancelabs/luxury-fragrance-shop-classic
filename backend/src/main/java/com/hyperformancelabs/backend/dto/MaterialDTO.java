package com.hyperformancelabs.backend.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDTO {

    private Integer materialId;

    private String materialName;

    private String description;

    private String unit;

    private Integer quantityInStock;

    private Integer reorderLevel;

    private BigDecimal price;
}
