package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.dto.InventoryTransactionDTO;
import com.hyperformancelabs.backend.model.InventoryTransaction;
import com.hyperformancelabs.backend.repository.InventoryTransactionRepository;
import com.hyperformancelabs.backend.service.InventoryTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryTransactionServiceImpl implements InventoryTransactionService {

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Override
    public List<InventoryTransactionDTO> findTop6ImportTransactionsNative() {
        return inventoryTransactionRepository.findTop6ImportTransactionsNative()
                .stream()
                .map(this::convertToInventoryTransactionDTO)
                .collect(Collectors.toList());
    }

    private InventoryTransactionDTO convertToInventoryTransactionDTO(InventoryTransaction inventoryTransaction) {
        return new InventoryTransactionDTO(
                inventoryTransaction.getInventoryTransactionId(),
                inventoryTransaction.getProductVariant().getProductVariantId(),
                inventoryTransaction.getPerformedBy().getEmployeeId(),
                inventoryTransaction.getTransactionType(),
                inventoryTransaction.getTransactionDate(),
                inventoryTransaction.getBeforeQuantity(),
                inventoryTransaction.getQuantity(),
                inventoryTransaction.getAfterQuantity(),
                inventoryTransaction.getReason(),
                inventoryTransaction.getNote(),
                inventoryTransaction.getCostPrice()
        );
    }
}
