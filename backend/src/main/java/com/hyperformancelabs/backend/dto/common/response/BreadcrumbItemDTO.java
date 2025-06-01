package com.hyperformancelabs.backend.dto.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for breadcrumb navigation items
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BreadcrumbItemDTO {
    private String name;
    private String url;
    
    /**
     * Create a breadcrumb item with name only (no URL, used for current page)
     */
    public static BreadcrumbItemDTO createCurrentPage(String name) {
        return new BreadcrumbItemDTO(name, null);
    }
    
    /**
     * Create a breadcrumb item with name and URL (used for parent pages)
     */
    public static BreadcrumbItemDTO createLink(String name, String url) {
        return new BreadcrumbItemDTO(name, url);
    }
} 