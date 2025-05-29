package com.hyperformancelabs.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "[Material]")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id")
    private Integer materialId;

    @NotBlank(message = "Material name cannot be empty")
    @Column(name = "material_name", nullable = false, length = 100, unique = true)
    private String materialName;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @NotBlank(message = "Unit cannot be empty")
    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    @NotNull(message = "Quantity in stock cannot be empty")
    @Min(value = 0, message = "Quantity in stock cannot be negative")
    @Column(name = "quantity_in_stock", nullable = false)
    private Integer quantityInStock;

    @Min(value = 0, message = "Reorder level cannot be negative")
    @Column(name = "reorder_level")
    private Integer reorderLevel;

    @NotNull(message = "Price cannot be empty")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Getters and Setters
}
