package com.hyperformancelabs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.sql.In;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopSellingDisplayDTO {
    Integer productId;
    String productName;
    Integer volume;
    Integer quantitySold;
    BigDecimal revenue;
    String percentage;

    public TopSellingDisplayDTO(Integer productId, String productName, Integer quantitySold, Integer volume, BigDecimal revenue) {
        this.productId = productId;
        this.productName = productName;
        this.volume = volume;
        this.quantitySold = quantitySold;
        this.revenue = revenue;
    }
}
