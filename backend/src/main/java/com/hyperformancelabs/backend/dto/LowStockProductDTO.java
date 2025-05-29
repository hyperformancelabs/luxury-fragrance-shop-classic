package com.hyperformancelabs.backend.dto;

import java.math.BigDecimal;

public class LowStockProductDTO {
    private Integer productVariantId;
    private String productName;
    private String brandName;
    private Integer volume;
    private BigDecimal price;
    private String imageUrl;
    private Integer quantityInStock;
    private Integer reorderLevel;
    
    // Calculate stock percentage for sorting and display
    private Double stockPercentage;

    public LowStockProductDTO(Integer productVariantId, String productName, String brandName,
                              Integer volume, BigDecimal price, String imageUrl,
                              Integer quantityInStock, Integer reorderLevel) {
        this.productVariantId = productVariantId;
        this.productName = productName;
        this.brandName = brandName;
        this.volume = volume;
        this.price = price;
        this.imageUrl = imageUrl;
        this.quantityInStock = quantityInStock;
        this.reorderLevel = reorderLevel;
        
        // Calculate stock percentage (quantity / reorder level)
        if (reorderLevel != null && reorderLevel > 0) {
            this.stockPercentage = (double) quantityInStock / reorderLevel;
        } else {
            this.stockPercentage = 0.0;
        }
    }
    
    // Static factory method to create from database result
    public static LowStockProductDTO fromDatabaseResult(Object[] result) {
        return new LowStockProductDTO(
            (Integer) result[0],         // product_variant_id
            (String) result[1],          // product_name
            (String) result[2],          // brand_name
            (Integer) result[3],         // volume
            (BigDecimal) result[4],      // price
            (String) result[5],          // image_url
            (Integer) result[6],         // quantity_in_stock
            (Integer) result[7]          // reorder_level
        );
    }

    // Getters and setters
    public Integer getProductVariantId() {
        return productVariantId;
    }

    public void setProductVariantId(Integer productVariantId) {
        this.productVariantId = productVariantId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(Integer quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public Double getStockPercentage() {
        return stockPercentage;
    }

    public void setStockPercentage(Double stockPercentage) {
        this.stockPercentage = stockPercentage;
    }
}
