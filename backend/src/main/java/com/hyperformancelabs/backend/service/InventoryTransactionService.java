package com.hyperformancelabs.backend.service;

import com.hyperformancelabs.backend.dto.InventoryTransactionDTO;
import com.hyperformancelabs.backend.model.InventoryTransaction;
import com.hyperformancelabs.backend.model.ProductVariant;
import org.springframework.stereotype.Service;

import java.util.List;

public interface InventoryTransactionService {
    // Lấy top 6 giao dịch nhập hàng mới nhất
    List<InventoryTransactionDTO> findTop6ImportTransactionsNative();
}
