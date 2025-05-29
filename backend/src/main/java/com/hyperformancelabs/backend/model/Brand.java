package com.hyperformancelabs.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "[Brand]")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id")
    private Integer brandId;

    @NotBlank(message = "Brand name cannot be empty")
    @Column(name = "brand_name", nullable = false, length = 100, unique = true)
    private String brandName;

    @Column(name = "brand_description", columnDefinition = "NVARCHAR(MAX)")
    private String brandDescription;

    @Column(name = "country_of_origin", length = 100)
    private String countryOfOrigin;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Pattern(regexp = "^(http://|https://|).*$", message = "Invalid website URL format")
    @Column(name = "website_url", length = 255)
    private String websiteUrl;
}
