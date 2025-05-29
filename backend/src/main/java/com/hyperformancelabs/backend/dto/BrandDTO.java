package com.hyperformancelabs.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandDTO {

    private Integer brandId;

    @NotBlank(message = "Brand name cannot be empty")
    @Size(max = 100, message = "Brand name must be at most 100 characters")
    private String brandName;

    private String brandDescription;

    @Size(max = 100, message = "Country of origin must be at most 100 characters")
    private String countryOfOrigin;

    @Size(max = 255, message = "Logo URL must be at most 255 characters")
    private String logoUrl;

    @Pattern(regexp = "^(http://|https://|)?[\\w.-]+(?:\\.[\\w\\.-]+)+[/#?]?.*$",
            message = "Invalid website URL format")
    @Size(max = 255, message = "Website URL must be at most 255 characters")
    private String websiteUrl;
}
