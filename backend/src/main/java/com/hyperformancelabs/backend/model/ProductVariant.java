package com.hyperformancelabs.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "[ProductVariant]", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_ProductVariant", columnNames = {"product_id", "volume"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_variant_id")
    private Integer productVariantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "Volume cannot be empty")
    @Positive(message = "Volume must be positive")
    @Column(name = "volume", nullable = false)
    private Integer volume;

    @NotNull(message = "Price cannot be empty")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = true, message = "Discount price cannot be negative")
    @Column(name = "discount_price", precision = 10, scale = 2)
    private BigDecimal discountPrice;

    @NotNull(message = "Quantity in stock cannot be empty")
    @Min(value = 0, message = "Quantity in stock cannot be negative")
    @Column(name = "quantity_in_stock", nullable = false)
    private Integer quantityInStock;

    @Min(value = 0, message = "Reorder level cannot be negative")
    @Column(name = "reorder_level")
    private Integer reorderLevel;
}
