package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.dto.LowStockProductDTO;
import com.hyperformancelabs.backend.repository.ProductRepository;
import com.hyperformancelabs.backend.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<LowStockProductDTO> getLowStockProducts(int limit) {
        List<Object[]> results = productRepository.findProductsWithLowStock(limit);
        
        return results.stream()
                .map(LowStockProductDTO::fromDatabaseResult)
                .collect(Collectors.toList());
    }
}
