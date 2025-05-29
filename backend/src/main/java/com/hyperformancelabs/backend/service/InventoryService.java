package com.hyperformancelabs.backend.service;

import com.hyperformancelabs.backend.dto.LowStockProductDTO;

import java.util.List;

public interface InventoryService {
    /**
     * Get products with low stock levels (below or equal to reorder level)
     * @param limit Number of products to return
     * @return List of products with low stock levels, sorted by stock level (ascending)
     */
    List<LowStockProductDTO> getLowStockProducts(int limit);
}
